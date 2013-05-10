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

package org.apache.james.mime4j.io;

import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MimeBoundaryInputStreamTest {

    @Test
    public void testBasicReading() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n--boundary--";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));

        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096);

        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 1\r\nLine 2", read(mime1, 5));

        Assert.assertFalse(mime1.isLastPart());

        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 3\r\nLine 4", read(mime2, 5));

        Assert.assertTrue(mime2.isLastPart());
    }

    @Test
    public void testLenientLineDelimiterReading() throws IOException {
        String text = "Line 1\r\nLine 2\n--boundary\n" +
                "Line 3\r\nLine 4\n--boundary--\n";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));

        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096);

        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 1\r\nLine 2", read(mime1, 5));

        Assert.assertFalse(mime1.isLastPart());

        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 3\r\nLine 4", read(mime2, 5));

        Assert.assertTrue(mime2.isLastPart());
    }

    @Test
    public void testBasicReadingSmallBuffer1() throws IOException {
        String text = "yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada\r\n--boundary\r\n" +
                "blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah\r\n--boundary--";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));

        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 20);

        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada",
                read(mime1, 10));

        Assert.assertFalse(mime1.isLastPart());

        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah",
                read(mime2, 10));

        Assert.assertTrue(mime2.isLastPart());
    }

    @Test
    public void testBasicReadingSmallBuffer2() throws IOException {
        String text = "yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada\r\n--boundary\r\n" +
                "blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah\r\n--boundary--";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));

        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 20);

        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");

        Assert.assertEquals("yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada",
                read(mime1, 25));

        Assert.assertFalse(mime1.isLastPart());

        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah",
                read(mime2, 25));

        Assert.assertTrue(mime2.isLastPart());
    }

    @Test
    public void testBasicReadingByOneByte() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n--boundary--";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));

        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096);

        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 1\r\nLine 2", readByOneByte(mime1));

        Assert.assertFalse(mime1.isLastPart());

        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 3\r\nLine 4", readByOneByte(mime2));

        Assert.assertTrue(mime2.isLastPart());
    }

    /**
     * Tests that a CRLF immediately preceding a boundary isn't included in
     * the stream.
     */
    @Test
    public void testCRLFPrecedingBoundary() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n\r\n--boundary\r\n";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));

        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096);

        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 1\r\nLine 2", read(mime1, 5));

        Assert.assertFalse(mime1.isLastPart());

        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 3\r\nLine 4\r\n", read(mime2, 5));

        Assert.assertFalse(mime2.isLastPart());
    }

    private String readByOneByte(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    private String read(InputStream is, int bufsize) throws IOException {
        StringBuilder sb = new StringBuilder();
        int l;
        byte[] tmp = new byte[bufsize];
        while ((l = is.read(tmp)) != -1) {
            for (int i = 0; i < l; i++) {
                sb.append((char) tmp[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Tests that a stream containing only a boundary is empty.
     */
    @Test
    public void testImmediateBoundary() throws IOException {
        String text1 = "--boundary\r\n";

        BufferedLineReaderInputStream buffer1 = new BufferedLineReaderInputStream(
                new ByteArrayInputStream(text1.getBytes()), 4096);
        MimeBoundaryInputStream stream1 = new MimeBoundaryInputStream(buffer1, "boundary");
        Assert.assertEquals(-1, stream1.read());

        stream1.close();

        String text2 = "\r\n--boundary\r\n";

        BufferedLineReaderInputStream buffer2 = new BufferedLineReaderInputStream(
                new ByteArrayInputStream(text2.getBytes()), 4096);
        MimeBoundaryInputStream stream2 = new MimeBoundaryInputStream(buffer2, "boundary");
        Assert.assertEquals(-1, stream2.read());

        stream2.close();
    }

    /**
     * Tests that hasMoreParts behave as expected.
     */
    @Test
    public void testHasMoreParts() throws IOException {
        String text = "--boundary--\r\n";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096);
        MimeBoundaryInputStream stream =
                new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals(-1, stream.read());
        Assert.assertTrue(stream.isLastPart());

        stream.close();
    }

    /**
     * Tests that a stream containing only a boundary is empty.
     */
    @Test
    public void testPrefixIsBoundary() throws IOException {
        String text = "Line 1\r\n\r\n--boundary\r\n";

        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096);
        MimeBoundaryInputStream stream = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals("Line 1\r\n", read(stream, 100));

        text = "--boundary\r\n";

        bis = new ByteArrayInputStream(text.getBytes());
        buffer = new BufferedLineReaderInputStream(bis, 4096);
        stream = new MimeBoundaryInputStream(buffer, "boundary");
        Assert.assertEquals(-1, stream.read());

        stream.close();
    }


    @Test
    public void testBasicReadLine() throws Exception {

        String[] teststrs = new String[5];
        teststrs[0] = "Hello\r\n";
        teststrs[1] = "This string should be much longer than the size of the input buffer " +
                "which is only 20 bytes for this test\r\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("123456789 ");
        }
        sb.append("and stuff like that\r\n");
        teststrs[2] = sb.toString();
        teststrs[3] = "\r\n";
        teststrs[4] = "And goodbye\r\n";

        String term = "\r\n--1234\r\n";

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();

        for (String teststr : teststrs) {
            outstream.write(teststr.getBytes("US-ASCII"));
        }
        outstream.write(term.getBytes("US-ASCII"));
        byte[] raw = outstream.toByteArray();

        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(
                new ByteArrayInputStream(raw), 20);
        LineReaderInputStream instream = new MimeBoundaryInputStream(inbuffer, "1234");

        ByteArrayBuffer linebuf = new ByteArrayBuffer(8);
        for (String teststr : teststrs) {
            linebuf.clear();
            instream.readLine(linebuf);
            String s = new String(linebuf.toByteArray(), "US-ASCII");
            Assert.assertEquals(teststr, s);
        }
        Assert.assertEquals(-1, instream.readLine(linebuf));
        Assert.assertEquals(-1, instream.readLine(linebuf));

        instream.close();
    }

    @Test
    public void testReadEmptyLine() throws Exception {

        String teststr = "01234567890123456789\n\n\r\n\r\r\n\n\n\n\n\n--1234\r\n";
        byte[] raw = teststr.getBytes("US-ASCII");

        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(
                new ByteArrayInputStream(raw), 20);
        LineReaderInputStream instream = new MimeBoundaryInputStream(inbuffer, "1234");

        ByteArrayBuffer linebuf = new ByteArrayBuffer(8);
        linebuf.clear();
        instream.readLine(linebuf);
        String s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("01234567890123456789\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\r\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\r\r\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        Assert.assertEquals("\n", s);

        Assert.assertEquals(-1, instream.readLine(linebuf));
        Assert.assertEquals(-1, instream.readLine(linebuf));

        instream.close();
    }

}
