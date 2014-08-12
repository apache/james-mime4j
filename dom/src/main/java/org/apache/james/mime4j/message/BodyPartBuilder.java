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
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;

/**
 * {@link org.apache.james.mime4j.message.BodyPart} builder.
 */
public class BodyPartBuilder extends AbstractEntityBuilder {

    private BodyFactory bodyFactory;

    public static BodyPartBuilder create() {
        return new BodyPartBuilder();
    }

    /**
     * Sets {@link org.apache.james.mime4j.message.BodyFactory} that will be
     * used to generate message body.
     *
     * @param bodyFactory body factory.
     */
    public BodyPartBuilder use(BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
        return this;
    }

    @Override
    public BodyPartBuilder setField(Field field) {
        super.setField(field);
        return this;
    }

    @Override
    public AbstractEntityBuilder addField(Field field) {
        super.addField(field);
        return this;
    }

    @Override
    public AbstractEntityBuilder removeFields(String name) {
        super.removeFields(name);
        return this;
    }

    @Override
    public BodyPartBuilder setContentTransferEncoding(String contentTransferEncoding) {
        super.setContentTransferEncoding(contentTransferEncoding);
        return this;
    }

    @Override
    public BodyPartBuilder setContentType(String mimeType, NameValuePair... parameters) {
        super.setContentType(mimeType, parameters);
        return this;
    }

    @Override
    public BodyPartBuilder setContentDisposition(String dispositionType) {
        super.setContentDisposition(dispositionType);
        return this;
    }

    @Override
    public BodyPartBuilder setContentDisposition(String dispositionType, String filename) {
        super.setContentDisposition(dispositionType, filename);
        return this;
    }

    @Override
    public BodyPartBuilder setContentDisposition(String dispositionType, String filename, long size) {
        super.setContentDisposition(dispositionType, filename, size);
        return this;
    }

    @Override
    public BodyPartBuilder setContentDisposition(String dispositionType,
                                                 String filename,
                                                 long size,
                                                 Date creationDate,
                                                 Date modificationDate,
                                                 Date readDate) {
        super.setContentDisposition(dispositionType, filename, size, creationDate, modificationDate, readDate);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(Body body) {
        super.setBody(body);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(TextBody textBody) {
        super.setBody(textBody);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(BinaryBody binaryBody) {
        super.setBody(binaryBody);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(Multipart multipart) {
        super.setBody(multipart);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(Message message) {
        super.setBody(message);
        return this;
    }

    /**
     * Sets text of this message with the charset.
     *
     * @param text
     *            the text.
     * @param charset
     *            the charset of the text.
     */
    public BodyPartBuilder setBody(String text, Charset charset) throws IOException {
        return setBody(text, null, charset);
    }

    /**
     * Sets text of this message with the given MIME subtype and charset.
     *
     * @param text
     *            the text.
     * @param charset
     *            the charset of the text.
     * @param subtype
     *            the text subtype (e.g. &quot;plain&quot;, &quot;html&quot; or
     *            &quot;xml&quot;).
     */
    public BodyPartBuilder setBody(String text, String subtype, Charset charset) throws IOException {
        String mimeType = "text/" + (subtype != null ? subtype : "plain");
        if (charset != null) {
            setField(Fields.contentType(mimeType, new NameValuePair("charset", charset.name())));
        } else {
            setField(Fields.contentType(mimeType));
        }
        Body textBody;
        if (bodyFactory != null) {
            textBody = bodyFactory.textBody(
                    InputStreams.create(text, charset),
                    charset != null ? charset.name() : null);
        } else {
            textBody = BasicBodyFactory.INSTANCE.textBody(text, charset);
        }
        return setBody(textBody);
    }

    /**
     * Sets binary content of this message with the given MIME type.
     *
     * @param body
     *            the body.
     * @param mimeType
     *            the MIME media type of the specified body
     *            (&quot;type/subtype&quot;).
     */
    public BodyPartBuilder setBody(byte[] bin, String mimeType) throws IOException {
        setField(Fields.contentType(mimeType != null ? mimeType : "application/octet-stream"));
        Body binBody;
        if (bodyFactory != null) {
            binBody = bodyFactory.binaryBody(InputStreams.create(bin));
        } else {
            binBody = BasicBodyFactory.INSTANCE.binaryBody(bin);
        }
        return setBody(binBody);
    }

    public BodyPart build() {
        BodyPart bodyPart = new BodyPart();
        HeaderImpl header = new HeaderImpl();
        bodyPart.setHeader(header);
        for (Field field : getFields()) {
            header.addField(field);
        }

        bodyPart.setBody(getBody());

        return bodyPart;
    }

}
