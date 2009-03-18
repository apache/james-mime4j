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
import java.io.IOException;

import org.apache.james.mime4j.codec.CodecUtil;

import junit.framework.TestCase;

public class StorageProviderTest extends TestCase {

    public void testMemoryStorageProvider() throws Exception {
        StorageProvider provider = new MemoryStorageProvider();

        testReadWrite(provider, 0);
        testReadWrite(provider, 1);
        testReadWrite(provider, 1024);
        testReadWrite(provider, 20000);

        testDelete(provider);
    }

    public void testTempFileStorageProvider() throws Exception {
        StorageProvider provider = new TempFileStorageProvider();

        testReadWrite(provider, 0);
        testReadWrite(provider, 1);
        testReadWrite(provider, 1024);
        testReadWrite(provider, 20000);

        testDelete(provider);
    }

    public void testThresholdStorageProvider() throws Exception {
        final int threshold = 5000;
        StorageProvider backend = new TempFileStorageProvider();
        StorageProvider provider = new ThresholdStorageProvider(backend,
                threshold);

        testReadWrite(provider, 0);
        testReadWrite(provider, 1);
        testReadWrite(provider, threshold - 1);
        testReadWrite(provider, threshold);
        testReadWrite(provider, threshold + 1);
        testReadWrite(provider, 2 * threshold);
        testReadWrite(provider, 10 * threshold);

        testDelete(provider);
    }

    public void testCipherStorageProvider() throws Exception {
        StorageProvider backend = new TempFileStorageProvider();
        StorageProvider provider = new CipherStorageProvider(backend);

        testReadWrite(provider, 0);
        testReadWrite(provider, 1);
        testReadWrite(provider, 1024);
        testReadWrite(provider, 20000);

        testDelete(provider);
    }

    private void testReadWrite(StorageProvider provider, int size)
            throws IOException {
        testStore(provider, size);
        testCreateStorageOutputStream(provider, size);
    }

    private void testStore(StorageProvider provider, int size)
            throws IOException {
        byte[] data = createData(size);
        assertEquals(size, data.length);

        Storage storage = provider.store(new ByteArrayInputStream(data));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CodecUtil.copy(storage.getInputStream(), baos);
        verifyData(data, baos.toByteArray());
    }

    private void testCreateStorageOutputStream(StorageProvider provider,
            int size) throws IOException {
        byte[] data = createData(size);
        assertEquals(size, data.length);

        StorageOutputStream out = provider.createStorageOutputStream();
        CodecUtil.copy(new ByteArrayInputStream(data), out);
        Storage storage = out.toStorage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CodecUtil.copy(storage.getInputStream(), baos);
        verifyData(data, baos.toByteArray());
    }

    private void verifyData(byte[] expected, byte[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    private byte[] createData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) i;
        }
        return data;
    }

    private void testDelete(StorageProvider provider) throws IOException {
        Storage storage = provider.store(new ByteArrayInputStream(
                createData(512)));

        storage.delete();

        // getInputStream has to throw an IllegalStateException
        try {
            storage.getInputStream();
            fail();
        } catch (IllegalStateException expected) {
        }

        // invoking delete a second time should not have any effect
        storage.delete();
    }

}
