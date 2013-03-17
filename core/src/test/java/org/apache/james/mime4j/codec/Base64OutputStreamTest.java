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

import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Base64OutputStreamTest {

    @Test
    public void testEncode() throws IOException {
        ByteArrayOutputStream bos;
        Base64OutputStream encoder;

        /*
         * Simple initial test.
         */
        bos = new ByteArrayOutputStream();
        encoder = new Base64OutputStream(bos);
        encoder.write(fromString("This is the plain text message!"));
        encoder.close();
        Assert.assertEquals("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==\r\n", toString(bos.toByteArray()));
    }

    @Test
    public void testEncodeUnderlyingStreamStaysOpen() throws IOException {
        ByteArrayOutputStream bos;
        Base64OutputStream encoder;

        bos = new ByteArrayOutputStream();
        encoder = new Base64OutputStream(bos);
        encoder.write(fromString("This is the plain text message!"));
        encoder.close();

        try {
            encoder.write('b');
            Assert.fail();
        } catch (IOException expected) {
        }

        bos.write('y');
        bos.write('a');
        bos.write('d');
        bos.write('a');
        Assert.assertEquals("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==\r\nyada", toString(bos.toByteArray()));
    }

    @Test
    public void testNoLineSeparators() throws IOException {
        Assert.assertEquals("", encodeNoLs(""));
        Assert.assertEquals("YQ==", encodeNoLs("a"));
        Assert.assertEquals("YWI=", encodeNoLs("ab"));
        Assert.assertEquals("YWJj", encodeNoLs("abc"));
        Assert.assertEquals("YWJjZA==", encodeNoLs("abcd"));
        Assert.assertEquals("YWJjZGU=", encodeNoLs("abcde"));
        Assert.assertEquals("YWJjZGVm", encodeNoLs("abcdef"));
        Assert.assertEquals("YWJjZGVmZw==", encodeNoLs("abcdefg"));
        Assert.assertEquals("YWJjZGVmZ2g=", encodeNoLs("abcdefgh"));
        Assert.assertEquals("YWJjZGVmZ2hp", encodeNoLs("abcdefghi"));
        Assert.assertEquals("DQoMCQ==", encodeNoLs("\r\n\f\t"));
        Assert.assertEquals("LT0/VGhhdCdzIGEgdGVzdD89LQ==",
                encodeNoLs("-=?That's a test?=-"));
    }

    @Test
    public void testLineSeparators() throws IOException {
        Assert.assertEquals("", encodeLs(""));
        Assert.assertEquals("YQ==\r\n", encodeLs("a"));
        Assert.assertEquals("YWJjZA==\r\n", encodeLs("abcd"));
        Assert.assertEquals("YWJjZGVmZw==\r\n", encodeLs("abcdefg"));
        Assert.assertEquals("YWJjZGVmZ2g=\r\n", encodeLs("abcdefgh"));
        Assert.assertEquals("YWJjZGVmZ2hp\r\n", encodeLs("abcdefghi"));
        Assert.assertEquals("YWJjZGVmZ2hp\r\nag==\r\n", encodeLs("abcdefghij"));
        Assert.assertEquals("YWJjZGVmZ2hp\r\nams=\r\n", encodeLs("abcdefghijk"));
        Assert.assertEquals("YWJjZGVmZ2hp\r\namts\r\n", encodeLs("abcdefghijkl"));
    }

    /**
     * tests {@link OutputStream#write(int)}
     */
    @Test
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

        Assert.assertEquals(expected, actual);
    }

    /**
     * tests {@link OutputStream#write(byte[], int, int)} with various offsets
     */
    @Test
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

        Assert.assertEquals(expected, actual);
    }

    /**
     * tests {@link OutputStream#flush()} while writing
     */
    @Test
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

        Assert.assertEquals(expected, actual);
    }

    private String encodeNoLs(String str) throws IOException {
        return encode(str, 0, new byte[]{});
    }

    private String encodeLs(String str) throws IOException {
        return encode(str, 12, new byte[]{'\r', '\n'});
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
