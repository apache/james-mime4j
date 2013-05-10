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
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class BufferedLineReaderInputStreamTest {

    private static BufferedLineReaderInputStream create(final String s, int bufSize) {
        return new BufferedLineReaderInputStream(InputStreams.createAscii(s), bufSize);
    }

    private static BufferedLineReaderInputStream create(final byte[] b, int bufSize) {
        return new BufferedLineReaderInputStream(InputStreams.create(b), bufSize);
    }

    private static BufferedLineReaderInputStream create(final byte[] b, int bufSize, int maxLen) {
        return new BufferedLineReaderInputStream(InputStreams.create(b), bufSize, maxLen);
    }

    @Test
    public void testBasicOperations() throws Exception {
        String text = "ah blahblah";
        BufferedLineReaderInputStream instream = create(text, 32);

        Assert.assertEquals((byte) 'a', instream.read());
        Assert.assertEquals((byte) 'h', instream.read());
        Assert.assertEquals((byte) ' ', instream.read());

        byte[] tmp1 = new byte[4];
        Assert.assertEquals(4, instream.read(tmp1));
        Assert.assertEquals(4, instream.read(tmp1));

        Assert.assertEquals(-1, instream.read(tmp1));
        Assert.assertEquals(-1, instream.read(tmp1));
        Assert.assertEquals(-1, instream.read());
        Assert.assertEquals(-1, instream.read());

        instream.close();
    }

    @Test
    public void testBasicReadLine() throws Exception {

        String[] teststrs = new String[5];
        teststrs[0] = "Hello\r\n";
        teststrs[1] = "This string should be much longer than the size of the input buffer " +
                "which is only 16 bytes for this test\r\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("123456789 ");
        }
        sb.append("and stuff like that\r\n");
        teststrs[2] = sb.toString();
        teststrs[3] = "\r\n";
        teststrs[4] = "And goodbye\r\n";

        ByteArrayBuffer buf = new ByteArrayBuffer(128);

        for (String teststr : teststrs) {
            byte[] b = ContentUtil.toAsciiByteArray(teststr);
            buf.append(b, 0, b.length);
        }
        byte[] raw = buf.toByteArray();

        BufferedLineReaderInputStream instream = create(raw, 16);

        ByteArrayBuffer linebuf = new ByteArrayBuffer(8);
        for (String teststr : teststrs) {
            linebuf.clear();
            instream.readLine(linebuf);
            String s = ContentUtil.toAsciiString(linebuf);
            Assert.assertEquals(teststr, s);
        }
        Assert.assertEquals(-1, instream.readLine(linebuf));
        Assert.assertEquals(-1, instream.readLine(linebuf));

        instream.close();
    }

    @Test
    public void testReadEmptyLine() throws Exception {

        String teststr = "\n\n\r\n\r\r\n\n\n\n\n\n";
        byte[] raw = ContentUtil.toAsciiByteArray(teststr);

        BufferedLineReaderInputStream instream = create(raw, 4);

        ByteArrayBuffer linebuf = new ByteArrayBuffer(8);
        linebuf.clear();
        instream.readLine(linebuf);
        String s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\r\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\r\r\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = ContentUtil.toAsciiString(linebuf);
        Assert.assertEquals("\n", s);

        Assert.assertEquals(-1, instream.readLine(linebuf));
        Assert.assertEquals(-1, instream.readLine(linebuf));

        instream.close();
    }

    @Test
    public void testReadEmptyLineMaxLimit() throws Exception {

        String teststr = "1234567890\r\n";
        byte[] raw = ContentUtil.toAsciiByteArray(teststr);

        LineReaderInputStream instream1 = create(raw, 1024, 13);
        ByteArrayBuffer linebuf = new ByteArrayBuffer(8);
        linebuf.clear();
        instream1.readLine(linebuf);
        instream1.close();

        LineReaderInputStream instream2 = create(raw, 1024, 12);
        linebuf.clear();
        try {
            instream2.readLine(linebuf);
            Assert.fail("MaxLineLimitException should have been thrown");
        } catch (MaxLineLimitException ex) {
        }

        instream2.close();
    }

}
