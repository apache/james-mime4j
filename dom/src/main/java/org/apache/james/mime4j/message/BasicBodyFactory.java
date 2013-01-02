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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.util.CharsetUtil;

/**
 * Factory for creating message bodies.
 */
public class BasicBodyFactory implements BodyFactory {

    public BinaryBody binaryBody(final InputStream is) throws IOException {
        return new BasicBinaryBody(bufferContent(is));
    }

    protected Charset resolveCharset(final String mimeCharset) {
        return mimeCharset != null ? Charset.forName(mimeCharset) : null;
    }

    public TextBody textBody(final InputStream is, final String mimeCharset) throws IOException {
        return new BasicTextBody(bufferContent(is), resolveCharset(mimeCharset));
    }

    private static byte[] bufferContent(final InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] tmp = new byte[2048];
        int l;
        while ((l = is.read(tmp)) != -1) {
            buf.write(tmp, 0, l);
        }
        return buf.toByteArray();
    }

    public TextBody textBody(final String text, final String mimeCharset) throws UnsupportedEncodingException {
        if (text == null) {
            throw new IllegalArgumentException("Text may not be null");
        }
        return new StringBody(text, resolveCharset(mimeCharset));
    }

    public TextBody textBody(final String text, final Charset charset) {
        if (text == null) {
            throw new IllegalArgumentException("Text may not be null");
        }
        return new StringBody(text, charset);
    }

    public TextBody textBody(final String text) {
        return textBody(text, CharsetUtil.DEFAULT_CHARSET);
    }

    public BinaryBody binaryBody(final byte[] buf) {
        return new BasicBinaryBody(buf);
    }

}
