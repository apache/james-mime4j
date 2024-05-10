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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.Message;
import org.junit.Test;

public class DefaultMessageWriterTest {

    @Test
    public void asBytesShouldSerializeTheMessage() throws Exception {
        byte[] bytes = DefaultMessageWriter.asBytes(
            Message.Builder.of()
                .setBody("this is the body", Charsets.UTF_8)
                .setFrom("sender@localhost")
                .setTo("receiver@localhost")
                .setSubject("Cool subject")
                .build());

        assertThat(new String(bytes, Charsets.UTF_8.name()))
            .isEqualTo("MIME-Version: 1.0\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "From: sender@localhost\r\n" +
                "To: receiver@localhost\r\n" +
                "Subject: Cool subject\r\n" +
                "\r\n" +
                "this is the body");
    }
    
    @Test
    public void shouldThrowOnHeaderInjectionAttempt() throws Exception {
        Message.Builder builder = Message.Builder.of()
            .setBody("this is the body", Charsets.UTF_8)
            .setFrom("sender@localhost");

        assertThatThrownBy(() -> builder.setContentTransferEncoding("victim@attacker.com\r\nReply-To: attacker@evil.com"))
            .isInstanceOf(IllegalArgumentException.class);
    }

}