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

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.MimeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class MimeTokenStreamReaderTest {

    MimeTokenStream parser;

    @Before
    public void setUp() throws Exception {
        parser = new MimeTokenStream();
    }

    @Test
    public void testShouldReadSimpleBody() throws Exception {
        byte[] bytes = ExampleMail.RFC822_SIMPLE_BYTES;
        String body = ExampleMail.RFC822_SIMPLE_BODY;
        checkSimpleMail(bytes, body, 4);
    }

    @Test
    public void testShouldReadOnePartMimeASCIIBody() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_ASCII_BYTES;
        String body = ExampleMail.ONE_PART_MIME_ASCII_BODY;
        checkSimpleMail(bytes, body, 11);
    }

    @Test
    public void testShouldReadOnePartMime8859Body() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_8859_BYTES;
        String body = ExampleMail.ONE_PART_MIME_8859_BODY;
        checkSimpleMail(bytes, body, 13);
    }

    @Test
    public void testShouldReadOnePartMimeBase64ASCIIBody() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_BASE64_ASCII_BYTES;
        String body = ExampleMail.ONE_PART_MIME_BASE64_ASCII_BODY;
        checkSimpleMail(bytes, body, 11);
    }

    @Test
    public void testShouldReadOnePartMimeBase64Latin1Body() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BYTES;
        String body = ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BODY;
        checkSimpleMail(bytes, body, 11);
    }

    @Test
    public void testShouldReadOnePartMimeQuotedPrintable() throws Exception {
        byte[] bytes = ExampleMail.ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BYTES;
        String body = ExampleMail.ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BODY;
        checkSimpleMail(bytes, body, 11);
    }

    @Test
    public void testShouldReadPartBodies() throws IOException, MimeException {
        InputStream in = new ByteArrayInputStream(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTES);
        parser.parse(in);
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        }
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_MULTIPART), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_PREAMBLE), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_BODYPART), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_BODY), MimeTokenStream.stateToString(parser.next()));
        checkBody(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_7BIT);
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_BODYPART), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_BODYPART), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_BODY), MimeTokenStream.stateToString(parser.next()));
        checkBody(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_QUOTED_PRINTABLE);
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_BODYPART), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_BODYPART), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_BODY), MimeTokenStream.stateToString(parser.next()));
        checkBody(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BASE64);
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_BODYPART), MimeTokenStream.stateToString(parser.next()));


    }

    private void checkSimpleMail(byte[] bytes, String body, int fields) throws IOException, MimeException {
        InputStream in = new ByteArrayInputStream(bytes);
        parser.parse(in);
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        for (int i = 0; i < fields; i++) {
            Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        }
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        Assert.assertEquals(MimeTokenStream.stateToString(EntityState.T_BODY), MimeTokenStream.stateToString(parser.next()));
        checkBody(body);
    }

    private void checkBody(String body) throws IOException {
        Reader reader = parser.getReader();
        Assert.assertNotNull(reader);
        Assert.assertEquals(body, IOUtils.toString(reader));
    }
}
