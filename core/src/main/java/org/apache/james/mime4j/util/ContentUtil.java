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

package org.apache.james.mime4j.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.james.mime4j.Charsets;

/**
 * Utility methods for converting textual content of a message.
 */
public class ContentUtil {

    private ContentUtil() {
    }

    static final int DEFAULT_COPY_BUFFER_SIZE = 1024;

    /**
     * Copies the contents of one stream to the other.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[DEFAULT_COPY_BUFFER_SIZE];
        int inputLength;
        while (-1 != (inputLength = in.read(buffer))) {
            out.write(buffer, 0, inputLength);
        }
    }

    /**
     * Copies the contents of one stream to the other.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void copy(final Reader in, final Writer out) throws IOException {
        final char[] buffer = new char[DEFAULT_COPY_BUFFER_SIZE];
        int inputLength;
        while (-1 != (inputLength = in.read(buffer))) {
            out.write(buffer, 0, inputLength);
        }
    }

    public static byte[] buffer(final InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        copy(in, buf);
        return buf.toByteArray();
    }

    public static String buffer(final Reader in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("Reader may not be null");
        }
        StringWriter buf = new StringWriter();
        copy(in, buf);
        return buf.toString();
    }

    /**
     * Encodes the specified string into an immutable sequence of bytes using
     * the US-ASCII charset.
     *
     * @param string
     *            string to encode.
     * @return encoded string as an immutable sequence of bytes.
     */
    public static ByteSequence encode(CharSequence string) {
        if (string == null) {
            return null;
        }
        ByteArrayBuffer buf = new ByteArrayBuffer(string.length());
        for (int i = 0; i < string.length(); i++) {
            buf.append((byte) string.charAt(i));
        }
        return buf;
    }

    /**
     * Encodes the specified string into an immutable sequence of bytes using
     * the specified charset.
     *
     * @param charset
     *            Java charset to be used for the conversion.
     * @param string
     *            string to encode.
     * @return encoded string as an immutable sequence of bytes.
     */
    public static ByteSequence encode(Charset charset, CharSequence string) {
        if (string == null) {
            return null;
        }
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        ByteBuffer encoded = charset.encode(CharBuffer.wrap(string));
        ByteArrayBuffer buf = new ByteArrayBuffer(encoded.remaining());
        buf.append(encoded.array(), encoded.position(), encoded.remaining());
        return buf;
    }

    /**
     * Decodes the specified sequence of bytes into a string using the US-ASCII
     * charset.
     *
     * @param byteSequence
     *            sequence of bytes to decode.
     * @return decoded string.
     */
    public static String decode(ByteSequence byteSequence) {
        if (byteSequence == null) {
            return null;
        }
        return decode(byteSequence, 0, byteSequence.length());
    }

    /**
     * Decodes the specified sequence of bytes into a string using the specified
     * charset.
     *
     * @param charset
     *            Java charset to be used for the conversion.
     * @param byteSequence
     *            sequence of bytes to decode.
     * @return decoded string.
     */
    public static String decode(Charset charset, ByteSequence byteSequence) {
        return decode(charset, byteSequence, 0, byteSequence.length());
    }

    /**
     * Decodes a sub-sequence of the specified sequence of bytes into a string
     * using the US-ASCII charset.
     *
     * @param byteSequence
     *            sequence of bytes to decode.
     * @param offset
     *            offset into the byte sequence.
     * @param length
     *            number of bytes.
     * @return decoded string.
     */
    public static String decode(ByteSequence byteSequence, int offset, int length) {
        if (byteSequence == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(length);
        for (int i = offset; i < offset + length; i++) {
            buf.append((char) (byteSequence.byteAt(i) & 0xff));
        }
        return buf.toString();
    }

    /**
     * Decodes a sub-sequence of the specified sequence of bytes into a string
     * using the specified charset.
     *
     * @param charset
     *            Java charset to be used for the conversion.
     * @param byteSequence
     *            sequence of bytes to decode.
     * @param offset
     *            offset into the byte sequence.
     * @param length
     *            number of bytes.
     * @return decoded string.
     */
    public static String decode(Charset charset, ByteSequence byteSequence,
            int offset, int length) {
        if (byteSequence == null) {
            return null;
        }
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        if (byteSequence instanceof ByteArrayBuffer) {
            ByteArrayBuffer bab = (ByteArrayBuffer) byteSequence;
            return decode(charset, bab.buffer(), offset, length);
        } else {
            byte[] bytes = byteSequence.toByteArray();
            return decode(charset, bytes, offset, length);
        }
    }

    private static String decode(Charset charset, byte[] buffer, int offset,
            int length) {
        return charset.decode(ByteBuffer.wrap(buffer, offset, length))
                .toString();
    }

    public static byte[] toByteArray(final String s, final Charset charset) {
        if (s == null) {
            return null;
        }
        try {
            return s.getBytes((charset != null ? charset : Charsets.DEFAULT_CHARSET).name());
        } catch (UnsupportedEncodingException ex) {
            // Should never happen
            throw new Error(ex);
        }
    }

    public static byte[] toAsciiByteArray(final String s) {
        return toByteArray(s, Charsets.US_ASCII);
    }

    public static String toString(final byte[] b, final Charset charset) {
        if (b == null) {
            return null;
        }
        try {
            return new String(b, (charset != null ? charset : Charsets.DEFAULT_CHARSET).name());
        } catch (UnsupportedEncodingException ex) {
            // Should never happen
            throw new Error(ex);
        }
    }

    public static String toAsciiString(final byte[] b) {
        return toString(b, Charsets.US_ASCII);
    }

    public static String toString(final byte[] b, int off, int len, final Charset charset) {
        if (b == null) {
            return null;
        }
        try {
            return new String(b, off, len,
                    (charset != null ? charset : Charsets.DEFAULT_CHARSET).name());
        } catch (UnsupportedEncodingException ex) {
            // Should never happen
            throw new Error(ex);
        }
    }

    public static String toAsciiString(final byte[] b, int off, int len) {
        return toString(b, off, len, Charsets.US_ASCII);
    }

    public static String toString(final ByteArrayBuffer b, final Charset charset) {
        if (b == null) {
            return null;
        }
        try {
            return new String(b.buffer(), 0, b.length(),
                    (charset != null ? charset : Charsets.DEFAULT_CHARSET).name());
        } catch (UnsupportedEncodingException ex) {
            // Should never happen
            throw new Error(ex);
        }
    }

    public static String toAsciiString(final ByteArrayBuffer b) {
        return toString(b, Charsets.US_ASCII);
    }

}
