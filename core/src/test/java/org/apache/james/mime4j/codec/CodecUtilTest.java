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

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class CodecUtilTest {

    @Test
    public void testCopy() throws Exception {
        byte[] content = ExampleMail.MULTIPART_WITH_BINARY_ATTACHMENTS_BYTES;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.copy(InputStreams.create(content), out);
        assertArrayEquals(content, out.toByteArray());
    }

    @Test
    public void testEncodeQuotedPrintableLargeInput() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024 * 5; i++) {
            sb.append((char) ('0' + (i % 10)));
        }
        String expected = sb.toString().replaceAll("(\\d{75})", "$1=\r\n");

        InputStream in = InputStreams.createAscii(sb.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.encodeQuotedPrintableBinary(in, out);
        String actual = ContentUtil.toAsciiString(out.toByteArray());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testEncodeQuotedPrintableNonAsciiChars() throws Exception {
        String s = "7bit content with euro \u20AC symbol";
        InputStream in = InputStreams.create(s, Charset.forName("iso-8859-15"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.encodeQuotedPrintableBinary(in, out);
        String actual = new String(out.toByteArray(), "US-ASCII");
        Assert.assertEquals("7bit=20content=20with=20euro=20=A4=20symbol", actual);
    }

    @Test
    public void testBase64OutputStream() throws Exception {
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < 128; i++) {
            sb.append("0123456789ABCDEF");
        }
        String input = sb.toString();
        String output = roundtripUsingOutputStream(input);
        Assert.assertEquals(input, output);
    }

    private String roundtripUsingOutputStream(String input) throws IOException {
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        Base64OutputStream outb64 = new Base64OutputStream(out2, 76);
        CodecUtil.copy(InputStreams.create(input, CharsetUtil.ISO_8859_1), outb64);
        outb64.flush();
        outb64.close();

        InputStream is = new Base64InputStream(InputStreams.create(out2.toByteArray()));
        ByteArrayOutputStream outRoundtrip = new ByteArrayOutputStream();
        CodecUtil.copy(is, outRoundtrip);
        return new String(outRoundtrip.toByteArray());
    }

    /**
     * This test is a proof for MIME4J-67
     */
    @Test
    public void testBase64Encoder() throws Exception {
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < 128; i++) {
            sb.append("0123456789ABCDEF");
        }
        String input = sb.toString();
        String output = roundtripUsingEncoder(input);
        Assert.assertEquals(input, output);
    }

    private String roundtripUsingEncoder(String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.encodeBase64(InputStreams.createAscii(input), out);

        InputStream is = new Base64InputStream(InputStreams.create(out.toByteArray()));
        ByteArrayOutputStream outRoundtrip = new ByteArrayOutputStream();
        CodecUtil.copy(is, outRoundtrip);
        return new String(outRoundtrip.toByteArray());
    }

    /* performance test, not a unit test */
    /*
    public void testPerformance() throws Exception {
        // if (true) return;
        byte[] bytes = new byte[10000];
        Random r = new Random(432875623874L);
        r.nextBytes(bytes);
        long totalEncoder1 = 0;
        long totalStream1 = 0;
        long totalEncoder2 = 0;
        for (int i = 0; i < 10000; i++) {
            int length = r.nextInt(1000);
            int pos = r.nextInt(9000);
            String input = new String(bytes, pos, length);
            long time1 = System.currentTimeMillis();
            roundtripUsingEncoder(input);
            long time2 = System.currentTimeMillis();
            roundtripUsingOutputStream(input);
            long time3 = System.currentTimeMillis();
            roundtripUsingEncoder(input);
            long time4 = System.currentTimeMillis();

            totalEncoder1 += time2-time1;
            totalStream1 += time3-time2;
            totalEncoder2 += time4-time3;
        }

        System.out.println("Encoder 1st: "+totalEncoder1);
        System.out.println("Encoder 2nd: "+totalEncoder2);
        System.out.println("Stream 1st: "+totalStream1);
    }
    */
}
