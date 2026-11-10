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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.junit.Assert;
import org.junit.Test;

public class BasicBodyFactoryCopyTest {
    private static Message parse(String s) throws Exception {
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setBodyFactory(new BasicBodyFactory());
        return builder.parseMessage(new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII)));
    }

    private static String body(Message m) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ((SingleBody) m.getBody()).writeTo(out);
        return out.toString("US-ASCII");
    }

    @Test
    public void disposingACopyShouldNotChangeTheOriginal() throws Exception {
        Message original = parse("Content-Type: text/plain\r\n\r\nhello\r\n");
        Message copy = new DefaultMessageBuilder().copy(original);
        copy.dispose();
        Message other = parse("Content-Type: text/plain\r\n\r\nsomeone else's mail\r\n");

        Assert.assertEquals("hello\r\n", body(original));
        Assert.assertEquals("someone else's mail\r\n", body(other));
    }

    @Test
    public void disposingACopyShouldNotChangeTheOriginalBinaryBody() throws Exception {
        Message original = parse("Content-Type: application/octet-stream\r\n\r\nhello\r\n");
        Message copy = new DefaultMessageBuilder().copy(original);
        copy.dispose();
        Message other = parse("Content-Type: application/octet-stream\r\n\r\nsomeone else's mail\r\n");

        Assert.assertEquals("hello\r\n", body(original));
        Assert.assertEquals("someone else's mail\r\n", body(other));
    }

    @Test
    public void disposingOriginalAndCopyShouldNotMakeTwoLaterBodiesShareABuffer() throws Exception {
        Message original = parse("Content-Type: text/plain\r\n\r\nhello\r\n");
        Message copy = new DefaultMessageBuilder().copy(original);
        original.dispose();
        copy.dispose();
        Message first = parse("Content-Type: text/plain\r\n\r\nfirst\r\n");
        Message second = parse("Content-Type: text/plain\r\n\r\nsecond\r\n");

        Assert.assertEquals("first\r\n", body(first));
        Assert.assertEquals("second\r\n", body(second));
    }

    @Test
    public void disposingTwiceShouldNotMakeTwoLaterBodiesShareABuffer() throws Exception {
        Message original = parse("Content-Type: text/plain\r\n\r\nhello\r\n");
        original.dispose();
        original.dispose();
        Message first = parse("Content-Type: text/plain\r\n\r\nfirst\r\n");
        Message second = parse("Content-Type: text/plain\r\n\r\nsecond\r\n");

        Assert.assertEquals("first\r\n", body(first));
        Assert.assertEquals("second\r\n", body(second));
    }

    private interface BodyAccess {
        void run(SingleBody body) throws Exception;
    }

    private static void assertDisposed(SingleBody body, BodyAccess access) throws Exception {
        try {
            access.run(body);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // the body no longer reaches its recycled buffer
        }
    }

    private static void assertDisposedBodyDoesNotServeTheNextMessage(String contentType) throws Exception {
        Message original = parse("Content-Type: " + contentType + "\r\n\r\nhello\r\n");
        SingleBody body = (SingleBody) original.getBody();
        original.dispose();
        Message other = parse("Content-Type: " + contentType + "\r\n\r\nsomeone else's mail\r\n");

        assertDisposed(body, b -> b.getInputStream());
        assertDisposed(body, b -> b.writeTo(new ByteArrayOutputStream()));
        assertDisposed(body, b -> b.size());
        assertDisposed(body, b -> b.copy());
        if (body instanceof TextBody) {
            assertDisposed(body, b -> ((TextBody) b).getReader());
        }
        Assert.assertEquals("someone else's mail\r\n", body(other));
    }

    @Test
    public void aDisposedTextBodyShouldNotServeTheNextMessage() throws Exception {
        assertDisposedBodyDoesNotServeTheNextMessage("text/plain");
    }

    @Test
    public void aDisposedBinaryBodyShouldNotServeTheNextMessage() throws Exception {
        assertDisposedBodyDoesNotServeTheNextMessage("application/octet-stream");
    }

    @Test
    public void copyAfterDisposeShouldNotShareTheNextMessageBuffer() throws Exception {
        Message original = parse("Content-Type: text/plain\r\n\r\nhello\r\n");
        SingleBody body = (SingleBody) original.getBody();
        original.dispose();

        assertDisposed(body, b -> b.copy());
        Message other = parse("Content-Type: text/plain\r\n\r\nsomeone else's mail\r\n");
        Assert.assertEquals("someone else's mail\r\n", body(other));
    }

    @Test
    public void aStreamOpenedBeforeDisposeShouldKeepItsContent() throws Exception {
        Message original = parse("Content-Type: text/plain\r\n\r\nhello\r\n");
        InputStream in = ((SingleBody) original.getBody()).getInputStream();
        original.dispose();
        parse("Content-Type: text/plain\r\n\r\nsomeone else's mail\r\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        Assert.assertEquals("hello\r\n", out.toString("US-ASCII"));
    }
}
