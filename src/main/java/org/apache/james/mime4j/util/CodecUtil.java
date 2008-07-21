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
import java.io.OutputStream;

/**
 * Utility methods related to codecs.
 */
public class CodecUtil {
    
    
    public static final byte[] CRLF = {'\r', '\n'};
    
    public static final byte[] CRLF_CRLF = {'\r', '\n', '\r', '\n'};
    
    private static final int DEFAULT_ENCODING_BUFFER_SIZE = 1024;
    
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
     * This assumes that text is binary and therefore escapes
     * all line endings.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void encodeQuotedPrintableBinary(final InputStream in, final OutputStream out) throws IOException {
        
        BinaryQuotedPrintableEncoder encoder = new BinaryQuotedPrintableEncoder(DEFAULT_ENCODING_BUFFER_SIZE);
        encoder.encode(in, out);
    }
    
    private static final class BinaryQuotedPrintableEncoder {
        private final byte[] inBuffer;
        private final byte[] outBuffer;
        
        private int nextSoftBreak;
        private int inputIndex;
        private int outputIndex;
        private int inputLength;
        private InputStream in;
        private OutputStream out;
        
        
        public BinaryQuotedPrintableEncoder(int bufferSize) {
            inBuffer = new byte[bufferSize];
            outBuffer = new byte[3*bufferSize];
            inputLength = 0;
            outputIndex = 0;
            nextSoftBreak = QUOTED_PRINTABLE_MAX_LINE_LENGTH + 1;
            in = null;
            out = null;
        }
        
        public void encode(final InputStream in, final OutputStream out) throws IOException {
            this.in = in;
            this.out = out;
            nextSoftBreak = QUOTED_PRINTABLE_MAX_LINE_LENGTH + 1;
            read();
            while(inputLength > -1) {
                while (inputIndex < inputLength) { 
                    final byte next = inBuffer[inputIndex];
                    encode(next);
                    inputIndex++;
                }
                read();
            }
            flushOutput();
        }

        private void read() throws IOException {
            inputLength = in.read(inBuffer);
            inputIndex = 0;
        }
        
        private void encode(byte next) throws IOException {
            if (next <= SPACE) {
                escape(next);
            } else if (next > QUOTED_PRINTABLE_LAST_PLAIN) {
                escape(next);
            } else if (next == EQUALS) {
                escape(next);
            } else {
                plain(next);
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
            write(EQUALS);
            --nextSoftBreak;
            write(HEX_DIGITS[next >> 4]);
            --nextSoftBreak;
            write(HEX_DIGITS[next % 0x10]);
        }
        
        private void write(byte next) throws IOException {
            outBuffer[outputIndex++] = next;
            if (outputIndex >= outBuffer.length) {
                flushOutput();
            }
        }
        
        private void softBreak() throws IOException {
            write(EQUALS);
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
    
    /**
     * Encodes the given stream using Base64.
     * @param in not null
     * @param out not null 
     * @throws IOException
     */
    public static void encodeBase64(final InputStream in, final OutputStream out) throws IOException {
        Base64Encoder encoder = new Base64Encoder(DEFAULT_ENCODING_BUFFER_SIZE);
        encoder.encode(in, out);
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
            int inputLength = inStream.read(in);
            while (inputLength > -1) {
                int outputLength = encodeInputBuffer(inputLength);
                if (outputLength > 0) {
                    outStream.write(out, 0, outputLength);
                }
                inputLength = inStream.read(in);
            }
        }
        
        private int encodeInputBuffer(final int inputLength) {
            if (inputLength == 0) {
                return 0;
            }
            int inputIndex = 0;
            int outputIndex = 0;
            while (inputLength - inputIndex > 2) {
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
            
            switch (inputLength - inputIndex) {
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
