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

package org.apache.james.mime4j.samples.transform;

import java.util.Date;
import java.util.Random;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.BodyPartBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MultipartBuilder;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.storage.TempFileStorageProvider;

/**
 * This code should illustrate how to transform a message into another message
 * without modifying the original.
 */
public class TransformMessage {

    // Host name used in message identifiers.
    private static final String HOSTNAME = "localhost";

    public static void main(String[] args) throws Exception {
        // Explicitly set a strategy for storing body parts. Usually not
        // necessary; for most applications the default setting is appropriate.
        StorageProvider storageProvider = new TempFileStorageProvider();
        StorageBodyFactory bodyFactory = new StorageBodyFactory(storageProvider, DecodeMonitor.SILENT);

        // Create a template message. It would be possible to load a message
        // from an input stream but for this example a message object is created
        // from scratch for demonstration purposes.
        Message template = Message.Builder.of()
                .setBody(MultipartBuilder.create("mixed")
                        .addBodyPart(BodyPartBuilder.create()
                                .use(bodyFactory)
                                .setBody("This is the first part of the template..", Charsets.UTF_8)
                                .setContentTransferEncoding("quoted-printable")
                                .build())
                        .addBodyPart(BodyPartBuilder.create()
                                .use(bodyFactory)
                                .setBody(createRandomBinary(200), "application/octet-stream")
                                .setContentTransferEncoding("base64")
                                .build())
                        .addBodyPart(BodyPartBuilder.create()
                                .use(bodyFactory)
                                .setBody(createRandomBinary(300), "application/octet-stream")
                                .setContentTransferEncoding("base64")
                                .build())
                        .build())
                .setSubject("Template message")
                .build();

        // Create a new message by transforming the template.
        // Create a copy of the template. The copy can be modified without
        // affecting the original.
        final Message.Builder messageBuilder = Message.Builder.of(template);
        // In this example we know we have a multipart message. Use
        // Message#isMultipart() if uncertain.
        Multipart multipart = (Multipart) messageBuilder.getBody();

        // Insert a new text/plain body part after every body part of the
        // template.
        final int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            String text = "Text inserted after part " + (i + 1);
            BodyPart bodyPart = BodyPartBuilder.create()
                    .use(bodyFactory)
                    .setBody(text, Charsets.UTF_8)
                    .setContentTransferEncoding("quoted-printable")
                    .build();
            multipart.addBodyPart(bodyPart, 2 * i + 1);
        }

        // For no particular reason remove the second binary body part (now
        // at index four).
        Entity removed = multipart.removeBodyPart(4);

        // The removed body part no longer has a parent entity it belongs to so
        // it should be disposed of.
        removed.dispose();

        // Set some headers on the transformed message
        messageBuilder.generateMessageId(HOSTNAME);
        messageBuilder.setSubject("Transformed message");
        messageBuilder.setDate(new Date());
        messageBuilder.setFrom("John Doe <jdoe@machine.example>");

        Message transformed = messageBuilder.build();

        MessageWriter writer = new DefaultMessageWriter();

        // Print transformed message.
        System.out.println("\n\nTransformed message:\n--------------------\n");
        writer.writeMessage(transformed, System.out);

        // Messages should be disposed of when they are no longer needed.
        // Disposing of a message also disposes of all child elements (e.g. body
        // parts) of the message.
        transformed.dispose();

        // Print original message to illustrate that it was not affected by the
        // transformation.
        System.out.println("\n\nOriginal template:\n------------------\n");
        writer.writeMessage(template, System.out);

        // Original message is no longer needed.
        template.dispose();

        // At this point all temporary files have been deleted because all
        // messages and body parts have been disposed of properly.
    }

    private static byte[] createRandomBinary(int numberOfBytes) {
        byte[] data = new byte[numberOfBytes];
        new Random().nextBytes(data);
        return data;
    }

}
