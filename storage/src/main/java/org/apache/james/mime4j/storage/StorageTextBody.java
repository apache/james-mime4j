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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.james.mime4j.dom.TextBody;

/**
 * Text body backed by a {@link org.apache.james.mime4j.storage.Storage}.
 */
class StorageTextBody extends TextBody {

    private MultiReferenceStorage storage;
    private final Charset charset;

    public StorageTextBody(MultiReferenceStorage storage, Charset charset) {
        this.storage = storage;
        this.charset = charset;
    }

    @Override
    public String getMimeCharset() {
        return charset.name();
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public Reader getReader() throws IOException {
        return new InputStreamReader(storage.getInputStream(), charset);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return storage.getInputStream();
    }

    @Override
    public StorageTextBody copy() {
        storage.addReference();
        return new StorageTextBody(storage, charset);
    }

    /**
     * Deletes the Storage that holds the content of this text body.
     *
     * @see org.apache.james.mime4j.dom.Disposable#dispose()
     */
    @Override
    public void dispose() {
        if (storage != null) {
            storage.delete();
            storage = null;
        }
    }

}
