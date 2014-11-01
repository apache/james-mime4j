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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.dom.field.MailboxField;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.field.address.DefaultAddressParser;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;
import org.junit.Assert;
import org.junit.Test;

public class FieldsTest {

    @Test
    public void testContentTypeString() throws Exception {
        ContentTypeField field = Fields.contentType("multipart/mixed; "
                + "boundary=\"-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-\"");
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: multipart/mixed;\r\n "
                + "boundary=\"-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-\"";
        Assert.assertEquals(expectedRaw, decode(field));
    }

    @Test
    public void testContentTypeStringParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("boundary",
                "-=Part.0.37877968dd4f6595.11eccf0271c.2dce5678cbc933d5=-");
        ContentTypeField field = Fields.contentType("multipart/mixed",
                parameters);
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: multipart/mixed;\r\n "
                + "boundary=\"-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-\"";
        Assert.assertEquals(expectedRaw, decode(field));
    }

    @Test
    public void testContentTypeStringParametersWithSpaces() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param", "value with space chars");
        ContentTypeField field = Fields.contentType("multipart/mixed",
                parameters);
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: multipart/mixed; "
                + "param=\"value with space chars\"";
        Assert.assertEquals(expectedRaw, decode(field));
    }

    @Test
    public void testContentTypeStringNullParameters() throws Exception {
        ContentTypeField field = Fields.contentType("text/plain", (Map<String, String>) null);
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Type: text/plain";
        Assert.assertEquals(expectedRaw, decode(field));
    }

    @Test
    public void testInvalidContentType() throws Exception {
        ContentTypeField field = Fields.contentType("multipart/mixed; "
                + "boundary=-=Part.0.37877968dd4f6595.11eccf0271c"
                + ".2dce5678cbc933d5=-");
        Assert.assertFalse(field.isValidField());

        Assert.assertEquals("multipart/mixed", field.getMimeType());
    }

    @Test
    public void testContentTransferEncoding() throws Exception {
        ContentTransferEncodingField field = Fields
                .contentTransferEncoding("base64");
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("Content-Transfer-Encoding: base64",
                decode(field));
    }

    @Test
    public void testContentDispositionString() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("inline; "
                + "filename=\"testing 1 2.dat\"; size=12345; "
                + "creation-date=\"Thu, 1 Jan 1970 00:00:00 +0000\"");
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Disposition: inline; filename="
                + "\"testing 1 2.dat\"; size=12345;\r\n creation-date="
                + "\"Thu, 1 Jan 1970 00:00:00 +0000\"";
        Assert.assertEquals(expectedRaw, decode(field));
    }

    @Test
    public void testContentDispositionStringParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("creation-date", MimeUtil.formatDate(new Date(0),
                TimeZone.getTimeZone("GMT")));
        ContentDispositionField field = Fields.contentDisposition("attachment",
                parameters);
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Disposition: attachment; "
                + "creation-date=\"Thu, 1 Jan 1970 00:00:00\r\n +0000\"";
        Assert.assertEquals(expectedRaw, decode(field));

        Assert.assertEquals(new Date(0), field.getCreationDate());
    }

    @Test
    public void testContentDispositionStringNullParameters() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("inline",
                (Map<String, String>) null);
        Assert.assertTrue(field.isValidField());

        String expectedRaw = "Content-Disposition: inline";
        Assert.assertEquals(expectedRaw, decode(field));
    }

    @Test
    public void testContentDispositionFilename() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("attachment",
                "some file.dat");
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("attachment", field.getDispositionType());
        Assert.assertEquals("some file.dat", field.getFilename());
    }

    @Test
    public void testContentDispositionFilenameSize() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("attachment",
                "some file.dat", 300);
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("attachment", field.getDispositionType());
        Assert.assertEquals("some file.dat", field.getFilename());
        Assert.assertEquals(300, field.getSize());
    }

    @Test
    public void testContentDispositionFilenameSizeDate() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("attachment",
                "some file.dat", 300, new Date(1000), new Date(2000), new Date(
                3000));
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("attachment", field.getDispositionType());
        Assert.assertEquals("some file.dat", field.getFilename());
        Assert.assertEquals(300, field.getSize());
        Assert.assertEquals(new Date(1000), field.getCreationDate());
        Assert.assertEquals(new Date(2000), field.getModificationDate());
        Assert.assertEquals(new Date(3000), field.getReadDate());
    }

    @Test
    public void testInvalidContentDisposition() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("inline; "
                + "filename=some file.dat");
        Assert.assertFalse(field.isValidField());

        Assert.assertEquals("inline", field.getDispositionType());
    }

    @Test
    public void testDateStringDateTimeZone() throws Exception {
        DateTimeField field = Fields.date("Date", new Date(0), TimeZone
                .getTimeZone("GMT"));
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("Date: Thu, 1 Jan 1970 00:00:00 +0000", decode(field
        ));
        Assert.assertEquals(new Date(0), field.getDate());

        field = Fields.date("Resent-Date", new Date(0), TimeZone
                .getTimeZone("GMT+1"));
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("Resent-Date: Thu, 1 Jan 1970 01:00:00 +0100",
                decode(field));
        Assert.assertEquals(new Date(0), field.getDate());
    }

    @Test
    public void testDateDST() throws Exception {
        long millis = 1216221153000l;
        DateTimeField field = Fields.date("Date", new Date(millis), TimeZone
                .getTimeZone("CET"));
        Assert.assertTrue(field.isValidField());

        Assert.assertEquals("Date: Wed, 16 Jul 2008 17:12:33 +0200", decode(field
        ));
        Assert.assertEquals(new Date(millis), field.getDate());
    }

    @Test
    public void testMessageId() throws Exception {
        Field messageId = Fields.generateMessageId("acme.org");

        String raw = decode(messageId);
        Assert.assertTrue(raw.startsWith("Message-ID: <Mime4j."));
        Assert.assertTrue(raw.endsWith("@acme.org>"));
    }

    @Test
    public void testSubject() throws Exception {
        Assert.assertEquals("Subject: ", decode(Fields.subject("")));
        Assert.assertEquals("Subject: test", decode(Fields.subject("test")));
        Assert.assertEquals("Subject: =?ISO-8859-1?Q?Sm=F8rebr=F8d?=", decode(Fields
                .subject("Sm\370rebr\370d")));

        String seventyEight = "12345678901234567890123456789012345678901234567890123456789012345678";
        Assert.assertEquals("Subject:\r\n " + seventyEight, decode(Fields.subject(
                seventyEight)));

        String seventyNine = seventyEight + "9";
        String expected = "Subject: =?US-ASCII?Q?1234567890123456789012345678901234?="
                + "\r\n =?US-ASCII?Q?56789012345678901234567890123456789?=";
        Assert.assertEquals(expected, decode(Fields.subject(seventyNine)));
    }

    @Test
    public void testSender() throws Exception {
        MailboxField field = Fields.sender(DefaultAddressParser.DEFAULT
                .parseMailbox("JD <john.doe@acme.org>"));
        Assert.assertEquals("Sender: JD <john.doe@acme.org>", decode(field));
    }

    @Test
    public void testFrom() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        MailboxListField field = Fields.from(mailbox1);
        Assert.assertEquals("From: JD <john.doe@acme.org>", decode(field));

        field = Fields.from(mailbox1, mailbox2);
        Assert.assertEquals("From: JD <john.doe@acme.org>, "
                + "Mary Smith <mary@example.net>", decode(field));

        field = Fields.from(Arrays.asList(mailbox1, mailbox2));
        Assert.assertEquals("From: JD <john.doe@acme.org>, "
                + "Mary Smith <mary@example.net>", decode(field));
    }

    @Test
    public void testTo() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.org");
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");
        Group group = new Group("The Does", mailbox1, mailbox2);

        AddressListField field = Fields.to(group);
        Assert.assertEquals("To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;", decode(field));

        field = Fields.to(group, mailbox3);
        Assert.assertEquals("To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary Smith\r\n <mary@example.net>",
                decode(field));

        field = Fields.to(Arrays.asList(group, mailbox3));
        Assert.assertEquals("To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary Smith\r\n <mary@example.net>",
                decode(field));
    }

    @Test
    public void testCc() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.org");
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");
        Group group = new Group("The Does", mailbox1, mailbox2);

        AddressListField field = Fields.cc(group);
        Assert.assertEquals("Cc: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;", decode(field));

        field = Fields.cc(group, mailbox3);
        Assert.assertEquals("Cc: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary Smith\r\n <mary@example.net>",
                decode(field));

        field = Fields.cc(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Cc: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary Smith\r\n <mary@example.net>",
                decode(field));
    }

    @Test
    public void testBcc() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.org");
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");
        Group group = new Group("The Does", mailbox1, mailbox2);

        AddressListField field = Fields.bcc(group);
        Assert.assertEquals("Bcc: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;", decode(field));

        field = Fields.bcc(group, mailbox3);
        Assert.assertEquals("Bcc: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary Smith\r\n <mary@example.net>",
                decode(field));

        field = Fields.bcc(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Bcc: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary Smith\r\n <mary@example.net>",
                decode(field));
    }

    @Test
    public void testReplyTo() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.org");
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");
        Group group = new Group("The Does", mailbox1, mailbox2);

        AddressListField field = Fields.replyTo(group);
        Assert.assertEquals("Reply-To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;", decode(field));

        field = Fields.replyTo(group, mailbox3);
        Assert.assertEquals("Reply-To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary\r\n Smith <mary@example.net>",
                decode(field));

        field = Fields.replyTo(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Reply-To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary\r\n Smith <mary@example.net>",
                decode(field));
    }

    @Test
    public void testMailbox() throws Exception {
        MailboxField field = Fields.mailbox("Resent-Sender", DefaultAddressParser.DEFAULT
                .parseMailbox("JD <john.doe@acme.org>"));
        Assert.assertEquals("Resent-Sender: JD <john.doe@acme.org>", decode(field));
    }

    @Test
    public void testMailboxList() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        MailboxListField field = Fields.mailboxList("Resent-From", Arrays
                .asList(mailbox1, mailbox2));
        Assert.assertEquals("Resent-From: JD <john.doe@acme.org>, "
                + "Mary Smith <mary@example.net>", decode(field));
    }

    @Test
    public void testAddressList() throws Exception {
        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("JD <john.doe@acme.org>");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.org");
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");
        Group group = new Group("The Does", mailbox1, mailbox2);

        AddressListField field = Fields.addressList("Resent-To", Arrays.asList(
                group, mailbox3));
        Assert.assertEquals("Resent-To: The Does: JD <john.doe@acme.org>, "
                + "jane.doe@example.org;, Mary\r\n Smith <mary@example.net>",
                decode(field));
    }

    @Test
    public void testInvalidFieldName() throws Exception {
        try {
            Fields.date("invalid field name", new Date());
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public static String decode(Field f) throws IOException {
        String s = null;
        ByteSequence raw = f.getRaw();
        if (raw != null) {
            s = ContentUtil.decode(raw);
        }
        if (s == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(f.getName());
            buf.append(": ");
            String body = f.getBody();
            if (body != null) {
                buf.append(body);
            }
            s = MimeUtil.fold(buf.toString(), 0);
        }
        return s;
    }

}
