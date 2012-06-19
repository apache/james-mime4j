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
package org.apache.james.mime4j.samples.mbox;

import com.google.common.base.Charsets;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.mboxiterator.CharBufferWrapper;
import org.apache.james.mime4j.mboxiterator.MboxIterator;
import org.apache.james.mime4j.message.DefaultMessageBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetEncoder;

/**
 * Simple example of how to use Apache Mime4j Mbox Iterator. We split one mbox file file into
 * individual email messages.
 */
public class IterateOverMbox {

    private final static CharsetEncoder ENCODER = Charsets.UTF_8.newEncoder();

    // simple example of how to split an mbox into individual files
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Please supply a path to an mbox file to parse");
        }

        final File mbox = new File(args[0]);
        long start = System.currentTimeMillis();
        int count = 0;

        for (CharBufferWrapper message : MboxIterator.fromFile(mbox).charset(Charsets.UTF_8).build()) {
            // saveMessageToFile(count, buf);
            System.out.println(messageSummary(message.asInputStreamUTF8Encoded()));
            count++;
        }
        System.out.println("Found " + count + " messages");
        long end = System.currentTimeMillis();
        System.out.println("Done in: " + (end - start) + " milis");
    }

    private static void saveMessageToFile(int count, CharBuffer buf) throws IOException {
        FileOutputStream fout = new FileOutputStream(new File("target/messages/msg-" + count));
        FileChannel fileChannel = fout.getChannel();
        ByteBuffer buf2 = ENCODER.encode(buf);
        fileChannel.write(buf2);
        fileChannel.close();
        fout.close();
    }

    private static String messageSummary(InputStream messageBytes) throws IOException, MimeException {
        MessageBuilder builder = new DefaultMessageBuilder();
        Message message = builder.parseMessage(messageBytes);
        return String.format("\nMessage %s \n" +
                "Sent by:\t%s\n" +
                "To:\t%s\n",
                message.getSubject(),
                message.getSender(),
                message.getTo());
    }
}
