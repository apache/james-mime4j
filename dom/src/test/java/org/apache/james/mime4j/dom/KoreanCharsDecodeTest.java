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

package org.apache.james.mime4j.dom;

import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

/**
 * This class has a test that replicates the issue described in MIME4J-327. Namely, when a recipient name contains
 * Korean characters, and the value is _not_ base64-encoded, it is decoded incorrectly.
 */
public class KoreanCharsDecodeTest {
    // This test passes as the value is base64-encoded.
    @Test
    public void testKoreanCharsDecodeBase64Encoded() throws Exception {
        String sb = "From: foo@bar.com\r\n" +
                "To: =?UTF-8?B?7Iuc7ZeY?= <koreantest@example.com>\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "<div>foo bar</div>\r\n";

        Message parsed = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(sb.getBytes()));
        String to = ((Mailbox) parsed.getTo().get(0)).getName();
        assertEquals("시험", to);
    }

    // This test fails as the value is not base64-encoded.
    @Test
    public void testKoreanCharsDecodeNotBase64Encoded() throws Exception {
        String sb = "From: foo@bar.com\r\n" +
                "To: \"시험\" <koreantest@example.com>\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "<div>foo bar</div>\r\n";

        Message parsed = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(sb.getBytes()));
        String to = ((Mailbox) parsed.getTo().get(0)).getName();
        assertEquals("시험", to);
    }
}
