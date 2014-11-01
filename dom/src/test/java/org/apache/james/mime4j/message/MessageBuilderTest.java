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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.address.DefaultAddressParser;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MessageBuilderTest {

    @Test
    public void testSetCustomBody() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        BodyFactory bodyFactory = Mockito.spy(new BasicBodyFactory());
        builder.use(bodyFactory);

        builder.setBody("stuff", "stuff", Charsets.UTF_8);

        Assert.assertTrue(builder.getBody() instanceof TextBody);
        ContentTypeField ct1 = builder.getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/stuff", ct1.getMimeType());
        Assert.assertEquals("UTF-8", ct1.getCharset());

        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(Mockito.<InputStream>any(), Mockito.eq("UTF-8"));

        builder.setBody("stuff", "other-stuff", Charsets.US_ASCII);

        Assert.assertTrue(builder.getBody() instanceof TextBody);
        ContentTypeField ct2 = builder.getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/other-stuff", ct2.getMimeType());
        Assert.assertEquals("US-ASCII", ct2.getCharset());

        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(Mockito.<InputStream>any(), Mockito.eq("US-ASCII"));

        builder.setBody("stuff", null);

        Assert.assertTrue(builder.getBody() instanceof TextBody);
        ContentTypeField ct3 = builder.getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/plain", ct3.getMimeType());
        Assert.assertEquals(null, ct3.getCharset());

        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(Mockito.<InputStream>any(), Mockito.isNull(String.class));

        builder.setBody(new byte[] {1,2,3}, null);

        Assert.assertTrue(builder.getBody() instanceof BinaryBody);
        ContentTypeField ct4 = builder.getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("application/octet-stream", ct4.getMimeType());
        Assert.assertEquals(null, ct4.getCharset());

        Mockito.verify(bodyFactory, Mockito.times(1)).binaryBody(Mockito.<InputStream>any());
    }

    @Test
    public void testSetMessageId() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        String id = "<msg17@localhost>";
        builder.setMessageId(id);
        Assert.assertEquals(id, builder.getMessageId());
    }

    @Test
    public void testCreateMessageId() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        builder.generateMessageId("hostname");

        String id = builder.getMessageId();
        Assert.assertNotNull(id);
        Assert.assertTrue(id.startsWith("<Mime4j."));
        Assert.assertTrue(id.endsWith("@hostname>"));
    }

    @Test
    public void testGetSubject() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getSubject());

        String subject = "testing 1 2";
        builder.setSubject(subject);
        Assert.assertEquals(subject, builder.getSubject());

        builder.setField(DefaultFieldParser.parse("Subject: =?windows-1252?Q?99_=80?="));
        Assert.assertEquals("99 \u20ac", builder.getSubject());
    }

    @Test
    public void testSetSubject() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        builder.setSubject("Semmelbr\366sel");
        Assert.assertEquals("Semmelbr\366sel", builder.getSubject());
        Assert.assertEquals("=?ISO-8859-1?Q?Semmelbr=F6sel?=", builder.getField(
                "Subject").getBody());

        builder.setSubject(null);
        Assert.assertNull(builder.getField("Subject"));
    }

    @Test
    public void testGetDate() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getDate());

        builder.setField(DefaultFieldParser.parse("Date: Thu, 1 Jan 1970 05:30:00 +0530"));

        Assert.assertEquals(new Date(0), builder.getDate());
    }

    @Test
    public void testSetDate() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        builder.setDate(new Date(86400000), TimeZone.getTimeZone("GMT"));
        Assert.assertEquals(new Date(86400000), builder.getDate());
        Assert.assertEquals("Fri, 2 Jan 1970 00:00:00 +0000", builder.getField(
                "Date").getBody());

        builder.setDate(null);
        Assert.assertNull(builder.getField("Date"));
    }

    @Test
    public void testGetSender() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getSender());

        builder.setField(DefaultFieldParser.parse("Sender: john.doe@example.net"));

        Assert.assertEquals("john.doe@example.net", builder.getSender().getAddress());
    }

    @Test
    public void testSetSender() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        builder.setSender("john.doe@example.net");
        Assert.assertEquals("john.doe@example.net", builder.getField("Sender").getBody());

        builder.setSender((String) null);
        Assert.assertNull(builder.getField("Sender"));
    }

    @Test
    public void testGetFrom() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getFrom());

        builder.setField(DefaultFieldParser.parse("From: john.doe@example.net"));

        Assert.assertEquals("john.doe@example.net", builder.getFrom().get(0).getAddress());
    }

    @Test
    public void testSetFrom() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.net");

        builder.setFrom(mailbox1);
        Assert.assertEquals("john.doe@example.net", builder.getField("From")
                .getBody());

        builder.setFrom(mailbox1, mailbox2);
        Assert.assertEquals("john.doe@example.net, jane.doe@example.net",
                builder.getField("From").getBody());

        builder.setFrom(Arrays.asList(mailbox1, mailbox2));
        Assert.assertEquals("john.doe@example.net, jane.doe@example.net",
                builder.getField("From").getBody());

        builder.setFrom((Mailbox) null);
        Assert.assertNull(builder.getField("From"));
    }

    @Test
    public void testGetTo() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getTo());

        builder.setField(DefaultFieldParser.parse("To: john.doe@example.net"));

        Assert.assertEquals("john.doe@example.net", ((Mailbox) builder.getTo().get(0))
                .getAddress());
    }

    @Test
    public void testSetTo() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        builder.setTo(group);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;",
                builder.getField("To").getBody());

        builder.setTo(group, mailbox3);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder.getField("To")
                .getBody());

        builder.setTo(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder.getField("To")
                .getBody());

        builder.setTo((Mailbox) null);
        Assert.assertNull(builder.getField("To"));
    }

    @Test
    public void testGetCc() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getCc());

        builder.setField(DefaultFieldParser.parse("Cc: john.doe@example.net"));

        Assert.assertEquals("john.doe@example.net", ((Mailbox) builder.getCc().get(0))
                .getAddress());
    }

    @Test
    public void testSetCc() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        builder.setCc(group);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;",
                builder.getField("Cc").getBody());

        builder.setCc(group, mailbox3);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder.getField("Cc")
                .getBody());

        builder.setCc(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder.getField("Cc")
                .getBody());

        builder.setCc((Mailbox) null);
        Assert.assertNull(builder.getField("Cc"));
    }

    @Test
    public void testGetBcc() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getBcc());

        builder.setField(DefaultFieldParser.parse("Bcc: john.doe@example.net"));

        Assert.assertEquals("john.doe@example.net", ((Mailbox) builder.getBcc().get(0))
                .getAddress());
    }

    @Test
    public void testSetBcc() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        builder.setBcc(group);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;",
                builder.getField("Bcc").getBody());

        builder.setBcc(group, mailbox3);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder
                .getField("Bcc").getBody());

        builder.setBcc(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder
                .getField("Bcc").getBody());

        builder.setBcc((Mailbox) null);
        Assert.assertNull(builder.getField("Bcc"));
    }

    @Test
    public void testGetReplyTo() throws Exception {
        MessageBuilder builder = MessageBuilder.create();
        Assert.assertNull(builder.getReplyTo());

        builder.setField(DefaultFieldParser.parse("Reply-To: john.doe@example.net"));

        Assert.assertEquals("john.doe@example.net", ((Mailbox) builder.getReplyTo().get(0))
                .getAddress());
    }

    @Test
    public void testSetReplyTo() throws Exception {
        MessageBuilder builder = MessageBuilder.create();

        Mailbox mailbox1 = DefaultAddressParser.DEFAULT.parseMailbox("john.doe@example.net");
        Mailbox mailbox2 = DefaultAddressParser.DEFAULT.parseMailbox("jane.doe@example.net");
        Group group = new Group("Does", mailbox1, mailbox2);
        Mailbox mailbox3 = DefaultAddressParser.DEFAULT.parseMailbox("Mary Smith <mary@example.net>");

        builder.setReplyTo(group);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;",
                builder.getField("Reply-To").getBody());

        builder.setReplyTo(group, mailbox3);
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder.getField(
                "Reply-To").getBody());

        builder.setReplyTo(Arrays.asList(group, mailbox3));
        Assert.assertEquals("Does: john.doe@example.net, jane.doe@example.net;, "
                + "Mary Smith <mary@example.net>", builder.getField(
                "Reply-To").getBody());

        builder.setReplyTo((Mailbox) null);
        Assert.assertNull(builder.getField("Reply-To"));
    }

    @Test
    public void testBuildWithDefaults() throws Exception {
        Message message = MessageBuilder.create()
                .generateMessageId("hostname")
                .setSubject("testing ...")
                .setFrom("batman@localhost", "superman@localhost")
                .setTo("\"Big momma\" <big_momma@localhost>")
                .setBody("Yo, big momma!", Charsets.UTF_8)
                .build();

        Assert.assertEquals("text/plain", message.getMimeType());
        Assert.assertNotNull(message.getMessageId());
        Assert.assertNotNull(message.getDate());
        Assert.assertEquals("1.0", message.getHeader().getField(FieldName.MIME_VERSION).getBody());
        Assert.assertEquals("testing ...", message.getSubject());
        Assert.assertEquals(new MailboxList(
                new Mailbox("", "batman", "localhost"),
                new Mailbox("", "superman", "localhost")), message.getFrom());
        Assert.assertEquals(new AddressList(
                new Mailbox("Big momma", "big_momma", "localhost")), message.getTo());
        Assert.assertEquals("text/plain", message.getMimeType());
        Assert.assertEquals("UTF-8", message.getCharset());
    }

    @Test
    public void testBuildWithNoDefaults() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = dateFormat.parse("2014-08-01 12:00");

        Message message = MessageBuilder.create()
                .setMessageId("blah@hostname")
                .setDate(date)
                .addField(Fields.version("1.0.1"))
                .setSubject("testing ...")
                .setFrom("batman@localhost", "superman@localhost")
                .setTo("\"Big momma\" <big_momma@localhost>")
                .setBody("Yo, big momma!", Charsets.UTF_8)
                .build();

        Assert.assertEquals("text/plain", message.getMimeType());
        Assert.assertEquals("blah@hostname", message.getMessageId());
        Assert.assertEquals(date, message.getDate());
        Assert.assertEquals("1.0.1", message.getHeader().getField(FieldName.MIME_VERSION).getBody());
        Assert.assertEquals("testing ...", message.getSubject());
        Assert.assertEquals(new MailboxList(
                new Mailbox("", "batman", "localhost"),
                new Mailbox("", "superman", "localhost")), message.getFrom());
        Assert.assertEquals(new AddressList(
                new Mailbox("Big momma", "big_momma", "localhost")), message.getTo());
        Assert.assertEquals("text/plain", message.getMimeType());
        Assert.assertEquals("UTF-8", message.getCharset());
    }

    @Test
    public void testCopy() throws Exception {
        Message original = MessageBuilder.create()
                .generateMessageId("hostname")
                .setSubject("testing ...")
                .setFrom("batman@localhost", "superman@localhost")
                .setTo("\"Big momma\" <big_momma@localhost>")
                .setBody("Yo, big momma!", Charsets.UTF_8)
                .build();

        MessageBuilder builder = MessageBuilder.createCopy(original);
        Assert.assertEquals(original.getHeader().getFields(), builder.getFields());
        Assert.assertNotSame(original.getBody(), builder.getBody());
        Assert.assertSame(original.getBody().getClass(), builder.getBody().getClass());
    }

}
