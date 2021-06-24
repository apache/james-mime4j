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

import java.net.InetAddress;
import java.util.Date;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MessageBuilder;

/**
 * This example generates a message very similar to the one from RFC 5322
 * Appendix A.1.1.
 */
public class TextPlainMessage {
    public static void main(String[] args) throws Exception {
        // 1) start with an empty message
        Message message = Message.Builder.of()
        // 2) set header fields
        //    Date and From are required fields
        //    Message-ID should be present
                .setFrom("John Doe <jdoe@machine.example>")
                .setTo("Mary Smith <mary@example.net>")
                .setSubject("Saying Hello")
                .setDate(new Date())
                .generateMessageId(InetAddress.getLocalHost().getCanonicalHostName())
                .setBody("This is a message just to say hello.\r\nSo, \"Hello\".", Charsets.ISO_8859_1)
                .build();
        try {
        // 4) print message to standard output
            MessageWriter writer = new DefaultMessageWriter();
            writer.writeMessage(message, System.out);
        } finally {
        // 5) message is no longer needed and should be disposed of
            message.dispose();
        }
    }
}
