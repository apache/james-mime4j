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
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * A {@link StorageProvider} that transparently encrypts and decrypts the
 * data stored by another <code>StorageProvider</code>.
 *
 * <p>
 * Each storage gets its own random AES-256 key and nonce prefix, kept in memory
 * only. The data is cut into segments of {@value #SEGMENT_SIZE} bytes, each
 * encrypted and authenticated with AES/GCM under a distinct nonce that also
 * tells whether it is the last segment: the back-end learns nothing but the
 * length of the data, and a modified, reordered or truncated content makes the
 * read fail with an {@link IOException} before any altered byte is returned.
 * A read buffers one segment at a time, whatever the size of the data.
 * </p>
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
public class CipherStorageProvider extends AbstractStorageProvider {

    private static final String KEY_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int TAG_BITS = 128;
    private static final int TAG_LEN = TAG_BITS / 8;
    private static final int NONCE_LEN = 12;
    // nonce = random prefix | segment counter (4 bytes) | last segment flag (1 byte)
    private static final int NONCE_PREFIX_LEN = NONCE_LEN - 5;

    /** Plaintext bytes per segment. */
    static final int SEGMENT_SIZE = 64 * 1024;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StorageProvider backend;
    private final KeyGenerator keygen;

    /**
     * Creates a new <code>CipherStorageProvider</code> for the given back-end,
     * using AES-256 in GCM mode.
     *
     * @param backend
     *            back-end storage strategy to encrypt.
     */
    public CipherStorageProvider(StorageProvider backend) {
        this(backend, KEY_ALGORITHM);
    }

    /**
     * Creates a new <code>CipherStorageProvider</code> for the given back-end
     * and cipher algorithm.
     *
     * @param backend
     *            back-end storage strategy to encrypt.
     * @param algorithm
     *            <code>"AES"</code> or <code>"AES/GCM/NoPadding"</code>, which
     *            both mean AES-256 in GCM mode. Other algorithms are refused: a
     *            bare name such as <code>"Blowfish"</code> means ECB mode, which
     *            leaks the patterns of the data and authenticates nothing.
     * @throws IllegalArgumentException
     *            if the algorithm is not supported.
     */
    public CipherStorageProvider(StorageProvider backend, String algorithm) {
        if (backend == null)
            throw new IllegalArgumentException();
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm may not be null");
        }
        String normalized = algorithm.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals(KEY_ALGORITHM) && !normalized.equals(TRANSFORMATION.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported algorithm " + algorithm
                    + ": only " + TRANSFORMATION + " is supported");
        }

        try {
            this.backend = backend;
            this.keygen = KeyGenerator.getInstance(KEY_ALGORITHM);
            this.keygen.init(KEY_SIZE, RANDOM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public StorageOutputStream createStorageOutputStream() throws IOException {
        SecretKey key;
        synchronized (keygen) {
            key = keygen.generateKey();
        }
        byte[] noncePrefix = new byte[NONCE_PREFIX_LEN];
        RANDOM.nextBytes(noncePrefix);

        return new CipherStorageOutputStream(backend.createStorageOutputStream(), key, noncePrefix);
    }

    private static GCMParameterSpec segmentSpec(byte[] noncePrefix, int segment, boolean last) {
        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(noncePrefix, 0, nonce, 0, NONCE_PREFIX_LEN);
        nonce[NONCE_PREFIX_LEN] = (byte) (segment >>> 24);
        nonce[NONCE_PREFIX_LEN + 1] = (byte) (segment >>> 16);
        nonce[NONCE_PREFIX_LEN + 2] = (byte) (segment >>> 8);
        nonce[NONCE_PREFIX_LEN + 3] = (byte) segment;
        nonce[NONCE_LEN - 1] = (byte) (last ? 1 : 0);
        return new GCMParameterSpec(TAG_BITS, nonce);
    }

    private static IOException ioException(String message, GeneralSecurityException cause) {
        return (IOException) new IOException(message).initCause(cause);
    }

    private static final class CipherStorageOutputStream extends
            StorageOutputStream {
        private final StorageOutputStream storageOut;
        private final SecretKey key;
        private final byte[] noncePrefix;
        private final Cipher cipher;
        private final byte[] segment = new byte[SEGMENT_SIZE];
        private int segmentLen;
        private int segmentCount;
        private boolean closed;

        public CipherStorageOutputStream(StorageOutputStream out,
                SecretKey key, byte[] noncePrefix) throws IOException {
            this.storageOut = out;
            this.key = key;
            this.noncePrefix = noncePrefix;
            try {
                this.cipher = Cipher.getInstance(TRANSFORMATION);
            } catch (GeneralSecurityException e) {
                throw ioException("Cannot create the cipher", e);
            }
        }

        private void flushSegment(boolean last) throws IOException {
            if (segmentCount == Integer.MAX_VALUE) {
                throw new IOException("Too much data to encrypt");
            }
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key, segmentSpec(noncePrefix, segmentCount, last));
                byte[] encrypted = cipher.doFinal(segment, 0, segmentLen);
                storageOut.write(encrypted);
            } catch (GeneralSecurityException e) {
                throw ioException("Cannot encrypt", e);
            }
            segmentCount++;
            segmentLen = 0;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (!closed) {
                closed = true;
                try {
                    flushSegment(true);
                } finally {
                    storageOut.close();
                }
            }
        }

        @Override
        protected void write0(byte[] buffer, int offset, int length)
                throws IOException {
            while (length > 0) {
                if (segmentLen == SEGMENT_SIZE) {
                    // More data follows: this segment is not the last one
                    flushSegment(false);
                }
                int chunk = Math.min(length, SEGMENT_SIZE - segmentLen);
                System.arraycopy(buffer, offset, segment, segmentLen, chunk);
                segmentLen += chunk;
                offset += chunk;
                length -= chunk;
            }
        }

        @Override
        protected Storage toStorage0() throws IOException {
            // close() has already been called because toStorage calls close
            Storage encrypted = storageOut.toStorage();
            return new CipherStorage(encrypted, key, noncePrefix);
        }
    }

    private static final class CipherStorage implements Storage {
        private Storage encrypted;
        private final SecretKey key;
        private final byte[] noncePrefix;

        public CipherStorage(Storage encrypted, SecretKey key, byte[] noncePrefix) {
            this.encrypted = encrypted;
            this.key = key;
            this.noncePrefix = noncePrefix;
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

            Cipher cipher;
            try {
                cipher = Cipher.getInstance(TRANSFORMATION);
            } catch (GeneralSecurityException e) {
                throw ioException("Cannot create the cipher", e);
            }
            return new SegmentDecryptingInputStream(encrypted.getInputStream(), cipher, key, noncePrefix);
        }
    }

    /**
     * Decrypts one segment at a time, and returns its bytes only once its tag is verified.
     */
    private static final class SegmentDecryptingInputStream extends InputStream {
        private static final int ENCRYPTED_SEGMENT_SIZE = SEGMENT_SIZE + TAG_LEN;

        private final InputStream in;
        private final Cipher cipher;
        private final SecretKey key;
        private final byte[] noncePrefix;
        // one encrypted segment, plus one byte read ahead to tell whether it is the last one
        private final byte[] encryptedSegment = new byte[ENCRYPTED_SEGMENT_SIZE + 1];
        private int readAhead = -1;
        private byte[] plain = new byte[0];
        private int plainPos;
        private int segmentCount;
        private boolean lastDone;

        SegmentDecryptingInputStream(InputStream in, Cipher cipher, SecretKey key, byte[] noncePrefix) {
            this.in = in;
            this.cipher = cipher;
            this.key = key;
            this.noncePrefix = noncePrefix;
        }

        private int readFully(byte[] b, int off, int len) throws IOException {
            int total = 0;
            while (total < len) {
                int n = in.read(b, off + total, len - total);
                if (n == -1) {
                    break;
                }
                total += n;
            }
            return total;
        }

        /**
         * @return false at the end of the data, once the last segment has been verified
         */
        private boolean nextSegment() throws IOException {
            if (lastDone) {
                return false;
            }
            int len = 0;
            if (readAhead != -1) {
                encryptedSegment[0] = (byte) readAhead;
                len = 1;
                readAhead = -1;
            }
            len += readFully(encryptedSegment, len, ENCRYPTED_SEGMENT_SIZE - len);
            boolean last;
            if (len < ENCRYPTED_SEGMENT_SIZE) {
                last = true;
            } else {
                readAhead = in.read();
                last = readAhead == -1;
            }
            if (segmentCount == Integer.MAX_VALUE) {
                throw new IOException("Too many encrypted segments");
            }
            try {
                cipher.init(Cipher.DECRYPT_MODE, key, segmentSpec(noncePrefix, segmentCount, last));
                plain = cipher.doFinal(encryptedSegment, 0, len);
            } catch (GeneralSecurityException e) {
                throw ioException("Encrypted storage has been altered or truncated", e);
            }
            plainPos = 0;
            segmentCount++;
            lastDone = last;
            return true;
        }

        @Override
        public int read() throws IOException {
            byte[] one = new byte[1];
            int n = read(one, 0, 1);
            return n == -1 ? -1 : one[0] & 0xff;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            while (plainPos == plain.length) {
                if (!nextSegment()) {
                    return -1;
                }
            }
            int n = Math.min(len, plain.length - plainPos);
            System.arraycopy(plain, plainPos, b, off, n);
            plainPos += n;
            return n;
        }

        @Override
        public int available() {
            return plain.length - plainPos;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}
