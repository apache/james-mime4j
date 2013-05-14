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

package org.apache.james.mime4j.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class StringInputStreamTest {

    private static final String SWISS_GERMAN_HELLO = "Gr\374ezi_z\344m\344";
    private static final String RUSSIAN_HELLO = "\u0412\u0441\u0435\u043C_\u043F\u0440\u0438\u0432\u0435\u0442";
    private static final String TEST_STRING = "Hello and stuff " + SWISS_GERMAN_HELLO + " " + RUSSIAN_HELLO;
    private static final String LARGE_TEST_STRING;

    static {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private static void singleByteReadTest(final String testString) throws IOException {
        byte[] bytes = ContentUtil.toByteArray(testString, Charsets.UTF_8);
        InputStream in = new StringInputStream(testString, Charsets.UTF_8);
        for (byte b : bytes) {
            int read = in.read();
            assertTrue(read >= 0);
            assertTrue(read <= 255);
            assertEquals(b, (byte) read);
        }
        in.close();
        assertEquals(-1, in.read());
    }

    private static void bufferedReadTest(final String testString) throws IOException {
        SecureRandom rnd = new SecureRandom();
        byte[] expected = ContentUtil.toByteArray(testString, Charsets.UTF_8);
        InputStream in = new StringInputStream(testString, Charsets.UTF_8);
        byte[] buffer = new byte[128];
        int offset = 0;
        while (true) {
            int bufferOffset = rnd.nextInt(64);
            int bufferLength = rnd.nextInt(64);
            int read = in.read(buffer, bufferOffset, bufferLength);
            if (read == -1) {
                assertEquals(offset, expected.length);
                break;
            } else {
                assertTrue(read <= bufferLength);
                while (read > 0) {
                    assertTrue(offset < expected.length);
                    assertEquals(expected[offset], buffer[bufferOffset]);
                    offset++;
                    bufferOffset++;
                    read--;
                }
            }
        }
        in.close();
    }

    @Test
    public void testSingleByteRead() throws IOException {
        singleByteReadTest(TEST_STRING);
    }

    @Test
    public void testLargeSingleByteRead() throws IOException {
        singleByteReadTest(LARGE_TEST_STRING);
    }

    @Test
    public void testBufferedRead() throws IOException {
        bufferedReadTest(TEST_STRING);
    }

    @Test
    public void testLargeBufferedRead() throws IOException {
        bufferedReadTest(LARGE_TEST_STRING);
    }

    @Test
    public void testReadZero() throws Exception {
        InputStream r = new StringInputStream("test", Charsets.UTF_8);
        byte[] bytes = new byte[30];
        Assert.assertEquals(0, r.read(bytes, 0, 0));
        r.close();
    }

    @Test
    public void testSkip() throws Exception {
        InputStream r = new StringInputStream("test", Charsets.UTF_8);
        r.skip(1);
        r.skip(2);
        Assert.assertEquals('t', r.read());
        r.skip(100);
        Assert.assertEquals(-1, r.read());
        r.close();
    }

    @Test
    public void testMarkReset() throws Exception {
        InputStream r = new StringInputStream("test", Charsets.UTF_8);
        r.skip(2);
        r.mark(0);
        Assert.assertEquals('s', r.read());
        Assert.assertEquals('t', r.read());
        Assert.assertEquals(-1, r.read());
        r.reset();
        Assert.assertEquals('s', r.read());
        Assert.assertEquals('t', r.read());
        Assert.assertEquals(-1, r.read());
        r.reset();
        r.reset();
        r.close();
    }

}
