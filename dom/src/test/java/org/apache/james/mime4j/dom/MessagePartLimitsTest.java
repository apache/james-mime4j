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

package org.apache.james.mime4j.dom;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.io.MaxNestingDepthLimitException;
import org.apache.james.mime4j.io.MaxPartCountLimitException;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Building a DOM allocates an object graph per MIME entity, so a small message
 * made of a huge number of tiny parts is an amplification vector. These tests
 * pin down that the message wide entity limits bound that work, including under
 * {@link MimeConfig#PERMISSIVE} which is what mail servers typically use on
 * untrusted inbound traffic.
 */
public class MessagePartLimitsTest {

    private static InputStream flatMultipart(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("Subject: hi\r\n");
        sb.append("Content-Type: multipart/mixed; boundary=b\r\n\r\n");
        for (int i = 0; i < count; i++) {
            sb.append("--b\r\n\r\n");
        }
        sb.append("--b--\r\n");
        return new ByteArrayInputStream(sb.toString().getBytes(Charsets.US_ASCII));
    }

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

    private static DefaultMessageBuilder builder(MimeConfig config) {
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(config);
        return builder;
    }

    @Test
    public void parseMessageShouldRejectTooManyPartsWithPermissiveConfig() throws Exception {
        try {
            builder(MimeConfig.PERMISSIVE).parseMessage(flatMultipart(50000));
            Assert.fail("MimeIOException expected");
        } catch (MimeIOException e) {
            Assert.assertTrue(e.getCause() instanceof MaxPartCountLimitException);
        }
    }

    @Test
    public void parseMessageShouldRejectTooDeeplyNestedPartsWithPermissiveConfig() throws Exception {
        MimeConfig config = MimeConfig.copy(MimeConfig.PERMISSIVE).setMaxPartCount(-1).build();
        try {
            builder(config).parseMessage(nestedMultipart(5000));
            Assert.fail("MimeIOException expected");
        } catch (MimeIOException e) {
            Assert.assertTrue(e.getCause() instanceof MaxNestingDepthLimitException);
        }
    }

    @Test
    public void parseMessageShouldAcceptAMessageWithinTheLimits() throws Exception {
        Message message = builder(MimeConfig.PERMISSIVE).parseMessage(flatMultipart(512));
        Assert.assertEquals(512, ((Multipart) message.getBody()).getCount());
    }
}
