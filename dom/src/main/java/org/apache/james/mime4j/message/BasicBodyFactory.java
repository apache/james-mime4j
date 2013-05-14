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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.TextBody;

/**
 * Factory for creating message bodies.
 */
public class BasicBodyFactory implements BodyFactory {

    public BinaryBody binaryBody(final InputStream is) throws IOException {
        return BodyBuilder.create()
                .readFrom(is).
                buildBinary();
    }

    protected Charset resolveCharset(final String mimeCharset) throws UnsupportedEncodingException {
        try {
            return mimeCharset != null ? Charset.forName(mimeCharset) : null;
        } catch (UnsupportedCharsetException ex) {
            throw new UnsupportedEncodingException(mimeCharset);
        }
    }

    public TextBody textBody(final InputStream is, final String mimeCharset) throws IOException {
        return BodyBuilder.create()
                .readFrom(is)
                .setCharset(resolveCharset(mimeCharset))
                .buildText();
    }

    public TextBody textBody(final String text, final String mimeCharset) throws UnsupportedEncodingException {
        if (text == null) {
            throw new IllegalArgumentException("Text may not be null");
        }
        return BodyBuilder.create()
                .setText(text)
                .setCharset(resolveCharset(mimeCharset))
                .buildText();
    }

    public TextBody textBody(final String text, final Charset charset) {
        if (text == null) {
            throw new IllegalArgumentException("Text may not be null");
        }
        return BodyBuilder.create()
                .setText(text)
                .setCharset(charset)
                .buildText();
    }

    public TextBody textBody(final String text) {
        return textBody(text, Charsets.DEFAULT_CHARSET);
    }

    public BinaryBody binaryBody(final byte[] buf) {
        return BodyBuilder.create()
                .setByteArray(buf)
                .buildBinary();
    }

}
