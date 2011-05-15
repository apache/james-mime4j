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

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Disposable;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeEntityConfig;
import org.apache.james.mime4j.stream.MutableBodyDescriptorFactory;

/**
 * Utility class for copying message and parsing message elements.
 */
public class MimeBuilder {

    public static final MimeBuilder DEFAULT = new MimeBuilder();
    
    protected MimeBuilder() {
        super();
    }

    /**
     * Creates a new <code>Header</code> from the specified
     * <code>Header</code>. The <code>Header</code> instance is initialized
     * with a copy of the list of {@link Field}s of the specified
     * <code>Header</code>. The <code>Field</code> objects are not copied
     * because they are immutable and can safely be shared between headers.
     * 
     * @param other
     *            header to copy.
     */
    public Header copy(Header other) {
        HeaderImpl copy = new HeaderImpl();
        for (Field otherField : other.getFields()) {
            copy.addField(otherField);
        }
        return copy;
    }

    /**
     * Creates a new <code>BodyPart</code> from the specified
     * <code>Entity</code>. The <code>BodyPart</code> instance is initialized
     * with copies of header and body of the specified <code>Entity</code>.
     * The parent entity of the new body part is <code>null</code>.
     * 
     * @param other
     *            body part to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link Message}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public BodyPart copy(Entity other) {
        BodyPart copy = new BodyPart(); 
        if (other.getHeader() != null) {
            copy.setHeader(copy(other.getHeader()));
        }
        if (other.getBody() != null) {
            copy.setBody(copy(other.getBody()));
        }
        return copy;
    }

    /**
     * Creates a new <code>Multipart</code> from the specified
     * <code>Multipart</code>. The <code>Multipart</code> instance is
     * initialized with copies of preamble, epilogue, sub type and the list of
     * body parts of the specified <code>Multipart</code>. The parent entity
     * of the new multipart is <code>null</code>.
     * 
     * @param other
     *            multipart to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link Message}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public Multipart copy(Multipart other) {
        MultipartImpl copy = new MultipartImpl(other.getSubType());
        for (Entity otherBodyPart : other.getBodyParts()) {
            copy.addBodyPart(copy(otherBodyPart));
        }
        copy.setPreamble(other.getPreamble());
        copy.setEpilogue(other.getEpilogue());
        return copy;
    }

    
    /**
     * Returns a copy of the given {@link Body} that can be used (and modified)
     * independently of the original. The copy should be
     * {@link Disposable#dispose() disposed of} when it is no longer needed.
     * <p>
     * The {@link Body#getParent() parent} of the returned copy is
     * <code>null</code>, that is, the copy is detached from the parent
     * entity of the original.
     * 
     * @param body
     *            body to copy.
     * @return a copy of the given body.
     * @throws UnsupportedOperationException
     *             if <code>body</code> is an instance of {@link SingleBody}
     *             that does not support the {@link SingleBody#copy() copy()}
     *             operation (or contains such a <code>SingleBody</code>).
     * @throws IllegalArgumentException
     *             if <code>body</code> is <code>null</code> or
     *             <code>body</code> is a <code>Body</code> that is neither
     *             a {@link MessageImpl}, {@link Multipart} or {@link SingleBody}
     *             (or contains such a <code>Body</code>).
     */
    public Body copy(Body body) {
        if (body == null)
            throw new IllegalArgumentException("Body is null");

        if (body instanceof Message)
            return copy((Message) body);

        if (body instanceof Multipart)
            return copy((Multipart) body);

        if (body instanceof SingleBody)
            return ((SingleBody) body).copy();

        throw new IllegalArgumentException("Unsupported body class");
    }

    /**
     * Creates a new <code>Message</code> from the specified
     * <code>Message</code>. The <code>Message</code> instance is
     * initialized with copies of header and body of the specified
     * <code>Message</code>. The parent entity of the new message is
     * <code>null</code>.
     * 
     * @param other
     *            message to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link MessageImpl}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public Message copy(Message other) {
        MessageImpl copy = new MessageImpl();
        if (other.getHeader() != null) {
            copy.setHeader(copy(other.getHeader()));
        }
        if (other.getBody() != null) {
            copy.setBody(copy(other.getBody()));
        }
        return copy;
    }
    
    /**
     * Creates a new <code>Header</code> from the specified stream.
     * 
     * @param is the stream to read the header from.
     * 
     * @throws IOException on I/O errors.
     * @throws MimeIOException on MIME protocol violations.
     */
    public Header parse(
            final InputStream is,
            final DecodeMonitor monitor) throws IOException, MimeIOException {
        final HeaderImpl header = new HeaderImpl();
        final MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void endHeader() {
                parser.stop();
            }
            @Override
            public void field(Field field) throws MimeException {
                Field parsedField = DefaultFieldParser.parse(field, monitor); 
                header.addField(parsedField);
            }
        });
        try {
            parser.parse(is);
        } catch (MimeException ex) {
            throw new MimeIOException(ex);
        }
        return header;
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig} and {@link StorageProvider}.
     * 
     * @param is
     *            the stream to parse.
     * @param config
     *            {@link MimeEntityConfig} to use.
     * @param bodyFactory
     *            {@link BodyFactory} to use for storing text and binary
     *            message bodies.
     * @param bodyDescFactory
     *            {@link MutableBodyDescriptorFactory} to use for creating body descriptors.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message parse(
            final InputStream is, 
            final MimeEntityConfig config,
            final DecodeMonitor monitor,
            final BodyFactory bodyFactory, 
            final MutableBodyDescriptorFactory bodyDescFactory,
            final boolean contentDecoding,
            final boolean flatMode) throws IOException, MimeIOException {
        try {
            MessageImpl message = new MessageImpl();
            DecodeMonitor mon = monitor != null ? monitor : DecodeMonitor.SILENT;
            MimeStreamParser parser = new MimeStreamParser(config, mon, bodyDescFactory);
            parser.setContentHandler(new EntityBuilder(message, bodyFactory, mon));
            parser.setContentDecoding(contentDecoding);
            if (flatMode) {
                parser.setFlat();
            } else {
                parser.setRecurse();
            }
            parser.parse(is);
            return message;
        } catch (MimeException e) {
            throw new MimeIOException(e);
        }
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig} and {@link StorageProvider}.
     * 
     * @param is
     *            the stream to parse.
     * @param config
     *            {@link MimeEntityConfig} to use.
     * @param storageProvider
     *            {@link StorageProvider} to use for storing text and binary
     *            message bodies.
     * @param bodyDescFactory
     *            {@link MutableBodyDescriptorFactory} to use for creating body descriptors.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message parse(
            final InputStream is, 
            final MimeEntityConfig config,
            final DecodeMonitor monitor,
            final BodyFactory bodyFactory, 
            final MutableBodyDescriptorFactory bodyDescFactory) throws IOException, MimeIOException {
        return parse(is, config, monitor, bodyFactory, bodyDescFactory, true, false);
    }
    
    public Message parse(
            final InputStream is, 
            final MimeEntityConfig config,
            final BodyFactory bodyFactory, 
            final MutableBodyDescriptorFactory bodyDescFactory) throws IOException, MimeIOException {
        return parse(is, config, null, bodyFactory, bodyDescFactory);
    }

    public Message parse(
            final InputStream is, 
            final MimeEntityConfig config,
            final BodyFactory bodyFactory) throws IOException, MimeIOException {
        return parse(is, config, null, bodyFactory, null);
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance.
     * 
     * @param is
     *            the stream to parse.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message parse(InputStream is) throws IOException, MimeIOException {
        return parse(is, null, null);
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig}.
     * 
     * @param is
     *            the stream to parse.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message parse(InputStream is, MimeEntityConfig config) throws IOException,
            MimeIOException {
        return parse(is, config, null);
    }
    
}
