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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CipherStorageProviderTest {

    /**
     * A back-end whose stored bytes the test can read and alter.
     */
    private static final class InspectableBackend extends AbstractStorageProvider {
        byte[] stored;

        public StorageOutputStream createStorageOutputStream() {
            return new StorageOutputStream() {
                private final ByteArrayOutputStream out = new ByteArrayOutputStream();

                @Override
                protected void write0(byte[] buffer, int offset, int length) {
                    out.write(buffer, offset, length);
                }

                @Override
                protected Storage toStorage0() {
                    stored = out.toByteArray();
                    return new Storage() {
                        public InputStream getInputStream() {
                            return new ByteArrayInputStream(stored);
                        }

                        public void delete() {
                        }
                    };
                }
            };
        }
    }

    private static byte[] data(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i * 31 + i / 7);
        }
        return data;
    }

    private static byte[] read(Storage storage) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = storage.getInputStream()) {
            byte[] buf = new byte[1000];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
        return out.toByteArray();
    }

    @Test
    public void shouldReadBackWhatWasWritten() throws Exception {
        int segment = CipherStorageProvider.SEGMENT_SIZE;
        int[] sizes = {0, 1, 15, 16, 17, segment - 1, segment, segment + 1, 2 * segment, 3 * segment + 5};
        CipherStorageProvider provider = new CipherStorageProvider(new InspectableBackend());
        for (int size : sizes) {
            byte[] data = data(size);
            Storage storage = provider.store(new ByteArrayInputStream(data));
            Assert.assertArrayEquals("size " + size, data, read(storage));
            // read byte per byte
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = storage.getInputStream()) {
                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
            }
            Assert.assertArrayEquals("size " + size, data, out.toByteArray());
        }
    }

    @Test
    public void repeatedPlaintextBlocksShouldNotGiveRepeatedCiphertextBlocks() throws Exception {
        InspectableBackend backend = new InspectableBackend();
        byte[] data = new byte[16 * 10000];
        Arrays.fill(data, (byte) 'A');
        new CipherStorageProvider(backend).store(new ByteArrayInputStream(data));

        Set<ByteBuffer> blocks = new HashSet<>();
        for (int i = 0; i + 16 <= backend.stored.length; i += 16) {
            Assert.assertTrue("ciphertext block " + i / 16 + " repeats",
                    blocks.add(ByteBuffer.wrap(Arrays.copyOfRange(backend.stored, i, i + 16))));
        }
    }

    @Test
    public void twoStoragesOfTheSameDataShouldNotShareCiphertext() throws Exception {
        InspectableBackend backend = new InspectableBackend();
        CipherStorageProvider provider = new CipherStorageProvider(backend);
        byte[] data = data(100);
        provider.store(new ByteArrayInputStream(data));
        byte[] first = backend.stored;
        provider.store(new ByteArrayInputStream(data));
        Assert.assertFalse(Arrays.equals(first, backend.stored));
    }

    private static void assertReadFails(Storage storage) {
        try {
            read(storage);
            Assert.fail("Expected IOException");
        } catch (IOException expected) {
            // expected
        }
    }

    @Test
    public void aFlippedCiphertextByteShouldMakeTheReadFail() throws Exception {
        int segment = CipherStorageProvider.SEGMENT_SIZE;
        int[] positions = {0, 100, segment + 10, 2 * segment + 20};
        for (int position : positions) {
            InspectableBackend backend = new InspectableBackend();
            Storage storage = new CipherStorageProvider(backend).store(new ByteArrayInputStream(data(2 * segment + 100)));
            backend.stored[position] ^= 1;
            assertReadFails(storage);
        }
    }

    @Test
    public void aTruncatedCiphertextShouldMakeTheReadFail() throws Exception {
        int segment = CipherStorageProvider.SEGMENT_SIZE;
        int encryptedSegment = segment + 16;
        // dropping the last segment, part of it, or the tag of a lone segment
        int[][] cases = {{2 * segment, encryptedSegment}, {2 * segment + 100, encryptedSegment + 50}, {10, 20}};
        for (int[] c : cases) {
            InspectableBackend backend = new InspectableBackend();
            Storage storage = new CipherStorageProvider(backend).store(new ByteArrayInputStream(data(c[0])));
            backend.stored = Arrays.copyOf(backend.stored, c[1]);
            assertReadFails(storage);
        }
    }

    @Test
    public void swappedSegmentsShouldMakeTheReadFail() throws Exception {
        int encryptedSegment = CipherStorageProvider.SEGMENT_SIZE + 16;
        InspectableBackend backend = new InspectableBackend();
        Storage storage = new CipherStorageProvider(backend)
                .store(new ByteArrayInputStream(data(3 * CipherStorageProvider.SEGMENT_SIZE)));
        byte[] swapped = backend.stored.clone();
        System.arraycopy(backend.stored, 0, swapped, encryptedSegment, encryptedSegment);
        System.arraycopy(backend.stored, encryptedSegment, swapped, 0, encryptedSegment);
        backend.stored = swapped;
        assertReadFails(storage);
    }

    @Test
    public void aesShouldBeAccepted() {
        new CipherStorageProvider(new MemoryStorageProvider(), "AES");
        new CipherStorageProvider(new MemoryStorageProvider(), "aes/gcm/nopadding");
    }

    @Test(expected = IllegalArgumentException.class)
    public void aBareBlowfishShouldBeRefused() {
        new CipherStorageProvider(new MemoryStorageProvider(), "Blowfish");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ecbShouldBeRefused() {
        new CipherStorageProvider(new MemoryStorageProvider(), "AES/ECB/PKCS5Padding");
    }
}
