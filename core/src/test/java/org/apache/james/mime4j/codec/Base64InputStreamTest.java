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
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class Base64InputStreamTest {

    private static Base64InputStream create(final String s) {
        return new Base64InputStream(InputStreams.createAscii(s));
    }

    private static Base64InputStream createStrict(final String s) {
        return new Base64InputStream(InputStreams.createAscii(s), true);
    }

    private static Base64InputStream create(final byte[] b) {
        return new Base64InputStream(InputStreams.create(b));
    }

    private static byte[] readBin(final InputStream is) throws IOException {
        return IOUtils.toByteArray(is);
    }

    private static String readText(final InputStream is) throws IOException {
        return ContentUtil.toAsciiString(IOUtils.toByteArray(is));
    }

    @Test
    public void testDecode() throws IOException {
        Base64InputStream decoder;

        /*
         * Simple initial test.
         */
        decoder = create("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==");
        Assert.assertEquals("This is the plain text message!", readText(decoder));

        /*
         * Test encoded text padded once, twice and not at all.
         */
        decoder = create(("VGhpcyBpcyBhIHRleHQgd2hpY2ggaGFzIHRvIGJl"
                        + "IHBhZGRlZCBvbmNlLi4="));
        Assert.assertEquals("This is a text which has to be padded once..", readText(decoder));
        decoder = create(("VGhpcyBpcyBhIHRleHQgd2hpY2ggaGFzIHRvIGJl"
                        + "IHBhZGRlZCB0d2ljZQ=="));
        Assert.assertEquals("This is a text which has to be padded twice", readText(decoder));
        decoder = create(("VGhpcyBpcyBhIHRleHQgd2hpY2ggd2lsbCBub3Qg"
                        + "YmUgcGFkZGVk"));
        Assert.assertEquals("This is a text which will not be padded", readText(decoder));

        /*
         * Test that non base64 characters are ignored.
         */
        decoder = create((" &% VGhp\r\ncyBp\r\ncyB0aGUgcGxhaW4g "
                        + " \tdGV4dCBtZ?!XNzY*WdlIQ=="));
        Assert.assertEquals("This is the plain text message!", readText(decoder));

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

        byte[] bytes;

        decoder = create(s1);
        bytes = readBin(decoder);

        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals("Position " + i, bytes[i], (byte) i);
        }

        decoder = create(s2);
        bytes = readBin(decoder);

        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals("Position " + i, bytes[i], (byte) (i + 1));
        }

        decoder = create(s3);
        bytes = readBin(decoder);

        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals("Position " + i, bytes[i], (byte) (i + 2));
        }
    }

    @Test
    public void testDecodePrematureClose() throws IOException {
        Base64InputStream decoder = create("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==");
        Assert.assertEquals('T', decoder.read());
        Assert.assertEquals('h', decoder.read());
        decoder.close();

        try {
            decoder.read();
            Assert.fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testRoundtripWithVariousBufferSizes() throws Exception {
        byte[] data = new byte[3719];
        new Random(0).nextBytes(data);

        ByteArrayOutputStream eOut = new ByteArrayOutputStream();
        EncoderUtil.encodeB(InputStreams.create(data), eOut);
        byte[] encoded = eOut.toByteArray();

        for (int bufferSize = 1; bufferSize <= 1009; bufferSize++) {
            Base64InputStream decoder = create(encoded);
            ByteArrayOutputStream dOut = new ByteArrayOutputStream();

            final byte[] buffer = new byte[bufferSize];
            int inputLength;
            while (-1 != (inputLength = decoder.read(buffer))) {
                dOut.write(buffer, 0, inputLength);
            }

            byte[] decoded = dOut.toByteArray();

            Assert.assertEquals(data.length, decoded.length);
            for (int i = 0; i < data.length; i++) {
                Assert.assertEquals(data[i], decoded[i]);
            }
            decoder.close();
        }
    }

    /**
     * Tests {@link InputStream#read()}
     */
    @Test
    public void testReadInt() throws Exception {
        Base64InputStream decoder = create("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (true) {
            int x = decoder.read();
            if (x == -1)
                break;
            out.write(x);
        }
        decoder.close();

        Assert.assertEquals("This is the plain text message!",
                ContentUtil.toAsciiString(out.toByteArray()));
    }

    /**
     * Tests {@link InputStream#read(byte[], int, int)} with various offsets
     */
    @Test
    public void testReadOffset() throws Exception {
        Base64InputStream decoder = create("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlIQ==");
        byte[] data = new byte[36];
        for (int i = 0; ; ) {
            int bytes = decoder.read(data, i, 5);
            if (bytes == -1)
                break;
            i += bytes;
        }
        decoder.close();

        Assert.assertEquals("This is the plain text message!\0\0\0\0\0",
                ContentUtil.toAsciiString(data));
    }

    @Test
    public void testStrictUnexpectedEof() throws Exception {
        Base64InputStream decoder = createStrict("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI");
        try {
            ContentUtil.copy(decoder, new NullOutputStream());
            Assert.fail();
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().toLowerCase().contains(
                    "end of base64 stream"));
        }
    }

    @Test
    public void testLenientUnexpectedEof() throws Exception {
        Base64InputStream decoder = create("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI");
        byte[] buf = ContentUtil.buffer(decoder);
        Assert.assertEquals("This is the plain text message", ContentUtil.toAsciiString(buf));
    }

    @Test
    public void testStrictUnexpectedPad() throws Exception {
        Base64InputStream decoder = createStrict("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI=");
        try {
            ContentUtil.copy(decoder, new NullOutputStream());
            Assert.fail();
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().toLowerCase().contains("pad"));
        }
    }

    @Test
    public void testLenientUnexpectedPad() throws Exception {
        Base64InputStream decoder = create("VGhpcyBpcyB0aGUgcGxhaW4gdGV4dCBtZXNzYWdlI=");
        byte[] buf = ContentUtil.buffer(decoder);
        Assert.assertEquals("This is the plain text message", ContentUtil.toAsciiString(buf));
    }

}
