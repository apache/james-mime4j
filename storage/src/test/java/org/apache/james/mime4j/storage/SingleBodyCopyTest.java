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

package org.apache.james.mime4j.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.message.MessageImpl;
import org.junit.Assert;
import org.junit.Test;

public class SingleBodyCopyTest {

    @Test
    public void testCopyStorageBinaryBody() throws Exception {
        Storage storage = new MemoryStorageProvider()
                .store(new ByteArrayInputStream("test".getBytes()));
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);
        SingleBody body = new StorageBinaryBody(multiReferenceStorage);
        copyTest(body);
    }

    @Test
    public void testCopyStorageTextBody() throws Exception {
        Storage storage = new MemoryStorageProvider()
                .store(new ByteArrayInputStream("test".getBytes()));
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);
        SingleBody body = new StorageTextBody(multiReferenceStorage, Charsets.US_ASCII);
        copyTest(body);
    }

    @Test
    public void testCopyStringTextBody() throws Exception {
        SingleBody body = new StringTextBody("test", Charsets.US_ASCII);
        copyTest(body);
    }

    @Test
    public void testDisposeStorageBinaryBody() throws Exception {
        Storage storage = new MemoryStorageProvider()
                .store(new ByteArrayInputStream("test".getBytes()));
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);
        SingleBody body = new StorageBinaryBody(multiReferenceStorage);
        disposeTest(body, storage);
    }

    @Test
    public void testDisposeStorageTextBody() throws Exception {
        Storage storage = new MemoryStorageProvider()
                .store(new ByteArrayInputStream("test".getBytes()));
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);
        SingleBody body = new StorageTextBody(multiReferenceStorage, Charsets.US_ASCII);
        disposeTest(body, storage);
    }

    private void copyTest(SingleBody body) throws Exception {
        MessageImpl parent = new MessageImpl();
        parent.setBody(body);

        SingleBody copy = body.copy();
        Assert.assertNotNull(copy);
        Assert.assertNotSame(body, copy);

        Assert.assertSame(parent, body.getParent());
        Assert.assertNull(copy.getParent());

        sameContentTest(body, copy);
    }

    private void sameContentTest(SingleBody expectedBody, SingleBody actualBody)
            throws Exception {
        ByteArrayOutputStream expBaos = new ByteArrayOutputStream();
        expectedBody.writeTo(expBaos);
        byte[] expected = expBaos.toByteArray();

        ByteArrayOutputStream actBaos = new ByteArrayOutputStream();
        actualBody.writeTo(actBaos);
        byte[] actual = actBaos.toByteArray();

        Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i], actual[i]);
        }
    }

    private void disposeTest(SingleBody body, Storage storage) throws Exception {
        Assert.assertTrue(storageIsReadable(storage));

        SingleBody copy = body.copy();
        Assert.assertTrue(storageIsReadable(storage));

        body.dispose();
        Assert.assertTrue(storageIsReadable(storage));

        copy.dispose();
        Assert.assertFalse(storageIsReadable(storage));
    }

    private boolean storageIsReadable(Storage storage) throws Exception {
        try {
            storage.getInputStream().close();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

}
