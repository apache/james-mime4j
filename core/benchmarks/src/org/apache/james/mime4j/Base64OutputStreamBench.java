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

import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.james.mime4j.codec.Base64OutputStream;

public class Base64OutputStreamBench {

    public static void main(String[] args) throws Exception {
        byte[] data = initData(1024);

        OutputStream nullOut = new NullOutputStream();
        Base64OutputStream base64Out = new Base64OutputStream(nullOut);

        // warmup

        for (int i = 0; i < 2000; i++) {
            base64Out.write(data);
        }
        Thread.sleep(100);

        // test

        long t0 = System.currentTimeMillis();

        final int repetitions = 500000;
        for (int i = 0; i < repetitions; i++) {
            base64Out.write(data);
        }
        base64Out.close();

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

}