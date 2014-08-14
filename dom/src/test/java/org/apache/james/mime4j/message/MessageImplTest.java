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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MessageImplTest {
    private Header headerTextPlain = null;
    private Header headerMessageRFC822 = null;
    private Header headerEmpty = null;
    private Header headerMultipartMixed = null;
    private Header headerMultipartDigest = null;

    @Before
    public void setUp() throws Exception {
        headerTextPlain = new HeaderImpl();
        headerMessageRFC822 = new HeaderImpl();
        headerEmpty = new HeaderImpl();
        headerMultipartMixed = new HeaderImpl();
        headerMultipartDigest = new HeaderImpl();

        headerTextPlain.addField(
                DefaultFieldParser.parse("Content-Type: text/plain"));
        headerMessageRFC822.addField(
                DefaultFieldParser.parse("Content-Type: message/RFC822"));
        headerMultipartMixed.addField(
                DefaultFieldParser.parse("Content-Type: multipart/mixed; boundary=foo"));
        headerMultipartDigest.addField(
                DefaultFieldParser.parse("Content-Type: multipart/digest; boundary=foo"));
    }

    @Test
    public void testGetMimeType() {
        MessageImpl parent;
        MessageImpl child;

        parent = new MessageImpl();
        child = new MessageImpl();
        child.setParent(parent);
        parent.setHeader(headerMultipartDigest);
        child.setHeader(headerEmpty);
        Assert.assertEquals("multipart/digest, empty", "message/rfc822",
                child.getMimeType());
        child.setHeader(headerTextPlain);
        Assert.assertEquals("multipart/digest, text/plain", "text/plain",
                child.getMimeType());
        child.setHeader(headerMessageRFC822);
        Assert.assertEquals("multipart/digest, message/rfc822", "message/rfc822",
                child.getMimeType());

        parent = new MessageImpl();
        child = new MessageImpl();
        child.setParent(parent);
        parent.setHeader(headerMultipartMixed);
        child.setHeader(headerEmpty);
        Assert.assertEquals("multipart/mixed, empty", "text/plain",
                child.getMimeType());
        child.setHeader(headerTextPlain);
        Assert.assertEquals("multipart/mixed, text/plain", "text/plain",
                child.getMimeType());
        child.setHeader(headerMessageRFC822);
        Assert.assertEquals("multipart/mixed, message/rfc822", "message/rfc822",
                child.getMimeType());

        child = new MessageImpl();
        child.setHeader(headerEmpty);
        Assert.assertEquals("null, empty", "text/plain", child.getMimeType());
        child.setHeader(headerTextPlain);
        Assert.assertEquals("null, text/plain", "text/plain", child.getMimeType());
        child.setHeader(headerMessageRFC822);
        Assert.assertEquals("null, message/rfc822", "message/rfc822",
                child.getMimeType());
    }

    @Test
    public void testIsMultipart() {
        MessageImpl m = new MessageImpl();

        m.setHeader(headerEmpty);
        Assert.assertTrue("empty", !m.isMultipart());

        m.setHeader(headerTextPlain);
        Assert.assertTrue("text/plain", !m.isMultipart());

        m.setHeader(headerMultipartDigest);
        Assert.assertTrue("multipart/digest", m.isMultipart());

        m.setHeader(headerMultipartMixed);
        Assert.assertTrue("multipart/mixed", m.isMultipart());
    }

    @Test
    public void testWriteTo() throws Exception {
        byte[] inputByte = getRawMessageAsByteArray();

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        DefaultMessageWriter writer = new DefaultMessageWriter();

        Message m = builder.parseMessage(new ByteArrayInputStream(inputByte));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writer.writeMessage(m, out);

        InputStream output = new ByteArrayInputStream(out.toByteArray());

        int b;
        int i = 0;
        while ((b = output.read()) != -1) {
            Assert.assertEquals("same byte", b, inputByte[i]);
            i++;
        }
    }

    @Test
    public void testAddHeaderWriteTo() throws Exception {
        String headerName = "testheader";
        String headerValue = "testvalue";
        String testheader = headerName + ": " + headerValue;

        byte[] inputByte = getRawMessageAsByteArray();

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        DefaultMessageWriter writer = new DefaultMessageWriter();

        Message m = builder.parseMessage(new ByteArrayInputStream(inputByte));
        m.getHeader().addField(DefaultFieldParser.parse(testheader));

        Assert.assertEquals("header added", m.getHeader().getField(headerName)
                .getBody(), headerValue);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeMessage(m, out);
        List<?> lines = IOUtils.readLines((new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out
                        .toByteArray())))));

        Assert.assertTrue("header added", lines.contains(testheader));
    }

    @Test
    public void testMimeVersion() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNotNull(m.getHeader());
        MimeVersionField field = (MimeVersionField) m.getHeader().getField(FieldName.MIME_VERSION);
        Assert.assertNotNull(field);
        Assert.assertEquals(1, field.getMajorVersion());
        Assert.assertEquals(0, field.getMinorVersion());
    }

    @Test
    public void testGetMessageId() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getMessageId());

        String id = "<msg17@localhost>";
        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Message-ID: " + id));
        m.setHeader(header);
        Assert.assertEquals(id, m.getMessageId());
    }

    @Test
    public void testGetSubject() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getSubject());

        String subject = "testing 1 2";
        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Subject: " + subject));
        m.setHeader(header);
        Assert.assertEquals(subject, m.getSubject());

        header.setField(DefaultFieldParser.parse("Subject: =?windows-1252?Q?99_=80?="));
        Assert.assertEquals("99 \u20ac", m.getSubject());
    }

    @Test
    public void testGetDate() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getDate());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Date: Thu, 1 Jan 1970 05:30:00 +0530"));
        m.setHeader(header);

        Assert.assertEquals(new Date(0), m.getDate());
    }

    @Test
    public void testGetSender() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getSender());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Sender: john.doe@example.net"));
        m.setHeader(header);

        Assert.assertEquals("john.doe@example.net", m.getSender().getAddress());
    }

    @Test
    public void testGetFrom() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getFrom());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("From: john.doe@example.net"));
        m.setHeader(header);

        Assert.assertEquals("john.doe@example.net", m.getFrom().get(0).getAddress());
    }

    @Test
    public void testGetTo() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getTo());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("To: john.doe@example.net"));
        m.setHeader(header);

        Assert.assertEquals("john.doe@example.net", ((Mailbox) m.getTo().get(0))
                .getAddress());
    }

    @Test
    public void testGetCc() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getCc());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Cc: john.doe@example.net"));
        m.setHeader(header);

        Assert.assertEquals("john.doe@example.net", ((Mailbox) m.getCc().get(0))
                .getAddress());
    }

    @Test
    public void testGetBcc() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getBcc());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Bcc: john.doe@example.net"));
        m.setHeader(header);

        Assert.assertEquals("john.doe@example.net", ((Mailbox) m.getBcc().get(0))
                .getAddress());
    }

    @Test
    public void testGetReplyTo() throws Exception {
        MessageImpl m = new MessageImpl();
        Assert.assertNull(m.getReplyTo());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Reply-To: john.doe@example.net"));
        m.setHeader(header);

        Assert.assertEquals("john.doe@example.net", ((Mailbox) m.getReplyTo().get(0))
                .getAddress());
    }

    @Test
    public void testDisposeGetsPropagatedToBody() throws Exception {
        DummyBody body1 = new DummyBody();
        BodyPart part1 = new BodyPart();
        part1.setHeader(headerEmpty);
        part1.setBody(body1);

        DummyBody body2 = new DummyBody();
        BodyPart part2 = new BodyPart();
        part2.setHeader(headerEmpty);
        part2.setBody(body2);

        Multipart mp = new MultipartImpl("mixed");
        mp.addBodyPart(part1);
        mp.addBodyPart(part2);

        MessageImpl m = new MessageImpl();
        m.setHeader(headerMultipartMixed);
        m.setBody(mp);

        Assert.assertFalse(body1.disposed);
        Assert.assertFalse(body2.disposed);

        m.dispose();

        Assert.assertTrue(body1.disposed);
        Assert.assertTrue(body2.disposed);
    }

    private byte[] getRawMessageAsByteArray() {
        StringBuilder header = new StringBuilder();
        StringBuilder body = new StringBuilder();
        StringBuilder complete = new StringBuilder();

        header.append("Date: Wed, 21 Feb 2007 11:09:27 +0100\r\n");
        header.append("From: Test <test@test>\r\n");
        header.append("To: Norman Maurer <nm@byteaction.de>\r\n");
        header.append("Subject: Testmail\r\n");
        header
                .append("Content-Type: text/plain; charset=ISO-8859-15; format=flowed\r\n");
        header.append("Content-Transfer-Encoding: 8bit\r\n\r\n");
        body.append("testbody\r\n");
        complete.append(header);
        complete.append(body);

        return complete.toString().getBytes();
    }

    private static final class DummyBody extends SingleBody {

        public boolean disposed = false;

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream("dummy".getBytes("US-ASCII"));
        }

        @Override
        public void dispose() {
            disposed = true;
        }

    }

}
