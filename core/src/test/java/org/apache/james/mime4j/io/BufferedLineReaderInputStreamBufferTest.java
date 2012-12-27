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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class BufferedLineReaderInputStreamBufferTest {

    @Test
    public void testInvalidInput() throws Exception {
        String text = "blah blah yada yada";
        byte[] b1 = text.getBytes("US-ASCII");
        String pattern = "blah";
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
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
    }

    @Test
    public void testBasicOperations() throws Exception {
        String text = "bla bla yada yada haha haha";
        byte[] b1 = text.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
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
    }

    @Test
    public void testPatternMatching1() throws Exception {
        String text = "blabla d is the word";
        String pattern = "d";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(7, i);
    }

    @Test
    public void testPatternMatching2() throws Exception {
        String text = "disddisdissdsidsidsiid";
        String pattern = "siid";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(18, i);
    }

    @Test
    public void testPatternMatching3() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "blah";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(-1, i);
    }

    @Test
    public void testPatternMatching4() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "bla";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(0, i);
    }

    @Test
    public void testPatternOutOfBound() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern1 = "bla bla";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern1.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        byte[] tmp = new byte[3];
        inbuffer.read(tmp);
        int i = inbuffer.indexOf(b2, inbuffer.pos(), inbuffer.length());
        Assert.assertEquals(-1, i);
        i = inbuffer.indexOf(b2, inbuffer.pos(), inbuffer.length() - 1);
        Assert.assertEquals(-1, i);
    }

    @Test
    public void testCharOutOfBound() throws Exception {
        String text = "zzz blah blah blah ggg";
        byte[] b1 = text.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        byte[] tmp = new byte[3];
        inbuffer.read(tmp);
        int i = inbuffer.indexOf((byte) 'z', inbuffer.pos(), inbuffer.length());
        Assert.assertEquals(-1, i);
        i = inbuffer.indexOf((byte) 'g', inbuffer.pos(), inbuffer.length() - 3);
        Assert.assertEquals(-1, i);
    }

    @Test
    public void test0xFFInBinaryStream() throws Exception {
        byte[] b1 = new byte[]{1, 2, 3, (byte) 0xff, 10, 1, 2, 3};
        byte[] b2 = new byte[]{10};
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        Assert.assertEquals(4, i);
    }
}
