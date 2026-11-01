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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.io.MaxContentLengthLimitException;
import org.apache.james.mime4j.io.MaxHeaderLengthLimitException;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Test;

public class MimeTokenStreamLimitsTest {

    private static MimeTokenStream parse(String msg, MimeConfig config) {
        MimeTokenStream stream = new MimeTokenStream(config);
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        return stream;
    }

    private static String line(int length) {
        StringBuilder line = new StringBuilder("Subject: ");
        while (line.length() < length) {
            line.append('a');
        }
        return line.toString();
    }

    @Test
    public void headerLineOfExactlyMaxLineLenBytesShouldBeAccepted() throws Exception {
        // 998 characters + CRLF: the longest line RFC 5322 allows
        MimeTokenStream stream = parse(line(998) + "\r\n\r\nbody\r\n", MimeConfig.DEFAULT);
        assertEquals(EntityState.T_START_HEADER, stream.next());
        assertEquals(EntityState.T_FIELD, stream.next());
        assertEquals(EntityState.T_END_HEADER, stream.next());
        assertEquals(EntityState.T_BODY, stream.next());
    }

    @Test
    public void lfTerminatedHeaderLineOfExactlyMaxLineLenBytesShouldBeAccepted() throws Exception {
        MimeTokenStream stream = parse("Subject: a\n\nbody\n", MimeConfig.custom().setMaxLineLen(11).build());
        assertEquals(EntityState.T_START_HEADER, stream.next());
        assertEquals(EntityState.T_FIELD, stream.next());
        assertEquals(EntityState.T_END_HEADER, stream.next());
    }

    @Test
    public void unterminatedLineOfExactlyMaxLineLenBytesShouldBeAccepted() throws Exception {
        // Measured as if it ended with a LF, as once written: 11
        MimeTokenStream stream = parse("Subject: a", MimeConfig.custom().setMaxLineLen(11).build());
        assertEquals(EntityState.T_START_HEADER, stream.next());
        assertEquals(EntityState.T_FIELD, stream.next());
        assertEquals("Subject", stream.getField().getName());
    }

    @Test
    public void headerLineLongerThanMaxLineLenShouldBeRejected() throws Exception {
        // 12 characters + CRLF, the terminator counting as one byte: 13
        MimeTokenStream stream = parse("Subject: abc\r\n\r\nbody\r\n", MimeConfig.custom().setMaxLineLen(12).build());
        stream.next();
        try {
            stream.next();
            fail("MimeException expected");
        } catch (MimeException expected) {
        }
    }

    @Test
    public void defaultConfigShouldRejectALineOf1000CharactersAndCrlf() throws Exception {
        MimeTokenStream stream = parse(line(1000) + "\r\n\r\nbody\r\n", MimeConfig.DEFAULT);
        stream.next();
        try {
            stream.next();
            fail("MimeException expected");
        } catch (MimeException expected) {
        }
    }

    @Test
    public void unterminatedLineLongerThanMaxLineLenShouldBeRejected() throws Exception {
        MimeTokenStream stream = parse("Subject: a", MimeConfig.custom().setMaxLineLen(10).build());
        stream.next();
        try {
            stream.next();
            fail("MimeException expected");
        } catch (MimeException expected) {
        }
    }

    private static MimeTokenStream atBody(String msg, MimeConfig config) throws Exception {
        MimeTokenStream stream = parse(msg, config);
        while (stream.getState() != EntityState.T_BODY) {
            stream.next();
        }
        return stream;
    }

    @Test
    public void zeroMaxContentLenShouldDisableTheCheck() throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
            + "preamble\r\n--b\r\n\r\npart\r\n--b--\r\nepilogue\r\n";
        MimeTokenStream stream = parse(msg, MimeConfig.custom().setMaxContentLen(0).build());
        int bodies = 0;
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
            if (state == EntityState.T_BODY) {
                assertEquals("part", new String(ContentUtil.buffer(stream.getInputStream()), StandardCharsets.US_ASCII));
                bodies++;
            }
        }
        assertEquals(1, bodies);
    }

    @Test
    public void emptyBodyShouldNotExceedAZeroLimit() throws Exception {
        MimeTokenStream stream = atBody("", MimeConfig.custom().setMaxContentLen(0).build());
        assertEquals(-1, stream.getInputStream().read());
    }

    @Test
    public void bodyOfExactlyMaxContentLenBytesShouldBeAccepted() throws Exception {
        MimeTokenStream stream = atBody("Subject: a\r\n\r\nhello", MimeConfig.custom().setMaxContentLen(5).build());
        assertEquals("hello", new String(ContentUtil.buffer(stream.getInputStream()), StandardCharsets.US_ASCII));
        assertEquals(EntityState.T_END_MESSAGE, stream.next());
    }

    @Test
    public void partsOfExactlyMaxContentLenBytesShouldBeSkippedByNext() throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
            + "01234\r\n--b\r\n\r\nabcde\r\n--b--\r\n";
        MimeTokenStream stream = parse(msg, MimeConfig.custom().setMaxContentLen(5).build());
        EntityState state = stream.getState();
        while (state != EntityState.T_END_OF_STREAM) {
            state = stream.next();
        }
    }

    @Test
    public void contentLimitShouldApplyToTheWholeBodyAcrossContentStreams() throws Exception {
        MimeTokenStream stream = atBody("Subject: a\r\n\r\nhello!", MimeConfig.custom().setMaxContentLen(5).build());
        byte[] buf = new byte[4];
        assertEquals(4, stream.getInputStream().read(buf));
        try {
            ContentUtil.buffer(stream.getInputStream());
            fail("the 6-byte body exceeds maxContentLen=5");
        } catch (IOException expected) {
        }
    }

    @Test
    public void contentLimitShouldApplyAcrossRawAndDecodedContentStreams() throws Exception {
        MimeTokenStream stream = atBody("Subject: a\r\n\r\nhello!", MimeConfig.custom().setMaxContentLen(5).build());
        assertEquals(4, stream.getInputStream().read(new byte[4]));
        try {
            ContentUtil.buffer(stream.getDecodedInputStream());
            fail("the 6-byte body exceeds maxContentLen=5");
        } catch (IOException expected) {
        }
    }

    @Test
    public void bodyOfExactlyTheLimitReadThroughSeveralStreamsShouldBeAccepted() throws Exception {
        MimeTokenStream stream = atBody("Subject: a\r\n\r\nhello", MimeConfig.custom().setMaxContentLen(5).build());
        assertEquals(4, stream.getInputStream().read(new byte[4]));
        assertEquals("o", new String(ContentUtil.buffer(stream.getInputStream()), StandardCharsets.US_ASCII));
    }

    @Test
    public void contentLimitShouldApplyWhenNextDrainsAPartlyReadPreamble() throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
            + "0123456789\r\n--b\r\n\r\nx\r\n--b--\r\n";
        MimeTokenStream stream = parse(msg, MimeConfig.custom().setMaxContentLen(8).build());
        while (stream.getState() != EntityState.T_PREAMBLE) {
            stream.next();
        }
        assertEquals(5, stream.getInputStream().read(new byte[5]));
        try {
            stream.next();
            fail("the 10-byte preamble exceeds maxContentLen=8");
        } catch (IOException | MimeException expected) {
        }
    }

    @Test
    public void eachPartShouldGetItsOwnContentLimit() throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
            + "01234\r\n--b\r\n\r\nabcde\r\n--b\r\n\r\nfghij\r\n--b--\r\nvwxyz";
        MimeTokenStream stream = parse(msg, MimeConfig.custom().setMaxContentLen(5).build());
        StringBuilder bodies = new StringBuilder();
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
            if (state == EntityState.T_BODY || state == EntityState.T_PREAMBLE || state == EntityState.T_EPILOGUE) {
                assertEquals(2, stream.getInputStream().read(new byte[2]));
                bodies.append(new String(ContentUtil.buffer(stream.getInputStream()), StandardCharsets.US_ASCII)).append('|');
            }
        }
        assertEquals("234|cde|hij|xyz|", bodies.toString());
    }

    @Test
    public void exceededContentLimitShouldBeReportedAsMimeException() throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
            + "0123456789\r\n--b\r\n\r\nx\r\n--b--\r\n";
        MimeTokenStream stream = parse(msg, MimeConfig.custom().setMaxContentLen(5).build());
        try {
            while (stream.next() != EntityState.T_END_OF_STREAM) {
            }
            fail("the preamble exceeds maxContentLen");
        } catch (MaxContentLengthLimitException expected) {
        }
    }

    @Test
    public void exceededContentLimitOnABodyReadShouldBeAMimeIOException() throws Exception {
        MimeTokenStream stream = atBody("Subject: a\r\n\r\nhello!", MimeConfig.custom().setMaxContentLen(5).build());
        try {
            ContentUtil.buffer(stream.getInputStream());
            fail("the 6-byte body exceeds maxContentLen=5");
        } catch (MimeIOException expected) {
            assertTrue(expected.getCause() instanceof MaxContentLengthLimitException);
        }
    }

    @Test
    public void fieldOfExactlyMaxHeaderLenBytesShouldBeAccepted() throws Exception {
        // "Subject: a" + its terminator, counting as one byte: 11
        for (String eol : new String[] {"\n", "\r\n"}) {
            MimeTokenStream stream = parse("Subject: a" + eol + eol + "body" + eol, MimeConfig.custom().setMaxHeaderLen(11).build());
            assertEquals(EntityState.T_START_HEADER, stream.next());
            assertEquals(EntityState.T_FIELD, stream.next());
            assertEquals("Subject", stream.getField().getName());
        }
    }

    @Test
    public void foldedFieldOfExactlyMaxHeaderLenBytesShouldBeAccepted() throws Exception {
        // "Subject: a" + "\r\n" + " b" + "\r\n": 10 + 1 + 2 + 1
        MimeTokenStream stream = parse("Subject: a\r\n b\r\n\r\nbody\r\n", MimeConfig.custom().setMaxHeaderLen(14).build());
        assertEquals(EntityState.T_START_HEADER, stream.next());
        assertEquals(EntityState.T_FIELD, stream.next());
        assertEquals("Subject", stream.getField().getName());
    }

    @Test
    public void fieldLongerThanMaxHeaderLenShouldBeRejected() throws Exception {
        MimeTokenStream stream = parse("Subject: a\r\n\r\nbody\r\n", MimeConfig.custom().setMaxHeaderLen(10).build());
        stream.next();
        try {
            stream.next();
            fail("MaxHeaderLengthLimitException expected");
        } catch (MaxHeaderLengthLimitException expected) {
        }
    }
}
