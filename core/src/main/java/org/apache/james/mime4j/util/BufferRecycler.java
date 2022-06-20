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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * This is a small utility class, whose main functionality is to allow
 * simple reuse of raw byte/char buffers. It is usually used through
 * <code>ThreadLocal</code> member of the owning class pointing to
 * instance of this class through a <code>SoftReference</code>. The
 * end result is a low-overhead GC-cleanable recycling: hopefully
 * ideal for use by stream readers.
 *<p>
 * Rewritten in 2.10 to be thread-safe (see [jackson-core#479] for details),
 * to not rely on {@code ThreadLocal} access.
 */
public class BufferRecycler {
    protected final ArrayList<byte[]>[] _byteBuffers;
    protected final ArrayList<char[]>[] _charBuffers;
    protected final ArrayList<int[]> _intBuffers;

    /**
     * Default constructor used for creating instances of this default
     * implementation.
     */
    public BufferRecycler() {
        this(4, 4);
    }

    /**
     * Alternate constructor to be used by sub-classes, to allow customization
     * of number of low-level buffers in use.
     *
     * @param bbCount Number of {@code byte[]} buffers to allocate
     * @param cbCount Number of {@code char[]} buffers to allocate
     *
     * @since 2.4
     */
    protected BufferRecycler(int bbCount, int cbCount) {
        _byteBuffers = new ArrayList[bbCount];
        for (int i = 0; i < bbCount; i++) {
            _byteBuffers[i] = new ArrayList<>();
        }
        _charBuffers = new ArrayList[cbCount];
        for (int i = 0; i < cbCount; i++) {
            _charBuffers[i] = new ArrayList<>();
        }
        _intBuffers = new ArrayList<>();
    }
    
    /**
     * @param ix One of <code>READ_IO_BUFFER</code> constants.
     *
     * @return Buffer allocated (possibly recycled)
     */
    public final byte[] allocByteBuffer(int ix) {
        return allocByteBuffer(ix, 0);
    }

    public final int[] allocintBuffer(int minSize) {
        final int DEF_SIZE = 256;
        if (minSize < DEF_SIZE) {
            minSize = DEF_SIZE;
        }
        final ArrayList<int[]> buffers = _intBuffers;
        int[] buffer = null;
        if (buffers.size() > 0) {
            buffer = buffers.remove(buffers.size() -1);
        }
        if (buffer == null || buffer.length < minSize) {
            buffer = new int[minSize];
        }
        return buffer;
    }

    public byte[] allocByteBuffer(int ix, int minSize) {
        final int DEF_SIZE = 4000;
        if (minSize < DEF_SIZE) {
            minSize = DEF_SIZE;
        }
        final ArrayList<byte[]> buffers = _byteBuffers[ix];
        byte[] buffer = null;
        if (buffers.size() > 0) {
            buffer = buffers.remove(buffers.size() -1);
        }
        if (buffer == null || buffer.length < minSize) {
            buffer = balloc(minSize);
        }
        return buffer;
    }

    public void releaseByteBuffer(int ix, byte[] buffer) {
        if (buffer == null) {
            return;
        }
        _byteBuffers[ix].add(buffer);
    }

    public void releaseIntBuffer(int[] buffer) {
        if (buffer == null) {
            return;
        }
        _intBuffers.add(buffer);
    }
    
    public final char[] allocCharBuffer(int ix) {
        return allocCharBuffer(ix, 0);
    }

    public char[] allocCharBuffer(int ix, int minSize) {
        final int DEF_SIZE = 4000;
        if (minSize < DEF_SIZE) {
            minSize = DEF_SIZE;
        }
        final ArrayList<char[]> buffers = _charBuffers[ix];
        char[] buffer = null;
        if (buffers.size() > 0) {
            buffer = buffers.remove(buffers.size() -1);
        }
        if (buffer == null || buffer.length < minSize) {
            buffer = calloc(minSize);
        }
        return buffer;
    }

    public void releaseCharBuffer(int ix, char[] buffer) {
        _charBuffers[ix].add(buffer);
    }

    protected byte[] balloc(int size) {
        return new byte[size];
    }

    protected char[] calloc(int size) {
        return new char[size];
    }
}
