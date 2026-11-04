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

package org.apache.james.mime4j.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.stream.MimeConfig;
import org.junit.Test;

public class DefaultMessageBuilderParseHeaderTest {

    @Test
    public void parseHeaderShouldStopOnAMultipartLongerThanTheBuffer() throws Exception {
        StringBuilder msg = new StringBuilder()
            .append("Subject: test\r\n")
            .append("Content-Type: multipart/mixed; boundary=b\r\n")
            .append("\r\n")
            .append("--b\r\n")
            .append("Content-Type: text/plain\r\n")
            .append("\r\n");
        while (msg.length() < 64 * 1024) {
            msg.append("some content line\r\n");
        }
        msg.append("--b--\r\n");

        Header header = new DefaultMessageBuilder()
            .parseHeader(new ByteArrayInputStream(msg.toString().getBytes(StandardCharsets.US_ASCII)));

        assertThat(header.getField("Subject").getBody()).isEqualTo("test");
        assertThat(header.getFields()).hasSize(2);
    }

    private static String twoPartMessage(int firstBodyLines) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < firstBodyLines; i++) {
            body.append("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\n");
        }
        return "From: a@example.org\nContent-Type: multipart/mixed; boundary=b1\n\n"
            + "--b1\nX-Part: one\n\n" + body + "--b1\nX-Part: two\n\nsecond body\n--b1--\n";
    }

    /** Returns at most 8 bytes per read, like a socket. */
    private static InputStream chunked(final byte[] msg) {
        return new InputStream() {
            private int delivered;

            @Override
            public int read() {
                return delivered < msg.length ? msg[delivered++] & 0xff : -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (delivered >= msg.length) {
                    return -1;
                }
                int n = Math.min(Math.min(len, 8), msg.length - delivered);
                System.arraycopy(msg, delivered, b, off, n);
                delivered += n;
                return n;
            }
        };
    }

    @Test(timeout = 10000)
    public void parseHeaderShouldReturnOnAMultipartOfSeveralPartsLargerThanTheBuffer() throws Exception {
        Header header = new DefaultMessageBuilder()
            .parseHeader(new ByteArrayInputStream(twoPartMessage(50).getBytes(StandardCharsets.US_ASCII)));

        assertThat(header.getFields()).hasSize(2);
    }

    @Test(timeout = 10000)
    public void parseHeaderShouldReturnOnAMultipartReadInChunks() throws Exception {
        Header header = new DefaultMessageBuilder()
            .parseHeader(chunked(twoPartMessage(1).getBytes(StandardCharsets.US_ASCII)));

        assertThat(header.getFields()).hasSize(2);
    }

    @Test(timeout = 10000)
    public void strictParseHeaderShouldReturnOnAMultipartReadInChunks() throws Exception {
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(MimeConfig.STRICT);

        Header header = builder.parseHeader(chunked(twoPartMessage(50).getBytes(StandardCharsets.US_ASCII)));

        assertThat(header.getFields()).hasSize(2);
    }
}
