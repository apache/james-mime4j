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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * A {@link StorageProvider} that transparently scrambles and unscrambles the
 * data stored by another <code>StorageProvider</code>.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * StorageProvider mistrusted = new TempFileStorageProvider();
 * StorageProvider enciphered = new CipherStorageProvider(mistrusted);
 * StorageProvider provider = new ThresholdStorageProvider(enciphered);
 * DefaultStorageProvider.setInstance(provider);
 * </pre>
 */
public class CipherStorageProvider implements StorageProvider {

    private final StorageProvider backend;
    private final String algorithm;
    private final KeyGenerator keygen;

    /**
     * Creates a new <code>CipherStorageProvider</code> for the given back-end
     * using the Blowfish cipher algorithm.
     * 
     * @param backend
     *            back-end storage strategy to encrypt.
     */
    public CipherStorageProvider(StorageProvider backend) {
        this(backend, "Blowfish");
    }

    /**
     * Creates a new <code>CipherStorageProvider</code> for the given back-end
     * and cipher algorithm.
     * 
     * @param backend
     *            back-end storage strategy to encrypt.
     * @param algorithm
     *            the name of the symmetric block cipher algorithm such as
     *            "Blowfish", "AES" or "RC2".
     */
    public CipherStorageProvider(StorageProvider backend, String algorithm) {
        if (backend == null)
            throw new IllegalArgumentException();

        try {
            this.backend = backend;
            this.algorithm = algorithm;
            this.keygen = KeyGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Storage store(InputStream in) throws IOException {
        try {
            byte[] raw = keygen.generateKey().getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, algorithm);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            InputStream encrypted = new CipherInputStream(in, cipher);
            Storage storage = backend.store(encrypted);
            return new CipherStorage(storage, skeySpec);
        } catch (GeneralSecurityException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    private final class CipherStorage implements Storage {
        private Storage encrypted;
        private final SecretKeySpec skeySpec;

        public CipherStorage(Storage encrypted, SecretKeySpec skeySpec) {
            this.encrypted = encrypted;
            this.skeySpec = skeySpec;
        }

        public void delete() {
            if (encrypted != null) {
                encrypted.delete();
                encrypted = null;
            }
        }

        public InputStream getInputStream() throws IOException {
            if (encrypted == null)
                throw new IllegalStateException("storage has been deleted");

            try {
                Cipher cipher = Cipher.getInstance(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);

                InputStream in = encrypted.getInputStream();
                return new CipherInputStream(in, cipher);
            } catch (GeneralSecurityException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }
    }

}
