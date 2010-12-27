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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.MimeTokenStream;

import junit.framework.TestCase;

public class MimeTokenStreamReaderTest extends TestCase {
    
    MimeTokenStream parser;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new MimeTokenStream();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testShouldReadSimpleBody() throws Exception {
        byte[] bytes = ExampleMail.RFC822_SIMPLE_BYTES;
        String body = ExampleMail.RFC822_SIMPLE_BODY;
        checkSimpleMail(bytes, body, 4);
    }

    public void testShouldReadOnePartMimeASCIIBody() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_ASCII_BYTES;
        String body = ExampleMail.ONE_PART_MIME_ASCII_BODY;
        checkSimpleMail(bytes, body, 11);
    }

    public void testShouldReadOnePartMime8859Body() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_8859_BYTES;
        String body = ExampleMail.ONE_PART_MIME_8859_BODY;
        checkSimpleMail(bytes, body, 13);
    }
    
    public void testShouldReadOnePartMimeBase64ASCIIBody() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_BASE64_ASCII_BYTES;
        String body = ExampleMail.ONE_PART_MIME_BASE64_ASCII_BODY;
        checkSimpleMail(bytes, body, 11);
    }
    
    public void testShouldReadOnePartMimeBase64Latin1Body() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BYTES;
        String body = ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BODY;
        checkSimpleMail(bytes, body, 11);
    }
    
    public void testShouldReadOnePartMimeQuotedPrintable() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BYTES;
        String body = ExampleMail.ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BODY;
        checkSimpleMail(bytes, body, 11);
    }
    
    public void testShouldReadPartBodies() throws IOException, MimeException {
        InputStream in = new ByteArrayInputStream(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTES);
        parser.parse(in);
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER),MimeTokenStream.stateToString(parser.next()));
        for (int i=0;i<5;i++) {
            assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        }
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_MULTIPART),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_PREAMBLE),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_BODYPART),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY),MimeTokenStream.stateToString(parser.next()));
        checkBody(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_7BIT);
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_BODYPART),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_BODYPART),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY),MimeTokenStream.stateToString(parser.next()));
        checkBody(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_QUOTED_PRINTABLE);
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_BODYPART),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_BODYPART),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY),MimeTokenStream.stateToString(parser.next()));
        checkBody(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BASE64);
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_BODYPART),MimeTokenStream.stateToString(parser.next()));

        
    }
    
    private void checkSimpleMail(byte[] bytes, String body, int fields) throws IOException, MimeException {
        InputStream in = new ByteArrayInputStream(bytes);
        parser.parse(in);
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER),MimeTokenStream.stateToString(parser.next()));
        for (int i=0;i<fields;i++) {
            assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD),MimeTokenStream.stateToString(parser.next()));
        }
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER),MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY),MimeTokenStream.stateToString(parser.next()));
        checkBody(body);
    }

    private void checkBody(String body) throws IOException {
        Reader reader = parser.getReader();
        assertNotNull(reader);
        assertEquals(body, IOUtils.toString(reader));
    }
}
