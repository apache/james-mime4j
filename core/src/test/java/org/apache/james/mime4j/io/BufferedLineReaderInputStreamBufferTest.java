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

import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class BufferedLineReaderInputStreamBufferTest {

    private static BufferedLineReaderInputStream create(final String s) {
        return new BufferedLineReaderInputStream(InputStreams.createAscii(s), 4096);
    }

    private static BufferedLineReaderInputStream create(final byte[] b) {
        return new BufferedLineReaderInputStream(InputStreams.create(b), 4096);
    }

    @Test
    public void testInvalidInput() throws Exception {
        String text = "blah blah yada yada";
        String pattern = "blah";
        byte[] b2 = ContentUtil.toAsciiByteArray(pattern);
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();

        Assert.assertEquals('b', inbuffer.read());
        Assert.assertEquals('l', inbuffer.read());

        try {
            inbuffer.byteAt(1);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.byteAt(20);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, -1, 3);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, 1, 3);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, 2, -1);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, 2, 18);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        Assert.assertEquals(5, inbuffer.indexOf(b2, 2, 17));
        try {
            inbuffer.indexOf((byte) ' ', -1, 3);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf((byte) ' ', 1, 3);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf((byte) ' ', 2, -1);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf((byte) ' ', 2, 18);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        Assert.assertEquals(10, inbuffer.indexOf((byte) 'y', 2, 17));
        inbuffer.close();
    }

    @Test
    public void testBasicOperations() throws Exception {
        String text = "bla bla yada yada haha haha";
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        Assert.assertEquals(0, inbuffer.pos());
        Assert.assertEquals(27, inbuffer.limit());
        Assert.assertEquals(27, inbuffer.length());

        inbuffer.read();
        inbuffer.read();

        Assert.assertEquals(2, inbuffer.pos());
        Assert.assertEquals(27, inbuffer.limit());
        Assert.assertEquals(25, inbuffer.length());

        byte[] tmp1 = new byte[3];
        Assert.assertEquals(3, inbuffer.read(tmp1));

        Assert.assertEquals(5, inbuffer.pos());
        Assert.assertEquals(27, inbuffer.limit());
        Assert.assertEquals(22, inbuffer.length());

        byte[] tmp2 = new byte[22];
        Assert.assertEquals(22, inbuffer.read(tmp2));

        Assert.assertEquals(27, inbuffer.pos());
        Assert.assertEquals(27, inbuffer.limit());
        Assert.assertEquals(0, inbuffer.length());

        Assert.assertEquals(-1, inbuffer.read(tmp1));
        Assert.assertEquals(-1, inbuffer.read(tmp1));
        Assert.assertEquals(-1, inbuffer.read());
        Assert.assertEquals(-1, inbuffer.read());

        inbuffer.close();
    }

    @Test
    public void testPatternMatching1() throws Exception {
        String text = "blabla d is the word";
        String pattern = "d";
        byte[] b2 = ContentUtil.toAsciiByteArray(pattern);
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(7, i);

        inbuffer.close();
    }

    @Test
    public void testPatternMatching2() throws Exception {
        String text = "disddisdissdsidsidsiid";
        String pattern = "siid";
        byte[] b2 = ContentUtil.toAsciiByteArray(pattern);
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(18, i);

        inbuffer.close();
    }

    @Test
    public void testPatternMatching3() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "blah";
        byte[] b2 = ContentUtil.toAsciiByteArray(pattern);
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(-1, i);

        inbuffer.close();
    }

    @Test
    public void testPatternMatching4() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "bla";
        byte[] b2 = ContentUtil.toAsciiByteArray(pattern);
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(0, i);

        inbuffer.close();
    }

    @Test
    public void testPatternOutOfBound() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern1 = "bla bla";
        byte[] b2 = ContentUtil.toAsciiByteArray(pattern1);
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        byte[] tmp = new byte[3];
        inbuffer.read(tmp);
        int i = inbuffer.indexOf(b2, inbuffer.pos(), inbuffer.length());
        Assert.assertEquals(-1, i);
        i = inbuffer.indexOf(b2, inbuffer.pos(), inbuffer.length() - 1);
        Assert.assertEquals(-1, i);

        inbuffer.close();
    }

    @Test
    public void testCharOutOfBound() throws Exception {
        String text = "zzz blah blah blah ggg";
        BufferedLineReaderInputStream inbuffer = create(text);
        inbuffer.fillBuffer();
        byte[] tmp = new byte[3];
        inbuffer.read(tmp);
        int i = inbuffer.indexOf((byte) 'z', inbuffer.pos(), inbuffer.length());
        Assert.assertEquals(-1, i);
        i = inbuffer.indexOf((byte) 'g', inbuffer.pos(), inbuffer.length() - 3);
        Assert.assertEquals(-1, i);

        inbuffer.close();
    }

    @Test
    public void test0xFFInBinaryStream() throws Exception {
        byte[] b1 = new byte[]{1, 2, 3, (byte) 0xff, 10, 1, 2, 3};
        byte[] b2 = new byte[]{10};
        BufferedLineReaderInputStream inbuffer = create(b1);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(4, i);

        inbuffer.close();
    }
}
