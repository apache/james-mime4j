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
import java.io.IOException;

public class LineNumberInputStreamTest {
    /**
     * Tests that reading single bytes updates the line number appropriately.
     */
    @Test
    public void testReadSingleByte() throws IOException {
        String s = "Yada\r\nyada\r\nyada\r\n";
        LineNumberInputStream is = new LineNumberInputStream(
                new ByteArrayInputStream(s.getBytes()));

        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(1, is.getLineNumber());
            is.read();
        }

        for (int i = 6; i < 12; i++) {
            Assert.assertEquals(2, is.getLineNumber());
            is.read();
        }

        for (int i = 12; i < 18; i++) {
            Assert.assertEquals(3, is.getLineNumber());
            is.read();
        }

        Assert.assertEquals(4, is.getLineNumber());
        Assert.assertEquals(-1, is.read());

        is.close();
    }

    /**
     * Tests that reading multiple bytes at once updates the line number
     * appropriately.
     */
    @Test
    public void testReadManyBytes() throws IOException {
        String s = "Yada\r\nyada\r\nyada\r\n";
        LineNumberInputStream is = new LineNumberInputStream(
                new ByteArrayInputStream(s.getBytes()));

        byte[] buf = new byte[4];
        Assert.assertEquals(1, is.getLineNumber());
        is.read(buf);
        Assert.assertEquals(1, is.getLineNumber());
        is.read(buf);
        Assert.assertEquals(2, is.getLineNumber());
        is.read(buf);
        Assert.assertEquals(3, is.getLineNumber());
        is.read(buf);
        Assert.assertEquals(3, is.getLineNumber());
        is.read(buf);
        Assert.assertEquals(4, is.getLineNumber());

        Assert.assertEquals(-1, is.read());

        is.close();
    }
}
