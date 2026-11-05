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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.io.MaxContentLengthLimitException;
import org.junit.Assert;
import org.junit.Test;

public class MimeTokenStreamContentLimitTest {
    private static final String TAIL = "QQ==\n"
        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n"
        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n"
        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n"
        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n"
        + "--b--\n";
    private static final String MESSAGE = "Content-Type: multipart/mixed; boundary=b\n\n"
        + "--b\nContent-Transfer-Encoding: base64\n\n" + TAIL;
    private static final String ENCAPSULATED = "Content-Type: multipart/mixed; boundary=b\n\n"
        + "--b\nContent-Type: message/rfc822\nContent-Transfer-Encoding: base64\n\n" + TAIL;

    private static byte[] bytes(String message) {
        return message.getBytes(StandardCharsets.US_ASCII);
    }

    private static void parse(InputStream source) throws IOException, MimeException {
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setMaxContentLen(100).build());
        stream.setRecursionMode(RecursionMode.M_NO_RECURSE);
        stream.parse(source);
        byte[] buffer = new byte[256];
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
            if (state == EntityState.T_BODY) {
                InputStream body = stream.getDecodedInputStream();
                while (body.read(buffer) != -1) {
                    // drain
                }
            }
        }
    }

    private static InputStream oneByteAtATime(byte[] bytes) {
        return new FilterInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, 1));
            }
        };
    }

    private static String outcome(InputStream source, int maxContentLen) throws IOException {
        MimeTokenStream stream = new MimeTokenStream(MimeConfig.custom().setMaxContentLen(maxContentLen).build());
        stream.parse(source);
        try {
            while (stream.next() != EntityState.T_END_OF_STREAM) {
                // the drain happens in next()
            }
            return "completed";
        } catch (MimeException e) {
            return e.getClass().getSimpleName();
        }
    }

    @Test(expected = MaxContentLengthLimitException.class)
    public void partOverTheLimitShouldFailWhenReadAtOnce() throws Exception {
        parse(new ByteArrayInputStream(bytes(MESSAGE)));
    }

    @Test(expected = MaxContentLengthLimitException.class)
    public void partOverTheLimitShouldFailWhenReadByteByByte() throws Exception {
        parse(oneByteAtATime(bytes(MESSAGE)));
    }

    @Test
    public void encodedEncapsulatedMessageShouldNotDependOnReadSizes() throws Exception {
        Assert.assertEquals("completed", outcome(new ByteArrayInputStream(bytes(ENCAPSULATED)), 100));
        Assert.assertEquals("completed", outcome(oneByteAtATime(bytes(ENCAPSULATED)), 100));
    }

    @Test
    public void encodedEncapsulatedMessageShouldNotDependOnItsPositionInTheBuffer() throws Exception {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 97; i++) {
            header.append("X-Long: 0123456789012345678901234567890\n");
        }
        byte[] message = bytes(header + ENCAPSULATED);

        Assert.assertEquals("completed", outcome(new ByteArrayInputStream(message), 100));
    }

    @Test
    public void unreadBodyOfAnEncapsulatedMessageShouldStillBeLimited() throws Exception {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            body.append("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n");
        }
        String plain = "Content-Type: multipart/mixed; boundary=b\n\n"
            + "--b\nContent-Type: message/rfc822\n\nSubject: a\n\n" + body + "--b--\n";

        Assert.assertEquals("MaxContentLengthLimitException", outcome(new ByteArrayInputStream(bytes(plain)), 100));
        Assert.assertEquals("MaxContentLengthLimitException", outcome(oneByteAtATime(bytes(plain)), 100));
    }
}
