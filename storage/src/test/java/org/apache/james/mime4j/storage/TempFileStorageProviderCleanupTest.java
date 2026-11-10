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

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

/**
 * A deleted temporary storage leaves nothing registered behind it, unlike
 * {@link File#deleteOnExit()} whose registrations stay in memory until the JVM stops.
 */
public class TempFileStorageProviderCleanupTest {

    private static File fileOf(Storage storage) throws Exception {
        Field field = storage.getClass().getDeclaredField("file");
        field.setAccessible(true);
        return (File) field.get(storage);
    }

    @Test
    public void deletedStoragesShouldLeaveNothingRegistered() throws Exception {
        TempFileStorageProvider provider = new TempFileStorageProvider();
        int before = TempFileStorageProvider.liveFileCount();
        for (int i = 0; i < 1000; i++) {
            StorageOutputStream out = provider.createStorageOutputStream();
            out.write(new byte[] {1, 2, 3});
            Storage storage = out.toStorage();
            File file = fileOf(storage);
            Assert.assertTrue(file.exists());
            Assert.assertTrue(TempFileStorageProvider.isRegistered(file));

            storage.delete();

            Assert.assertFalse(file.exists());
            Assert.assertFalse(TempFileStorageProvider.isRegistered(file));
        }
        Assert.assertEquals(before, TempFileStorageProvider.liveFileCount());
    }

    @Test
    public void deletingTwiceShouldBeHarmless() throws Exception {
        StorageOutputStream out = new TempFileStorageProvider().createStorageOutputStream();
        out.write(new byte[] {1});
        Storage storage = out.toStorage();
        File file = fileOf(storage);
        storage.delete();
        storage.delete();
        Assert.assertFalse(file.exists());
        Assert.assertFalse(TempFileStorageProvider.isRegistered(file));
        try {
            storage.getInputStream();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    @Test
    public void aFileRemovedBehindTheStorageShouldStillBeUnregistered() throws Exception {
        StorageOutputStream out = new TempFileStorageProvider().createStorageOutputStream();
        out.write(new byte[] {1});
        Storage storage = out.toStorage();
        File file = fileOf(storage);
        Assert.assertTrue(file.delete());
        storage.delete();
        Assert.assertFalse(TempFileStorageProvider.isRegistered(file));
    }
}
