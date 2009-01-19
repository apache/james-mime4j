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
 * Performs Base-64 decoding on an underlying stream.
 */
public class Base64InputStream extends InputStream {
    private static Log log = LogFactory.getLog(Base64InputStream.class);

    private static final int ENCODED_BUFFER_SIZE = 1536;

    private static final int[] BASE64_DECODE = new int[256];

    static {
        for (int i = 0; i < 256; i++)
            BASE64_DECODE[i] = -1;
        for (int i = 0; i < Base64OutputStream.BASE64_TABLE.length; i++)
            BASE64_DECODE[Base64OutputStream.BASE64_TABLE[i] & 0xff] = i;
    }

    private static final byte BASE64_PAD = '=';

    private static final int EOF = -1;

    private final byte[] singleByte = new byte[1];

    private boolean strict;

    private final InputStream in;
    private boolean closed = false;

    private final byte[] encoded = new byte[ENCODED_BUFFER_SIZE];
    private int position = 0; // current index into encoded buffer
    private int size = 0; // current size of encoded buffer

    private final ByteQueue q = new ByteQueue();

    private boolean eof; // end of file or pad character reached

    public Base64InputStream(InputStream in) {
        this(in, false);
    }

    public Base64InputStream(InputStream in, boolean strict) {
        if (in == null)
            throw new IllegalArgumentException();

        this.in = in;
        this.strict = strict;
    }

    @Override
    public int read() throws IOException {
        if (closed)
            throw new IOException("Base64InputStream has been closed");

        while (true) {
            int bytes = read0(singleByte, 0, 1);
            if (bytes == EOF)
                return EOF;

            if (bytes == 1)
                return singleByte[0] & 0xff;
        }
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (closed)
            throw new IOException("Base64InputStream has been closed");

        if (buffer == null)
            throw new NullPointerException();

        if (buffer.length == 0)
            return 0;

        return read0(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (closed)
            throw new IOException("Base64InputStream has been closed");

        if (buffer == null)
            throw new NullPointerException();

        if (offset < 0 || length < 0 || offset + length > buffer.length)
            throw new IndexOutOfBoundsException();

        if (length == 0)
            return 0;

        return read0(buffer, offset, offset + length);
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;

        closed = true;
    }

    private int read0(final byte[] buffer, final int from, final int to)
            throws IOException {
        int index = from; // index into given buffer

        // check if a previous invocation left decoded bytes in the queue

        int qCount = q.count();
        while (qCount-- > 0 && index < to) {
            buffer[index++] = q.dequeue();
        }

        // eof or pad reached?

        if (eof)
            return index == from ? EOF : index - from;

        // decode into given buffer

        int data = 0; // holds decoded data; up to four sextets
        int sextets = 0; // number of sextets

        while (index < to) {
            // make sure buffer not empty

            while (position == size) {
                int n = in.read(encoded, 0, encoded.length);
                if (n == EOF) {
                    eof = true;

                    if (sextets != 0) {
                        // error in encoded data
                        handleUnexpectedEof(sextets);
                    }

                    return index == from ? EOF : index - from;
                } else if (n > 0) {
                    position = 0;
                    size = n;
                } else {
                    assert n == 0;
                }
            }

            // decode buffer

            while (position < size && index < to) {
                int value = encoded[position++] & 0xff;

                if (value == BASE64_PAD) {
                    index = decodePad(data, sextets, buffer, index, to);
                    return index - from;
                }

                int decoded = BASE64_DECODE[value];
                if (decoded < 0) // -1: not a base64 char
                    continue;

                data = (data << 6) | decoded;
                sextets++;

                if (sextets == 4) {
                    sextets = 0;

                    byte b1 = (byte) (data >>> 16);
                    byte b2 = (byte) (data >>> 8);
                    byte b3 = (byte) data;

                    if (index < to - 2) {
                        buffer[index++] = b1;
                        buffer[index++] = b2;
                        buffer[index++] = b3;
                    } else {
                        if (index < to - 1) {
                            buffer[index++] = b1;
                            buffer[index++] = b2;
                            q.enqueue(b3);
                        } else if (index < to) {
                            buffer[index++] = b1;
                            q.enqueue(b2);
                            q.enqueue(b3);
                        } else {
                            q.enqueue(b1);
                            q.enqueue(b2);
                            q.enqueue(b3);
                        }

                        assert index == to;
                        return to - from;
                    }
                }
            }
        }

        assert sextets == 0;
        assert index == to;
        return to - from;
    }

    private int decodePad(int data, int sextets, final byte[] buffer,
            int index, final int end) throws IOException {
        eof = true;

        if (sextets == 2) {
            // one byte encoded as "XY=="

            byte b = (byte) (data >>> 4);
            if (index < end) {
                buffer[index++] = b;
            } else {
                q.enqueue(b);
            }
        } else if (sextets == 3) {
            // two bytes encoded as "XYZ="

            byte b1 = (byte) (data >>> 10);
            byte b2 = (byte) ((data >>> 2) & 0xFF);

            if (index < end - 1) {
                buffer[index++] = b1;
                buffer[index++] = b2;
            } else if (index < end) {
                buffer[index++] = b1;
                q.enqueue(b2);
            } else {
                q.enqueue(b1);
                q.enqueue(b2);
            }
        } else {
            // error in encoded data
            handleUnexpecedPad(sextets);
        }

        return index;
    }

    private void handleUnexpectedEof(int sextets) throws IOException {
        if (strict)
            throw new IOException("unexpected end of file");
        else
            log.warn("unexpected end of file; dropping " + sextets
                    + " sextet(s)");
    }

    private void handleUnexpecedPad(int sextets) throws IOException {
        if (strict)
            throw new IOException("unexpected padding character");
        else
            log.warn("unexpected padding character; dropping " + sextets
                    + " sextet(s)");
    }
}
