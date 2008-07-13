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

package org.apache.james.mime4j.stream;


import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.InputBuffer;

import java.io.IOException;

/**
 * Implementation of {@link BufferingInputStream} backed by an {@link InputBuffer} instance.
 * 
 * @version $Id$
 */
public class BasicBufferingInputStream extends BufferingInputStream {

    private final InputBuffer buffer;

    public BasicBufferingInputStream(final InputBuffer buffer) {
        super();
        this.buffer = buffer;
    }

    public void close() throws IOException {
        this.buffer.closeStream();
    }

    public boolean markSupported() {
        return false;
    }

    public int read() throws IOException {
        return this.buffer.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return this.buffer.read(b, off, len);
    }
    
    public int readLine(final ByteArrayBuffer linebuf) throws IOException {
        if (linebuf == null) {
            throw new IllegalArgumentException("Buffer may not be null");
        }
        int total = 0;
        boolean found = false;
        int bytesRead = 0;
        while (!found) {
            if (!this.buffer.hasBufferedData()) {
                bytesRead = this.buffer.fillBuffer();
                if (bytesRead == -1) {
                    break;
                }
            }
            int i = this.buffer.indexOf((byte)'\n');
            int chunk;
            if (i != -1) {
                found = true;
                chunk = i + 1 - this.buffer.pos();
            } else {
                chunk = this.buffer.length();
            }
            if (chunk > 0) {
                linebuf.append(this.buffer.buf(), this.buffer.pos(), chunk);
                this.buffer.skip(chunk);
                total += chunk;
            }
        }
        if (total == 0 && bytesRead == -1) {
            return -1;
        } else {
            return total;
        }
    }
    
    public void reset() {
    }

}
