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

package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.james.mime4j.decoder.CodecUtil;
import org.apache.james.mime4j.message.storage.Storage;

/**
 * Binary body backed by a
 * {@link org.apache.james.mime4j.message.storage.Storage}
 */
class StorageBinaryBody extends AbstractBody implements BinaryBody {

    private Storage storage = null;

    public StorageBinaryBody(final Storage storage) {
        this.storage = storage;
    }

    /**
     * @see org.apache.james.mime4j.message.BinaryBody#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return storage.getInputStream();
    }

    /**
     * @see org.apache.james.mime4j.message.Body#writeTo(java.io.OutputStream,
     *      Mode)
     */
    public void writeTo(OutputStream out, Mode mode) throws IOException {
        if (out == null)
            throw new IllegalArgumentException();

        InputStream in = storage.getInputStream();
        CodecUtil.copy(in, out);
        in.close();
    }

    /**
     * Deletes the Storage that holds the content of this binary body.
     * 
     * @see org.apache.james.mime4j.message.Disposable#dispose()
     */
    @Override
    public void dispose() {
        if (storage != null) {
            storage.delete();
            storage = null;
        }
    }

}
