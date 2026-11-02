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

package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;
import org.junit.Assert;
import org.junit.Test;

/** Requalification of closed issues on origin/master, written from their titles. */
public class RequalificationCoreTest {

    private static List<String> bodies(String msg) throws Exception {
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        List<String> bodies = new ArrayList<>();
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
            if (state == EntityState.T_BODY) {
                bodies.add(new String(ContentUtil.buffer(stream.getInputStream()), StandardCharsets.US_ASCII));
            }
        }
        return bodies;
    }

    // #29 a quoted boundary that looks like an encoded word is not decoded
    @Test
    public void issue29_encodedWordLookalikeBoundaryShouldBeTakenLiterally() throws Exception {
        String[] boundaries = {"=?utf-8?q?x?=", "=?utf-8?b?eA==?=", "=?ISO-8859-1?Q?x?=", "a=?utf-8?q?x?=b"};
        for (String boundary : boundaries) {
            for (String header : new String[] {"Content-Type: multipart/mixed; boundary=\"" + boundary + "\"",
                "Content-Type: multipart/mixed;\r\n boundary=\"" + boundary + "\"",
                "Content-Type: multipart/mixed; BOUNDARY=\"" + boundary + "\""}) {
                String msg = header + "\r\n\r\n"
                    + "--x\r\n\r\nwrong\r\n"
                    + "--" + boundary + "\r\n\r\nright\r\n"
                    + "--" + boundary + "--\r\n";
                List<String> bodies = bodies(msg);
                Assert.assertEquals(msg, 1, bodies.size());
                Assert.assertEquals(msg, "right", bodies.get(0));
            }
        }
    }

    // #49 formatDate with an offset that has seconds denotes the same instant
    @Test
    public void issue49_formatDateShouldDenoteTheInstantForOffsetsWithSeconds() {
        String[] zones = {"Europe/Paris", "Africa/Abidjan", "Europe/Amsterdam", "America/New_York", "Asia/Kolkata",
            "Europe/Dublin", "Asia/Kathmandu", "Pacific/Chatham", "Africa/Monrovia", "America/Caracas"};
        long[] instants = {-2208988800000L, -2524521600000L, -1893456000000L, -1830383032000L, -3000000000000L, 1700000000123L};
        StringBuilder failures = new StringBuilder();
        for (String zone : zones) {
            for (long instant : instants) {
                Date date = new Date(instant);
                String formatted = MimeUtil.formatDate(date, TimeZone.getTimeZone(zone));
                long back = ZonedDateTime.parse(formatted, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
                if (back != instant / 1000 * 1000) {
                    failures.append(zone).append(' ').append(date.toInstant()).append(" -> ").append(formatted)
                        .append(" (").append((back - instant) / 1000).append(" s off)\n");
                }
            }
        }
        Assert.assertEquals("", failures.toString());
    }
}
