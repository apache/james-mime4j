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

package org.apache.james.mime4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.field.ContentDispositionFieldLenientImpl;
import org.apache.james.mime4j.field.DateTimeFieldLenientImpl;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Test;

/** Requalification of closed issues on origin/master, written from their titles. */
public class RequalificationDomTest {

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    /** MimeType and decoded body of the first leaf as MimeTokenStream sees it. */
    private static String tokenStreamView(String msg) throws Exception {
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(new ByteArrayInputStream(ascii(msg)));
        StringBuilder view = new StringBuilder();
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
            if (state == EntityState.T_BODY) {
                BodyDescriptor bd = stream.getBodyDescriptor();
                view.append(bd.getMimeType()).append('|').append(bd.getTransferEncoding()).append('|')
                    .append(new String(ContentUtil.buffer(stream.getDecodedInputStream()), StandardCharsets.US_ASCII)).append(';');
            }
        }
        return view.toString();
    }

    private static void domView(org.apache.james.mime4j.dom.Entity entity, StringBuilder view) throws Exception {
        if (entity.getBody() instanceof Multipart) {
            for (org.apache.james.mime4j.dom.Entity part : ((Multipart) entity.getBody()).getBodyParts()) {
                domView(part, view);
            }
        } else if (entity.getBody() instanceof Message) {
            domView((Message) entity.getBody(), view);
        } else {
            org.apache.james.mime4j.dom.SingleBody body = (org.apache.james.mime4j.dom.SingleBody) entity.getBody();
            view.append(entity.getMimeType()).append('|').append(entity.getContentTransferEncoding()).append('|')
                .append(new String(ContentUtil.buffer(body.getInputStream()), StandardCharsets.US_ASCII)).append(';');
        }
    }

    private static String domView(String msg) throws Exception {
        Message message = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(ascii(msg)));
        StringBuilder view = new StringBuilder();
        domView(message, view);
        return view.toString();
    }

    // #32 two Content-Type / two Content-Transfer-Encoding fields: core and DOM agree
    @Test
    public void issue32_twoContentTypesCoreAndDomShouldAgree() throws Exception {
        String[] firsts = {"Content-Type: text/plain", "Content-Type: ", "Content-Type: invalid",
            "Content-Type: multipart/mixed", "Content-Type: text/plain; charset=utf-8"};
        String second = "Content-Type: multipart/mixed; boundary=b";
        String body = "\r\n--b\r\n\r\nx\r\n--b--\r\n";
        StringBuilder failures = new StringBuilder();
        for (String first : firsts) {
            for (String msg : new String[] {first + "\r\n" + second + "\r\n" + body, second + "\r\n" + first + "\r\n" + body}) {
                String core = tokenStreamView(msg);
                String dom = domView(msg);
                if (!core.equals(dom)) {
                    failures.append(msg.replace("\r\n", "\\r\\n")).append("\n  core=").append(core).append("\n  dom =").append(dom).append('\n');
                }
            }
        }
        assertThat(failures.toString()).isEmpty();
    }

    @Test
    public void issue32_twoTransferEncodingsCoreAndDomShouldAgree() throws Exception {
        String[] firsts = {"Content-Transfer-Encoding: base64", "Content-Transfer-Encoding: ", "Content-Transfer-Encoding: 7bit",
            "Content-Transfer-Encoding: x-unknown", "Content-Transfer-Encoding: quoted-printable"};
        String second = "Content-Transfer-Encoding: base64";
        String body = "\r\naGVsbG8=\r\n";
        StringBuilder failures = new StringBuilder();
        for (String first : firsts) {
            for (String msg : new String[] {"Content-Type: text/plain\r\n" + first + "\r\n" + second + "\r\n" + body,
                "Content-Type: text/plain\r\n" + second + "\r\n" + first + "\r\n" + body}) {
                String core = tokenStreamView(msg);
                String dom = domView(msg);
                if (!core.equals(dom)) {
                    failures.append(msg.replace("\r\n", "\\r\\n")).append("\n  core=").append(core).append("\n  dom =").append(dom).append('\n');
                }
            }
        }
        assertThat(failures.toString()).isEmpty();
    }

    // #33 ContentDispositionFieldLenientImpl#getParameter case insensitive
    @Test
    public void issue33_contentDispositionGetParameterShouldBeCaseInsensitive() {
        ContentDispositionField field = ContentDispositionFieldLenientImpl.PARSER.parse(
            new RawField("Content-Disposition", "attachment; Name=x; FILENAME=y.txt; size=3"), DecodeMonitor.SILENT);
        assertThat(field.getParameter("name")).isEqualTo("x");
        assertThat(field.getParameter("Name")).isEqualTo("x");
        assertThat(field.getParameter("NAME")).isEqualTo("x");
        assertThat(field.getParameter("filename")).isEqualTo("y.txt");
        assertThat(field.getParameter("Size")).isEqualTo("3");
        assertThat(field.getFilename()).isEqualTo("y.txt");
    }

    // #36 literal "=?...?=" in a subject or a display name survives a write / read
    @Test
    public void issue36_subjectHoldingAnEncodedWordLookalikeShouldReadBack() throws Exception {
        for (String subject : new String[] {"=?utf-8?q?x?=", "hello =?utf-8?b?eA==?= world", "=?ISO-8859-1?Q?a?= =?ISO-8859-1?Q?b?=",
            "a =?x?q?y?= b", "=?utf-8?q?caf=C3=A9?="}) {
            Message message = Message.Builder.of().setSubject(subject).setBody("b", StandardCharsets.US_ASCII).build();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new DefaultMessageWriter().writeMessage(message, out);
            Message back = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(out.toByteArray()));
            assertThat(back.getSubject()).as(out.toString("US-ASCII")).isEqualTo(subject);
        }
    }

    @Test
    public void issue36_displayNameHoldingAnEncodedWordLookalikeShouldReadBack() throws Exception {
        for (String name : new String[] {"=?utf-8?q?x?=", "Bob =?utf-8?b?eA==?=", "=?utf-8?q?a?= =?utf-8?q?b?="}) {
            Message message = Message.Builder.of().setFrom(new Mailbox(name, "bob", "example.com"))
                .setBody("b", StandardCharsets.US_ASCII).build();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new DefaultMessageWriter().writeMessage(message, out);
            Message back = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(out.toByteArray()));
            assertThat(back.getFrom().get(0).getName()).as(out.toString("US-ASCII")).isEqualTo(name);
            // the strict parser too
            DefaultMessageBuilder strictBuilder = new DefaultMessageBuilder();
            strictBuilder.setFieldParser(org.apache.james.mime4j.field.DefaultFieldParser.getParser());
            Message strictBack = strictBuilder.parseMessage(new ByteArrayInputStream(out.toByteArray()));
            assertThat(strictBack.getFrom()).as(out.toString("US-ASCII")).isNotNull();
            assertThat(strictBack.getFrom().get(0).getName()).as(out.toString("US-ASCII")).isEqualTo(name);
        }
    }

    private static Date lenientDate(String value) {
        DateTimeField field = DateTimeFieldLenientImpl.PARSER.parse(new RawField("Date", value), DecodeMonitor.SILENT);
        return field.getDate();
    }

    // #24 obsolete zones
    @Test
    public void issue24_lenientDateShouldHonourObsoleteZones() {
        String[][] cases = {
            {"Tue, 26 Apr 2022 09:55:06 EST", "2022-04-26T14:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 EDT", "2022-04-26T13:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 CST", "2022-04-26T15:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 CDT", "2022-04-26T14:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 MST", "2022-04-26T16:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 MDT", "2022-04-26T15:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 PST", "2022-04-26T17:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 PDT", "2022-04-26T16:55:06Z"},
            {"26 Apr 2022 09:55 EST", "2022-04-26T14:55:00Z"},
            {"Tue, 26 Apr 2022 09:55:06 est", "2022-04-26T14:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 GMT", "2022-04-26T09:55:06Z"},
            {"Tue, 26 Apr 2022 09:55:06 UT", "2022-04-26T09:55:06Z"},
        };
        StringBuilder failures = new StringBuilder();
        for (String[] c : cases) {
            Date d = lenientDate(c[0]);
            if (d == null || !d.toInstant().equals(Instant.parse(c[1]))) {
                failures.append(c[0]).append(" -> ").append(d == null ? null : d.toInstant()).append(", expected ").append(c[1]).append('\n');
            }
        }
        assertThat(failures.toString()).isEmpty();
    }

    // #43 one digit year
    @Test
    public void issue43_lenientDateShouldMapOneDigitYearsAsTheStrictParser() throws Exception {
        String[] values = {"4 Oct 0 20:12:26 +0000", "Thu, 4 Oct 0 20:12:26 +0000", "4 Oct 9 20:12:26 +0000",
            "4 Oct 0 20:12 +0000", "4 Oct 0 20:12:26 GMT", "4 Oct 0 20:12:26 EST", "4 Oct 99 20:12:26 +0000",
            "4 Oct 101 20:12:26 +0000"};
        StringBuilder failures = new StringBuilder();
        for (String value : values) {
            Date lenient = lenientDate(value);
            Date strict = ((DateTimeField) org.apache.james.mime4j.field.DateTimeFieldImpl.PARSER
                .parse(new RawField("Date", value), DecodeMonitor.SILENT)).getDate();
            if (lenient == null || !lenient.equals(strict)) {
                failures.append(value).append(" lenient=").append(lenient == null ? null : lenient.toInstant())
                    .append(" strict=").append(strict == null ? null : strict.toInstant()).append('\n');
            }
        }
        assertThat(failures.toString()).isEmpty();
    }

    // header-list-index-disagree-case-folding / header-setfield-indexoutofbounds
    private static org.apache.james.mime4j.stream.Field parsed(String name, String value) throws Exception {
        // as read from a message: the bytes go through RawFieldParser, not RawField(name, body)
        return org.apache.james.mime4j.stream.RawFieldParser.DEFAULT.parseField(ContentUtil.encode(name + ": " + value));
    }

    @Test
    public void headerListAndIndexShouldAgreeWhateverTheCase() throws Exception {
        String[] names = {"X-Test", "x-test", "X-TEST", "X-Tİst", "x-tıst", "X-Teſt", "İ", "i", "I", "ı"};
        for (String a : names) {
            for (String b : names) {
                HeaderImpl header = new HeaderImpl();
                header.addField(parsed(a, "1"));
                header.addField(parsed("Other", "o"));
                header.addField(parsed(b, "2"));
                int removed = header.removeFields(a);
                assertThat(header.getFields(a)).as(a + "/" + b).isEmpty();
                assertThat(header.getField(a)).as(a + "/" + b).isNull();
                assertThat(header.getFields().size() + removed).as(a + "/" + b).isEqualTo(3);

                HeaderImpl h2 = new HeaderImpl();
                h2.addField(parsed(a, "1"));
                h2.addField(parsed(b, "2"));
                h2.setField(parsed(b, "3"));
                assertThat(h2.getField(b).getBody()).as(a + "/" + b).isEqualTo("3");
                assertThat(h2.getFields(b)).as(a + "/" + b).hasSize(1);
                HeaderImpl h3 = new HeaderImpl();
                h3.setField(parsed(a, "1"));
                h3.removeFields(b);
                h3.setField(parsed(b, "2"));
                assertThat(h3.getField(b).getBody()).as(a + "/" + b).isEqualTo("2");
            }
        }
    }
}
