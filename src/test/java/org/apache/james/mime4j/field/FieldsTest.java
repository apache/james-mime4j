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

package org.apache.james.mime4j.field;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

public class FieldsTest extends TestCase {

    public void testContentTypeString() throws Exception {
        ContentTypeField field = Fields.contentType("multipart/mixed; "
                + "boundary=\"-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-\"");
        assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: multipart/mixed;\r\n "
                + "boundary=\"-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-\"";
        assertEquals(expectedRaw, field.getRaw());
    }

    public void testContentTypeStringParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("boundary",
                "-=Part.0.37877968dd4f6595.11eccf0271c.2dce5678cbc933d5=-");
        ContentTypeField field = Fields.contentType("multipart/mixed",
                parameters);
        assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: multipart/mixed;\r\n "
                + "boundary=\"-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-\"";
        assertEquals(expectedRaw, field.getRaw());
    }

    public void testContentTypeStringParametersWithSpaces() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param", "value with space chars");
        ContentTypeField field = Fields.contentType("multipart/mixed",
                parameters);
        assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: multipart/mixed; "
                + "param=\"value with space chars\"";
        assertEquals(expectedRaw, field.getRaw());
    }

    public void testContentTypeStringNullParameters() throws Exception {
        ContentTypeField field = Fields.contentType("text/plain", null);
        assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: text/plain";
        assertEquals(expectedRaw, field.getRaw());
    }

    public void testInvalidContentType() throws Exception {
        ContentTypeField field = Fields.contentType("multipart/mixed; "
                + "boundary=-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-");
        assertFalse(field.isValidField());

        assertEquals("multipart/mixed", field.getMimeType());
    }

    public void testContentTransferEncoding() throws Exception {
        ContentTransferEncodingField field = Fields
                .contentTransferEncoding("base64");
        assertTrue(field.isValidField());

        assertEquals("Content-Transfer-Encoding: base64", field.getRaw());
    }

    public void testDateString() throws Exception {
        DateTimeField field = Fields.date("Thu, 1 Jan 1970 00:00:00 +0000");
        assertTrue(field.isValidField());

        assertEquals("Date: Thu, 1 Jan 1970 00:00:00 +0000", field.getRaw());
        assertEquals(new Date(0), field.getDate());
    }

    public void testDateStringString() throws Exception {
        DateTimeField field = Fields.date("Resent-Date",
                "Thu, 1 Jan 1970 00:00:00 +0000");
        assertTrue(field.isValidField());

        assertEquals("Resent-Date: Thu, 1 Jan 1970 00:00:00 +0000", field
                .getRaw());
        assertEquals(new Date(0), field.getDate());
    }

    public void testDateStringDateTimeZone() throws Exception {
        DateTimeField field = Fields.date("Date", new Date(0), TimeZone
                .getTimeZone("GMT"));
        assertTrue(field.isValidField());

        assertEquals("Date: Thu, 1 Jan 1970 00:00:00 +0000", field.getRaw());
        assertEquals(new Date(0), field.getDate());

        field = Fields.date("Resent-Date", new Date(0), TimeZone
                .getTimeZone("GMT+1"));
        assertTrue(field.isValidField());

        assertEquals("Resent-Date: Thu, 1 Jan 1970 01:00:00 +0100", field
                .getRaw());
        assertEquals(new Date(0), field.getDate());
    }

    public void testDateDST() throws Exception {
        long millis = 1216221153000l;
        DateTimeField field = Fields.date("Date", new Date(millis), TimeZone
                .getTimeZone("CET"));
        assertTrue(field.isValidField());

        assertEquals("Date: Wed, 16 Jul 2008 17:12:33 +0200", field.getRaw());
        assertEquals(new Date(millis), field.getDate());
    }

    public void testInvalidDate() throws Exception {
        DateTimeField field = Fields.date("Thu, Jan 1 1969 00:00:00 +0000");
        assertFalse(field.isValidField());
    }

    public void testMessageId() throws Exception {
        Field messageId = Fields.messageId("acme.org");

        String raw = messageId.getRaw();
        assertTrue(raw.startsWith("Message-ID: <Mime4j."));
        assertTrue(raw.endsWith("@acme.org>"));
    }

    public void testSubject() throws Exception {
        assertEquals("Subject: ", Fields.subject("").getRaw());
        assertEquals("Subject: test", Fields.subject("test").getRaw());
        assertEquals("Subject: =?ISO-8859-1?Q?Sm=F8rebr=F8d?=", Fields.subject(
                "Sm\370rebr\370d").getRaw());

        String seventyNine = "123456789012345678901234567890123456789012345678901234567890123456789";
        assertEquals("Subject: " + seventyNine, Fields.subject(seventyNine)
                .getRaw());

        String eighty = seventyNine + "0";
        String expected = "Subject: =?US-ASCII?Q?12345678901234567890123456789012345?="
                + "\r\n =?US-ASCII?Q?67890123456789012345678901234567890?=";
        assertEquals(expected, Fields.subject(eighty).getRaw());
    }

}
