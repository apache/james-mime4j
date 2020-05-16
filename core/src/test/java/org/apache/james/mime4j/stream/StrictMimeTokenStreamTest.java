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

import org.apache.james.mime4j.MimeConfig;
import org.apache.james.mime4j.MimeIOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StrictMimeTokenStreamTest {

    private static final String HEADER_ONLY = "From: foo@abr.com\r\nSubject: A subject\r\n";
    private static final String CORRECT_HEADERS = HEADER_ONLY + "\r\n";

    MimeTokenStream parser;

    @Before
    public void setUp() throws Exception {
        parser = new MimeTokenStream(MimeConfig.STRICT);
    }

    @Test
    public void testUnexpectedEndOfHeaders() throws Exception {
        parser.parse(new ByteArrayInputStream(HEADER_ONLY.getBytes("US-ASCII")));

        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        try {
            parser.next();
            Assert.fail("Expected exception to be thrown");
        } catch (MimeParseEventException e) {
            Assert.assertEquals("Premature end of headers", Event.HEADERS_PREMATURE_END, e.getEvent());
        }
    }

    @Test
    public void testCorrectEndOfHeaders() throws Exception {
        parser.parse(new ByteArrayInputStream(CORRECT_HEADERS.getBytes("US-ASCII")));

        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
    }

    @Test
    public void testMissingBoundary() throws Exception {
        String message =
                "Content-Type: multipart/mixed;boundary=aaaa\r\n\r\n" +
                        "--aaaa\r\n" +
                        "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
                        "Oh my god! Boundary is missing!\r\n";
        parser.parse(new ByteArrayInputStream(message.getBytes("US-ASCII")));
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_START_MULTIPART);
        checkNextIs(EntityState.T_START_BODYPART);
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_BODY);
        InputStream in = parser.getInputStream();
        StringBuilder sb = new StringBuilder();
        try {
            byte[] tmp = new byte[1024];
            int l;
            while ((l = in.read(tmp)) != -1) {
                sb.append(new String(tmp, 0, l, "US-ASCII"));
            }
            Assert.fail("MimeIOException should have been thrown");
        } catch (MimeIOException expected) {
            Assert.assertEquals("Oh my god! Boundary is missing!\r\n", sb.toString());
        }
    }

    private void checkNextIs(EntityState expected) throws Exception {
        Assert.assertEquals(MimeTokenStream.stateToString(expected), MimeTokenStream.stateToString(parser.next()));
    }
}
