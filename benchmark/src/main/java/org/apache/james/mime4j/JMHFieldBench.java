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

import java.util.concurrent.TimeUnit;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.field.AddressListFieldImpl;
import org.apache.james.mime4j.field.AddressListFieldLenientImpl;
import org.apache.james.mime4j.field.ContentDispositionFieldImpl;
import org.apache.james.mime4j.field.ContentDispositionFieldLenientImpl;
import org.apache.james.mime4j.field.ContentTypeFieldImpl;
import org.apache.james.mime4j.field.ContentTypeFieldLenientImpl;
import org.apache.james.mime4j.field.DateTimeFieldImpl;
import org.apache.james.mime4j.field.DateTimeFieldLenientImpl;
import org.apache.james.mime4j.stream.RawField;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class JMHFieldBench {
    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
            .include(this.getClass().getName() + ".*")
            .mode (Mode.AverageTime)
            .timeUnit(TimeUnit.MICROSECONDS)
            .warmupTime(TimeValue.seconds(5))
            .warmupIterations(3)
            .measurementTime(TimeValue.seconds(2))
            .measurementIterations(5)
            .threads(1)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void dateLenient(Blackhole bh) {
        bh.consume(DateTimeFieldLenientImpl.PARSER.parse(new RawField("Date", "Tue, 26 Apr 2022 02:27:54 +0000"), DecodeMonitor.SILENT)
            .getDate());
    }

    @Benchmark
    public void dateStrict(Blackhole bh) {
        bh.consume(DateTimeFieldImpl.PARSER.parse(new RawField("Date", "Tue, 26 Apr 2022 02:27:54 +0000"), DecodeMonitor.SILENT)
            .getDate());
    }

    @Benchmark
    public void mailboxLenient(Blackhole bh) {
        bh.consume(AddressListFieldLenientImpl.PARSER.parse(new RawField("From", "Me <me@example.com>, \"You\" <you@exemple.com>"), DecodeMonitor.SILENT).getAddressList());
    }

    @Benchmark
    public void mailboxStrict(Blackhole bh) {
        bh.consume(AddressListFieldImpl.PARSER.parse(new RawField("From", "Me <me@example.com>, \"You\" <you@exemple.com>"), DecodeMonitor.SILENT).getAddressList());
    }

    @Benchmark
    public void contentTypeLenient(Blackhole bh) {
        bh.consume(ContentTypeFieldLenientImpl.PARSER.parse(new RawField("Content-Type", "multipart/mixed; boundary=\"------------090404080405080108000909\""), DecodeMonitor.SILENT).getBoundary());
    }

    @Benchmark
    public void contentTypeLenientFolded(Blackhole bh) {
        bh.consume(ContentTypeFieldLenientImpl.PARSER.parse(new RawField("Content-Type", "multipart/mixed;\r\n boundary=\"------------090404080405080108000909\""), DecodeMonitor.SILENT).getBoundary());
    }

    @Benchmark
    public void contentTypeStrictFolded(Blackhole bh) {
        bh.consume(ContentTypeFieldImpl.PARSER.parse(new RawField("Content-Type", "multipart/mixed;\r\n boundary=\"------------090404080405080108000909\""), DecodeMonitor.SILENT).getBoundary());
    }

    @Benchmark
    public void contentTypeStrict(Blackhole bh) {
        bh.consume(ContentTypeFieldImpl.PARSER.parse(new RawField("Content-Type", "multipart/mixed; boundary=\"------------090404080405080108000909\""), DecodeMonitor.SILENT).getBoundary());
    }

    @Benchmark
    public void contentDispositionLenient(Blackhole bh) {
        bh.consume(ContentDispositionFieldLenientImpl.PARSER.parse(new RawField("Content-Disposition", "attachment; filename=blob.png"), DecodeMonitor.SILENT).getFilename());
    }

    @Benchmark
    public void contentDispositionStrict(Blackhole bh) {
        bh.consume(ContentDispositionFieldImpl.PARSER.parse(new RawField("Content-Disposition", "attachment; filename=blob.png"), DecodeMonitor.SILENT).getFilename());
    }
}
