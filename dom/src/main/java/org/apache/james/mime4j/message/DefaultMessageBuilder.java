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
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.field.LenientFieldParser;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;

/**
 * Default implementation of {@link MessageBuilder}.
 */
public class DefaultMessageBuilder implements MessageBuilder {

    private FieldParser<? extends ParsedField> fieldParser = null;
    private MessageImplFactory messageImplFactory = null;
    private BodyFactory bodyFactory = null;
    private MimeConfig config = null;
    private BodyDescriptorBuilder bodyDescBuilder = null;
    private boolean contentDecoding = true;
    private boolean flatMode = false;
    private DecodeMonitor monitor = null;

    public DefaultMessageBuilder() {
        super();
    }

    public void setFieldParser(final FieldParser<? extends ParsedField> fieldParser) {
        this.fieldParser = fieldParser;
    }

    public void setMessageImplFactory(final MessageImplFactory messageImplFactory) {
        this.messageImplFactory = messageImplFactory;
    }

    public void setBodyFactory(final BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
    }

    public void setMimeEntityConfig(final MimeConfig config) {
        this.config = config;
    }

    public void setBodyDescriptorBuilder(final BodyDescriptorBuilder bodyDescBuilder) {
        this.bodyDescBuilder  = bodyDescBuilder;
    }

    public void setDecodeMonitor(final DecodeMonitor monitor) {
        this.monitor = monitor;
    }

    public void setContentDecoding(boolean contentDecoding) {
        this.contentDecoding = contentDecoding;
    }

    public void setFlatMode(boolean flatMode) {
        this.flatMode = flatMode;
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
        MessageImpl copy = newMessageImpl();
        if (other.getHeader() != null) {
            copy.setHeader(copy(other.getHeader()));
        }
        if (other.getBody() != null) {
            copy.setBody(copy(other.getBody()));
        }
        return copy;
    }

    public Header newHeader() {
        return new HeaderImpl();
    }

    public Header newHeader(final Header source) {
        return copy(source);
    }

    public Multipart newMultipart(final String subType) {
        return new MultipartImpl(subType);
    }

    public Multipart newMultipart(final Multipart source) {
        return copy(source);
    }

    public Header parseHeader(final InputStream is) throws IOException, MimeIOException {
        final MimeConfig cfg = config != null ? config : MimeConfig.DEFAULT;
        boolean strict = cfg.isStrictParsing();
        final DecodeMonitor mon = monitor != null ? monitor :
            strict ? DecodeMonitor.STRICT : DecodeMonitor.SILENT;
        final FieldParser<? extends ParsedField> fp = fieldParser != null ? fieldParser :
            strict ? DefaultFieldParser.getParser() : LenientFieldParser.getParser();
        final HeaderImpl header = new HeaderImpl();
        final MimeStreamParser parser = new MimeStreamParser(cfg, mon, null);
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void endHeader() {
                parser.stop();
            }
            @Override
            public void field(Field field) throws MimeException {
                ParsedField parsedField;
                if (field instanceof ParsedField) {
                    parsedField = (ParsedField) field;
                } else {
                    parsedField = fp.parse(field, mon);
                }
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

    public Message newMessage() {
        return newMessageImpl();
    }

    public Message newMessage(final Message source) {
        return copy(source);
    }

    public Message parseMessage(final InputStream is) throws IOException, MimeIOException {
        try {
            MessageImpl message = newMessageImpl();
            MimeConfig cfg = config != null ? config : MimeConfig.DEFAULT;
            boolean strict = cfg.isStrictParsing();
            DecodeMonitor mon = monitor != null ? monitor :
                strict ? DecodeMonitor.STRICT : DecodeMonitor.SILENT;
            BodyDescriptorBuilder bdb = bodyDescBuilder != null ? bodyDescBuilder :
                new DefaultBodyDescriptorBuilder(null, fieldParser != null ? fieldParser :
                    strict ? DefaultFieldParser.getParser() : LenientFieldParser.getParser(), mon);
            BodyFactory bf = bodyFactory != null ? bodyFactory : new BasicBodyFactory(!strict);
            MimeStreamParser parser = new MimeStreamParser(cfg, mon, bdb);
            parser.setContentHandler(new ParserStreamContentHandler(message, bf));
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

    private MessageImpl newMessageImpl() {
        MessageImplFactory mif = messageImplFactory != null ? messageImplFactory : new DefaultMessageImplFactory();
        return mif.messageImpl();
    }

}
