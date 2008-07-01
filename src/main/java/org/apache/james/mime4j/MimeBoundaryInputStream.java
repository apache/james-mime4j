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

package org.apache.james.mime4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stream that constrains itself to a single MIME body part.
 * After the stream ends (i.e. read() returns -1) {@link #isLastPart()}
 * can be used to determine if a final boundary has been seen or not.
 * 
 * @version $Id: MimeBoundaryInputStream.java,v 1.2 2004/11/29 13:15:42 ntherning Exp $
 */
public class MimeBoundaryInputStream extends InputStream {

    private final InputBuffer buffer;
    private final byte[] boundary;
    
    private boolean eof;
    private int limit;
    private boolean atBoundary;
    private int boundaryLen;
    private boolean lastPart;
    private boolean completed;

    /**
     * Creates a new MimeBoundaryInputStream.
     * @param s The underlying stream.
     * @param boundary Boundary string (not including leading hyphens).
     */
    public MimeBoundaryInputStream(InputBuffer inbuffer, String boundary) 
            throws IOException {
        this.buffer = inbuffer;
        this.eof = false;
        this.limit = 0;
        this.atBoundary = false;
        this.boundaryLen = 0;
        this.lastPart = false;
        this.completed = false;
        
        this.boundary = new byte[boundary.length() + 2];
        this.boundary[0] = (byte) '-';
        this.boundary[1] = (byte) '-';
        for (int i = 0; i < boundary.length(); i++) {
            byte ch = (byte) boundary.charAt(i);
            if (ch == '\r' || ch == '\n') {
                throw new IllegalArgumentException("Boundary may not contain CR or LF");
            }
            this.boundary[i + 2] = ch;
        }
        fillBuffer();
    }

    /**
     * Closes the underlying stream.
     * 
     * @throws IOException on I/O errors.
     */
    public void close() throws IOException {
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (completed) {
            return -1;
        }
        if (endOfStream() && !hasData()) {
            skipBoundary();            
            return -1;
        }
        for (;;) {
            if (hasData()) {
                return buffer.read();
            } else if (endOfStream()) {
                skipBoundary();            
                return -1;
            }
            fillBuffer();
        }
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        if (completed) {
            return -1;
        }
        if (endOfStream() && !hasData()) {
            skipBoundary();            
            return -1;
        }
        fillBuffer();
        if (!hasData()) {
            return 0;
        }
        int chunk = Math.min(len, limit - buffer.pos());
        return buffer.read(b, off, chunk);
    }

    private boolean endOfStream() {
        return eof || atBoundary;
    }
    
    private boolean hasData() {
        return limit > buffer.pos() && limit < buffer.length();
    }
    
    private int fillBuffer() throws IOException {
        if (eof) {
            return -1;
        }
        int bytesRead;
        if (!hasData()) {
            bytesRead = buffer.fillBuffer();
        } else {
            bytesRead = 0;
        }
        eof = bytesRead == -1;
        
        int i = buffer.indexOf(boundary);
        if (i != -1) {
            limit = i;
            atBoundary = true;
            calculateBoundaryLen();
        } else {
            if (eof) {
                limit = buffer.length();
            } else {
                limit = buffer.length() - (boundary.length + 1); 
                                          // \r\n + (boundary - one char)
            }
        }
        return bytesRead;
    }
    
    private void calculateBoundaryLen() throws IOException {
        boundaryLen = boundary.length;
        int len = limit - buffer.pos();
        if (len > 0) {
            if (buffer.charAt(limit - 1) == '\n') {
                boundaryLen++;
                limit--;
            }
        }
        if (len > 1) {
            if (buffer.charAt(limit - 1) == '\r') {
                boundaryLen++;
                limit--;
            }
        }
    }
    
    private void skipBoundary() throws IOException {
        if (!completed) {
            completed = true;
            buffer.skip(boundaryLen);
            for (;;) {
                if (buffer.length() > 1) {
                    int ch1 = buffer.charAt(buffer.pos());
                    int ch2 = buffer.charAt(buffer.pos() + 1);
                    if (ch1 == '-' && ch2 == '-') {
                        this.lastPart = true;
                        buffer.skip(2);
                        if (buffer.length() > 1) {
                            ch1 = buffer.charAt(buffer.pos());
                            ch2 = buffer.charAt(buffer.pos() + 1);
                            if (ch1 == '\r' && ch2 == '\n') {
                                buffer.skip(2);
                            }
                        }
                    } else if (ch1 == '\r' && ch2 == '\n') {
                        buffer.skip(2);
                    }
                    break;
                } else {
                    fillBuffer();
                }
                if (eof) {
                    break;
                }
            }
        }
    }
    
    public boolean isLastPart() {
        return lastPart;        
    }
    
    public boolean eof() {
        return eof && !buffer.hasBufferedData();
    }
    
}
