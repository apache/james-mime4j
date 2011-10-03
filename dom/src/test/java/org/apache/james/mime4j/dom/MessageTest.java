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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MultipartImpl;

public class MessageTest extends TestCase {
    private Header headerTextPlain = null;
    private Header headerMessageRFC822 = null;
    private Header headerEmpty = null;
    private Header headerMultipartMixed = null;
    private Header headerMultipartDigest = null;

    @Override
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

    public void testGetMimeType() {
        MessageImpl parent = null;
        MessageImpl child = null;

        parent = new MessageImpl();
        child = new MessageImpl();
        child.setParent(parent);
        parent.setHeader(headerMultipartDigest);
        child.setHeader(headerEmpty);
        assertEquals("multipart/digest, empty", "message/rfc822",
                child.getMimeType());
        child.setHeader(headerTextPlain);
        assertEquals("multipart/digest, text/plain", "text/plain",
                child.getMimeType());
        child.setHeader(headerMessageRFC822);
        assertEquals("multipart/digest, message/rfc822", "message/rfc822",
                child.getMimeType());

        parent = new MessageImpl();
        child = new MessageImpl();
        child.setParent(parent);
        parent.setHeader(headerMultipartMixed);
        child.setHeader(headerEmpty);
        assertEquals("multipart/mixed, empty", "text/plain",
                child.getMimeType());
        child.setHeader(headerTextPlain);
        assertEquals("multipart/mixed, text/plain", "text/plain",
                child.getMimeType());
        child.setHeader(headerMessageRFC822);
        assertEquals("multipart/mixed, message/rfc822", "message/rfc822",
                child.getMimeType());

        child = new MessageImpl();
        child.setHeader(headerEmpty);
        assertEquals("null, empty", "text/plain", child.getMimeType());
        child.setHeader(headerTextPlain);
        assertEquals("null, text/plain", "text/plain", child.getMimeType());
        child.setHeader(headerMessageRFC822);
        assertEquals("null, message/rfc822", "message/rfc822",
                child.getMimeType());
    }

    public void testIsMultipart() {
        MessageImpl m = new MessageImpl();

        m.setHeader(headerEmpty);
        assertTrue("empty", !m.isMultipart());

        m.setHeader(headerTextPlain);
        assertTrue("text/plain", !m.isMultipart());

        m.setHeader(headerMultipartDigest);
        assertTrue("multipart/digest", m.isMultipart());

        m.setHeader(headerMultipartMixed);
        assertTrue("multipart/mixed", m.isMultipart());
    }

    public void testWriteTo() throws Exception {
        byte[] inputByte = getRawMessageAsByteArray();

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        DefaultMessageWriter writer = new DefaultMessageWriter();

        Message m = builder.parseMessage(new ByteArrayInputStream(inputByte));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writer.writeMessage(m, out);

        InputStream output = new ByteArrayInputStream(out.toByteArray());

        int b = -1;
        int i = 0;
        while ((b = output.read()) != -1) {
            assertEquals("same byte", b, inputByte[i]);
            i++;
        }
    }

    public void testAddHeaderWriteTo() throws Exception {
        String headerName = "testheader";
        String headerValue = "testvalue";
        String testheader = headerName + ": " + headerValue;

        byte[] inputByte = getRawMessageAsByteArray();

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        DefaultMessageWriter writer = new DefaultMessageWriter();

        Message m = builder.parseMessage(new ByteArrayInputStream(inputByte));
        m.getHeader().addField(DefaultFieldParser.parse(testheader));

        assertEquals("header added", m.getHeader().getField(headerName)
                .getBody(), headerValue);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeMessage(m, out);
        List<?> lines = IOUtils.readLines((new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out
                        .toByteArray())))));

        assertTrue("header added", lines.contains(testheader));
    }

    public void testMimeVersion() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNotNull(m.getHeader());
        MimeVersionField field = (MimeVersionField) m.getHeader().getField(FieldName.MIME_VERSION);
        assertNotNull(field);
        assertEquals(1, field.getMajorVersion());
        assertEquals(0, field.getMinorVersion());
    }

    public void testGetMessageId() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getMessageId());

        String id = "<msg17@localhost>";
        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Message-ID: " + id));
        m.setHeader(header);
        assertEquals(id, m.getMessageId());
    }

    public void testCreateMessageId() throws Exception {
        MessageImpl m = new MessageImpl();
        m.createMessageId("hostname");

        String id = m.getMessageId();
        assertNotNull(id);
        assertTrue(id.startsWith("<Mime4j."));
        assertTrue(id.endsWith("@hostname>"));
    }

    public void testGetSubject() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getSubject());

        String subject = "testing 1 2";
        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Subject: " + subject));
        m.setHeader(header);
        assertEquals(subject, m.getSubject());

        header.setField(DefaultFieldParser.parse("Subject: =?windows-1252?Q?99_=80?="));
        assertEquals("99 \u20ac", m.getSubject());
    }

    public void testSetSubject() throws Exception {
        MessageImpl m = new MessageImpl();

        m.setSubject("Semmelbr\366sel");
        assertEquals("Semmelbr\366sel", m.getSubject());
        assertEquals("=?ISO-8859-1?Q?Semmelbr=F6sel?=", m.getHeader().getField(
                "Subject").getBody());

        m.setSubject(null);
        assertNull(m.getHeader().getField("Subject"));
    }

    public void testGetDate() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getDate());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Date: Thu, 1 Jan 1970 05:30:00 +0530"));
        m.setHeader(header);

        assertEquals(new Date(0), m.getDate());
    }

    public void testSetDate() throws Exception {
        MessageImpl m = new MessageImpl();

        m.setDate(new Date(86400000), TimeZone.getTimeZone("GMT"));
        assertEquals(new Date(86400000), m.getDate());
        assertEquals("Fri, 2 Jan 1970 00:00:00 +0000", m.getHeader().getField(
                "Date").getBody());

        m.setDate(null);
        assertNull(m.getHeader().getField("Date"));
    }

    public void testGetSender() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getSender());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Sender: john.doe@example.net"));
        m.setHeader(header);

        assertEquals("john.doe@example.net", m.getSender().getAddress());
    }

    public void testSetSender() throws Exception {
        MessageImpl m = new MessageImpl();

        m.setSender(AddressBuilder.DEFAULT.parseMailbox("john.doe@example.net"));
        assertEquals("john.doe@example.net", m.getHeader().getField("Sender")
                .getBody());

        m.setSender(null);
        assertNull(m.getHeader().getField("Sender"));
    }

    public void testGetFrom() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getFrom());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("From: john.doe@example.net"));
        m.setHeader(header);

        assertEquals("john.doe@example.net", m.getFrom().get(0).getAddress());
    }

    public void testSetFrom() throws Exception {
        MessageImpl m = new MessageImpl();

        Mailbox mailbox1 = AddressBuilder.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = AddressBuilder.DEFAULT.parseMailbox("jane.doe@example.net");

        m.setFrom(mailbox1);
        assertEquals("john.doe@example.net", m.getHeader().getField("From")
                .getBody());

        m.setFrom(mailbox1, mailbox2);
        assertEquals("john.doe@example.net, jane.doe@example.net", m
                .getHeader().getField("From").getBody());

        m.setFrom(Arrays.asList(mailbox1, mailbox2));
        assertEquals("john.doe@example.net, jane.doe@example.net", m
                .getHeader().getField("From").getBody());

        m.setFrom((Mailbox) null);
        assertNull(m.getHeader().getField("From"));
    }

    public void testGetTo() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getTo());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("To: john.doe@example.net"));
        m.setHeader(header);

        assertEquals("john.doe@example.net", ((Mailbox) m.getTo().get(0))
                .getAddress());
    }

    public void testSetTo() throws Exception {
        MessageImpl m = new MessageImpl();

        Mailbox mailbox1 = AddressBuilder.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = AddressBuilder.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = AddressBuilder.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        m.setTo(group);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;", m
                .getHeader().getField("To").getBody());

        m.setTo(group, mailbox3);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader().getField("To")
                .getBody());

        m.setTo(Arrays.asList(group, mailbox3));
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader().getField("To")
                .getBody());

        m.setTo((Mailbox) null);
        assertNull(m.getHeader().getField("To"));
    }

    public void testGetCc() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getCc());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Cc: john.doe@example.net"));
        m.setHeader(header);

        assertEquals("john.doe@example.net", ((Mailbox) m.getCc().get(0))
                .getAddress());
    }

    public void testSetCc() throws Exception {
        MessageImpl m = new MessageImpl();

        Mailbox mailbox1 = AddressBuilder.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = AddressBuilder.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = AddressBuilder.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        m.setCc(group);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;", m
                .getHeader().getField("Cc").getBody());

        m.setCc(group, mailbox3);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader().getField("Cc")
                .getBody());

        m.setCc(Arrays.asList(group, mailbox3));
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader().getField("Cc")
                .getBody());

        m.setCc((Mailbox) null);
        assertNull(m.getHeader().getField("Cc"));
    }

    public void testGetBcc() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getBcc());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Bcc: john.doe@example.net"));
        m.setHeader(header);

        assertEquals("john.doe@example.net", ((Mailbox) m.getBcc().get(0))
                .getAddress());
    }

    public void testSetBcc() throws Exception {
        MessageImpl m = new MessageImpl();

        Mailbox mailbox1 = AddressBuilder.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = AddressBuilder.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = AddressBuilder.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        m.setBcc(group);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;", m
                .getHeader().getField("Bcc").getBody());

        m.setBcc(group, mailbox3);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader()
                .getField("Bcc").getBody());

        m.setBcc(Arrays.asList(group, mailbox3));
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader()
                .getField("Bcc").getBody());

        m.setBcc((Mailbox) null);
        assertNull(m.getHeader().getField("Bcc"));
    }

    public void testGetReplyTo() throws Exception {
        MessageImpl m = new MessageImpl();
        assertNull(m.getReplyTo());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Reply-To: john.doe@example.net"));
        m.setHeader(header);

        assertEquals("john.doe@example.net", ((Mailbox) m.getReplyTo().get(0))
                .getAddress());
    }

    public void testSetReplyTo() throws Exception {
        MessageImpl m = new MessageImpl();

        Mailbox mailbox1 = AddressBuilder.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = AddressBuilder.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = AddressBuilder.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        m.setReplyTo(group);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;", m
                .getHeader().getField("Reply-To").getBody());

        m.setReplyTo(group, mailbox3);
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader().getField(
                "Reply-To").getBody());

        m.setReplyTo(Arrays.asList(group, mailbox3));
        assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", m.getHeader().getField(
                "Reply-To").getBody());

        m.setReplyTo((Mailbox) null);
        assertNull(m.getHeader().getField("Reply-To"));
    }

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

        assertFalse(body1.disposed);
        assertFalse(body2.disposed);

        m.dispose();

        assertTrue(body1.disposed);
        assertTrue(body2.disposed);
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
