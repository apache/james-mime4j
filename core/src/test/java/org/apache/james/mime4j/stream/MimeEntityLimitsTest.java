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

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.io.MaxHeaderLimitException;
import org.apache.james.mime4j.io.MaxNestingDepthLimitException;
import org.apache.james.mime4j.io.MaxPartCountLimitException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the message wide limits bounding the number of MIME entities a message
 * may be made of and how deeply they may be nested.
 */
public class MimeEntityLimitsTest {

    /**
     * A multipart message made of <code>count</code> empty body parts. Each part
     * costs about 10 bytes on the wire but makes a consumer building an object
     * graph per part allocate far more than that.
     */
    private static InputStream flatMultipart(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Type: multipart/mixed; boundary=b\r\n\r\n");
        for (int i = 0; i < count; i++) {
            sb.append("--b\r\n\r\n");
        }
        sb.append("--b--\r\n");
        return new ByteArrayInputStream(sb.toString().getBytes(Charsets.US_ASCII));
    }

    /**
     * A message of nested multiparts, <code>depth</code> levels deep. The top
     * level message is at depth 1, so the innermost body part is at depth
     * <code>depth</code>.
     */
    private static InputStream nestedMultipart(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < depth; i++) {
            sb.append("Content-Type: multipart/mixed; boundary=b").append(i).append("\r\n\r\n");
            sb.append("--b").append(i).append("\r\n");
        }
        sb.append("\r\n");
        for (int i = depth - 1; i >= 1; i--) {
            sb.append("--b").append(i).append("--\r\n");
        }
        return new ByteArrayInputStream(sb.toString().getBytes(Charsets.US_ASCII));
    }

    private static int parse(MimeConfig config, InputStream in) throws Exception {
        MimeTokenStream stream = new MimeTokenStream(config);
        stream.parse(in);
        int parts = 0;
        for (EntityState state = stream.getState();
             state != EntityState.T_END_OF_STREAM;
             state = stream.next()) {
            if (state == EntityState.T_START_BODYPART) {
                parts++;
            }
        }
        return parts;
    }

    @Test
    public void partCountLimitShouldAcceptAMessageAtTheLimit() throws Exception {
        MimeConfig config = MimeConfig.custom().setMaxPartCount(3).build();
        Assert.assertEquals(3, parse(config, flatMultipart(3)));
    }

    @Test
    public void partCountLimitShouldRejectAMessageOverTheLimit() throws Exception {
        MimeConfig config = MimeConfig.custom().setMaxPartCount(3).build();
        try {
            parse(config, flatMultipart(4));
            Assert.fail("MaxPartCountLimitException expected");
        } catch (MaxPartCountLimitException expected) {
            Assert.assertEquals("Maximum part count limit (3) exceeded", expected.getMessage());
        }
    }

    @Test
    public void partCountLimitShouldBeDisabledWhenNegative() throws Exception {
        MimeConfig config = MimeConfig.custom().setMaxPartCount(-1).build();
        Assert.assertEquals(2000, parse(config, flatMultipart(2000)));
    }

    @Test
    public void partCountLimitShouldBeDisabledWhenZero() throws Exception {
        MimeConfig config = MimeConfig.custom().setMaxPartCount(0).build();
        Assert.assertEquals(2000, parse(config, flatMultipart(2000)));
    }

    @Test
    public void partCountLimitShouldBeDisabledOnACopyOfPermissive() throws Exception {
        MimeConfig config = MimeConfig.copy(MimeConfig.PERMISSIVE).setMaxPartCount(-1).build();
        Assert.assertEquals(-1, config.getMaxPartCount());
        Assert.assertEquals(2000, parse(config, flatMultipart(2000)));
    }

    @Test
    public void partCountLimitShouldCountEmbeddedMessages() throws Exception {
        // multipart/mixed holding one message/rfc822: the body part and the
        // embedded message are two entities.
        String message = "Content-Type: multipart/mixed; boundary=b\r\n\r\n"
                + "--b\r\n"
                + "Content-Type: message/rfc822\r\n\r\n"
                + "Subject: embedded\r\n\r\n"
                + "body\r\n"
                + "--b--\r\n";
        InputStream in = new ByteArrayInputStream(message.getBytes(Charsets.US_ASCII));
        try {
            parse(MimeConfig.custom().setMaxPartCount(1).build(), in);
            Assert.fail("MaxPartCountLimitException expected");
        } catch (MaxPartCountLimitException expected) {
            // expected
        }
    }

    @Test
    public void defaultConfigShouldAcceptAMessageWithinThePartCountDefault() throws Exception {
        Assert.assertEquals(512, parse(MimeConfig.DEFAULT, flatMultipart(512)));
    }

    @Test
    public void defaultConfigShouldBoundThePartCount() throws Exception {
        try {
            parse(MimeConfig.DEFAULT, flatMultipart(513));
            Assert.fail("MaxPartCountLimitException expected");
        } catch (MaxPartCountLimitException expected) {
            // expected
        }
    }

    @Test
    public void permissiveConfigShouldBoundThePartCount() throws Exception {
        try {
            parse(MimeConfig.PERMISSIVE, flatMultipart(513));
            Assert.fail("MaxPartCountLimitException expected");
        } catch (MaxPartCountLimitException expected) {
            // expected
        }
    }

    @Test
    public void nestingDepthLimitShouldAcceptAMessageAtTheLimit() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxNestingDepth(4)
                .setMaxPartCount(-1)
                .build();
        Assert.assertEquals(3, parse(config, nestedMultipart(4)));
    }

    @Test
    public void nestingDepthLimitShouldRejectAMessageOverTheLimit() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxNestingDepth(4)
                .setMaxPartCount(-1)
                .build();
        try {
            parse(config, nestedMultipart(5));
            Assert.fail("MaxNestingDepthLimitException expected");
        } catch (MaxNestingDepthLimitException expected) {
            Assert.assertEquals("Maximum nesting depth limit (4) exceeded", expected.getMessage());
        }
    }

    @Test
    public void nestingDepthLimitShouldBeDisabledWhenNegative() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxNestingDepth(-1)
                .setMaxPartCount(-1)
                .build();
        Assert.assertEquals(199, parse(config, nestedMultipart(200)));
    }

    @Test
    public void nestingDepthLimitShouldBeDisabledWhenZero() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxNestingDepth(0)
                .setMaxPartCount(0)
                .build();
        Assert.assertEquals(199, parse(config, nestedMultipart(200)));
    }

    @Test
    public void nestingDepthLimitShouldBeDisabledOnACopyOfPermissive() throws Exception {
        MimeConfig config = MimeConfig.copy(MimeConfig.PERMISSIVE)
                .setMaxNestingDepth(-1)
                .setMaxPartCount(-1)
                .build();
        Assert.assertEquals(-1, config.getMaxNestingDepth());
        Assert.assertEquals(199, parse(config, nestedMultipart(200)));
    }

    @Test
    public void defaultConfigShouldAcceptAMessageWithinTheNestingDepthDefault() throws Exception {
        MimeConfig config = MimeConfig.copy(MimeConfig.DEFAULT).setMaxPartCount(-1).build();
        Assert.assertEquals(63, parse(config, nestedMultipart(64)));
    }

    @Test
    public void defaultConfigShouldBoundTheNestingDepth() throws Exception {
        MimeConfig config = MimeConfig.copy(MimeConfig.DEFAULT).setMaxPartCount(-1).build();
        try {
            parse(config, nestedMultipart(65));
            Assert.fail("MaxNestingDepthLimitException expected");
        } catch (MaxNestingDepthLimitException expected) {
            // expected
        }
    }

    @Test
    public void limitsShouldBeResetBetweenParses() throws Exception {
        MimeConfig config = MimeConfig.custom().setMaxPartCount(3).build();
        MimeTokenStream stream = new MimeTokenStream(config);
        for (int i = 0; i < 3; i++) {
            stream.parse(flatMultipart(3));
            while (stream.getState() != EntityState.T_END_OF_STREAM) {
                stream.next();
            }
        }
    }

    @Test
    public void permissiveConfigShouldBoundTheHeaderCount() throws Exception {
        Assert.assertEquals(4096, MimeConfig.PERMISSIVE.getMaxHeaderCount());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4097; i++) {
            sb.append("x:\r\n");
        }
        sb.append("\r\nbody\r\n");
        InputStream in = new ByteArrayInputStream(sb.toString().getBytes(Charsets.US_ASCII));
        try {
            parse(MimeConfig.PERMISSIVE, in);
            Assert.fail("MaxHeaderLimitException expected");
        } catch (MaxHeaderLimitException expected) {
            // expected
        }
    }

    /** A multipart of {@code parts} body parts, each carrying {@code headers} fields. */
    private static InputStream multipartWithHeaders(int parts, int headers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Type: multipart/mixed; boundary=b\r\n\r\n");
        for (int p = 0; p < parts; p++) {
            sb.append("--b\r\n");
            for (int h = 0; h < headers; h++) {
                sb.append("x:\r\n");
            }
            sb.append("\r\n");
        }
        sb.append("--b--\r\n");
        return new ByteArrayInputStream(sb.toString().getBytes(Charsets.US_ASCII));
    }

    @Test
    public void totalHeaderLimitShouldAccumulateAcrossEntities() throws Exception {
        // 5 parts x 3 fields = 15 fields, none of which trips the per entity limit
        MimeConfig config = MimeConfig.custom()
                .setMaxHeaderCount(1000)
                .setMaxTotalHeaderCount(10)
                .build();
        try {
            parse(config, multipartWithHeaders(5, 3));
            Assert.fail("MaxHeaderLimitException expected");
        } catch (MaxHeaderLimitException expected) {
            Assert.assertEquals("Maximum total header limit (10) exceeded", expected.getMessage());
        }
    }

    @Test
    public void totalHeaderLimitShouldAcceptAMessageAtTheLimit() throws Exception {
        // 5 parts x 3 fields, plus the root message's own Content-Type field
        MimeConfig config = MimeConfig.custom()
                .setMaxHeaderCount(1000)
                .setMaxTotalHeaderCount(5 * 3 + 1)
                .build();
        Assert.assertEquals(5, parse(config, multipartWithHeaders(5, 3)));
    }

    @Test
    public void totalHeaderLimitShouldCountTheRootMessageFields() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxHeaderCount(1000)
                .setMaxTotalHeaderCount(5 * 3)
                .build();
        try {
            parse(config, multipartWithHeaders(5, 3));
            Assert.fail("MaxHeaderLimitException expected");
        } catch (MaxHeaderLimitException expected) {
            // the root Content-Type is the 16th field
        }
    }

    @Test
    public void totalHeaderLimitShouldBeDisabledWhenNegative() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxHeaderCount(-1)
                .setMaxTotalHeaderCount(-1)
                .build();
        Assert.assertEquals(50, parse(config, multipartWithHeaders(50, 100)));
    }

    @Test
    public void totalHeaderLimitShouldBeDisabledWhenZero() throws Exception {
        MimeConfig config = MimeConfig.custom()
                .setMaxHeaderCount(-1)
                .setMaxTotalHeaderCount(0)
                .build();
        Assert.assertEquals(50, parse(config, multipartWithHeaders(50, 100)));
    }

    @Test
    public void defaultConfigShouldBoundTheTotalHeaderCount() throws Exception {
        Assert.assertEquals(16384, MimeConfig.DEFAULT.getMaxTotalHeaderCount());
        Assert.assertEquals(16384, MimeConfig.PERMISSIVE.getMaxTotalHeaderCount());
        try {
            // 400 parts x 100 fields = 40000 fields, only 100 per entity
            parse(MimeConfig.PERMISSIVE, multipartWithHeaders(400, 100));
            Assert.fail("MaxHeaderLimitException expected");
        } catch (MaxHeaderLimitException expected) {
            // expected
        }
    }

    @Test
    public void copyShouldCarryTheLimitsOver() {
        MimeConfig config = MimeConfig.copy(MimeConfig.custom()
                .setMaxPartCount(7)
                .setMaxNestingDepth(9)
                .setMaxTotalHeaderCount(11)
                .build()).build();
        Assert.assertEquals(7, config.getMaxPartCount());
        Assert.assertEquals(9, config.getMaxNestingDepth());
        Assert.assertEquals(11, config.getMaxTotalHeaderCount());
    }
}
