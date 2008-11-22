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

package org.apache.james.mime4j.message.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.util.ByteArrayBuffer;

/**
 * A {@link StorageProvider} that stores the data entirely in memory.
 * <p>
 * Example usage:
 * 
 * <pre>
 * StorageProvider provider = new MemoryStorageProvider();
 * DefaultStorageProvider.setInstance(provider);
 * </pre>
 */
public class MemoryStorageProvider implements StorageProvider {

    /**
     * Creates a new <code>MemoryStorageProvider</code>.
     */
    public MemoryStorageProvider() {
    }

    public Storage store(InputStream in) throws IOException {
        ByteArrayBuffer bab = new ByteArrayBuffer(1024);

        final byte[] buffer = new byte[512];
        int inputLength;
        while ((inputLength = in.read(buffer)) != -1) {
            bab.append(buffer, 0, inputLength);
        }

        return new MemoryStorage(bab.buffer(), bab.length());
    }

    static final class MemoryStorage implements Storage {
        private byte[] data;
        private final int count;

        public MemoryStorage(byte[] data, int count) {
            this.data = data;
            this.count = count;
        }

        public InputStream getInputStream() throws IOException {
            if (data == null)
                throw new IllegalStateException("storage has been deleted");

            return new ByteArrayInputStream(data, 0, count);
        }

        public void delete() {
            data = null;
        }
    }

}
