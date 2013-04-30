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

package org.apache.james.mime4j.message;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * A <code>ContentHandler</code> for building an <code>Entity</code> to be
 * used in conjunction with a {@link org.apache.james.mime4j.parser.MimeStreamParser}.
 */
class EntityBuilder implements ContentHandler {

    private final Entity entity;
    private MessageImplFactory messageImplFactory;
    private final BodyFactory bodyFactory;
    private final Stack<Object> stack;

    EntityBuilder(
            final Entity entity,
            final BodyFactory bodyFactory) {
        this.entity = entity;
        this.messageImplFactory = new DefaultMessageImplFactory();
        this.bodyFactory = bodyFactory;
        this.stack = new Stack<Object>();
    }

    EntityBuilder(
            final Entity entity,
            final MessageImplFactory messageImplFactory,
            final BodyFactory bodyFactory) {
        this.entity = entity;
        this.messageImplFactory = messageImplFactory;
        this.bodyFactory = bodyFactory;
        this.stack = new Stack<Object>();
    }

    private void expect(Class<?> c) {
        if (!c.isInstance(stack.peek())) {
            throw new IllegalStateException("Internal stack error: "
                    + "Expected '" + c.getName() + "' found '"
                    + stack.peek().getClass().getName() + "'");
        }
    }

    public void startMessage() throws MimeException {
        if (stack.isEmpty()) {
            stack.push(this.entity);
        } else {
            expect(Entity.class);
            Message m = messageImplFactory.messageImpl();
            ((Entity) stack.peek()).setBody(m);
            stack.push(m);
        }
    }

    public void endMessage() throws MimeException {
        expect(Message.class);
        stack.pop();
    }

    public void startHeader() throws MimeException {
        stack.push(new HeaderImpl());
    }

    public void field(Field field) throws MimeException {
        expect(Header.class);
        ((Header) stack.peek()).addField(field);
    }

    public void endHeader() throws MimeException {
        expect(Header.class);
        Header h = (Header) stack.pop();
        expect(Entity.class);
        ((Entity) stack.peek()).setHeader(h);
    }

    public void startMultipart(final BodyDescriptor bd) throws MimeException {
        expect(Entity.class);

        final Entity e = (Entity) stack.peek();
        final String subType = bd.getSubType();
        final Multipart multiPart = new MultipartImpl(subType);
        e.setBody(multiPart);
        stack.push(multiPart);
    }

    public void body(BodyDescriptor bd, final InputStream is) throws MimeException, IOException {
        expect(Entity.class);

        // NO NEED TO MANUALLY RUN DECODING.
        // The parser has a "setContentDecoding" method. We should
        // simply instantiate the MimeStreamParser with that method.

        // final String enc = bd.getTransferEncoding();

        final Body body;

        /*
        final InputStream decodedStream;
        if (MimeUtil.ENC_BASE64.equals(enc)) {
            decodedStream = new Base64InputStream(is);
        } else if (MimeUtil.ENC_QUOTED_PRINTABLE.equals(enc)) {
            decodedStream = new QuotedPrintableInputStream(is);
        } else {
            decodedStream = is;
        }
        */

        if (bd.getMimeType().startsWith("text/")) {
            body = bodyFactory.textBody(is, bd.getCharset());
        } else {
            body = bodyFactory.binaryBody(is);
        }

        Entity entity = ((Entity) stack.peek());
        entity.setBody(body);
    }

    public void endMultipart() throws MimeException {
        stack.pop();
    }

    public void startBodyPart() throws MimeException {
        expect(Multipart.class);

        BodyPart bodyPart = new BodyPart();
        ((Multipart) stack.peek()).addBodyPart(bodyPart);
        stack.push(bodyPart);
    }

    public void endBodyPart() throws MimeException {
        expect(BodyPart.class);
        stack.pop();
    }

    public void epilogue(InputStream is) throws MimeException, IOException {
        expect(MultipartImpl.class);
        ByteSequence bytes = loadStream(is);
        ((MultipartImpl) stack.peek()).setEpilogueRaw(bytes);
    }

    public void preamble(InputStream is) throws MimeException, IOException {
        expect(MultipartImpl.class);
        ByteSequence bytes = loadStream(is);
        ((MultipartImpl) stack.peek()).setPreambleRaw(bytes);
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
        ByteArrayBuffer bab = new ByteArrayBuffer(64);

        int b;
        while ((b = in.read()) != -1) {
            bab.append(b);
        }

        return bab;
    }

}
