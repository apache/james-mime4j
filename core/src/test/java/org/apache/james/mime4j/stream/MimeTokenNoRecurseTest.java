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

package org.apache.james.mime4j.stream;

import org.apache.james.mime4j.util.CharsetUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MimeTokenNoRecurseTest {

    private static final String INNER_MAIL = "From: Timothy Tayler <tim@example.org>\r\n" +
            "To: Joshua Tetley <joshua@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Multipart Without RFC822 Part\r\n" +
            "Content-Type: multipart/mixed;boundary=42\r\n\r\n" +
            "--42\r\n" +
            "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
            "First part of this mail\r\n" +
            "--42\r\n" +
            "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
            "Second part of this mail\r\n" +
            "--42--\r\n";

    private static final String MAIL_WITH_RFC822_PART = "MIME-Version: 1.0\r\n" +
            "From: Timothy Tayler <tim@example.org>\r\n" +
            "To: Joshua Tetley <joshua@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Multipart With RFC822 Part\r\n" +
            "Content-Type: multipart/mixed;boundary=1729\r\n\r\n" +
            "A short premable\r\n" +
            "--1729\r\n\r\n" +
            "First part has no headers\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
            "Second part is plain text\r\n" +
            "--1729\r\n" +
            "Content-Type: message/rfc822\r\n\r\n" +
            INNER_MAIL +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
            "Last part is plain text\r\n" +
            "--1729--\r\n" +
            "The End";

    MimeTokenStream stream;

    @Before
    public void setUp() throws Exception {
        stream = new MimeTokenStream();
        byte[] bytes = CharsetUtil.US_ASCII.encode(MAIL_WITH_RFC822_PART).array();
        InputStream in = new ByteArrayInputStream(bytes);
        stream.parse(in);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWhenRecurseShouldRecurseInnerMail() throws Exception {
        stream.setRecursionMode(RecursionMode.M_RECURSE);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_START_MULTIPART);
        nextIs(EntityState.T_PREAMBLE);
        nextShouldBeStandardPart(false);

        nextShouldBeStandardPart(true);

        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_START_MESSAGE);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_START_MULTIPART);
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextIs(EntityState.T_END_MULTIPART);
        nextIs(EntityState.T_END_MESSAGE);
        nextIs(EntityState.T_END_BODYPART);
        nextShouldBeStandardPart(true);
        nextIs(EntityState.T_EPILOGUE);
        nextIs(EntityState.T_END_MULTIPART);
    }


    @Test
    public void testWhenRecurseShouldTreatInnerMailAsAnyOtherPart() throws Exception {
        stream.setRecursionMode(RecursionMode.M_NO_RECURSE);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_START_MULTIPART);
        nextIs(EntityState.T_PREAMBLE);
        nextShouldBeStandardPart(false);

        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextIs(EntityState.T_EPILOGUE);
        nextIs(EntityState.T_END_MULTIPART);
    }

    @Test
    public void testWhenNoRecurseInputStreamShouldContainInnerMail() throws Exception {
        stream.setRecursionMode(RecursionMode.M_NO_RECURSE);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_START_MULTIPART);
        nextIs(EntityState.T_PREAMBLE);
        nextShouldBeStandardPart(false);

        nextShouldBeStandardPart(true);
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        InputStream inputStream = stream.getInputStream();
        int next = inputStream.read();
        int i = 0;
        while (next != -1) {
            Assert.assertEquals("@" + i, INNER_MAIL.charAt(i++), (char) next);
            next = inputStream.read();
        }
        Assert.assertEquals(INNER_MAIL.length() - 2, i);
    }

    @Test
    public void testSetNoRecurseSoInputStreamShouldContainInnerMail() throws Exception {
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_START_MULTIPART);
        nextIs(EntityState.T_PREAMBLE);
        nextShouldBeStandardPart(false);

        nextShouldBeStandardPart(true);
        stream.setRecursionMode(RecursionMode.M_NO_RECURSE);
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        InputStream inputStream = stream.getInputStream();
        int next = inputStream.read();
        int i = 0;
        while (next != -1) {
            Assert.assertEquals("@" + i, INNER_MAIL.charAt(i++), (char) next);
            next = inputStream.read();
        }
        Assert.assertEquals(INNER_MAIL.length() - 2, i);
    }

    private void nextShouldBeStandardPart(boolean withHeader) throws Exception {
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        if (withHeader) {
            nextIs(EntityState.T_FIELD);
        }
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        nextIs(EntityState.T_END_BODYPART);
    }

    private void nextIs(EntityState state) throws Exception {
        Assert.assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(stream.next()));
    }
}
