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
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.message.storage.DefaultStorageProvider;
import org.apache.james.mime4j.message.storage.MultiReferenceStorage;
import org.apache.james.mime4j.message.storage.Storage;
import org.apache.james.mime4j.message.storage.StorageProvider;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.MessageUtils;

/**
 * Factory for creating message bodies.
 */
public class BodyFactory {

    private static Log log = LogFactory.getLog(BodyFactory.class);

    private static final Charset FALLBACK_CHARSET = MessageUtils.DEFAULT_CHARSET;

    private StorageProvider storageProvider;

    public BodyFactory() {
        this.storageProvider = DefaultStorageProvider.getInstance();
    }

    public BodyFactory(StorageProvider storageProvider) {
        if (storageProvider == null)
            storageProvider = DefaultStorageProvider.getInstance();

        this.storageProvider = storageProvider;
    }

    public BinaryBody binaryBody(InputStream is) throws IOException {
        if (is == null)
            throw new IllegalArgumentException();

        Storage storage = storageProvider.store(is);
        return new StorageBinaryBody(new MultiReferenceStorage(storage));
    }

    public TextBody textBody(InputStream is) throws IOException {
        if (is == null)
            throw new IllegalArgumentException();

        Storage storage = storageProvider.store(is);
        return new StorageTextBody(new MultiReferenceStorage(storage),
                MessageUtils.DEFAULT_CHARSET);
    }

    public TextBody textBody(InputStream is, String mimeCharset)
            throws IOException {
        if (is == null)
            throw new IllegalArgumentException();
        if (mimeCharset == null)
            throw new IllegalArgumentException();

        Storage storage = storageProvider.store(is);
        Charset charset = toJavaCharset(mimeCharset, false);
        return new StorageTextBody(new MultiReferenceStorage(storage), charset);
    }

    public TextBody textBody(String text) {
        if (text == null)
            throw new IllegalArgumentException();

        return new StringTextBody(text, MessageUtils.DEFAULT_CHARSET);
    }

    public TextBody textBody(String text, String mimeCharset) {
        if (text == null)
            throw new IllegalArgumentException();
        if (mimeCharset == null)
            throw new IllegalArgumentException();

        Charset charset = toJavaCharset(mimeCharset, true);
        return new StringTextBody(text, charset);
    }

    private static Charset toJavaCharset(String mimeCharset, boolean forEncoding) {
        String charset = CharsetUtil.toJavaCharset(mimeCharset);
        if (charset == null) {
            if (log.isWarnEnabled())
                log.warn("MIME charset '" + mimeCharset + "' has no "
                        + "corresponding Java charset. Using "
                        + FALLBACK_CHARSET + " instead.");
            return FALLBACK_CHARSET;
        }

        if (forEncoding && !CharsetUtil.isEncodingSupported(charset)) {
            if (log.isWarnEnabled())
                log.warn("MIME charset '" + mimeCharset
                        + "' does not support encoding. Using "
                        + FALLBACK_CHARSET + " instead.");
            return FALLBACK_CHARSET;
        }

        if (!forEncoding && !CharsetUtil.isDecodingSupported(charset)) {
            if (log.isWarnEnabled())
                log.warn("MIME charset '" + mimeCharset
                        + "' does not support decoding. Using "
                        + FALLBACK_CHARSET + " instead.");
            return FALLBACK_CHARSET;
        }

        return Charset.forName(charset);
    }

}
