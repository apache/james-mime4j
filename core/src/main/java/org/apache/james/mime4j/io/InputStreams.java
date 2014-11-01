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

package org.apache.james.mime4j.io;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.util.ByteArrayBuffer;

/**
 * Factory methods for {@link InputStream} instances backed by binary or textual data that attempt
 * to minimize intermediate copying while streaming data.
 */
public final class InputStreams {

    private InputStreams() {
    }

    public static InputStream create(final byte[] b, int off, int len) {
        if (b == null) {
            throw new IllegalArgumentException("Byte array may not be null");
        }
        return new BinaryInputStream(ByteBuffer.wrap(b, off, len));
    }

    public static InputStream create(final byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException("Byte array may not be null");
        }
        return new BinaryInputStream(ByteBuffer.wrap(b));
    }

    public static InputStream create(final ByteArrayBuffer b) {
        if (b == null) {
            throw new IllegalArgumentException("Byte array may not be null");
        }
        return new BinaryInputStream(ByteBuffer.wrap(b.buffer(), 0, b.length()));
    }

    public static InputStream create(final ByteBuffer b) {
        if (b == null) {
            throw new IllegalArgumentException("Byte array may not be null");
        }
        return new BinaryInputStream(b);
    }

    public static InputStream createAscii(final CharSequence s) {
        if (s == null) {
            throw new IllegalArgumentException("CharSequence may not be null");
        }
        return new TextInputStream(s, Charsets.US_ASCII, 1024);
    }

    public static InputStream create(final CharSequence s, final Charset charset) {
        if (s == null) {
            throw new IllegalArgumentException("CharSequence may not be null");
        }
        return new TextInputStream(s, charset != null ? charset : Charsets.DEFAULT_CHARSET, 1024);
    }

}
