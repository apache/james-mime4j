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

package org.apache.james.mime4j.samples.dom;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.MimeConfig;

public class ParsingMessage {
    private static String MESSAGE = "MIME-Version: 1.0\r\n" +
        "From: John Doe <jdoe@machine.example>\r\n" +
        "To: Mary Smith <mary@example.net>\r\n" +
        "Subject: Saying Hello\r\n" +
        "Date: Thu, 24 Jun 2021 14:04:28 +0700\r\n" +
        "Message-ID:\r\n" +
        " <Mime4j.0.8520adcfe13bd088.17a3cd59575@interview1-HP-ProBook-440-G6>\r\n" +
        "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
        "\r\n" +
        "This is a message just to say hello.\r\n" +
        "So, \"Hello\".\r\n";

    public static void main(String[] args) throws Exception {
        DefaultMessageBuilder defaultMessageBuilder = new DefaultMessageBuilder();
        defaultMessageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);

        Message message = defaultMessageBuilder.parseMessage(new ByteArrayInputStream(MESSAGE.getBytes(StandardCharsets.UTF_8)));

        System.out.println("The subject is " + message.getSubject());

        TextBody textBody = (TextBody) message.getBody();
        System.out.println("------------------------------");
        System.out.println("Content: " + IOUtils.toString(textBody.getInputStream(), textBody.getMimeCharset()));
        System.out.println("------------------------------");
    }
}
