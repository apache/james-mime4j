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

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apache.james.mime4j.util.CharsetUtil;

import junit.framework.TestCase;

public class StringInputStreamTest extends TestCase {

    private static final String SWISS_GERMAN_HELLO = "Gr\374ezi_z\344m\344";
    private static final String RUSSIAN_HELLO = "\u0412\u0441\u0435\u043C_\u043F\u0440\u0438\u0432\u0435\u0442";
    private static final String TEST_STRING = "Hello and stuff " + SWISS_GERMAN_HELLO + " " +  RUSSIAN_HELLO;
    private static final String LARGE_TEST_STRING;

    static {
        StringBuilder buffer = new StringBuilder();
        for (int i=0; i<100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private static void singleByteReadTest(final String testString) throws IOException {
        byte[] bytes = testString.getBytes(CharsetUtil.UTF_8.name());
        InputStream in = new StringInputStream(testString, CharsetUtil.UTF_8);
        for (byte b : bytes) {
            int read = in.read();
            assertTrue(read >= 0);
            assertTrue(read <= 255);
            assertEquals(b, (byte)read);
        }
        assertEquals(-1, in.read());
    }

    private static void bufferedReadTest(final String testString) throws IOException {
        SecureRandom rnd = new SecureRandom();
        byte[] expected = testString.getBytes(CharsetUtil.UTF_8.name());
        InputStream in = new StringInputStream(testString, CharsetUtil.UTF_8);
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
    }

    public void testSingleByteRead() throws IOException {
        singleByteReadTest(TEST_STRING);
    }

    public void testLargeSingleByteRead() throws IOException {
        singleByteReadTest(LARGE_TEST_STRING);
    }

    public void testBufferedRead() throws IOException {
        bufferedReadTest(TEST_STRING);
    }

    public void testLargeBufferedRead() throws IOException {
        bufferedReadTest(LARGE_TEST_STRING);
    }

    public void testReadZero() throws Exception {
        InputStream r = new StringInputStream("test", CharsetUtil.UTF_8);
        byte[] bytes = new byte[30];
        assertEquals(0, r.read(bytes, 0, 0));
    }

    public void testReadZero2() throws Exception {
        InputStream r = new StringInputStream("test", CharsetUtil.UTF_8);
        byte[] bytes = new byte[30];
        assertEquals(2, r.read(bytes, 0, 2));
        assertEquals(0, r.read(bytes, 2, 0));
        assertEquals(0, r.read(bytes, 2, 0));
        assertEquals(2, r.read(bytes, 2, 2));
        assertEquals(-1, r.read(bytes, 4, 0));
        assertEquals(-1, r.read(bytes, 4, 2));
    }

    public void testSkip() throws Exception {
        InputStream r = new StringInputStream("test", CharsetUtil.UTF_8);
        r.skip(1);
        r.skip(2);
        assertEquals('t', r.read());
        r.skip(100);
        assertEquals(-1, r.read());
    }

    public void testMarkReset() throws Exception {
        InputStream r = new StringInputStream("test", CharsetUtil.UTF_8);
        r.skip(2);
        r.mark(0);
        assertEquals('s', r.read());
        assertEquals('t', r.read());
        assertEquals(-1, r.read());
        r.reset();
        assertEquals('s', r.read());
        assertEquals('t', r.read());
        assertEquals(-1, r.read());
        r.reset();
        r.reset();
    }

}
