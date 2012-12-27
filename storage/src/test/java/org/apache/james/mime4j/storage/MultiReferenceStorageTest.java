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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultiReferenceStorageTest {

    @Test
    public void testForwardsGetInputStream() throws Exception {
        DummyStorage storage = new DummyStorage();
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);

        Assert.assertEquals(ByteArrayInputStream.class, multiReferenceStorage
                .getInputStream().getClass());
    }

    @Test
    public void testSingleReference() throws Exception {
        DummyStorage storage = new DummyStorage();
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);

        Assert.assertFalse(storage.deleted);

        multiReferenceStorage.delete();
        Assert.assertTrue(storage.deleted);
    }

    @Test
    public void testMultiReference() throws Exception {
        DummyStorage storage = new DummyStorage();
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);

        multiReferenceStorage.addReference();

        multiReferenceStorage.delete();
        Assert.assertFalse(storage.deleted);

        multiReferenceStorage.delete();
        Assert.assertTrue(storage.deleted);
    }

    @Test
    public void testGetInputStreamOnDeleted() throws Exception {
        DummyStorage storage = new DummyStorage();
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);

        multiReferenceStorage.delete();

        try {
            multiReferenceStorage.getInputStream();
            Assert.fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testAddReferenceOnDeleted() throws Exception {
        DummyStorage storage = new DummyStorage();
        MultiReferenceStorage multiReferenceStorage = new MultiReferenceStorage(
                storage);

        multiReferenceStorage.delete();

        try {
            multiReferenceStorage.addReference();
            Assert.fail();
        } catch (IllegalStateException expected) {
        }
    }

    private static final class DummyStorage implements Storage {
        public boolean deleted = false;

        public InputStream getInputStream() throws IOException {
            if (deleted)
                throw new IllegalStateException("deleted");

            return new ByteArrayInputStream("dummy".getBytes());
        }

        public void delete() {
            deleted = true;
        }
    }

}
