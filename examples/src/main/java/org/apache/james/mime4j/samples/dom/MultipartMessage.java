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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.message.BodyPartBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MessageBuilder;
import org.apache.james.mime4j.message.MultipartBuilder;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;

/**
 * Creates a multipart/mixed message that consists of a text/plain and an
 * image/png part. The image is created on the fly; a similar technique can be
 * used to create PDF or XML attachments, for example.
 */
public class MultipartMessage {

    public static void main(String[] args) throws Exception {
        // 1) start with an empty message
        StorageBodyFactory bodyFactory = new StorageBodyFactory();
        Message message = MessageBuilder.create()
        // 2) set header fields
        //    Date and From are required fields
        //    Message-ID should be present
                .setFrom("John Doe <jdoe@machine.example>")
                .setTo("Mary Smith <mary@example.net>")
                .setSubject("An image for you")
                .setDate(new Date())
                .generateMessageId(InetAddress.getLocalHost().getCanonicalHostName())
        // 3) set a multipart body
                .setBody(MultipartBuilder.create("mixed")
                        // a multipart may have a preamble
                        .setPreamble("This is a multi-part message in MIME format.")
                        // first part is text/plain
                        .addBodyPart(BodyPartBuilder.create()
                                .use(bodyFactory)
                                .setBody("Why so serious?", Charsets.UTF_8)
                                .setContentTransferEncoding("quoted-printable")
                                .build())
                        // second part is image/png (image is created on the fly)
                        .addBodyPart(BodyPartBuilder.create()
                                .setBody(createImageBody(bodyFactory, renderSampleImage()))
                                .setContentType("image/png")
                                .setContentTransferEncoding("base64")
                        // Specify a filename in the Content-Disposition header (implicitly sets
                        // the disposition type to "attachment")
                                .setContentDisposition("attachment", "smiley.png")
                                .build())
                        .build())
        // setBody also sets the Content-Type header field
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

    /**
     * Creates a binary part from the specified image.
     */
    private static BinaryBody createImageBody(StorageBodyFactory bodyFactory,
            BufferedImage image) throws IOException {
        // Create a binary message body from the image
        StorageProvider storageProvider = bodyFactory.getStorageProvider();
        Storage storage = storeImage(storageProvider, image, "png");
        return bodyFactory.binaryBody(storage);
    }

    /**
     * Stores the specified image in a Storage object.
     */
    private static Storage storeImage(StorageProvider storageProvider,
            BufferedImage image, String formatName) throws IOException {
        // An output stream that is capable of building a Storage object.
        StorageOutputStream out = storageProvider.createStorageOutputStream();

        // Write the image to our output stream. A StorageOutputStream can be
        // used to create attachments using any API that supports writing a
        // document to an output stream, e.g. iText's PdfWriter.
        ImageIO.write(image, formatName, out);

        // Implicitly closes the output stream and returns the data that has
        // been written to it.
        return out.toStorage();
    }

    /**
     * Draws an image; unrelated to Mime4j.
     */
    private static BufferedImage renderSampleImage() {
        System.setProperty("java.awt.headless", "true");

        final int size = 100;

        BufferedImage img = new BufferedImage(size, size,
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D gfx = img.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setStroke(new BasicStroke(size / 40f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        gfx.setColor(Color.BLACK);
        gfx.setBackground(Color.WHITE);
        gfx.clearRect(0, 0, size, size);

        int b = size / 30;
        gfx.drawOval(b, b, size - 1 - 2 * b, size - 1 - 2 * b);

        int esz = size / 7;
        int ex = (int) (0.27f * size);
        gfx.drawOval(ex, ex, esz, esz);
        gfx.drawOval(size - 1 - esz - ex, ex, esz, esz);

        b = size / 5;
        gfx.drawArc(b, b, size - 1 - 2 * b, size - 1 - 2 * b, 200, 140);

        return img;
    }

}
