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

import java.io.IOException;
import java.util.Date;

import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.field.address.ParseException;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MimeWriter;
import org.apache.james.mime4j.storage.StorageBodyFactory;

/**
 * This example generates a message very similar to the one from RFC 5322
 * Appendix A.1.1.
 */
public class TextPlainMessage {
    public static void main(String[] args) throws IOException, ParseException {
        // 1) start with an empty message

        MessageImpl message = new MessageImpl();

        // 2) set header fields

        // Date and From are required fields
        message.setDate(new Date());
        message.setFrom(AddressBuilder.DEFAULT.parseMailbox("John Doe <jdoe@machine.example>"));

        // Message-ID should be present
        message.createMessageId("machine.example");

        // set some optional fields
        message.setTo(AddressBuilder.DEFAULT.parseMailbox("Mary Smith <mary@example.net>"));
        message.setSubject("Saying Hello");

        // 3) set a text body

        StorageBodyFactory bodyFactory = new StorageBodyFactory();
        TextBody body = bodyFactory.textBody("This is a message just to "
                + "say hello.\r\nSo, \"Hello\".");

        // note that setText also sets the Content-Type header field
        message.setText(body);

        // 4) print message to standard output

        MessageWriter writer = new MimeWriter();
        writer.writeMessage(message, System.out);

        // 5) message is no longer needed and should be disposed of

        message.dispose();
    }
}
