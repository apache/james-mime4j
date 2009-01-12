/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.james.mime4j.util;

import junit.framework.TestCase;

public class MimeUtilTest extends TestCase {

    public void testFold() throws Exception {
        assertEquals("this is\r\n a test", MimeUtil.fold("this is a test", 70));
        assertEquals("this is\r\n a test", MimeUtil.fold("this is a test", 71));
        assertEquals("this\r\n is a test", MimeUtil.fold("this is a test", 72));
        assertEquals("this  \r\n   is a test", MimeUtil.fold(
                "this     is a test", 72));
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

}
