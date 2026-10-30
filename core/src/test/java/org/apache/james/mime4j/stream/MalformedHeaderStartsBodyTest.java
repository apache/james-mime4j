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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class MalformedHeaderStartsBodyTest {
    private static final MimeConfig CONFIG = MimeConfig.custom().setMalformedHeaderStartsBody(true).build();

    private static List<String> events(String message) throws Exception {
        MimeTokenStream stream = new MimeTokenStream(CONFIG);
        stream.parse(new ByteArrayInputStream(message.getBytes(StandardCharsets.US_ASCII)));
        List<String> out = new ArrayList<>();
        for (EntityState s = stream.getState(); s != EntityState.T_END_OF_STREAM; s = stream.next()) {
            if (s == EntityState.T_BODY || s == EntityState.T_PREAMBLE || s == EntityState.T_EPILOGUE) {
                out.add(s + " " + new String(IOUtils.toByteArray(stream.getInputStream()), StandardCharsets.US_ASCII));
            } else {
                out.add(s.toString());
            }
        }
        return out;
    }

    @Test
    public void malformedHeaderShouldStartTheBodyOfAMessage() throws Exception {
        List<String> events = events("Subject: x\r\nthis header line has no colon\r\nbody\r\n");

        Assert.assertEquals(events.toString(), "T_BODY this header line has no colon\r\nbody\r\n", events.get(events.size() - 2));
    }

    @Test
    public void malformedHeaderShouldStartThePreambleOfAMultipart() throws Exception {
        List<String> events = events("Content-Type: multipart/mixed; boundary=b\r\n"
            + "this header line has no colon\r\n"
            + "\r\n"
            + "--b\r\nContent-Type: text/plain\r\n\r\nx\r\n--b--\r\n");

        Assert.assertTrue(events.toString(), events.contains("T_PREAMBLE this header line has no colon\r\n"));
        Assert.assertTrue(events.toString(), events.contains("T_BODY x"));
    }

    @Test
    public void stopAfterAMalformedHeaderShouldNotBreakNext() throws Exception {
        String boundary = "0123456789012345678901234567890123456789012345678901234567890ab";
        String message = "Content-Type: multipart/mixed; boundary=" + boundary + "\r\n"
            + "Content-Ty the body i\r\n"
            + "x\r\n";
        MimeTokenStream stream = new MimeTokenStream(CONFIG);
        stream.parse(new ByteArrayInputStream(message.getBytes(StandardCharsets.US_ASCII)));

        List<EntityState> states = new ArrayList<>();
        for (EntityState s = stream.getState(); s != EntityState.T_END_OF_STREAM; s = stream.next()) {
            states.add(s);
            if (s == EntityState.T_END_HEADER) {
                stream.stop();
            }
        }
        Assert.assertEquals(EntityState.T_END_MESSAGE, states.get(states.size() - 1));
    }
}
