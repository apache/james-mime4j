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

    private final OutputStream delegate;

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
     * The given line length will be rounded up to the nearest multiple of 4. If
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
     * The given line length will be rounded up to the nearest multiple of 4. If
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
        ExtraCrlfOutputStream wrapped = new ExtraCrlfOutputStream(out, lineSeparator);
        this.delegate = Base64.getMimeEncoder(lineLength, lineSeparator).wrap(wrapped);
    }

    @Override
    public void write(int i) throws IOException {
        delegate.write(i);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private static class ExtraCrlfOutputStream extends OutputStream {
        private final OutputStream delegate;
        private final byte[] lineSeparator;
        private boolean appendExtraCrlf;

        private ExtraCrlfOutputStream(OutputStream delegate, byte[] lineSeparator) {
            this.delegate = delegate;
            this.lineSeparator = lineSeparator;
            this.appendExtraCrlf = false;
        }

        @Override
        public void write(int i) throws IOException {
            delegate.write(i);
            appendExtraCrlf = true;
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
            appendExtraCrlf |= b.length > 0;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            appendExtraCrlf |= len> 0;
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            if (appendExtraCrlf) {
                delegate.write(lineSeparator);
            }
        }
    }
}
