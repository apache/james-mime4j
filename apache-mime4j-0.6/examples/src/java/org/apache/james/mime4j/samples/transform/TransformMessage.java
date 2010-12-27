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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
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
        DefaultStorageProvider.setInstance(storageProvider);

        // Create a template message. It would be possible to load a message
        // from an input stream but for this example a message object is created
        // from scratch for demonstration purposes.
        Message template = createTemplate();

        // Create a new message by transforming the template.
        Message transformed = transform(template);

        // Print transformed message.
        System.out.println("\n\nTransformed message:\n--------------------\n");
        transformed.writeTo(System.out);

        // Messages should be disposed of when they are no longer needed.
        // Disposing of a message also disposes of all child elements (e.g. body
        // parts) of the message.
        transformed.dispose();

        // Print original message to illustrate that it was not affected by the
        // transformation.
        System.out.println("\n\nOriginal template:\n------------------\n");
        template.writeTo(System.out);

        // Original message is no longer needed.
        template.dispose();

        // At this point all temporary files have been deleted because all
        // messages and body parts have been disposed of properly.
    }

    /**
     * Copies the given message and makes some arbitrary changes to the copy.
     */
    private static Message transform(Message original) throws IOException {
        // Create a copy of the template. The copy can be modified without
        // affecting the original.
        Message message = new Message(original);

        // In this example we know we have a multipart message. Use
        // Message#isMultipart() if uncertain.
        Multipart multipart = (Multipart) message.getBody();

        // Insert a new text/plain body part after every body part of the
        // template.
        final int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            String text = "Text inserted after part " + (i + 1);
            BodyPart bodyPart = createTextPart(text);
            multipart.addBodyPart(bodyPart, 2 * i + 1);
        }

        // For no particular reason remove the second binary body part (now
        // at index four).
        BodyPart removed = multipart.removeBodyPart(4);

        // The removed body part no longer has a parent entity it belongs to so
        // it should be disposed of.
        removed.dispose();

        // Set some headers on the transformed message
        message.createMessageId(HOSTNAME);
        message.setSubject("Transformed message");
        message.setDate(new Date());
        message.setFrom(Mailbox.parse("John Doe <jdoe@machine.example>"));

        return message;
    }

    /**
     * Creates a multipart/mixed message that consists of three parts (one text,
     * two binary).
     */
    private static Message createTemplate() throws IOException {
        Multipart multipart = new Multipart("mixed");

        BodyPart part1 = createTextPart("This is the first part of the template..");
        multipart.addBodyPart(part1);

        BodyPart part2 = createRandomBinaryPart(200);
        multipart.addBodyPart(part2);

        BodyPart part3 = createRandomBinaryPart(300);
        multipart.addBodyPart(part3);

        Message message = new Message();
        message.setMultipart(multipart);

        message.setSubject("Template message");

        return message;
    }

    /**
     * Creates a text part from the specified string.
     */
    private static BodyPart createTextPart(String text) {
        TextBody body = new BodyFactory().textBody(text, "UTF-8");

        BodyPart bodyPart = new BodyPart();
        bodyPart.setText(body);
        bodyPart.setContentTransferEncoding("quoted-printable");

        return bodyPart;
    }

    /**
     * Creates a binary part with random content.
     */
    private static BodyPart createRandomBinaryPart(int numberOfBytes)
            throws IOException {
        byte[] data = new byte[numberOfBytes];
        new Random().nextBytes(data);

        Body body = new BodyFactory()
                .binaryBody(new ByteArrayInputStream(data));

        BodyPart bodyPart = new BodyPart();
        bodyPart.setBody(body, "application/octet-stream");
        bodyPart.setContentTransferEncoding("base64");

        return bodyPart;
    }

}
