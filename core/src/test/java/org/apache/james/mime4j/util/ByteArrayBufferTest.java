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

package org.apache.james.mime4j.util;

import org.junit.Assert;
import org.junit.Test;

public class ByteArrayBufferTest {

    @Test
    public void testConstructor() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(16);
        Assert.assertEquals(16, buffer.capacity());
        Assert.assertEquals(0, buffer.length());
        Assert.assertNotNull(buffer.buffer());
        Assert.assertEquals(16, buffer.buffer().length);
        try {
            new ByteArrayBuffer(-1);
            Assert.fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testSimpleAppend() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(16);
        Assert.assertEquals(16, buffer.capacity());
        Assert.assertEquals(0, buffer.length());
        byte[] b1 = buffer.toByteArray();
        Assert.assertNotNull(b1);
        Assert.assertEquals(0, b1.length);
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertFalse(buffer.isFull());

        byte[] tmp = new byte[]{1, 2, 3, 4};
        buffer.append(tmp, 0, tmp.length);
        Assert.assertEquals(16, buffer.capacity());
        Assert.assertEquals(4, buffer.length());
        Assert.assertFalse(buffer.isEmpty());
        Assert.assertFalse(buffer.isFull());

        byte[] b2 = buffer.toByteArray();
        Assert.assertNotNull(b2);
        Assert.assertEquals(4, b2.length);
        for (int i = 0; i < tmp.length; i++) {
            Assert.assertEquals(tmp[i], b2[i]);
            Assert.assertEquals(tmp[i], buffer.byteAt(i));
        }
        buffer.clear();
        Assert.assertEquals(16, buffer.capacity());
        Assert.assertEquals(0, buffer.length());
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertFalse(buffer.isFull());
    }

    @Test
    public void testExpandAppend() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(4);
        Assert.assertEquals(4, buffer.capacity());

        byte[] tmp = new byte[]{1, 2, 3, 4};
        buffer.append(tmp, 0, 2);
        buffer.append(tmp, 0, 4);
        buffer.append(tmp, 0, 0);

        Assert.assertEquals(8, buffer.capacity());
        Assert.assertEquals(6, buffer.length());

        buffer.append(tmp, 0, 4);

        Assert.assertEquals(16, buffer.capacity());
        Assert.assertEquals(10, buffer.length());
    }

    @Test
    public void testInvalidAppend() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(4);
        buffer.append((byte[]) null, 0, 0);

        byte[] tmp = new byte[]{1, 2, 3, 4};
        try {
            buffer.append(tmp, -1, 0);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.append(tmp, 0, -1);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.append(tmp, 0, 8);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.append(tmp, 10, Integer.MAX_VALUE);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.append(tmp, 2, 4);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    @Test
    public void testAppendOneByte() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(4);
        Assert.assertEquals(4, buffer.capacity());

        byte[] tmp = new byte[]{1, 127, -1, -128, 1, -2};
        for (byte b : tmp) {
            buffer.append(b);
        }
        Assert.assertEquals(8, buffer.capacity());
        Assert.assertEquals(6, buffer.length());

        for (int i = 0; i < tmp.length; i++) {
            Assert.assertEquals(tmp[i], buffer.byteAt(i));
        }
    }

    @Test
    public void testSetLength() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(4);
        buffer.setLength(2);
        Assert.assertEquals(2, buffer.length());
    }

    @Test
    public void testSetInvalidLength() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(4);
        try {
            buffer.setLength(-2);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.setLength(200);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    @Test
    public void testRemove() throws Exception {
        ByteArrayBuffer b = new ByteArrayBuffer(16);
        byte tmp[] = "--+++-".getBytes("US-ASCII");
        b.append(tmp, 0, tmp.length);

        b.remove(2, 3);
        Assert.assertEquals(3, b.length());
        Assert.assertEquals("---", new String(b.buffer(), 0, b.length(), "US-ASCII"));
        b.remove(2, 1);
        b.remove(1, 1);
        b.remove(0, 1);
        Assert.assertEquals(0, b.length());

        tmp = "+++---".getBytes("US-ASCII");
        b.append(tmp, 0, tmp.length);

        b.remove(0, 3);
        Assert.assertEquals(3, b.length());
        Assert.assertEquals("---", new String(b.buffer(), 0, b.length(), "US-ASCII"));
        b.remove(0, 3);
        Assert.assertEquals(0, b.length());

        tmp = "---+++".getBytes("US-ASCII");
        b.append(tmp, 0, tmp.length);

        b.remove(3, 3);
        Assert.assertEquals(3, b.length());
        Assert.assertEquals("---", new String(b.buffer(), 0, b.length(), "US-ASCII"));
        b.remove(0, 3);

        Assert.assertEquals(0, b.length());
    }

    @Test
    public void testInvalidRemove() throws Exception {
        ByteArrayBuffer buffer = new ByteArrayBuffer(16);
        buffer.setLength(8);
        try {
            buffer.remove(-1, 0);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.remove(0, -1);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.remove(0, 9);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
        try {
            buffer.remove(10, 2);
            Assert.fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

}
