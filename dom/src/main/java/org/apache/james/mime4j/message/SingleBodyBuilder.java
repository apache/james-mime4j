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
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Builder for {@link TextBody} and {@link BinaryBody} instances.
 */
public class SingleBodyBuilder {

    public static SingleBodyBuilder create() {
        return new SingleBodyBuilder();
    }

    private BodyFactory bodyFactory;

    private String text;
    private byte[] bin;
    private Charset charset;

    SingleBodyBuilder() {
        super();
    }

    public SingleBodyBuilder withFactory(final BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
        return this;
    }

    public SingleBodyBuilder setText(final String text) {
        this.text = text;
        this.bin = null;
        return this;
    }

    public SingleBodyBuilder setByteArray(final byte[] bin) {
        this.bin = bin;
        this.text = null;
        return this;
    }

    public SingleBodyBuilder setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public SingleBodyBuilder readFrom(final InputStream in) throws IOException {
        this.bin = ContentUtil.buffer(in);
        this.text = null;
        return this;
    }

    public SingleBodyBuilder readFrom(final Reader in) throws IOException {
        this.text = ContentUtil.buffer(in);
        this.bin = null;
        return this;
    }

    public TextBody buildText() throws IOException {
        Charset cs = this.charset != null ? this.charset : Charsets.DEFAULT_CHARSET;
        if (this.bodyFactory != null) {
            if (this.text != null) {
                return this.bodyFactory.textBody(InputStreams.create(this.text, cs), cs.name());
            } else if (this.bin != null) {
                return this.bodyFactory.textBody(InputStreams.create(this.bin), cs.name());
            } else {
                return this.bodyFactory.textBody(InputStreams.create(new byte [] {}), cs.name());
            }
        } else {
            if (this.text != null) {
                return BasicBodyFactory.INSTANCE.textBody(this.text, cs);
            } else if (this.bin != null) {
                return BasicBodyFactory.INSTANCE.textBody(this.bin, cs);
            } else {
                return BasicBodyFactory.INSTANCE.textBody(new byte [] {}, cs);
            }
        }
    }

    public BinaryBody buildBinary() throws IOException {
        Charset cs = this.charset != null ? this.charset : Charsets.DEFAULT_CHARSET;
        if (this.bodyFactory != null) {
            if (this.text != null) {
                return this.bodyFactory.binaryBody(InputStreams.create(this.text, cs));
            } else if (this.bin != null) {
                return this.bodyFactory.binaryBody(InputStreams.create(this.bin));
            } else {
                return this.bodyFactory.binaryBody((InputStreams.create(new byte[]{})));
            }
        } else {
            if (this.bin != null) {
                return BasicBodyFactory.INSTANCE.binaryBody(this.bin);
            } else if (this.text != null) {
                return BasicBodyFactory.INSTANCE.binaryBody(this.text, cs);
            } else {
                return BasicBodyFactory.INSTANCE.binaryBody(new byte [] {});
            }
        }
    }

    public SingleBody build() throws IOException {
        if (this.charset != null) {
            return buildText();
        } else {
            return buildBinary();
        }
    }

}
