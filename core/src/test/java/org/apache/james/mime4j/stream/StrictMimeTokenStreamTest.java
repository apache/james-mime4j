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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private static final String NESTED_UNTERMINATED =
        "Content-Type: multipart/mixed; boundary=a\r\n\r\n"
        + "--a\r\n"
        + "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
        + "Th\r\n"
        + "--b\r\n"
        + "x";

    private static List<String> events(InputStream in) throws Exception {
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.STRICT);
        stream.parse(in);
        List<String> events = new ArrayList<>();
        try {
            for (EntityState s = stream.getState(); s != EntityState.T_END_OF_STREAM; s = stream.next()) {
                if (s == EntityState.T_PREAMBLE || s == EntityState.T_BODY || s == EntityState.T_EPILOGUE) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStream body = stream.getInputStream();
                    byte[] b = new byte[512];
                    int n;
                    while ((n = body.read(b)) != -1) {
                        out.write(b, 0, n);
                    }
                    events.add(s + " " + out.toString("US-ASCII"));
                } else {
                    events.add(s.toString());
                }
            }
        } catch (MimeException | IOException e) {
            events.add("EXCEPTION " + e.getMessage());
        }
        return events;
    }

    @Test
    public void nestedMultipartWithoutClosingDelimiterDoesNotDependOnReadSizes() throws Exception {
        byte[] message = NESTED_UNTERMINATED.getBytes("US-ASCII");
        InputStream oneByteReads = new FilterInputStream(new ByteArrayInputStream(message)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, 1));
            }
        };
        List<String> whole = events(new ByteArrayInputStream(message));
        Assert.assertTrue(whole.toString(), whole.contains("T_PREAMBLE Th"));
        Assert.assertEquals(events(oneByteReads), whole);
    }

    private void checkNextIs(EntityState expected) throws Exception {
        Assert.assertEquals(MimeTokenStream.stateToString(expected), MimeTokenStream.stateToString(parser.next()));
    }

    @Test
    public void stopOnAFieldShouldEndTheHeaderInStrictMode() throws Exception {
        String msg = "Subject: a\r\nFrom: b@example.com\r\n\r\nbody\r\n";
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setStrictParsing(true).build());
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        stream.next(); // T_START_HEADER
        Assert.assertEquals(EntityState.T_FIELD, stream.next());
        stream.stop();

        EntityState state;
        do {
            state = stream.next();
        } while (state != EntityState.T_END_OF_STREAM);
    }

    @Test
    public void stopAtTheEndOfAMessageRfc822HeaderShouldNotParseTheEncapsulatedMessage() throws Exception {
        String msg = "Subject: a\r\nContent-Type: message/rfc822\r\n\r\n"
            + "From: b@example.org\r\nSubject: c\r\n\r\nbody\r\n";
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setStrictParsing(true).build());
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        while (stream.next() != EntityState.T_END_HEADER) {
            // read the header
        }
        stream.stop();

        List<EntityState> after = new ArrayList<>();
        EntityState state;
        do {
            state = stream.next();
            after.add(state);
        } while (state != EntityState.T_END_OF_STREAM);
        Assert.assertEquals(Arrays.asList(EntityState.T_BODY, EntityState.T_END_MESSAGE,
            EntityState.T_END_OF_STREAM), after);
    }

    @Test
    public void stopAtTheStartOfTheMessageShouldEndTheHeaderInStrictMode() throws Exception {
        String msg = "Subject: a\r\nFrom: b@example.com\r\n\r\nbody\r\n";
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setStrictParsing(true).build());
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        Assert.assertEquals(EntityState.T_START_MESSAGE, stream.getState());
        stream.stop();

        List<EntityState> after = new ArrayList<>();
        EntityState state;
        do {
            state = stream.next();
            after.add(state);
        } while (state != EntityState.T_END_OF_STREAM);
        Assert.assertEquals(Arrays.asList(EntityState.T_START_HEADER, EntityState.T_END_HEADER, EntityState.T_BODY,
            EntityState.T_END_MESSAGE, EntityState.T_END_OF_STREAM), after);
    }

    @Test
    public void stopAtTheStartOfABodyPartShouldNotFailInStrictMode() throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
            + "--b\r\nSubject: part\r\n\r\none\r\n--b--\r\n";
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setStrictParsing(true).build());
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        while (stream.next() != EntityState.T_START_BODYPART) {
            // up to the part
        }
        stream.stop();

        EntityState state;
        do {
            state = stream.next();
        } while (state != EntityState.T_END_OF_STREAM);
    }

    @Test
    public void stopOnAFieldFollowedByAMalformedLineShouldNotFailInStrictMode() throws Exception {
        String msg = "Subject: a\r\nbadline\r\n\r\nbody\r\n";
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setStrictParsing(true).build());
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        stream.next(); // T_START_HEADER
        Assert.assertEquals(EntityState.T_FIELD, stream.next());
        stream.stop();

        EntityState state;
        do {
            state = stream.next();
        } while (state != EntityState.T_END_OF_STREAM);
    }
}
