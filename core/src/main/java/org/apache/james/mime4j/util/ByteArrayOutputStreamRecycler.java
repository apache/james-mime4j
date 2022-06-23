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

package org.apache.james.mime4j.util;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

public class ByteArrayOutputStreamRecycler {
    public static class Wrapper {
        private final ByteArrayOutputStreamRecycler recycler;
        private final UnsynchronizedByteArrayOutputStream value;

        public Wrapper(ByteArrayOutputStreamRecycler recycler, UnsynchronizedByteArrayOutputStream value) {
            this.recycler = recycler;
            this.value = value;
        }

        public void release() {
            recycler.release(value);
        }

        public UnsynchronizedByteArrayOutputStream getValue() {
            return value;
        }
    }

    protected final ConcurrentLinkedQueue<UnsynchronizedByteArrayOutputStream> buffers;

    public ByteArrayOutputStreamRecycler() {
        buffers = new ConcurrentLinkedQueue<>();
    }

    public Wrapper allocOutputStream() {
        UnsynchronizedByteArrayOutputStream result = buffers.poll();
        if (result == null) {
            result = new UnsynchronizedByteArrayOutputStream();
        }
        return new Wrapper(this, result);
    }

    private void release(UnsynchronizedByteArrayOutputStream value) {
        if (value != null) {
            value.reset();
            buffers.offer(value);
        }
    }
}
