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
package org.apache.james.mime4j.mboxiterator;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteBufferInputStreamTest {
    private InputStream createTestUtf8Stream() {
        return new CharBufferWrapper(CharBuffer.wrap("ABCDE§")).asInputStream(StandardCharsets.UTF_8);
    }

    @Test
    public void testSingleRead() throws IOException {
        InputStream stream = createTestUtf8Stream();
        Assert.assertEquals(0x41, stream.read());
        Assert.assertEquals(0x42, stream.read());
        Assert.assertEquals(0x43, stream.read());
        Assert.assertEquals(0x44, stream.read());
        Assert.assertEquals(0x45, stream.read());
        Assert.assertEquals(0xC2, stream.read());
        Assert.assertEquals(0xA7, stream.read());
        Assert.assertEquals(-1, stream.read());
    }

    @Test
    public void testBulkRead() throws IOException {
        InputStream stream = createTestUtf8Stream();

        {
            byte[] byteArr = new byte[3];
            int bytesRead = stream.read(byteArr);
            Assert.assertEquals(3, bytesRead);
            Assert.assertArrayEquals(new byte[]{ 0x41, 0x42, 0x43 }, byteArr);
        }

        {
            byte[] byteArr = new byte[5];
            Arrays.fill(byteArr, (byte) -1);

            int bytesRead = stream.read(byteArr);
            Assert.assertEquals(4, bytesRead);
            Assert.assertArrayEquals(new byte[]{ 0x44, 0x45, (byte) 0xC2, (byte) 0xA7, (byte) - 1 }, byteArr);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArray() throws IOException {
        createTestUtf8Stream().read(null, 0, 12);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeOffset() throws IOException {
        createTestUtf8Stream().read(new byte[12], -12, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeLength() throws IOException {
        createTestUtf8Stream().read(new byte[12], 0, -12);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testLongLength() throws IOException {
        createTestUtf8Stream().read(new byte[12], 4, 13);
    }
}
