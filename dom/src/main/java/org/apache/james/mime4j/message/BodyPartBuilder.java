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

import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;

/**
 * {@link org.apache.james.mime4j.message.BodyPart} builder.
 */
public class BodyPartBuilder extends AbstractEntityBuilder {

    public static BodyPartBuilder create() {
        return new BodyPartBuilder();
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
    public BodyPartBuilder use(final BodyFactory bodyFactory) {
        super.use(bodyFactory);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(String text, Charset charset) throws IOException {
        super.setBody(text, charset);
        return this;
    }

    @Override
    public BodyPartBuilder setBody(String text, String subtype, Charset charset) throws IOException {
        super.setBody(text, subtype, charset);
        return this;
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
