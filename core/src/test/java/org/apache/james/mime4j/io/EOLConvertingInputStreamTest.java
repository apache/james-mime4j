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

import java.io.IOException;

import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class EOLConvertingInputStreamTest {

    @Test
    public void testRead() throws IOException {
        testConvertBoth("Line 1\r\nLine 2\r\n", "Line 1\r\nLine 2\r\n");
        testConvertCR("Line 1\r\nLine 2\r\n", "Line 1\r\nLine 2\r\n");
        testConvertLF("Line 1\r\nLine 2\r\n", "Line 1\r\nLine 2\r\n");

        testConvertBoth("Line 1\n\rLine 2\n\r", "Line 1\r\n\r\nLine 2\r\n\r\n");
        testConvertCR("Line 1\n\rLine 2\n\r", "Line 1\n\r\nLine 2\n\r\n");
        testConvertLF("Line 1\n\rLine 2\n\r", "Line 1\r\n\rLine 2\r\n\r");

        testConvertBoth("Line 1\nLine 2\n", "Line 1\r\nLine 2\r\n");
        testConvertCR("Line 1\nLine 2\n", "Line 1\nLine 2\n");
        testConvertLF("Line 1\nLine 2\n", "Line 1\r\nLine 2\r\n");

        testConvertBoth("Line 1\rLine 2\r", "Line 1\r\nLine 2\r\n");
        testConvertCR("Line 1\rLine 2\r", "Line 1\r\nLine 2\r\n");
        testConvertLF("Line 1\rLine 2\r", "Line 1\rLine 2\r");

        testConvertBoth("\r\n", "\r\n");
        testConvertCR("\r\n", "\r\n");
        testConvertLF("\r\n", "\r\n");

        testConvertBoth("\n", "\r\n");
        testConvertCR("\n", "\n");
        testConvertLF("\n", "\r\n");

        testConvertBoth("\r", "\r\n");
        testConvertCR("\r", "\r\n");
        testConvertLF("\r", "\r");

        testConvertBoth("", "");
        testConvertCR("", "");
        testConvertLF("", "");
    }

    private void testConvertBoth(String s1, String s2) throws IOException {
        byte[] bytes = new byte[1024];

        EOLConvertingInputStream in =
                new EOLConvertingInputStream(InputStreams.createAscii(s1),
                        EOLConvertingInputStream.CONVERT_BOTH);
        int n = in.read(bytes);
        Assert.assertEquals(s2, ContentUtil.toAsciiString(bytes, 0, n > 0 ? n : 0));

        in.close();
    }

    private void testConvertCR(String s1, String s2) throws IOException {
        byte[] bytes = new byte[1024];

        EOLConvertingInputStream in =
                new EOLConvertingInputStream(InputStreams.createAscii(s1),
                        EOLConvertingInputStream.CONVERT_CR);
        int n = in.read(bytes);
        Assert.assertEquals(s2, ContentUtil.toAsciiString(bytes, 0, n > 0 ? n : 0));

        in.close();
    }

    private void testConvertLF(String s1, String s2) throws IOException {
        byte[] bytes = new byte[1024];

        EOLConvertingInputStream in =
                new EOLConvertingInputStream(InputStreams.createAscii(s1),
                        EOLConvertingInputStream.CONVERT_LF);
        int n = in.read(bytes);
        Assert.assertEquals(s2, ContentUtil.toAsciiString(bytes, 0, n > 0 ? n : 0));

        in.close();
    }

}
