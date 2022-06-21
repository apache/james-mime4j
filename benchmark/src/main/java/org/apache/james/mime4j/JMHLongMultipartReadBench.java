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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.field.LenientFieldParser;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.SimpleContentHandler;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.util.NullOutputStream;

public class JMHLongMultipartReadBench {
    private static final byte[] CONTENT = loadMessage("long-multipart.msg");
    private static final byte[] CONTENT_HEADERS = loadMessage("long-headers.msg");
    private static final byte[] BUFFER = new byte[4096];
    private static final DefaultMessageBuilder MESSAGE_BUILDER = new DefaultMessageBuilder();

    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
            .include(this.getClass().getName() + ".benchmark*")
            .mode(Mode.AverageTime)
            .addProfiler(GCProfiler.class)
            .timeUnit(TimeUnit.MICROSECONDS)
            .warmupTime(TimeValue.seconds(5))
            .warmupIterations(5)
            .measurementTime(TimeValue.seconds(5))
            .measurementIterations(10)
            .threads(1)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }

    private static byte[] loadMessage(String resourceName) {
        try {
            ClassLoader cl = JMHLongMultipartReadBench.class.getClassLoader();

            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            try (InputStream instream = cl.getResourceAsStream(resourceName)) {
                ContentUtil.copy(instream, outstream);
            }

            return outstream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void benchmark1(Blackhole bh) throws Exception{
            MimeTokenStream stream = new MimeTokenStream();
            stream.parse(new ByteArrayInputStream(CONTENT));
            for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream
                .next()) {
            }
            stream.stop();
    }

    @Benchmark
    public void benchmark2(Blackhole bh) throws Exception{
        ContentHandler contentHandler = new AbstractContentHandler() {
        };

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(contentHandler);
        parser.parse(new ByteArrayInputStream(CONTENT));
        parser.stop();
    }

    @Benchmark
    public void benchmark3(Blackhole bh) throws Exception{
        ContentHandler contentHandler = new SimpleContentHandler() {
            @Override
            public void body(BodyDescriptor bd, InputStream is)
                throws IOException {
                while (is.read(BUFFER) != -1) ;
            }

            @Override
            public void headers(Header header) {
            }
        };

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentDecoding(true);
        parser.setContentHandler(contentHandler);
        parser.parse(new ByteArrayInputStream(CONTENT));
        parser.stop();
    }

    @Benchmark
    public void benchmark4(Blackhole bh) throws Exception{
        Message message = MESSAGE_BUILDER.parseMessage(new ByteArrayInputStream(CONTENT));
        bh.consume(message);
        message.dispose();
    }

    @Benchmark
    public void benchmark4headers(Blackhole bh) throws Exception{
        Message message = MESSAGE_BUILDER.parseMessage(new ByteArrayInputStream(CONTENT_HEADERS));
        bh.consume(message);
        message.dispose();
    }

    @Benchmark
    public void benchmark5(Blackhole bh) throws Exception{
        Message message = MESSAGE_BUILDER.parseMessage(new ByteArrayInputStream(CONTENT));
        new DefaultMessageWriter().writeMessage(message, new NullOutputStream());
        bh.consume(message);
        message.dispose();
    }

    @Benchmark
    public void benchmark7(Blackhole bh) throws Exception{
        Message message = Message.Builder.of()
            .use(new LenientFieldParser())
            .setDate(new Date())
            .setSubject("Test email")
            .setFrom("btellier@apache.org")
            .setTo("other@apache.org")
            .setBody("Body of the message", StandardCharsets.US_ASCII)
            .build();
        new DefaultMessageWriter().writeMessage(message, new NullOutputStream());
        bh.consume(message);
        message.dispose();
    }

    @Benchmark
    public void benchmark6(Blackhole bh) throws Exception{
        Header header = MESSAGE_BUILDER.parseHeader(new ByteArrayInputStream(CONTENT_HEADERS));
        bh.consume(header);
    }
}
