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
import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.SingleBody;
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
}
