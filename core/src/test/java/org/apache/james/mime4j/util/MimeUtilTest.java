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

import junit.framework.TestCase;

public class MimeUtilTest extends TestCase {

    public void testFold() throws Exception {
        assertEquals("this is\r\n a test", MimeUtil.fold("this is a test", 68));
        assertEquals("this is\r\n a test", MimeUtil.fold("this is a test", 69));
        assertEquals("this\r\n is a test", MimeUtil.fold("this is a test", 70));
        assertEquals("this  \r\n   is a test", MimeUtil.fold(
                "this     is a test", 70));
    }

    public void testFoldOverlyLongNonWhitespace() throws Exception {
        String ninety = "1234567890123456789012345678901234567890"
                + "12345678901234567890123456789012345678901234567890";
        String input = String.format("testing 1 2 %s testing %s", ninety,
                ninety);

        String expected = String.format(
                "testing 1 2\r\n %s\r\n testing\r\n %s", ninety, ninety);

        assertEquals(expected, MimeUtil.fold(input, 0));
    }

    public void testUnfold() throws Exception {
        assertEquals("", MimeUtil.unfold(""));
        assertEquals("x", MimeUtil.unfold("x"));
        assertEquals(" x ", MimeUtil.unfold(" x "));

        assertEquals("", MimeUtil.unfold("\r"));
        assertEquals("", MimeUtil.unfold("\n"));
        assertEquals("", MimeUtil.unfold("\r\n"));

        assertEquals(" ", MimeUtil.unfold(" \n"));
        assertEquals(" ", MimeUtil.unfold("\n "));
        assertEquals(" ", MimeUtil.unfold(" \r\n"));
        assertEquals(" ", MimeUtil.unfold("\r\n "));

        assertEquals("this is a test", MimeUtil.unfold("this is\r\n a test"));
        assertEquals("this is a test", MimeUtil.unfold("this is\r\n a test"));
        assertEquals("this is a test", MimeUtil.unfold("this\r\n is a test"));
        assertEquals("this     is a test", MimeUtil
                .unfold("this  \r\n   is a test"));

        assertEquals("this is a test", MimeUtil
                .unfold("this\r\n is\r\n a\r\n test"));
    }

}
