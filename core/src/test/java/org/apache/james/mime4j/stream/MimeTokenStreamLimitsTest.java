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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.MimeException;
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
}
