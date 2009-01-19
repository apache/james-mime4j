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

package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.CodecUtil;

public class Base64InputStreamBench {

    public static void main(String[] args) throws Exception {
        byte[] data = initData(2 * 1024 * 1024);
        byte[] encoded = encode(data);

        // decoder test to make sure everything is okay

        testDecode(data, encoded);

        // warmup

        OutputStream nullOut = new NullOutputStream();

        for (int i = 0; i < 5; i++) {
            ByteArrayInputStream ed = new ByteArrayInputStream(encoded);
            InputStream in = new Base64InputStream(ed);
            CodecUtil.copy(in, nullOut);
        }
        Thread.sleep(100);

        // test

        long t0 = System.currentTimeMillis();

        final int repetitions = 50;
        for (int i = 0; i < repetitions; i++) {
            ByteArrayInputStream ed = new ByteArrayInputStream(encoded);
            InputStream in = new Base64InputStream(ed);
            CodecUtil.copy(in, nullOut);
        }

        long dt = System.currentTimeMillis() - t0;
        long totalBytes = data.length * (long) repetitions;

        double mbPerSec = (totalBytes / 1024.0 / 1024) / (dt / 1000.0);

        System.out.println(dt + " ms");
        System.out.println(totalBytes + " bytes");
        System.out.println(mbPerSec + " mb/sec");
    }

    private static byte[] initData(int size) {
        Random random = new Random(size);
        byte[] data = new byte[size];
        random.nextBytes(data);
        return data;
    }

    private static byte[] encode(byte[] data) throws IOException {
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.encodeBase64(in, out);
        return out.toByteArray();
    }

    private static void testDecode(byte[] data, final byte[] encoded)
            throws IOException {
        ByteArrayInputStream ed = new ByteArrayInputStream(encoded);
        InputStream in = new Base64InputStream(ed);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodecUtil.copy(in, out);

        compare(data, out.toByteArray());
    }

    private static void compare(byte[] expected, byte[] actual) {
        if (expected.length != actual.length)
            throw new AssertionError("length: " + expected.length + ", "
                    + actual.length);

        for (int i = 0; i < expected.length; i++)
            if (expected[i] != actual[i])
                throw new AssertionError("value @ " + i);
    }

}