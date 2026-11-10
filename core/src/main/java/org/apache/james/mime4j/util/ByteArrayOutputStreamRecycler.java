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
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

public class ByteArrayOutputStreamRecycler {
    /**
     * Reference counted handle on a recycled buffer: the buffer goes back to the recycler
     * once, when the last reference is released.
     */
    public static class Wrapper {
        private static final AtomicIntegerFieldUpdater<Wrapper> REFERENCES =
            AtomicIntegerFieldUpdater.newUpdater(Wrapper.class, "references");

        private final ByteArrayOutputStreamRecycler recycler;
        private final UnsynchronizedByteArrayOutputStream value;
        private volatile int references = 1;

        public Wrapper(ByteArrayOutputStreamRecycler recycler, UnsynchronizedByteArrayOutputStream value) {
            this.recycler = recycler;
            this.value = value;
        }

        /**
         * Registers an additional owner of the underlying buffer, which then needs one more
         * {@link #release()} before being recycled.
         *
         * @throws IllegalStateException if the buffer has already been released: it may already
         *   be in use by another owner
         */
        public Wrapper retain() {
            for (;;) {
                int current = references;
                if (current <= 0) {
                    throw new IllegalStateException("buffer has been released");
                }
                if (REFERENCES.compareAndSet(this, current, current + 1)) {
                    return this;
                }
            }
        }

        public void release() {
            for (;;) {
                int current = references;
                if (current <= 0) {
                    // Already recycled: recycling it twice would hand it to two later owners
                    return;
                }
                if (REFERENCES.compareAndSet(this, current, current - 1)) {
                    if (current == 1) {
                        recycler.release(value);
                    }
                    return;
                }
            }
        }

        /**
         * @throws IllegalStateException if the buffer has been released: it may then hold the
         *   content of another owner
         */
        public UnsynchronizedByteArrayOutputStream getValue() {
            if (references <= 0) {
                throw new IllegalStateException("buffer has been released");
            }
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
        if (value != null && BufferRecycler.ENABLED) {
            value.reset();
            buffers.offer(value);
        }
    }
}
