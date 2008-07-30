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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Utility methods related to codecs.
 */
public class CodecUtil {
    
    
    public static final byte[] CRLF = {'\r', '\n'};
    
    public static final byte[] CRLF_CRLF = {'\r', '\n', '\r', '\n'};
    
    private static final int DEFAULT_ENCODING_BUFFER_SIZE = 1024;
    
    private static final byte TAB = 0x09;
    private static final byte SPACE = 0x20;
    private static final byte EQUALS = 0x3D;
    private static final byte CR = 0x0D;
    private static final byte LF = 0x0A;
    private static final byte QUOTED_PRINTABLE_LAST_PLAIN = 0x7E;
    private static final int QUOTED_PRINTABLE_MAX_LINE_LENGTH = 76;
    private static final int QUOTED_PRINTABLE_OCTETS_PER_ESCAPE = 3;
    private static final byte[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        
    /**
     * Copies the contents of one stream to the other.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[DEFAULT_ENCODING_BUFFER_SIZE];
        int inputLength;
        while (-1 != (inputLength = in.read(buffer))) {
            out.write(buffer, 0, inputLength);
        }
    }
    
    /**
     * Encodes the given stream using Quoted-Printable.
     * This assumes that stream is binary and therefore escapes
     * all line endings.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void encodeQuotedPrintableBinary(final InputStream in, final OutputStream out) throws IOException {
        
        QuotedPrintableEncoder encoder = new QuotedPrintableEncoder(DEFAULT_ENCODING_BUFFER_SIZE, true);
        encoder.encode(in, out);
    }
    
    private static final class QuotedPrintableEncoder {
        private final byte[] inBuffer;
        private final byte[] outBuffer;
        private final boolean binary;
        
        private boolean pendingSpace;
        private boolean pendingTab;
        private boolean pendingCR;
        private int nextSoftBreak;
        private int outputIndex;
        private OutputStream out;
        
        
        public QuotedPrintableEncoder(int bufferSize, boolean binary) {
            inBuffer = new byte[bufferSize];
            outBuffer = new byte[3*bufferSize];
            outputIndex = 0;
            nextSoftBreak = QUOTED_PRINTABLE_MAX_LINE_LENGTH + 1;
            out = null;
            this.binary = binary;
            pendingSpace = false;
            pendingTab = false;
            pendingCR = false;
        }
        
        void initEncoding(final OutputStream out) {
            this.out = out;
            pendingSpace = false;
            pendingTab = false;
            pendingCR = false;
            nextSoftBreak = QUOTED_PRINTABLE_MAX_LINE_LENGTH + 1;
        }
        
        void encodeChunk(byte[] buffer, int off, int len) throws IOException {
            for (int inputIndex = off; inputIndex < len + off; inputIndex++) {
                encode(buffer[inputIndex]);
            }
        }
        
        void completeEncoding() throws IOException {
            writePending();
            flushOutput();
        }
        
        public void encode(final InputStream in, final OutputStream out) throws IOException {
            initEncoding(out);
            int inputLength;
            while((inputLength = in.read(inBuffer)) > -1) {
                encodeChunk(inBuffer, 0, inputLength);
            }
            completeEncoding();
        }
        
        private void writePending() throws IOException {
            if (pendingSpace) {
                plain(SPACE);
            } else if (pendingTab) {
                plain(TAB);
            } else if (pendingCR) {
                plain(CR);
            }
            clearPending();
        }
        
        private void clearPending() throws IOException {
            pendingSpace  = false;
            pendingTab = false;
            pendingCR = false;
        }
        
        private void encode(byte next) throws IOException {
            if (next == LF) {
                if (binary) {
                    writePending();
                    escape(next);
                } else {
                    if (pendingCR) {
                        // Expect either space or tab pending 
                        // but not both
                        if (pendingSpace) {
                            escape(SPACE);
                        } else if (pendingTab) {
                            escape(TAB);
                        }
                        lineBreak();
                        clearPending();
                    } else {
                        writePending();
                        plain(next);
                    }
                }
            } else if (next == CR) {
                if (binary)  {
                    escape(next);
                } else {
                    pendingCR = true;
                }
            } else {
                writePending();
                if (next == SPACE) {
                    if (binary)  {
                        escape(next);
                    } else {
                        pendingSpace = true;
                    }
                } else if (next == TAB) {
                    if (binary)  {
                        escape(next);
                    } else {
                        pendingTab = true;
                    }
                } else if (next < SPACE) {
                    escape(next);
                } else if (next > QUOTED_PRINTABLE_LAST_PLAIN) {
                    escape(next);
                } else if (next == EQUALS) {
                    escape(next);
                } else {
                    plain(next);
                }
            }
        }
        
        private void plain(byte next) throws IOException {
            if (--nextSoftBreak <= 1) {
                softBreak();
            }
            write(next);
        }
        
        private void escape(byte next) throws IOException {
            if (--nextSoftBreak <= QUOTED_PRINTABLE_OCTETS_PER_ESCAPE) {
                softBreak();
            }
            
            int nextUnsigned = next & 0xff;
            
            write(EQUALS);
            --nextSoftBreak;
            write(HEX_DIGITS[nextUnsigned >> 4]);
            --nextSoftBreak;
            write(HEX_DIGITS[nextUnsigned % 0x10]);
        }
        
        private void write(byte next) throws IOException {
            outBuffer[outputIndex++] = next;
            if (outputIndex >= outBuffer.length) {
                flushOutput();
            }
        }
        
        private void softBreak() throws IOException {
            write(EQUALS);
            lineBreak();
        }

        private void lineBreak() throws IOException {
            write(CR);
            write(LF);
            nextSoftBreak = QUOTED_PRINTABLE_MAX_LINE_LENGTH;
        }
        
        private void flushOutput() throws IOException {
            if (outputIndex < outBuffer.length) {
                out.write(outBuffer, 0, outputIndex);
            } else {
                out.write(outBuffer);
            }
            outputIndex = 0;
        }
    }
    
    static class QuotedPrintableOutputStream extends FilterOutputStream {
        
        QuotedPrintableEncoder encoder;

        public QuotedPrintableOutputStream(OutputStream arg0, boolean binary) {
            super(arg0);
            encoder = new QuotedPrintableEncoder(DEFAULT_ENCODING_BUFFER_SIZE, binary);
            encoder.initEncoding(out);
        }

        public void close() throws IOException {
            encoder.completeEncoding();
            // do not close the wrapped stream
        }

        public void flush() throws IOException {
            encoder.flushOutput();
        }

        public void write(byte[] b, int off, int len) throws IOException {
            encoder.encodeChunk(b, off, len);
        }

        public void write(int b) throws IOException {
            if (true) throw new UnsupportedOperationException("QuotedPrintableOutputStream filter does not support byte per byte writes");
            encoder.encodeChunk(new byte[] { (byte) b }, 0, 1);
        }
        
    }
    
    /**
     * Encodes the given stream using Quoted-Printable.
     * This assumes that stream is text and therefore does not escape
     * all line endings.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void encodeQuotedPrintable(final InputStream in, final OutputStream out) throws IOException {
        final QuotedPrintableEncoder encoder = new QuotedPrintableEncoder(DEFAULT_ENCODING_BUFFER_SIZE, false);
        encoder.encode(in, out);
    }
    
    public static void encodeBase64(final InputStream in, final OutputStream out) throws IOException {
        final Base64Encoder encoder = new Base64Encoder(DEFAULT_ENCODING_BUFFER_SIZE);
        encoder.encode(in, out);
    }
    
    
    /**
     * Wraps the given stream in a Quoted-Printable encoder.
     * @param out not null
     * @return encoding outputstream 
     * @throws IOException
     */
    public static OutputStream wrapQuotedPrintable(final OutputStream out, boolean binary) throws IOException {
        return new QuotedPrintableOutputStream(out, binary);
    }
    
    /**
     * Wraps the given stream in a Base64 encoder.
     * @param out not null
     * @return encoding outputstream 
     * @throws IOException
     */
    public static OutputStream wrapBase64(final OutputStream out) throws IOException {
        return new Base64OutputStream(new OutputStreamWriter(new LineBreakingOutputStream(out, 76)) {

            public void close() throws IOException {
                // do not close wrapped stream but flush them
                flush();
            }
            
        });
    }
    
    private static final class Base64Encoder {
        private static final int MASK = 0x3F;
        private static final int FIRST_MASK = MASK << 18; 
        private static final int SECOND_MASK = MASK << 12; 
        private static final int THIRD_MASK = MASK << 6; 
        private static final int FORTH_MASK = MASK; 
        
        private static final byte[] ENCODING = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P' ,'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/'};
        
        private final byte[] in;
        private final byte[] out;
        
        public Base64Encoder(final int inputBufferSize) {
            in = new byte[inputBufferSize];
            int outputBufferSize = ((int) Math.floor((4*inputBufferSize) / 3f) + 3);
            outputBufferSize = outputBufferSize + 2 * (int) Math.floor(outputBufferSize / 76f);
            out = new byte[outputBufferSize];            
        }
        
        public void encode(final InputStream inStream, final OutputStream outStream) throws IOException {
            int inputLength;
            while ((inputLength = inStream.read(in)) > -1) {
                int outputLength = encodeInputBuffer(in, 0, inputLength);
                if (outputLength > 0) {
                    outStream.write(out, 0, outputLength);
                }
            }
        }
        
        private int encodeInputBuffer(byte[] in, final int pos, final int inputLength) {
            if (inputLength == 0) {
                return 0;
            }
            int inputEnd = pos + inputLength;
            int inputIndex = pos;
            int outputIndex = 0;
            while (inputEnd - inputIndex > 2) {
                int one = (toInt(in[inputIndex++]) << 16);
                int two = (toInt(in[inputIndex++]) << 8);
                int three = toInt(in[inputIndex++]);
                int quantum = one | two | three;
                int index = (quantum & FIRST_MASK) >> 18;
                outputIndex = setResult(out, outputIndex, ENCODING[index]);
                index = (quantum & SECOND_MASK) >> 12;
                outputIndex = setResult(out, outputIndex, ENCODING[index]);
                index = (quantum & THIRD_MASK) >> 6;
                outputIndex = setResult(out, outputIndex, ENCODING[index]);
                index = (quantum & FORTH_MASK);
                outputIndex = setResult(out, outputIndex, ENCODING[index]);
            }
            
            switch (inputEnd - inputIndex) {
                case 1:
                    int quantum = in[inputIndex++] << 16;
                    int index = (quantum & FIRST_MASK) >> 18;
                    outputIndex = setResult(out, outputIndex, ENCODING[index]);
                    index = (quantum & SECOND_MASK) >> 12;
                    outputIndex = setResult(out, outputIndex, ENCODING[index]);
                    outputIndex = setResult(out, outputIndex, (byte) '=');
                    outputIndex = setResult(out, outputIndex, (byte) '=');
                    break;
                    
                case 2:
                    quantum = (in[inputIndex++] << 16) + (in[inputIndex++] << 8);
                    index = (quantum & FIRST_MASK) >> 18;
                    outputIndex = setResult(out, outputIndex, ENCODING[index]);
                    index = (quantum & SECOND_MASK) >> 12;
                    outputIndex = setResult(out, outputIndex, ENCODING[index]);
                    index = (quantum & THIRD_MASK) >> 6;
                    outputIndex = setResult(out, outputIndex, ENCODING[index]);
                    outputIndex = setResult(out, outputIndex, (byte) '=');
                    break;
            }
            
            return outputIndex;
        }
        
        private int toInt(byte b) {
            return 255 & b;
        }

        private int setResult(byte[] results, int outputIndex, byte value) {
            results[outputIndex++] = value;
            outputIndex = checkLineLength(results, outputIndex);
            return outputIndex;
        }

        private int checkLineLength(byte[] results, int outputIndex) {
            if (outputIndex == 76 || outputIndex > 76 && (outputIndex - 2*Math.floor(outputIndex/76f - 1)) % 76 == 0) {
                results[outputIndex++] = '\r';
                results[outputIndex++] = '\n';
            }
            return outputIndex;
        }
    }

}
