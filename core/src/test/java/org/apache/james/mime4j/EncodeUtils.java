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


public class EncodeUtils {
    
    public static final int MASK = 0x3F;
    public static final int FIRST_MASK = MASK << 18; 
    public static final int SECOND_MASK = MASK << 12; 
    public static final int THIRD_MASK = MASK << 6; 
    public static final int FORTH_MASK = MASK; 
    
    public static final byte[] ENCODING = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P' ,'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'};
    
    public static final void main(String[] args) throws Exception {
        byte[] bytes = {(byte) 0, (byte) 128, (byte) 0};
        System.out.println(new String(toBase64(bytes)));
        System.out.println(new String(toBase64("Hello, World".getBytes())));
        System.out.println(new String(toBase64("Monday".getBytes())));
        System.out.println(new String(toBase64("M\u00F6nchengladbach\r\n".getBytes("ISO-8859-1"))));
    }
    
    public static byte[] toBase64(byte[] in) {
        int inputLength = in.length;
        int outputLength = (int) Math.floor((4*inputLength) / 3f) + 3;
        outputLength = outputLength + 2 * (int) Math.floor(outputLength / 76f);
        byte[] results = new byte[outputLength];
        int inputIndex = 0;
        int outputIndex = 0;
        while (inputLength - inputIndex > 2) {
            int one = (toInt(in[inputIndex++]) << 16);
            int two = (toInt(in[inputIndex++]) << 8);
            int three = toInt(in[inputIndex++]);
            int quantum = one | two | three;
            int index = (quantum & FIRST_MASK) >> 18;
            outputIndex = setResult(results, outputIndex, ENCODING[index]);
            index = (quantum & SECOND_MASK) >> 12;
            outputIndex = setResult(results, outputIndex, ENCODING[index]);
            index = (quantum & THIRD_MASK) >> 6;
            outputIndex = setResult(results, outputIndex, ENCODING[index]);
            index = (quantum & FORTH_MASK);
            outputIndex = setResult(results, outputIndex, ENCODING[index]);
        }
        
        switch (inputLength - inputIndex) {
            case 1:
                int quantum = in[inputIndex++] << 16;
                int index = (quantum & FIRST_MASK) >> 18;
                outputIndex = setResult(results, outputIndex, ENCODING[index]);
                index = (quantum & SECOND_MASK) >> 12;
                outputIndex = setResult(results, outputIndex, ENCODING[index]);
                outputIndex = setResult(results, outputIndex, (byte) '=');
                outputIndex = setResult(results, outputIndex, (byte) '=');
                break;
                
            case 2:
                quantum = (in[inputIndex++] << 16) + (in[inputIndex++] << 8);
                index = (quantum & FIRST_MASK) >> 18;
                outputIndex = setResult(results, outputIndex, ENCODING[index]);
                index = (quantum & SECOND_MASK) >> 12;
                outputIndex = setResult(results, outputIndex, ENCODING[index]);
                index = (quantum & THIRD_MASK) >> 6;
                outputIndex = setResult(results, outputIndex, ENCODING[index]);
                outputIndex = setResult(results, outputIndex, (byte) '=');
                break;
        }
        
        return results;
    }

    private static int toInt(byte b) {
        return 255 & b;
    }


    private static int setResult(byte[] results, int outputIndex, byte value) {
        results[outputIndex++] = value;
        outputIndex = checkLineLength(results, outputIndex);
        return outputIndex;
    }


    private static int checkLineLength(byte[] results, int outputIndex) {
        if (outputIndex == 76 || outputIndex > 76 && (outputIndex - 2*Math.floor(outputIndex/76f - 1)) % 76 == 0) {
            results[outputIndex++] = '\r';
            results[outputIndex++] = '\n';
        }
        return outputIndex;
    }
}
