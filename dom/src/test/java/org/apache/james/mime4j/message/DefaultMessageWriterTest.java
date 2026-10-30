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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.io.MaxNestingDepthLimitException;
import org.apache.james.mime4j.stream.MimeConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.address.LenientAddressParser;
import org.apache.james.mime4j.stream.RawField;
import org.junit.Test;

public class DefaultMessageWriterTest {

    @Test
    public void asBytesShouldSerializeTheMessage() throws Exception {
        byte[] bytes = DefaultMessageWriter.asBytes(
            Message.Builder.of()
                .setBody("this is the body", Charsets.UTF_8)
                .setFrom("sender@localhost")
                .setTo("receiver@localhost")
                .setSubject("Cool subject")
                .build());

        assertThat(new String(bytes, Charsets.UTF_8.name()))
            .isEqualTo("MIME-Version: 1.0\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "From: sender@localhost\r\n" +
                "To: receiver@localhost\r\n" +
                "Subject: Cool subject\r\n" +
                "\r\n" +
                "this is the body");
    }
    
    @Test
    public void shouldThrowOnHeaderInjectionAttempt() throws Exception {
        Message.Builder builder = Message.Builder.of()
            .setBody("this is the body", Charsets.UTF_8)
            .setFrom("sender@localhost");

        assertThatThrownBy(() -> builder.setContentTransferEncoding("victim@attacker.com\r\nReply-To: attacker@evil.com"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rawFieldShouldRejectBareLF() {
        assertThatThrownBy(() -> new RawField("X-Custom", "x\nBcc: victim@example.com"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rawFieldShouldRejectTrailingCRLF() {
        assertThatThrownBy(() -> new RawField("X-Custom", "\r\n"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void messageIdShouldRejectTrailingCRLF() {
        assertThatThrownBy(() -> Fields.messageId("<a@b>\r\n"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void messageIdShouldRejectBareLF() {
        assertThatThrownBy(() -> Fields.messageId("\n\n"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void fromShouldEncodeBareLFInDisplayName() {
        MailboxListField field = Fields.from(new Mailbox("\n!", "a", "example.com"));

        assertThat(field.getBody()).doesNotContain("\n").doesNotContain("\r");
        assertThat(field.getMailboxList().get(0).getName()).isEqualTo("\n!");
    }

    @Test
    public void parsedDisplayNameWithLineBreakShouldBeWrittenBack() throws Exception {
        Mailbox parsed = LenientAddressParser.DEFAULT.parseMailbox("=?us-ascii?q?a=0Ab?= <a@example.com>");

        MailboxListField field = Fields.from(parsed);

        assertThat(field.getBody()).doesNotContain("\n");
        assertThat(LenientAddressParser.DEFAULT.parseMailbox(field.getBody()).getName()).isEqualTo("a\nb");
    }

    @Test
    public void contentTypeShouldRejectBareLFInParameter() {
        assertThatThrownBy(() -> Fields.contentType("text/plain", Collections.singletonMap("name", "\n")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void contentDispositionShouldRejectBareLFInFilename() {
        assertThatThrownBy(() -> Fields.contentDisposition("attachment", "\n!"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static byte[] parseAndWrite(byte[] message) throws Exception {
        Message parsed = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(message));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DefaultMessageWriter().writeMessage(parsed, out);
        return out.toByteArray();
    }

    @Test
    public void writingAMessageWithANonAsciiBoundaryShouldBeIdempotent() throws Exception {
        byte[] message = ("Content-Type: multipart/mixed; boundary=\"été\"\r\n\r\n"
            + "--été\r\n\r\nfirst\r\n--été--\r\n").getBytes(StandardCharsets.UTF_8);

        byte[] once = parseAndWrite(message);

        assertThat(new String(parseAndWrite(once), StandardCharsets.UTF_8)).isEqualTo(new String(once, StandardCharsets.UTF_8));
    }

    @Test
    public void headerLineEndingWithLfShouldStillParseOnceWrittenWithCrlf() throws Exception {
        StringBuilder value = new StringBuilder();
        while (value.length() < 998 - "X-Long: ".length()) {
            value.append('a');
        }
        byte[] input = ("X-Long: " + value + "\n\n").getBytes(StandardCharsets.US_ASCII);
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Header header = builder.parseHeader(new ByteArrayInputStream(input));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DefaultMessageWriter().writeHeader(header, out);

        Header reparsed = builder.parseHeader(new ByteArrayInputStream(out.toByteArray()));

        assertThat(reparsed.getField("X-Long").getBody()).isEqualTo(value.toString());
    }

    @Test
    public void prefoldedFieldShouldNotEndTheHeaderBlock() throws Exception {
        StringBuilder x = new StringBuilder();
        while (x.length() < 70) {
            x.append('x');
        }
        Message message = Message.Builder.of()
            .setBody("SENTINEL-BODY", StandardCharsets.US_ASCII)
            .addField(new RawField("X-Custom", x + "\r\n yyyyyyyyyy z"))
            .addField(new RawField("X-Sentinel", "1"))
            .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DefaultMessageWriter().writeMessage(message, out);

        Message parsed = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(out.toByteArray()));

        assertThat(parsed.getHeader().getField("X-Sentinel")).isNotNull();
    }

    @Test
    public void headlessParsingShouldReportAFailureOfItsFirstStep() {
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(MimeConfig.custom()
            .setMaxNestingDepth(1)
            .setHeadlessParsing("message/rfc822")
            .build());

        assertThatThrownBy(() -> builder.parseMessage(new ByteArrayInputStream("From: a@b.c\r\n\r\nbody\r\n".getBytes(StandardCharsets.US_ASCII))))
            .isInstanceOf(MimeIOException.class)
            .hasCauseInstanceOf(MaxNestingDepthLimitException.class);
    }

    @Test
    public void toShouldRejectADomainAddingRecipients() {
        assertThatThrownBy(() -> Fields.to(new Mailbox("a", "example.com, other@example.org")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
