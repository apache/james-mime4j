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
import java.io.OutputStream;
import java.util.Base64;

/**
 * This class implements section <cite>6.8. Base64 Content-Transfer-Encoding</cite>
 * from RFC 2045 <cite>Multipurpose Internet Mail Extensions (MIME) Part One:
 * Format of Internet Message Bodies</cite> by Freed and Borenstein.
 * <p>
 * Code is based on Base64 and Base64OutputStream code from Commons-Codec 1.4.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
 */
public class Base64OutputStream extends OutputStream {

    // Default line length per RFC 2045 section 6.8.
    private static final int DEFAULT_LINE_LENGTH = 76;

    // CRLF line separator per RFC 2045 section 2.1.
    private static final byte[] CRLF_SEPARATOR = { '\r', '\n' };

    // This array is a lookup table that translates 6-bit positive integer index
    // values into their "Base64 Alphabet" equivalents as specified in Table 1
    // of RFC 2045.
    static final byte[] BASE64_TABLE = { 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '+', '/' };

    // Input bytes encoded per call of the JDK encoder: a whole number of lines, about 3 KB.
    private static final int BLOCK_INPUT_TARGET = 3072;

    // Room for the output of the per-group path before it is written out, besides one line separator.
    private static final int OUT_BUFFER_SIZE = 1024;

    // Line separators up to this length use the buffers of the thread
    private static final int SHARED_MAX_SEPARATOR = 16;

    /**
     * The work buffers. They hold nothing between two calls of the stream, so a thread shares one set
     * between its streams; a stream writing into another one on the same thread finds it in use and
     * takes its own.
     */
    private static final class Buffers {
        final byte[] out;
        byte[] block;
        byte[] encodedBlock;
        boolean inUse;

        Buffers(int outSize) {
            this.out = new byte[outSize];
        }
    }

    private static final ThreadLocal<Buffers> THREAD_BUFFERS =
        ThreadLocal.withInitial(() -> new Buffers(OUT_BUFFER_SIZE + SHARED_MAX_SEPARATOR));

    private final OutputStream out;
    private final byte[] lineSeparator;
    // Encoded chars per line, a multiple of 4; 0 or less: no line breaks
    private final int lineMax;
    // Input bytes encoded at once by the JDK encoder, from a line start
    private final int blockInput;
    private final Base64.Encoder blockEncoder;

    private final int encodedBlockLength;

    private final byte[] carry = new byte[3];
    private int leftover = 0;
    // Chars written on the current line. A separator is written before the next group once the line is full.
    private int linePos = 0;
    // The buffers of the call in progress, null between calls
    private Buffers buffers;
    private byte[] outBuffer;
    private int outPos = 0;
    // Used instead of the buffers of the thread when they are in use, or do not fit
    private Buffers ownBuffers;
    private boolean wroteAny = false;
    private boolean closed = false;

    /**
     * Creates a <code>Base64OutputStream</code> that writes the encoded data
     * to the given output stream using the default line length (76) and line
     * separator (CRLF).
     *
     * @param out
     *            underlying output stream.
     */
    public Base64OutputStream(OutputStream out) {
        this(out, DEFAULT_LINE_LENGTH, CRLF_SEPARATOR);
    }

    /**
     * Creates a <code>Base64OutputStream</code> that writes the encoded data
     * to the given output stream using the given line length and the default
     * line separator (CRLF).
     * <p>
     * The given line length will be rounded down to the nearest multiple of 4. If
     * the line length is zero then the output will not be split into lines.
     *
     * @param out
     *            underlying output stream.
     * @param lineLength
     *            desired line length.
     */
    public Base64OutputStream(OutputStream out, int lineLength) {
        this(out, lineLength, CRLF_SEPARATOR);
    }

    /**
     * Creates a <code>Base64OutputStream</code> that writes the encoded data
     * to the given output stream using the given line length and line
     * separator.
     * <p>
     * The given line length will be rounded down to the nearest multiple of 4. If
     * the line length is zero then the output will not be split into lines and
     * the line separator is ignored.
     * <p>
     * The line separator must not include characters from the BASE64 alphabet
     * (including the padding character <code>=</code>).
     *
     * @param out
     *            underlying output stream.
     * @param lineLength
     *            desired line length.
     * @param lineSeparator
     *            line separator to use.
     */
    public Base64OutputStream(OutputStream out, int lineLength, byte[] lineSeparator) {
        // Validates the separator as before, and gives the output of whole lines
        Base64.Encoder mimeEncoder = Base64.getMimeEncoder(lineLength, lineSeparator);
        this.out = out;
        this.lineSeparator = lineSeparator.clone();
        // As the JDK MIME encoder: rounded down to a multiple of 4
        this.lineMax = lineLength & ~0b11;
        if (lineMax > 0) {
            int lineInput = lineMax / 4 * 3;
            this.blockInput = Math.max(1, BLOCK_INPUT_TARGET / lineInput) * lineInput;
            this.blockEncoder = mimeEncoder;
            int lines = blockInput / lineInput;
            this.encodedBlockLength = lines * lineMax + (lines - 1) * this.lineSeparator.length;
        } else {
            this.blockInput = BLOCK_INPUT_TARGET;
            this.blockEncoder = Base64.getEncoder();
            this.encodedBlockLength = blockInput / 3 * 4;
        }
    }

    private void acquire() {
        Buffers shared = THREAD_BUFFERS.get();
        if (!shared.inUse && lineSeparator.length <= SHARED_MAX_SEPARATOR
                && blockInput <= BLOCK_INPUT_TARGET) {
            shared.inUse = true;
            buffers = shared;
        } else {
            if (ownBuffers == null) {
                ownBuffers = new Buffers(OUT_BUFFER_SIZE + lineSeparator.length);
            }
            buffers = ownBuffers;
        }
        outBuffer = buffers.out;
    }

    private void release() {
        if (buffers != null) {
            buffers.inUse = false;
            buffers = null;
            outBuffer = null;
            // nothing is left there, unless the underlying stream failed
            outPos = 0;
        }
    }

    @Override
    public void write(int i) throws IOException {
        ensureOpen();
        carry[leftover++] = (byte) i;
        if (leftover == 3) {
            acquire();
            try {
                encodeGroup(carry, 0);
                leftover = 0;
                flushOutBuffer();
            } finally {
                release();
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        if (leftover + len < 3) {
            while (len > 0) {
                carry[leftover++] = b[off++];
                len--;
            }
            return;
        }
        acquire();
        try {
            encode(b, off, len);
        } finally {
            release();
        }
    }

    private void encode(byte[] b, int off, int len) throws IOException {
        if (leftover > 0) {
            while (leftover < 3 && len > 0) {
                carry[leftover++] = b[off++];
                len--;
            }
            if (leftover < 3) {
                return;
            }
            encodeGroup(carry, 0);
            leftover = 0;
        }
        while (len >= 3) {
            if (len >= blockInput && atLineStart()) {
                flushOutBuffer();
                encodeBlock(b, off);
                off += blockInput;
                len -= blockInput;
            } else {
                encodeGroup(b, off);
                off += 3;
                len -= 3;
            }
        }
        while (len > 0) {
            carry[leftover++] = b[off++];
            len--;
        }
        flushOutBuffer();
    }

    @Override
    public void flush() throws IOException {
        // The output buffer is written at the end of each call
        out.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        if (leftover > 0) {
            acquire();
            try {
                writeLastGroup();
            } finally {
                release();
            }
        }
        // As the last line has no separator, one is added once something was written
        if (wroteAny) {
            out.write(lineSeparator);
        }
    }

    /** The one or two bytes left, padded. */
    private void writeLastGroup() throws IOException {
        int b0 = carry[0] & 0xff;
        int b1 = leftover == 2 ? carry[1] & 0xff : 0;
        beforeGroup();
        outBuffer[outPos++] = BASE64_TABLE[b0 >> 2];
        outBuffer[outPos++] = BASE64_TABLE[(b0 << 4 | b1 >> 4) & 0x3f];
        outBuffer[outPos++] = leftover == 2 ? BASE64_TABLE[(b1 << 2) & 0x3f] : (byte) '=';
        outBuffer[outPos++] = '=';
        leftover = 0;
        flushOutBuffer();
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
    }

    private boolean atLineStart() {
        return lineMax <= 0 || linePos == 0 || linePos == lineMax;
    }

    /**
     * Makes room for a group of 4 chars in the output buffer, after the pending line separator if the line is full.
     */
    private void beforeGroup() throws IOException {
        if (outPos > outBuffer.length - 4 - lineSeparator.length) {
            flushOutBuffer();
        }
        if (lineMax > 0) {
            if (linePos == lineMax) {
                System.arraycopy(lineSeparator, 0, outBuffer, outPos, lineSeparator.length);
                outPos += lineSeparator.length;
                linePos = 0;
            }
            linePos += 4;
        }
    }

    private void encodeGroup(byte[] b, int off) throws IOException {
        beforeGroup();
        int bits = (b[off] & 0xff) << 16 | (b[off + 1] & 0xff) << 8 | (b[off + 2] & 0xff);
        outBuffer[outPos++] = BASE64_TABLE[bits >>> 18];
        outBuffer[outPos++] = BASE64_TABLE[(bits >>> 12) & 0x3f];
        outBuffer[outPos++] = BASE64_TABLE[(bits >>> 6) & 0x3f];
        outBuffer[outPos++] = BASE64_TABLE[bits & 0x3f];
    }

    /**
     * Encodes <code>blockInput</code> bytes from a line start with the (intrinsic) array encoder of the JDK:
     * whole lines, separated, without a trailing separator.
     */
    private void encodeBlock(byte[] b, int off) throws IOException {
        byte[] block = buffers.block;
        byte[] encodedBlock = buffers.encodedBlock;
        if (block == null || block.length != blockInput || encodedBlock.length != encodedBlockLength) {
            // The JDK encodes the whole array: it has the exact block length
            block = new byte[blockInput];
            encodedBlock = new byte[encodedBlockLength];
            buffers.block = block;
            buffers.encodedBlock = encodedBlock;
        }
        if (lineMax > 0 && linePos == lineMax) {
            out.write(lineSeparator);
        }
        System.arraycopy(b, off, block, 0, blockInput);
        int n = blockEncoder.encode(block, encodedBlock);
        out.write(encodedBlock, 0, n);
        wroteAny = true;
        if (lineMax > 0) {
            linePos = lineMax;
        }
    }

    private void flushOutBuffer() throws IOException {
        if (outPos > 0) {
            out.write(outBuffer, 0, outPos);
            outPos = 0;
            wroteAny = true;
        }
    }
}
