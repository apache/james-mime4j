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
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.io.MaxHeaderLengthLimitException;
import org.junit.Assert;
import org.junit.Test;

/**
 * A header line is not buffered beyond maxHeaderLen, even when maxLineLen does not bound it
 * (as in {@link MimeConfig#PERMISSIVE}).
 */
public class HeaderLineBoundTest {

    private static final int MAX_HEADER_LEN = 64 * 1024;
    private static final long TEN_MB = 10L * 1024 * 1024;

    /**
     * Serves <code>size</code> bytes of 'a' with no line feed, and counts what is read.
     */
    private static class LongLine extends InputStream {
        private final long size;
        private long served;

        LongLine(long size) {
            this.size = size;
        }

        @Override
        public int read() {
            if (served >= size) {
                return -1;
            }
            served++;
            return 'a';
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (len == 0) {
                return 0;
            }
            if (served >= size) {
                return -1;
            }
            int n = (int) Math.min(len, size - served);
            for (int i = 0; i < n; i++) {
                b[off + i] = 'a';
            }
            served += n;
            return n;
        }
    }

    private static InputStream ascii(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII));
    }

    private static void parseAll(MimeConfig config, InputStream in) throws Exception {
        MimeTokenStream stream = new MimeTokenStream(config);
        stream.parse(in);
        for (EntityState state = stream.getState();
             state != EntityState.T_END_OF_STREAM;
             state = stream.next()) {
            // just walk the stream
        }
    }

    private static void assertRejectedEarly(InputStream prefix) throws Exception {
        LongLine line = new LongLine(TEN_MB);
        try {
            parseAll(MimeConfig.PERMISSIVE, new SequenceInputStream(prefix, line));
            Assert.fail("Expected MaxHeaderLengthLimitException");
        } catch (MaxHeaderLengthLimitException expected) {
            // a few read buffers past the limit at most, not the whole line
            Assert.assertTrue("read " + line.served + " bytes of the line", line.served < MAX_HEADER_LEN + 64 * 1024);
        }
    }

    @Test
    public void permissiveShouldStopReadingAHugeMessageHeaderLineAtTheHeaderLimit() throws Exception {
        assertRejectedEarly(ascii("X-A: "));
    }

    @Test
    public void permissiveShouldStopReadingAHugeFoldedHeaderLineAtTheHeaderLimit() throws Exception {
        assertRejectedEarly(ascii("X-A: b\r\n "));
    }

    @Test
    public void permissiveShouldStopReadingAHugeBodyPartHeaderLineAtTheHeaderLimit() throws Exception {
        assertRejectedEarly(ascii("Content-Type: multipart/mixed; boundary=b\r\n\r\n--b\r\nX-A: "));
    }

    private static MimeConfig headerLimit(int maxHeaderLen) {
        return MimeConfig.custom().setMaxLineLen(-1).setMaxHeaderLen(maxHeaderLen).build();
    }

    private static String field(int len) {
        StringBuilder sb = new StringBuilder("X-A: ");
        while (sb.length() < len) {
            sb.append('a');
        }
        return sb.toString();
    }

    @Test
    public void aHeaderOfExactlyTheLimitShouldStillBeAccepted() throws Exception {
        // measured with a single LF: 99 bytes + LF
        parseAll(headerLimit(100), ascii(field(99) + "\r\n\r\nbody"));
        parseAll(headerLimit(100), ascii(field(99) + "\n\nbody"));
        // unterminated at the end of the input
        parseAll(headerLimit(100), ascii(field(100)));
    }

    @Test
    public void aHeaderOneByteOverTheLimitShouldBeRejected() throws Exception {
        String[] inputs = {field(100) + "\r\n\r\nbody", field(100) + "\n\nbody", field(101)};
        for (String input : inputs) {
            try {
                parseAll(headerLimit(100), ascii(input));
                Assert.fail("Expected MaxHeaderLengthLimitException for " + input.length() + " bytes");
            } catch (MaxHeaderLengthLimitException expected) {
                // expected
            }
        }
    }

    @Test
    public void bodyLinesShouldStayUnboundedUnderPermissive() throws Exception {
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.PERMISSIVE);
        LongLine body = new LongLine(1024 * 1024);
        stream.parse(new SequenceInputStream(ascii("X-A: b\r\n\r\n"), body));
        for (EntityState state = stream.getState();
             state != EntityState.T_END_OF_STREAM;
             state = stream.next()) {
            if (state == EntityState.T_BODY) {
                InputStream in = stream.getInputStream();
                byte[] buf = new byte[8192];
                while (in.read(buf) != -1) {
                    // drain
                }
            }
        }
        Assert.assertEquals(1024 * 1024, body.served);
    }
}
