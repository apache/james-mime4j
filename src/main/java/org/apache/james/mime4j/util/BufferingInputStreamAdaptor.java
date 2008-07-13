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


import java.io.IOException;
import java.io.InputStream;

/**
 * <code>InputStream</code> used by the MIME parser to detect whether the
 * underlying data stream was used (read from) and whether the end of the 
 * stream was reached.
 * 
 * @version $Id$
 */
public class BufferingInputStreamAdaptor extends BufferingInputStream {

    private final InputStream is;
    private final BufferingInputStream bis;
    private boolean used = false;
    private boolean eof = false;

    public BufferingInputStreamAdaptor(final InputStream is) {
        super();
        this.is = is;
        if (is instanceof BufferingInputStream) {
            this.bis = (BufferingInputStream) is;
        } else {
            this.bis = null;
        }
    }

    public int read() throws IOException {
        int i = this.is.read();
        this.eof = i == -1;
        this.used = true;
        return i;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int i = this.is.read(b, off, len);
        this.eof = i == -1;
        this.used = true;
        return i;
    }
    
    public int readLine(final ByteArrayBuffer dst) throws IOException {
        int i;
        if (this.bis != null) {
             i = this.bis.readLine(dst);
        } else {
             i = doReadLine(dst);
        }
        this.eof = i == -1;
        this.used = true;
        return i;
    }

    private int doReadLine(final ByteArrayBuffer dst) throws IOException {
        int total = 0;
        int ch;
        while ((ch = this.is.read()) != -1) {
            dst.append(ch);
            total++;
            if (ch == '\n') {
                break;
            }
        }
        if (total == 0 && ch == -1) {
            return -1;
        } else {
            return total;
        }
    }
    
    public boolean eof() {
        return this.eof;
    }

    public boolean isUsed() {
        return this.used;
    }

    public void reset() {
        if (this.bis != null) {
            this.bis.reset();
        }
    }
    
}
