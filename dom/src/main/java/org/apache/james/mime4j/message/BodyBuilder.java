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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Builder for {@link TextBody} and {@link BinaryBody} instances.
 */
public class BodyBuilder {

    public static BodyBuilder create() {
        return new BodyBuilder();
    }

    private String text;
    private byte[] bin;
    private Charset charset;

    BodyBuilder() {
        super();
    }

    public BodyBuilder setText(final String text) {
        this.text = text;
        this.bin = null;
        return this;
    }

    public BodyBuilder setByteArray(final byte[] bin) {
        this.bin = bin;
        this.text = null;
        return this;
    }

    public BodyBuilder setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public BodyBuilder readFrom(final InputStream in) throws IOException {
        this.bin = ContentUtil.buffer(in);
        this.text = null;
        return this;
    }

    public BodyBuilder readFrom(final Reader in) throws IOException {
        this.text = ContentUtil.buffer(in);
        this.bin = null;
        return this;
    }

    public TextBody buildText() {
        Charset cs = this.charset != null ? this.charset : Charsets.DEFAULT_CHARSET;
        if (this.text != null) {
            return new StringBody1(this.text, cs);
        } else if (this.bin != null) {
            return new StringBody2(this.bin, cs);
        } else {
            return new StringBody2(new byte [] {}, cs);
        }
    }

    public BinaryBody buildBinary() {
        if (this.bin != null) {
            return new BinaryBody1(this.bin);
        } else if (this.text != null) {
            return new BinaryBody2(this.text, this.charset);
        } else {
            return new BinaryBody1(new byte [] {});
        }
    }

    public Body build() {
        if (this.charset != null) {
            return buildText();
        } else {
            return buildBinary();
        }
    }

    static class StringBody1 extends TextBody {

        private final String content;
        private final Charset charset;

        StringBody1(final String content, final Charset charset) {
            super();
            this.content = content;
            this.charset = charset;
        }

        @Override
        public String getMimeCharset() {
            return this.charset != null ? this.charset.name() : null;
        }

        @Override
        public Reader getReader() throws IOException {
            return new StringReader(this.content);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return InputStreams.create(this.content,
                    this.charset != null ? this.charset : Charsets.DEFAULT_CHARSET);
        }

        @Override
        public void dispose() {
        }

        @Override
        public SingleBody copy() {
            return new StringBody1(this.content, this.charset);
        }

    }

    static class StringBody2 extends TextBody {

        private final byte[] content;
        private final Charset charset;

        StringBody2(final byte[] content, final Charset charset) {
            super();
            this.content = content;
            this.charset = charset;
        }

        @Override
        public String getMimeCharset() {
            return this.charset != null ? this.charset.name() : null;
        }

        @Override
        public Reader getReader() throws IOException {
            return new InputStreamReader(InputStreams.create(this.content), this.charset);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return InputStreams.create(this.content);
        }

        @Override
        public void dispose() {
        }

        @Override
        public SingleBody copy() {
            return new StringBody2(this.content, this.charset);
        }

    }

    static class BinaryBody1 extends BinaryBody {

        private final byte[] content;

        BinaryBody1(final byte[] content) {
            super();
            this.content = content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return InputStreams.create(this.content);
        }

        @Override
        public void dispose() {
        }

        @Override
        public SingleBody copy() {
            return new BinaryBody1(this.content);
        }

    }

    static class BinaryBody2 extends BinaryBody {

        private final String content;
        private final Charset charset;

        BinaryBody2(final String content, final Charset charset) {
            super();
            this.content = content;
            this.charset = charset;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return InputStreams.create(this.content,
                    this.charset != null ? this.charset : Charsets.DEFAULT_CHARSET);
        }

        @Override
        public void dispose() {
        }

        @Override
        public SingleBody copy() {
            return new BinaryBody2(this.content, this.charset);
        }

    }

}
