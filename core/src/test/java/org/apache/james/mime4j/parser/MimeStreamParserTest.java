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

package org.apache.james.mime4j.parser;

import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class MimeStreamParserTest {

    @Test
    public void testBoundaryInEpilogue() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("From: foo@bar.com\r\n");
        sb.append("To: someone@else.com\r\n");
        sb.append("Content-type: multipart/something; boundary=myboundary\r\n");
        sb.append("\r\n");
        sb.append("This is the preamble.\r\n");
        sb.append("--myboundary\r\n");
        sb.append("Content-type: text/plain\r\n");
        sb.append("\r\n");
        sb.append("This is the first body.\r\n");
        sb.append("It's completely meaningless.\r\n");
        sb.append("After this line the body ends.\r\n");
        sb.append("\r\n");
        sb.append("--myboundary--\r\n");

        StringBuilder epilogue = new StringBuilder();
        epilogue.append("Content-type: text/plain\r\n");
        epilogue.append("\r\n");
        epilogue.append("This is actually the epilogue but it looks like a second body.\r\n");
        epilogue.append("Yada yada yada.\r\n");
        epilogue.append("\r\n");
        epilogue.append("--myboundary--\r\n");
        epilogue.append("This is still the epilogue.\r\n");

        sb.append(epilogue.toString());

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("US-ASCII"));

        final StringBuilder actual = new StringBuilder();

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void epilogue(InputStream is) throws IOException {
                int b;
                while ((b = is.read()) != -1) {
                    actual.append((char) b);
                }
            }
        });
        parser.parse(bais);

        Assert.assertEquals(epilogue.toString(), actual.toString());
    }

    @Test
    public void testParseOneLineFields() throws Exception {
        StringBuilder sb = new StringBuilder();
        final LinkedList<String> expected = new LinkedList<String>();
        expected.add("From: foo@abr.com");
        sb.append(expected.getLast()).append("\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast()).append("\r\n");

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());
    }

    @Test
    public void testCRWithoutLFInHeader() throws Exception {
        /*
         * Test added because \r:s not followed by \n:s in the header would
         * cause an infinite loop.
         */
        StringBuilder sb = new StringBuilder();
        final LinkedList<String> expected = new LinkedList<String>();
        expected.add("The-field: This field\r\rcontains CR:s\r\r"
                + "not\r\n\tfollowed by LF");
        sb.append(expected.getLast()).append("\r\n");

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());
    }

    @Test
    public void testParseMultiLineFields() throws Exception {
        StringBuilder sb = new StringBuilder();
        final LinkedList<String> expected = new LinkedList<String>();
        expected.add("Received: by netmbx.netmbx.de (/\\==/\\ Smail3.1.28.1)\r\n"
                + "\tfrom mail.cs.tu-berlin.de with smtp\r\n"
                + "\tid &lt;m0uWPrO-0004wpC&gt;;"
                + " Wed, 19 Jun 96 18:12 MES");
        sb.append(expected.getLast()).append("\r\n");
        expected.add("Subject: A folded subject\r\n Line 2\r\n\tLine 3");
        sb.append(expected.getLast()).append("\r\n");

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());
    }

    @Test
    public void testStop() throws Exception {
        final MimeStreamParser parser = new MimeStreamParser();
        TestHandler handler = new TestHandler() {
            @Override
            public void endHeader() {
                super.endHeader();
                parser.stop();
            }
        };
        parser.setContentHandler(handler);

        String msg = "Subject: Yada yada\r\n"
                + "From: foo@bar.com\r\n"
                + "\r\n"
                + "Line 1\r\n"
                + "Line 2\r\n";
        String expected = "<message>\r\n"
                + "<header>\r\n"
                + "<field>\r\n"
                + "Subject: Yada yada"
                + "</field>\r\n"
                + "<field>\r\n"
                + "From: foo@bar.com"
                + "</field>\r\n"
                + "</header>\r\n"
                + "<body>\r\n"
                + "</body>\r\n"
                + "</message>\r\n";

        parser.parse(new ByteArrayInputStream(msg.getBytes()));
        String result = handler.sb.toString();

        Assert.assertEquals(expected, result);
    }

    /*
     * Tests that invalid fields are ignored.
     */
    @Test
    public void testInvalidFields() throws Exception {
        StringBuilder sb = new StringBuilder();
        final LinkedList<String> expected = new LinkedList<String>();
        sb.append("From - foo@abr.com\r\n");
        expected.add("From: some@one.com");
        sb.append(expected.getLast()).append("\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast()).append("\r\n");
        sb.append("A line which should be ignored\r\n");

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());
    }

    /*
     * Tests that empty streams still generate the expected series of events.
     */
    @Test
    public void testEmptyStream() throws Exception {
        final LinkedList<String> expected = new LinkedList<String>();
        expected.add("startMessage");
        expected.add("startHeader");
        expected.add("endHeader");
        expected.add("body");
        expected.add("endMessage");

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void body(BodyDescriptor bd, InputStream is) {
                Assert.assertEquals(expected.removeFirst(), "body");
            }

            @Override
            public void endMultipart() {
                Assert.fail("endMultipart shouldn't be called for empty stream");
            }

            @Override
            public void endBodyPart() {
                Assert.fail("endBodyPart shouldn't be called for empty stream");
            }

            @Override
            public void endHeader() {
                Assert.assertEquals(expected.removeFirst(), "endHeader");
            }

            @Override
            public void endMessage() {
                Assert.assertEquals(expected.removeFirst(), "endMessage");
            }

            @Override
            public void field(Field field) {
                Assert.fail("field shouldn't be called for empty stream");
            }

            @Override
            public void startMultipart(BodyDescriptor bd) {
                Assert.fail("startMultipart shouldn't be called for empty stream");
            }

            @Override
            public void startBodyPart() {
                Assert.fail("startBodyPart shouldn't be called for empty stream");
            }

            @Override
            public void startHeader() {
                Assert.assertEquals(expected.removeFirst(), "startHeader");
            }

            @Override
            public void startMessage() {
                Assert.assertEquals(expected.removeFirst(), "startMessage");
            }
        });

        parser.parse(new ByteArrayInputStream(new byte[0]));

        Assert.assertEquals(0, expected.size());
    }

    /*
     * Tests parsing of empty headers.
     */
    @Test
    public void testEmpyHeader() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n");
        sb.append("The body is right here\r\n");

        final StringBuilder body = new StringBuilder();

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.fail("No fields should be reported");
            }

            @Override
            public void body(BodyDescriptor bd, InputStream is) throws IOException {
                int b;
                while ((b = is.read()) != -1) {
                    body.append((char) b);
                }
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals("The body is right here\r\n", body.toString());
    }

    /*
     * Tests parsing of empty body.
     */
    @Test
    public void testEmptyBody() throws Exception {
        StringBuilder sb = new StringBuilder();
        final LinkedList<String> expected = new LinkedList<String>();
        expected.add("From: some@one.com");
        sb.append(expected.getLast()).append("\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast()).append("\r\n\r\n");

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }

            @Override
            public void body(BodyDescriptor bd, InputStream is) throws IOException {
                Assert.assertEquals(-1, is.read());
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());
    }

    /*
     * Tests that invalid fields are ignored.
     */
    @Test
    public void testPrematureEOFAfterFields() throws Exception {
        StringBuilder sb = new StringBuilder();
        final LinkedList<String> expected = new LinkedList<String>();
        expected.add("From: some@one.com");
        sb.append(expected.getLast()).append("\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast());

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());

        sb = new StringBuilder();
        expected.clear();
        expected.add("From: some@one.com");
        sb.append(expected.getLast()).append("\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast()).append("\r\n");

        parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void field(Field field) {
                Assert.assertEquals(expected.removeFirst(), decode(field.getRaw()));
            }
        });

        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));

        Assert.assertEquals(0, expected.size());
    }

    @Test
    public void testAutomaticContentDecoding() throws Exception {
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentDecoding(true);
        TestHandler handler = new TestHandler();
        parser.setContentHandler(handler);

        String msg = "Subject: Yada yada\r\n"
                + "From: foo@bar.com\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "\r\n"
                + "V2hvIGF0ZSBteSBjYWtlPwo=";
        String expected = "<message>\r\n"
                + "<header>\r\n"
                + "<field>\r\n"
                + "Subject: Yada yada"
                + "</field>\r\n"
                + "<field>\r\n"
                + "From: foo@bar.com"
                + "</field>\r\n"
                + "<field>\r\n"
                + "Content-Type: application/octet-stream"
                + "</field>\r\n"
                + "<field>\r\n"
                + "Content-Transfer-Encoding: base64"
                + "</field>\r\n"
                + "</header>\r\n"
                + "<body>\r\n"
                + "Who ate my cake?\n"
                + "</body>\r\n"
                + "</message>\r\n";

        parser.parse(new ByteArrayInputStream(msg.getBytes()));
        String result = handler.sb.toString();

        Assert.assertEquals(expected, result);
    }

    protected String decode(ByteSequence byteSequence) {
        return ContentUtil.decode(byteSequence);
    }

}
