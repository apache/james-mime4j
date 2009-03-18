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

package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.SimpleContentHandler;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.parser.MimeTokenStream;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.MemoryStorageProvider;

public class LongMultipartReadBench {

    public static void main(String[] args) throws Exception {

        byte[] content = loadMessage("long-multipart.msg");
        if (content == null) {
            System.err.println("Test message not found");
            return;
        }

        int testNumber = args.length > 0 ? Integer.parseInt(args[0]) : 0;

        Test test = createTest(testNumber);
        if (test == null) {
            System.err.println("No such test: " + testNumber);
            return;
        }

        int repetitions = args.length > 1 ? Integer.parseInt(args[1]) : 25000;

        System.out.println("Multipart message read.");
        System.out.println("No of repetitions: " + repetitions);
        System.out.println("Content length: " + content.length);
        System.out.println("Test: " + test.getClass().getSimpleName());
        
        System.out.print("Warmup... ");
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < 1500) {
            test.run(content, 10);
        }
        System.out.println("done");

        System.out.println("--------------------------------");

        long start = System.currentTimeMillis();
        test.run(content, repetitions);
        long finish = System.currentTimeMillis();

        double seconds = (finish - start) / 1000.0;
        double mb = content.length * repetitions / 1024.0 / 1024;
        System.out.printf("Execution time: %f sec\n", seconds);
        System.out.printf("%.2f messages/sec\n", repetitions / seconds);
        System.out.printf("%.2f mb/sec\n", mb / seconds);
    }

    private static Test createTest(int testNumber) {
        switch (testNumber) {
        case 0:
            return new MimeTokenStreamTest();
        case 1:
            return new AbstractContentHandlerTest();
        case 2:
            return new SimpleContentHandlerTest();
        case 3:
            return new MessageTest();
        default:
            return null;
        }
    }

    private static byte[] loadMessage(String resourceName) throws IOException {
        ClassLoader cl = LongMultipartReadBench.class.getClassLoader();

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        InputStream instream = cl.getResourceAsStream(resourceName);
        if (instream == null) {
            return null;
        }
        try {
            CodecUtil.copy(instream, outstream);
        } finally {
            instream.close();
        }

        return outstream.toByteArray();
    }

    private interface Test {
        void run(byte[] content, int repetitions) throws Exception;
    }

    private static final class MimeTokenStreamTest implements Test {
        public void run(byte[] content, int repetitions) throws Exception {
            MimeTokenStream stream = new MimeTokenStream();
            for (int i = 0; i < repetitions; i++) {
                stream.parse(new ByteArrayInputStream(content));
                for (int state = stream.getState(); state != MimeTokenStream.T_END_OF_STREAM; state = stream
                        .next()) {
                }
            }
        }
    }

    private static final class AbstractContentHandlerTest implements Test {
        public void run(byte[] content, int repetitions) throws Exception {
            ContentHandler contentHandler = new AbstractContentHandler() {
            };

            for (int i = 0; i < repetitions; i++) {
                MimeStreamParser parser = new MimeStreamParser();
                parser.setContentHandler(contentHandler);
                parser.parse(new ByteArrayInputStream(content));
            }
        }
    }

    private static final class SimpleContentHandlerTest implements Test {
        public void run(byte[] content, int repetitions) throws Exception {
            ContentHandler contentHandler = new SimpleContentHandler() {
                @Override
                public void bodyDecoded(BodyDescriptor bd, InputStream is)
                        throws IOException {
                }

                @Override
                public void headers(Header header) {
                }
            };

            for (int i = 0; i < repetitions; i++) {
                MimeStreamParser parser = new MimeStreamParser();
                parser.setContentHandler(contentHandler);
                parser.parse(new ByteArrayInputStream(content));
            }
        }
    }

    private static final class MessageTest implements Test {
        public void run(byte[] content, int repetitions) throws Exception {
            DefaultStorageProvider.setInstance(new MemoryStorageProvider());

            for (int i = 0; i < repetitions; i++) {
                new Message(new ByteArrayInputStream(content));
            }
        }
    }

    /*
    // requires mail.jar and activation.jar to be present
    private static final class MimeMessageTest implements Test {
        public void run(byte[] content, int repetitions) throws Exception {
            for (int i = 0; i < repetitions; i++) {
                MimeMessage mm = new MimeMessage(null, new ByteArrayInputStream(content));
                Multipart multipart = (Multipart) mm.getContent();
                multipart.getCount(); // force parsing
            }
        }
    }
    */

}
