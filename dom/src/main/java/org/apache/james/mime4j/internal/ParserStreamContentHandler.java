/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.internal;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageImplFactory;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImplFactory;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.util.BufferRecycler;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A <code>ContentHandler</code> for building an <code>Entity</code> to be
 * used in conjunction with a {@link org.apache.james.mime4j.parser.MimeStreamParser}.
 */
public class ParserStreamContentHandler implements ContentHandler {

    private static final int COPY_BUFFER_SIZE = 4096;

    private final Entity entity;
    private final MessageImplFactory messageImplFactory;
    private final BodyFactory bodyFactory;
    private final Deque<Object> stack;

    public ParserStreamContentHandler(
            final Entity entity,
            final BodyFactory bodyFactory) {
        this(entity, new DefaultMessageImplFactory(), bodyFactory);
    }

    public ParserStreamContentHandler(
            final Entity entity,
            final MessageImplFactory messageImplFactory,
            final BodyFactory bodyFactory) {
        this.entity = entity;
        this.messageImplFactory = messageImplFactory;
        this.bodyFactory = bodyFactory;
        this.stack = new ArrayDeque<Object>();
    }

    private static IllegalStateException unexpected(final Class<?> expected, final Object found) {
        return new IllegalStateException("Internal stack error: "
                + "Expected '" + expected.getName() + "' found '"
                + found.getClass().getName() + "'");
    }

    private Entity peekEntity() {
        Object top = stack.peek();
        if (top instanceof Entity) {
            return (Entity) top;
        }
        throw unexpected(Entity.class, top);
    }

    private Header peekHeader() {
        Object top = stack.peek();
        if (top instanceof Header) {
            return (Header) top;
        }
        throw unexpected(Header.class, top);
    }

    private Multipart peekMultipart() {
        Object top = stack.peek();
        if (top instanceof Multipart) {
            return (Multipart) top;
        }
        throw unexpected(Multipart.class, top);
    }

    private MultipartImpl peekMultipartImpl() {
        Object top = stack.peek();
        if (top instanceof MultipartImpl) {
            return (MultipartImpl) top;
        }
        throw unexpected(MultipartImpl.class, top);
    }

    private void expectMessage() {
        Object top = stack.peek();
        if (!(top instanceof Message)) {
            throw unexpected(Message.class, top);
        }
    }

    private void expectBodyPart() {
        Object top = stack.peek();
        if (!(top instanceof BodyPart)) {
            throw unexpected(BodyPart.class, top);
        }
    }

    public void startMessage() throws MimeException {
        if (stack.isEmpty()) {
            stack.push(this.entity);
        } else {
            Message m = messageImplFactory.messageImpl();
            peekEntity().setBody(m);
            stack.push(m);
        }
    }

    public void endMessage() throws MimeException {
        expectMessage();
        stack.pop();
    }

    public void startHeader() throws MimeException {
        stack.push(new HeaderImpl());
    }

    public void field(Field field) throws MimeException {
        peekHeader().addField(field);
    }

    public void endHeader() throws MimeException {
        Header h = peekHeader();
        stack.pop();
        peekEntity().setHeader(h);
    }

    public void startMultipart(final BodyDescriptor bd) throws MimeException {
        final Entity e = peekEntity();
        final Multipart multiPart = new MultipartImpl(bd.getSubType());
        e.setBody(multiPart);
        stack.push(multiPart);
    }

    public void body(BodyDescriptor bd, final InputStream is) throws MimeException, IOException {
        final Entity e = peekEntity();
        final Body body;
        if (bd.getMimeType().startsWith("text/")) {
            body = bodyFactory.textBody(is, bd.getCharset());
        } else {
            body = bodyFactory.binaryBody(is);
        }
        e.setBody(body);
    }

    public void endMultipart() throws MimeException {
        stack.pop();
    }

    public void startBodyPart() throws MimeException {
        BodyPart bodyPart = new BodyPart();
        peekMultipart().addBodyPart(bodyPart);
        stack.push(bodyPart);
    }

    public void endBodyPart() throws MimeException {
        expectBodyPart();
        stack.pop();
    }

    public void epilogue(InputStream is) throws MimeException, IOException {
        peekMultipartImpl().setEpilogueRaw(loadStream(is));
    }

    public void preamble(InputStream is) throws MimeException, IOException {
        peekMultipartImpl().setPreambleRaw(loadStream(is));
    }

    /**
     * Unsupported.
     *
     * @param is the raw contents of the entity.
     * @throws UnsupportedOperationException
     */
    public void raw(InputStream is) throws MimeException, IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    private static ByteSequence loadStream(InputStream in) throws IOException {
        BufferRecycler recycler = ContentUtil.getBufferRecycler();
        byte[] chunk = recycler.allocByteBuffer(0, COPY_BUFFER_SIZE);
        try {
            ByteArrayBuffer bab = new ByteArrayBuffer(64);
            int len;
            while ((len = in.read(chunk)) != -1) {
                bab.append(chunk, 0, len);
            }
            return bab;
        } finally {
            recycler.releaseByteBuffer(0, chunk);
        }
    }

}
