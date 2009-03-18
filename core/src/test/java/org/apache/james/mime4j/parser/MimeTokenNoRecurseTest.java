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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.james.mime4j.util.CharsetUtil;

public class MimeTokenNoRecurseTest extends TestCase {

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
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        stream = new MimeTokenStream();
        byte[] bytes = CharsetUtil.US_ASCII.encode(MAIL_WITH_RFC822_PART).array();
        InputStream in = new ByteArrayInputStream(bytes);
        stream.parse(in);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWhenRecurseShouldRecurseInnerMail() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_RECURSE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(false);
        
        nextShouldBeStandardPart(true);
        
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MESSAGE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_BODYPART);
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
    }
    

    public void testWhenRecurseShouldTreatInnerMailAsAnyOtherPart() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_NO_RECURSE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(false);
        
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
    }
    
    public void testWhenNoRecurseInputStreamShouldContainInnerMail() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_NO_RECURSE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(false);
        
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        InputStream inputStream = stream.getInputStream();
        int next = inputStream.read();
        int i=0;
        while (next != -1) {
            assertEquals("@" + i, INNER_MAIL.charAt(i++), (char) next);
            next = inputStream.read();
        }
        assertEquals(INNER_MAIL.length()-2, i);
    }
    
    public void testSetNoRecurseSoInputStreamShouldContainInnerMail() throws Exception {
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(false);
        
        nextShouldBeStandardPart(true);
        stream.setRecursionMode(MimeTokenStream.M_NO_RECURSE);
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        InputStream inputStream = stream.getInputStream();
        int next = inputStream.read();
        int i=0;
        while (next != -1) {
            assertEquals("@" + i, INNER_MAIL.charAt(i++), (char) next);
            next = inputStream.read();
        }
        assertEquals(INNER_MAIL.length()-2, i);
    }

    private void nextShouldBeStandardPart(boolean withHeader) throws Exception {
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        if (withHeader) {
            nextIs(MimeTokenStream.T_FIELD);
        }
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        nextIs(MimeTokenStream.T_END_BODYPART);
    }
    
    private void nextIs(int state) throws Exception {
        assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(stream.next()));
    }
}
