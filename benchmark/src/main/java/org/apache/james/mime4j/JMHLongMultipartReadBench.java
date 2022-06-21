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
import java.util.concurrent.TimeUnit;

import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
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
    private static final byte[] BUFFER = new byte[4096];

    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
            // Specify which benchmarks to run.
            // You can be more specific if you'd like to run only one benchmark per test.
            .include(this.getClass().getName() + ".*")
            // Set the following options as needed
            .mode (Mode.AverageTime)
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
            InputStream instream = cl.getResourceAsStream(resourceName);
            if (instream == null) {
                return null;
            }
            try {
                ContentUtil.copy(instream, outstream);
            } finally {
                instream.close();
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
        MessageBuilder builder = new DefaultMessageBuilder();
        Message message = builder.parseMessage(new ByteArrayInputStream(CONTENT));
        message.dispose();
    }

    @Benchmark
    public void benchmark5(Blackhole bh) throws Exception{
        MessageBuilder builder = new DefaultMessageBuilder();
        Message message = builder.parseMessage(new ByteArrayInputStream(CONTENT));
        new DefaultMessageWriter().writeMessage(message, new NullOutputStream());
        message.dispose();
    }
}
