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

/**
 * {@link InputStream} backed by byte array.
 */
class BinaryInputStream extends InputStream {

    private final ByteBuffer bbuf;

    BinaryInputStream(final ByteBuffer b) {
        super();
        this.bbuf = b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (this.bbuf.hasRemaining()) {
            int chunk = Math.min(this.bbuf.remaining(), len);
            this.bbuf.get(b, off, chunk);
            return chunk;
        } else {
            return -1;
        }
    }

    @Override
    public int read() throws IOException {
        if (this.bbuf.hasRemaining()) {
            return this.bbuf.get() & 0xFF;
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        int skipped = 0;
        while (n > 0 && this.bbuf.hasRemaining()) {
            this.bbuf.get();
            n--;
            skipped++;
        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return this.bbuf.remaining();
    }

    @Override
    public void close() throws IOException {
    }

}
