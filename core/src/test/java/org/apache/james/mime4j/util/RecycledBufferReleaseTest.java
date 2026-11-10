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

import org.junit.Assert;
import org.junit.Test;

/**
 * A released recycled buffer is never reached again through its former owner.
 */
public class RecycledBufferReleaseTest {

    @Test
    public void retainAfterTheLastReleaseShouldBeRefused() {
        ByteArrayOutputStreamRecycler recycler = new ByteArrayOutputStreamRecycler();
        ByteArrayOutputStreamRecycler.Wrapper wrapper = recycler.allocOutputStream();
        wrapper.release();
        try {
            wrapper.retain();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    @Test
    public void getValueAfterTheLastReleaseShouldBeRefused() {
        ByteArrayOutputStreamRecycler recycler = new ByteArrayOutputStreamRecycler();
        ByteArrayOutputStreamRecycler.Wrapper wrapper = recycler.allocOutputStream();
        wrapper.retain();
        wrapper.release();
        Assert.assertNotNull(wrapper.getValue());
        wrapper.release();
        try {
            wrapper.getValue();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    @Test
    public void releasingTwiceShouldRecycleTheBufferOnce() {
        ByteArrayOutputStreamRecycler recycler = new ByteArrayOutputStreamRecycler();
        ByteArrayOutputStreamRecycler.Wrapper wrapper = recycler.allocOutputStream();
        wrapper.release();
        wrapper.release();
        Assert.assertTrue(recycler.buffers.size() <= 1);
    }

    @Test
    public void aReleasedByteArrayBufferShouldNotWriteIntoTheRecycledArray() {
        BufferRecycler recycler = new BufferRecycler();
        RecycledByteArrayBuffer buffer = new RecycledByteArrayBuffer(recycler, 16);
        byte[] recycled = buffer.buffer();
        buffer.release();
        buffer.append(new byte[] {'a', 'b'}, 0, 2);

        Assert.assertNotSame(recycled, buffer.buffer());
        Assert.assertEquals("ab", buffer.toString());
        Assert.assertEquals(0, recycled[0]);
    }
}
