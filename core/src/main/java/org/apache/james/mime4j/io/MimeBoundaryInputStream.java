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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.CharsetUtil;

import java.io.IOException;

/**
 * Stream that constrains itself to a single MIME body part.
 * After the stream ends (i.e. read() returns -1) {@link #isLastPart()}
 * can be used to determine if a final boundary has been seen or not.
 */
public class MimeBoundaryInputStream extends LineReaderInputStream {

    private final byte[] boundary;
    private final boolean strict;

    private boolean eof;
    private int limit;
    private boolean atBoundary;
    private int boundaryLen;
    private boolean lastPart;
    private boolean completed;

    private final BufferedLineReaderInputStream buffer;

    /**
     * Store the first buffer length.
     * Used to distinguish between an empty preamble and
     * no preamble.
     */
    private int initialLength;

    /**
     * Creates a new MimeBoundaryInputStream.
     *
     * @param inbuffer The underlying stream.
     * @param boundary Boundary string (not including leading hyphens).
     * @throws IllegalArgumentException when boundary is too long
     */
    public MimeBoundaryInputStream(
            final BufferedLineReaderInputStream inbuffer,
            final String boundary,
            final boolean strict) throws IOException {
        super(inbuffer);
        int bufferSize = 2 * boundary.length();
        if (bufferSize < 4096) {
            bufferSize = 4096;
        }
        inbuffer.ensureCapacity(bufferSize);
        this.buffer = inbuffer;
        this.eof = false;
        this.limit = -1;
        this.atBoundary = false;
        this.boundaryLen = 0;
        this.lastPart = false;
        this.initialLength = -1;
        this.completed = false;

        this.strict = strict;
        this.boundary = new byte[boundary.length() + 2];
        this.boundary[0] = (byte) '-';
        this.boundary[1] = (byte) '-';
        for (int i = 0; i < boundary.length(); i++) {
            byte ch = (byte) boundary.charAt(i);
            this.boundary[i + 2] = ch;
        }

        fillBuffer();
    }

    /**
     * Creates a new MimeBoundaryInputStream.
     *
     * @param inbuffer The underlying stream.
     * @param boundary Boundary string (not including leading hyphens).
     * @throws IllegalArgumentException when boundary is too long
     */
    public MimeBoundaryInputStream(
            final BufferedLineReaderInputStream inbuffer,
            final String boundary) throws IOException {
        this(inbuffer, boundary, false);
    }

    /**
     * Closes the underlying stream.
     *
     * @throws IOException on I/O errors.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    public boolean readAllowed() throws IOException {
        if (completed) {
            return false;
        }
        if (endOfStream() && !hasData()) {
            skipBoundary();
            verifyEndOfStream();
            return false;
        }
        return true;
    }

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        for (;;) {
            if (!readAllowed()) return -1;
            if (hasData()) {
                return buffer.read();
            }
            fillBuffer();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        for (;;) {
            if (!readAllowed()) return -1;
            if (hasData()) {
                int chunk = Math.min(len, limit - buffer.pos());
                return buffer.read(b, off, chunk);
            }
            fillBuffer();
        }
    }

    @Override
    public int readLine(final ByteArrayBuffer dst) throws IOException {
        if (dst == null) {
            throw new IllegalArgumentException("Destination buffer may not be null");
        }
        if (!readAllowed()) return -1;

        int total = 0;
        boolean found = false;
        int bytesRead = 0;
        while (!found) {
            if (!hasData()) {
                bytesRead = fillBuffer();
                if (endOfStream() && !hasData()) {
                    skipBoundary();
                    verifyEndOfStream();
                    bytesRead = -1;
                    break;
                }
            }
            int len = this.limit - this.buffer.pos();
            int i = this.buffer.indexOf((byte)'\n', this.buffer.pos(), len);
            int chunk;
            if (i != -1) {
                found = true;
                chunk = i + 1 - this.buffer.pos();
            } else {
                chunk = len;
            }
            if (chunk > 0) {
                dst.append(this.buffer.buf(), this.buffer.pos(), chunk);
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

    private void verifyEndOfStream() throws IOException {
        if (strict && eof && !atBoundary) {
            throw new MimeIOException(new MimeException("Unexpected end of stream"));
        }
    }

    private boolean endOfStream() {
        return eof || atBoundary;
    }

    private boolean hasData() {
        return limit > buffer.pos() && limit <= buffer.limit();
    }

    private int fillBuffer() throws IOException {
        if (eof) {
            return -1;
        }
        int bytesRead;
        if (!hasData()) {
            bytesRead = buffer.fillBuffer();
            if (bytesRead == -1) {
                eof = true;
            }
        } else {
            bytesRead = 0;
        }

        int i;
        int off = buffer.pos();
        for (;;) {
            i = buffer.indexOf(boundary, off, buffer.limit() - off);
            if (i == -1) {
                break;
            }
            // Make sure the boundary is either at the very beginning of the buffer
            // or preceded with LF
            if (i == buffer.pos() || buffer.byteAt(i - 1) == '\n') {
                int pos = i + boundary.length;
                int remaining = buffer.limit() - pos;
                if (remaining <= 0) {
                    // Make sure the boundary is terminated with EOS
                    break;
                } else {
                    // or with a whitespace or '--' 
                    char ch = (char)(buffer.byteAt(pos));
                    if (CharsetUtil.isWhitespace(ch)) {
                        break;
                    }
                    if (ch == '-' && remaining > 1 && (char)(buffer.byteAt(pos+1)) == '-') {
                        break;
                    } 
                }
            }
            off = i + boundary.length;
        }
        if (i != -1) {
            limit = i;
            atBoundary = true;
            calculateBoundaryLen();
        } else {
            if (eof) {
                limit = buffer.limit();
            } else {
                limit = buffer.limit() - (boundary.length + 2);
                                // [LF] [boundary] [CR][LF] minus one char
            }
        }
        return bytesRead;
    }

    public boolean isEmptyStream() {
        return initialLength == 0;
    }

    public boolean isFullyConsumed() {
        return completed && !buffer.hasBufferedData();
    }

    private void calculateBoundaryLen() throws IOException {
        boundaryLen = boundary.length;
        int len = limit - buffer.pos();
        if (len >= 0 && initialLength == -1) initialLength = len;
        if (len > 0) {
            if (buffer.byteAt(limit - 1) == '\n') {
                boundaryLen++;
                limit--;
            }
        }
        if (len > 1) {
            if (buffer.byteAt(limit - 1) == '\r') {
                boundaryLen++;
                limit--;
            }
        }
    }

    private void skipBoundary() throws IOException {
        if (!completed) {
            completed = true;
            buffer.skip(boundaryLen);
            boolean checkForLastPart = true;
            for (;;) {
                if (buffer.length() > 1) {
                    int ch1 = buffer.byteAt(buffer.pos());
                    int ch2 = buffer.byteAt(buffer.pos() + 1);

                    if (checkForLastPart) if (ch1 == '-' && ch2 == '-') {
                        this.lastPart = true;
                        buffer.skip(2);
                        checkForLastPart = false;
                        continue;
                    }

                    if (ch1 == '\r' && ch2 == '\n') {
                        buffer.skip(2);
                        break;
                    } else if (ch1 == '\n') {
                        buffer.skip(1);
                        break;
                    } else {
                        // ignoring everything in a line starting with a boundary.
                        buffer.skip(1);
                    }

                } else {
                    if (eof) {
                        break;
                    }
                    fillBuffer();
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

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder("MimeBoundaryInputStream, boundary ");
        for (byte b : boundary) {
            buffer.append((char) b);
        }
        return buffer.toString();
    }

    @Override
    public boolean unread(ByteArrayBuffer buf) {
        return false;
    }
}
