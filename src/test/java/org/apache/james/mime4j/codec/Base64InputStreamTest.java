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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.log4j.BasicConfigurator;

import junit.framework.TestCase;

public class Base64InputStreamTest extends TestCase {

    @Override
    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }
    
    public void testDecode() throws IOException {
        ByteArrayInputStream bis = null;
        Base64InputStream decoder = null;
        byte[] bytes = null;
        
        /*
         * Simple initial test.
         */
        bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ=="));
        decoder = new Base64InputStream(bis);
        assertEquals("This is the plain text message!", toString(read(decoder)));
        
        /*
         * Test encoded text padded once, twice and not at all.
         */
        bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyBhIHRleHQgd2hpY2ggaGFzIHRvIGJl"
                        + "IHBhZGRlZCBvbmNlLi4="));
        decoder = new Base64InputStream(bis);
        assertEquals("This is a text which has to be padded once..", toString(read(decoder)));
        bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyBhIHRleHQgd2hpY2ggaGFzIHRvIGJl"
                        + "IHBhZGRlZCB0d2ljZQ=="));
        decoder = new Base64InputStream(bis);
        assertEquals("This is a text which has to be padded twice", toString(read(decoder)));
        bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyBhIHRleHQgd2hpY2ggd2lsbCBub3Qg"
                        + "YmUgcGFkZGVk"));
        decoder = new Base64InputStream(bis);
        assertEquals("This is a text which will not be padded", toString(read(decoder)));
        
        /*
         * Test that non base64 characters are ignored.
         */
        bis = new ByteArrayInputStream(
                fromString(" &% VGhp\r\ncyBp\r\ncyB0aGUgcGxhaW4g "
                        + " \tdGV4dCBtZ?!XNzY*WdlIQ=="));
        decoder = new Base64InputStream(bis);
        assertEquals("This is the plain text message!", toString(read(decoder)));
        
        /*
         * Test that the bytes 0-255 shifted 0, 1 and 2 positions are
         * decoded properly.
         */
        String s1 = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCU"
                  + "mJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZHSElKS0"
                  + "xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bxc"
                  + "nN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeY"
                  + "mZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6"
                  + "/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj5O"
                  + "Xm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";
        
        String s2 = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSY"
                  + "nKCkqKywtLi8wMTIzNDU2Nzg5Ojs8PT4/QEFCQ0RFRkdISUpLTE"
                  + "1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFyc"
                  + "3R1dnd4eXp7fH1+f4CBgoOEhYaHiImKi4yNjo+QkZKTlJWWl5iZ"
                  + "mpucnZ6foKGio6SlpqeoqaqrrK2ur7CxsrO0tba3uLm6u7y9vr/"
                  + "AwcLDxMXGx8jJysvMzc7P0NHS09TV1tfY2drb3N3e3+Dh4uPk5e"
                  + "bn6Onq6+zt7u/w8fLz9PX29/j5+vv8/f7/AA==";
        
        String s3 = "AgMEBQYHCAkKCwwNDg8QERITFBUWFxgZGhscHR4fICEiIyQlJic"
                  + "oKSorLC0uLzAxMjM0NTY3ODk6Ozw9Pj9AQUJDREVGR0hJSktMTU"
                  + "5PUFFSU1RVVldYWVpbXF1eX2BhYmNkZWZnaGlqa2xtbm9wcXJzd"
                  + "HV2d3h5ent8fX5/gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJma"
                  + "m5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8D"
                  + "BwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5u"
                  + "fo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8AAQ==";
        
        bis = new ByteArrayInputStream(fromString(s1));
        decoder = new Base64InputStream(bis);
        bytes = read(decoder);
        
        for (int i = 0; i < bytes.length; i++) {
            assertEquals("Position " + i, bytes[i], (byte) i);
        }
        
        bis = new ByteArrayInputStream(fromString(s2));
        decoder = new Base64InputStream(bis);
        bytes = read(decoder);
        
        for (int i = 0; i < bytes.length; i++) {
            assertEquals("Position " + i, bytes[i], (byte) (i + 1));
        }
        
        bis = new ByteArrayInputStream(fromString(s3));
        decoder = new Base64InputStream(bis);
        bytes = read(decoder);
        
        for (int i = 0; i < bytes.length; i++) {
            assertEquals("Position " + i, bytes[i], (byte) (i + 2));
        }
    }

    public void testDecodePrematureClose() throws IOException {
        ByteArrayInputStream bis = null;
        Base64InputStream decoder = null;
        
        bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ=="));
        decoder = new Base64InputStream(bis);
        assertEquals('T', decoder.read());
        assertEquals('h', decoder.read());
        decoder.close();
        
        try {
            decoder.read();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testRoundtripWithVariousBufferSizes() throws Exception {
        byte[] data = new byte[3719];
        new Random(0).nextBytes(data);

        ByteArrayOutputStream eOut = new ByteArrayOutputStream();
        CodecUtil.encodeBase64(new ByteArrayInputStream(data), eOut);
        byte[] encoded = eOut.toByteArray();

        for (int bufferSize = 1; bufferSize <= 1009; bufferSize++) {
            ByteArrayInputStream bis = new ByteArrayInputStream(encoded);
            Base64InputStream decoder = new Base64InputStream(bis);
            ByteArrayOutputStream dOut = new ByteArrayOutputStream();

            final byte[] buffer = new byte[bufferSize];
            int inputLength;
            while (-1 != (inputLength = decoder.read(buffer))) {
                dOut.write(buffer, 0, inputLength);
            }

            byte[] decoded = dOut.toByteArray();

            assertEquals(data.length, decoded.length);
            for (int i = 0; i < data.length; i++) {
                assertEquals(data[i], decoded[i]);
            }
        }
    }

    /**
     * Tests {@link InputStream#read()}
     */
    public void testReadInt() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ=="));
        Base64InputStream decoder = new Base64InputStream(bis);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (true) {
            int x = decoder.read();
            if (x == -1)
                break;
            out.write(x);
        }

        assertEquals("This is the plain text message!", toString(out
                .toByteArray()));
    }

    /**
     * Tests {@link InputStream#read(byte[], int, int)} with various offsets
     */
    public void testReadOffset() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ=="));
        Base64InputStream decoder = new Base64InputStream(bis);

        byte[] data = new byte[36];
        for (int i = 0;;) {
            int bytes = decoder.read(data, i, 5);
            if (bytes == -1)
                break;
            i += bytes;
        }

        assertEquals("This is the plain text message!\0\0\0\0\0",
                toString(data));
    }

    public void testStrictUnexpectedEof() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI"));
        Base64InputStream decoder = new Base64InputStream(bis, true);
        try {
            CodecUtil.copy(decoder, new NullOutputStream());
            fail();
        } catch (IOException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains(
                    "end of file"));
        }
    }

    public void testLenientUnexpectedEof() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI"));
        Base64InputStream decoder = new Base64InputStream(bis, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.copy(decoder, out);
        assertEquals("This is the plain text message", toString(out
                .toByteArray()));
    }

    public void testStrictUnexpectedPad() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI="));
        Base64InputStream decoder = new Base64InputStream(bis, true);
        try {
            CodecUtil.copy(decoder, new NullOutputStream());
            fail();
        } catch (IOException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("pad"));
        }
    }

    public void testLenientUnexpectedPad() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                fromString("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI="));
        Base64InputStream decoder = new Base64InputStream(bis, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.copy(decoder, out);
        assertEquals("This is the plain text message", toString(out
                .toByteArray()));
    }
        
    private byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            bos.write(b);
        }
        return bos.toByteArray();
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
