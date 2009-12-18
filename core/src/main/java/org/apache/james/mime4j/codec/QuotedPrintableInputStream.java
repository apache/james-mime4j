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

package org.apache.james.mime4j.codec;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performs Quoted-Printable decoding on an underlying stream.
 */
public class QuotedPrintableInputStream extends InputStream {
    
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 2;
    
    private static final byte EQ = 0x3D;
    private static final byte CR = 0x0D;
    private static final byte LF = 0x0A;
    
    private static Log log = LogFactory.getLog(QuotedPrintableInputStream.class);
    
    private final InputStream in;
    private boolean strict;
    private final ByteQueue data; 
    private final ByteQueue blanks; 
    
    private final byte[] encoded;
    private int pos = 0; // current index into encoded buffer
    private int limit = 0; // current size of encoded buffer
    
    private boolean closed;

    protected QuotedPrintableInputStream(final int bufsize, final InputStream in, boolean strict) {
        super();
        this.in = in;
        this.strict = strict;
        this.encoded = new byte[bufsize];
        this.data = new ByteQueue();
        this.blanks = new ByteQueue();
        this.closed = false;
    }
    
    public QuotedPrintableInputStream(final InputStream in, boolean strict) {
        this(DEFAULT_BUFFER_SIZE, in, strict);
    }
    
    public QuotedPrintableInputStream(final InputStream in) {
        this(DEFAULT_BUFFER_SIZE, in, false);
    }
    
    /**
     * Terminates Quoted-Printable coded content. This method does NOT close 
     * the underlying input stream.
     * 
     * @throws IOException on I/O errors.
     */
    @Override
    public void close() throws IOException {
        closed = true;
    }

    private int bufferLength() {
        return limit - pos;
    }
    
    private int fillBuffer() throws IOException {
        // Compact buffer if needed
        if (pos < limit) {
            System.arraycopy(encoded, pos, encoded, 0, limit - pos);
            limit -= pos;
            pos = 0;
        } else {
            limit = 0;
            pos = 0;
        }
        
        int capacity = encoded.length - limit;
        if (capacity > 0) {
            int bytesRead = in.read(encoded, limit, capacity);
            if (bytesRead > 0) {
                limit += bytesRead;
            }
            return bytesRead;
        } else {
            return 0;
        }
    }
    
    private byte advance() {
        if (pos < limit) {
            byte b =  encoded[pos];
            pos++;
            return b;
        } else {
            return -1;
        }
    }
    
    private byte peek(int i) {
        if (pos + i < limit) {
            return encoded[pos + i];
        } else {
            return -1;
        }
    }
    
    private void enqueueData() {
        for (int i = pos; i < limit; i++) {
            byte b = encoded[i];
            if (b == LF || b == EQ) {
                break;
            }
            if (Character.isWhitespace(b)) {
                blanks.enqueue(b);
            } else {
                enqueueBlanks();                
                data.enqueue(b);
            }
            pos++;
        }
    }    
    
    private void enqueueBlanks() {
        while (blanks.count() > 0) {
            data.enqueue(blanks.dequeue());
        }
    }
    
    private void decode() throws IOException {
        boolean endOfStream = false;
        while (data.count() == 0) {

            if (bufferLength() < 3) {
                int bytesRead = fillBuffer();
                endOfStream = bytesRead == -1;
            }
            // end of stream?
            if (bufferLength() == 0 && endOfStream) {
                break;
            }
            
            // copy plain bytes until a delimiter is encountered
            enqueueData();            
            
            int len = bufferLength();
            if (len > 0) {
                // found a delimiter of some kind
                if (len >= 3 || endOfStream) {
                    decodeSpecialSequence();
                }
            }
        }
    }

    private void decodeSpecialSequence() throws IOException {
        byte b1 = advance();
        if (b1 == LF) {
            // at end of line
            if (blanks.count() == 0) {
                data.enqueue((byte) LF);
            } else {
                if (blanks.dequeue() != EQ) {
                    // hard line break
                    data.enqueue((byte) CR);
                    data.enqueue((byte) LF);
                }
            }
            blanks.clear();
        } else if (b1 == EQ) {
            // found special char '='
            enqueueBlanks();
            byte b2 = advance();
            if (b2 == EQ) {
                data.enqueue(b2);
                // deal with '==\r\n' brokenness
                byte bb1 = peek(0);
                byte bb2 = peek(1);
                if (bb1 == LF || (bb1 == CR && bb2 == LF)) {
                    blanks.enqueue(b2);
                }
            } else if (Character.isWhitespace((char) b2)) {
                // soft line break
                if (b2 != LF) {
                    blanks.enqueue(b1);
                    blanks.enqueue(b2);
                }
            } else {
                byte b3 = advance();
                int upper = convert(b2);
                int lower = convert(b3);
                if (upper < 0 || lower < 0) {
                    if (strict) {
                        throw new IOException("Malformed encoded value encountered");
                    } else {
                        log.warn("Malformed encoded value encountered");
                        data.enqueue((byte) EQ);
                        if (b2 != -1) data.enqueue((byte) b2);
                        if (b3 != -1) data.enqueue((byte) b3);
                    }
                } else {
                    data.enqueue((byte)((upper << 4) | lower));
                }
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Converts '0' => 0, 'A' => 10, etc.
     * @param c ASCII character value.
     * @return Numeric value of hexadecimal character.
     */
    private int convert(byte c) {
        if (c >= '0' && c <= '9') {
            return (c - '0');
        } else if (c >= 'A' && c <= 'F') {
            return (0xA + (c - 'A'));
        } else if (c >= 'a' && c <= 'f') {
            return (0xA + (c - 'a'));
        } else {
            return -1;
        }
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Stream has been closed");
        }
        decode();
        if (data.count() == 0)
            return -1;
        else {
            byte val = data.dequeue();
            if (val >= 0)
                return val;
            else
                return val & 0xFF;
        }
    }

}
