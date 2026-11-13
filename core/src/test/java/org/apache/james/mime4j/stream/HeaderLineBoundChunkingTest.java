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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.james.mime4j.MimeException;
import org.junit.Assert;
import org.junit.Test;

public class HeaderLineBoundChunkingTest {

    /** read(byte[],int,int) returns at most the next size of the list, cycling. */
    private static class Chunked extends InputStream {
        private final byte[] data;
        private final int[] sizes;
        private int pos;
        private int next;

        Chunked(byte[] data, int... sizes) {
            this.data = data;
            this.sizes = sizes;
        }

        @Override
        public int read() {
            return pos < data.length ? data[pos++] & 0xff : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (len == 0) {
                return 0;
            }
            if (pos >= data.length) {
                return -1;
            }
            int n = Math.min(Math.min(len, sizes[next++ % sizes.length]), data.length - pos);
            System.arraycopy(data, pos, b, off, n);
            pos += n;
            return n;
        }
    }

    private static List<String> events(MimeConfig config, InputStream in) throws IOException {
        List<String> events = new ArrayList<>();
        MimeTokenStream stream = new MimeTokenStream(config);
        stream.parse(in);
        try {
            for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
                events.add(state == EntityState.T_FIELD ? "T_FIELD " + stream.getField().getName() : state.name());
            }
        } catch (MimeException e) {
            events.add(e.getClass().getSimpleName());
        }
        return events;
    }

    private static byte[] message(String head, int longLineLength, String tail) {
        StringBuilder sb = new StringBuilder(head).append("X-Long: ");
        while (sb.length() < head.length() + longLineLength - 2) {
            sb.append('a');
        }
        sb.append("\r\n").append(tail);
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    public void overLongHeaderLineGivesTheSameEventsWhateverTheReadSizes() throws Exception {
        byte[] message = message("Subject: a\r\n", 13000, "\r\nbody");
        MimeConfig config = MimeConfig.copy(MimeConfig.DEFAULT).setMaxLineLen(-1).build(); // maxHeaderLen 10000

        List<String> whole = events(config, new ByteArrayInputStream(message));
        // 12: the first line alone; then the read crossing 10001 bytes of the second line also holds its LF
        List<String> chunked = events(config, new Chunked(message, 12, 4096, 4096, 1000, 4096));
        List<String> byteByByte = events(config, new Chunked(message, 1));

        Assert.assertEquals(Arrays.asList("T_START_MESSAGE", "T_START_HEADER", "MaxHeaderLengthLimitException"), whole);
        Assert.assertEquals(whole, chunked);
        Assert.assertEquals(whole, byteByByte);
    }

    @Test
    public void overLongHeaderLineInABodyPartGivesTheSameEventsWhateverTheReadSizes() throws Exception {
        byte[] message = message("Content-Type: multipart/mixed; boundary=b\r\n\r\n--b\r\nSubject: a\r\n",
            13000, "\r\nbody\r\n--b--\r\n");
        // the long line starts after the message header
        MimeConfig config = MimeConfig.copy(MimeConfig.DEFAULT).setMaxLineLen(-1).build();

        List<String> whole = events(config, new ByteArrayInputStream(message));
        List<String> chunked = events(config, new Chunked(message, 45, 4, 12, 4096, 4096, 1000, 4096));

        Assert.assertEquals(whole, chunked);
        Assert.assertEquals("MaxHeaderLengthLimitException", whole.get(whole.size() - 1));
        Assert.assertFalse(whole.toString(), whole.contains("T_FIELD Subject"));
    }

    @Test
    public void headerLineOfExactlyMaxHeaderLenIsStillAccepted() throws Exception {
        MimeConfig config = MimeConfig.copy(MimeConfig.DEFAULT).setMaxLineLen(-1).setMaxHeaderLen(100).build();
        // 99 bytes and CRLF: 100 bytes once the CRLF counts as one
        byte[] message = message("", 101, "\r\nbody");

        List<String> whole = events(config, new ByteArrayInputStream(message));
        Assert.assertTrue(whole.toString(), whole.contains("T_FIELD X-Long"));
        Assert.assertEquals(whole, events(config, new Chunked(message, 50, 51, 1)));
    }
}
