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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * {@link InputStream} backed by {@link CharSequence}.
 */
class TextInputStream extends InputStream {

    private final CharsetEncoder encoder;
    private final CharBuffer cbuf;
    private final ByteBuffer bbuf;

    private int mark = -1;
    // encode(..., true) consumed all the input
    private boolean encoded;
    // flush wrote the final bytes of a stateful encoding, such as the ESC ( B of ISO-2022-JP
    private boolean flushed;

    TextInputStream(final CharSequence s, final Charset charset, int bufferSize) {
        super();
        this.encoder = charset.newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
        this.bbuf = ByteBuffer.allocate(bufferSize);
        this.bbuf.flip();
        this.cbuf = CharBuffer.wrap(s);
    }

    private void fillBuffer() throws CharacterCodingException {
        this.bbuf.compact();
        if (!this.encoded) {
            CoderResult result = this.encoder.encode(this.cbuf, this.bbuf, true);
            if (result.isError()) {
                result.throwException();
            }
            this.encoded = result.isUnderflow();
        }
        if (this.encoded && !this.flushed) {
            CoderResult result = this.encoder.flush(this.bbuf);
            if (result.isError()) {
                result.throwException();
            }
            this.flushed = result.isUnderflow();
        }
        this.bbuf.flip();
    }

    private boolean atEnd() {
        return !this.bbuf.hasRemaining() && this.flushed;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (atEnd()) {
            return -1;
        }
        int bytesRead = 0;
        while (len > 0) {
            if (this.bbuf.hasRemaining()) {
                int chunk = Math.min(this.bbuf.remaining(), len);
                this.bbuf.get(b, off, chunk);
                off += chunk;
                len -= chunk;
                bytesRead += chunk;
            } else {
                fillBuffer();
                if (atEnd()) {
                    break;
                }
            }
        }
        if (bytesRead > 0) {
            return bytesRead;
        } else {
            if (atEnd()) {
                return -1;
            } else {
                return bytesRead;
            }
        }
    }

    @Override
    public int read() throws IOException {
        for (;;) {
            if (this.bbuf.hasRemaining()) {
                return this.bbuf.get() & 0xFF;
            } else {
                fillBuffer();
                if (atEnd()) {
                    return -1;
                }
            }
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        int skipped = 0;
        while (n > 0 && this.cbuf.hasRemaining()) {
            this.cbuf.get();
            n--;
            skipped++;
        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return this.cbuf.remaining();
    }

    @Override
    public void mark(int readlimit) {
        this.mark = this.cbuf.position();
    }

    @Override
    public void reset() throws IOException {
        if (this.mark != -1) {
            this.cbuf.position(this.mark);
            this.mark = -1;
            this.encoder.reset();
            this.encoded = false;
            this.flushed = false;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }

}
