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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class Base64OutputStreamTest extends TestCase {

    public void testEncode() throws IOException {
        ByteArrayOutputStream bos = null;
        Base64OutputStream encoder = null;
        
        /*
         * Simple initial test.
         */
        bos = new ByteArrayOutputStream();
        encoder = new Base64OutputStream(bos);
        encoder.write(fromString("This is the plain text message!"));
        encoder.close();
        assertEquals("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==\r\n", toString(bos.toByteArray()));
    }

    public void testEncodeUnderlyingStreamStaysOpen() throws IOException {
        ByteArrayOutputStream bos = null;
        Base64OutputStream encoder = null;
        
        bos = new ByteArrayOutputStream();
        encoder = new Base64OutputStream(bos);
        encoder.write(fromString("This is the plain text message!"));
        encoder.close();

        try {
            encoder.write('b');
            fail();
        } catch (IOException expected) {
        }
        
        bos.write('y');
        bos.write('a');
        bos.write('d');
        bos.write('a');
        assertEquals("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==\r\nyada", toString(bos.toByteArray()));
    }

    public void testNoLineSeparators() throws IOException {
        assertEquals("", encodeNoLs(""));
        assertEquals("YQ==", encodeNoLs("a"));
        assertEquals("YWI=", encodeNoLs("ab"));
        assertEquals("YWJj", encodeNoLs("abc"));
        assertEquals("YWJjZA==", encodeNoLs("abcd"));
        assertEquals("YWJjZGU=", encodeNoLs("abcde"));
        assertEquals("YWJjZGVm", encodeNoLs("abcdef"));
        assertEquals("YWJjZGVmZw==", encodeNoLs("abcdefg"));
        assertEquals("YWJjZGVmZ2g=", encodeNoLs("abcdefgh"));
        assertEquals("YWJjZGVmZ2hp", encodeNoLs("abcdefghi"));
        assertEquals("DQoMCQ==", encodeNoLs("\r\n\f\t"));
        assertEquals("LT0/VGhhdCdzIGEgdGVzdD89LQ==",
                encodeNoLs("-=?That's a test?=-"));
    }

    public void testLineSeparators() throws IOException {
        assertEquals("", encodeLs(""));
        assertEquals("YQ==\r\n", encodeLs("a"));
        assertEquals("YWJjZA==\r\n", encodeLs("abcd"));
        assertEquals("YWJjZGVmZw==\r\n", encodeLs("abcdefg"));
        assertEquals("YWJjZGVmZ2g=\r\n", encodeLs("abcdefgh"));
        assertEquals("YWJjZGVmZ2hp\r\n", encodeLs("abcdefghi"));
        assertEquals("YWJjZGVmZ2hp\r\nag==\r\n", encodeLs("abcdefghij"));
        assertEquals("YWJjZGVmZ2hp\r\nams=\r\n", encodeLs("abcdefghijk"));
        assertEquals("YWJjZGVmZ2hp\r\namts\r\n", encodeLs("abcdefghijkl"));
    }

    /**
     * tests {@link OutputStream#write(int)}
     */
    public void testWriteInt() throws IOException {
        byte[] bytes = fromString("123456789012345678901234567890123456789012"
                + "3456789012345678901234567890123456789012345678901234567890"
                + "123456789012345");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(b);

        for (byte element : bytes)
            out.write(element);

        out.close();

        String actual = new String(b.toByteArray(), "US-ASCII");

        String expected = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nz"
                + "g5MDEyMzQ1Njc4OTAxMjM0NTY3\r\nODkwMTIzNDU2Nzg5MDEyMzQ1Njc4"
                + "OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0\r\nNQ==\r"
                + "\n";

        assertEquals(expected, actual);
    }

    /**
     * tests {@link OutputStream#write(byte[], int, int)} with various offsets
     */
    public void testWriteOffset() throws IOException {
        byte[] bytes = fromString("123456789012345678901234567890123456789012"
                + "3456789012345678901234567890123456789012345678901234567890"
                + "123456789012345");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(b);

        for (int offset = 0; offset < bytes.length; offset += 2) {
            int len = Math.min(2, bytes.length - offset);
            out.write(bytes, offset, len);
        }

        out.close();

        String actual = new String(b.toByteArray(), "US-ASCII");

        String expected = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nz"
                + "g5MDEyMzQ1Njc4OTAxMjM0NTY3\r\nODkwMTIzNDU2Nzg5MDEyMzQ1Njc4"
                + "OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0\r\nNQ==\r"
                + "\n";

        assertEquals(expected, actual);
    }

    /**
     * tests {@link OutputStream#flush()} while writing
     */
    public void testWriteFlush() throws IOException {
        byte[] bytes = fromString("123456789012345678901234567890123456789012"
                + "3456789012345678901234567890123456789012345678901234567890"
                + "123456789012345");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(b);

        for (int offset = 0; offset < bytes.length; offset += 7) {
            int len = Math.min(7, bytes.length - offset);
            out.write(bytes, offset, len);
            out.flush();
        }

        out.close();

        String actual = new String(b.toByteArray(), "US-ASCII");

        String expected = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nz"
                + "g5MDEyMzQ1Njc4OTAxMjM0NTY3\r\nODkwMTIzNDU2Nzg5MDEyMzQ1Njc4"
                + "OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0\r\nNQ==\r"
                + "\n";

        assertEquals(expected, actual);
    }

    private String encodeNoLs(String str) throws IOException {
        return encode(str, 0, new byte[] {});
    }

    private String encodeLs(String str) throws IOException {
        return encode(str, 12, new byte[] { '\r', '\n' });
    }

    private String encode(String str, int lineLength, byte[] lineSeparator)
            throws IOException {
        byte[] bytes = fromString(str);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(b, lineLength,
                lineSeparator);

        out.write(bytes);
        out.close();

        return toString(b.toByteArray());
    }
        
    private byte[] fromString(String s) {
        try {
            return s.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return null;
        }
    }
    
    private String toString(byte[] b) {
        try {
            return new String(b, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return null;
        }
    }
}
