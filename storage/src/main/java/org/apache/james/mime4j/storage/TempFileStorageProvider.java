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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;

/**
 * A {@link StorageProvider} that stores the data in temporary files. The files
 * are stored either in a user-specified directory or the default temporary-file
 * directory (specified by system property <code>java.io.tmpdir</code>).
 * <p>
 * Example usage:
 *
 * <pre>
 * File directory = new File(&quot;/tmp/mime4j&quot;);
 * StorageProvider provider = new TempFileStorageProvider(directory);
 * DefaultStorageProvider.setInstance(provider);
 * </pre>
 */
public class TempFileStorageProvider extends AbstractStorageProvider {

    private static final String DEFAULT_PREFIX = "m4j";

    /**
     * Bound on the files whose deletion failed and is retried by later deletions; the files
     * beyond it are only deleted at shutdown.
     */
    static final int MAX_PENDING_DELETIONS = 1024;

    /**
     * The temporary files created and not deleted yet. Unlike {@link File#deleteOnExit()}, whose
     * registrations stay in memory until the JVM stops, a file leaves this set once deleted.
     */
    private static final Set<File> LIVE_FILES = ConcurrentHashMap.newKeySet();

    /** The files whose deletion failed, retried by later deletions. */
    private static final Set<File> PENDING_DELETIONS = ConcurrentHashMap.newKeySet();

    private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean();

    private final String prefix;
    private final String suffix;
    private final File directory;

    /**
     * Equivalent to using constructor
     * <code>TempFileStorageProvider("m4j", null, null)</code>.
     */
    public TempFileStorageProvider() {
        this(DEFAULT_PREFIX, null, null);
    }

    /**
     * Equivalent to using constructor
     * <code>TempFileStorageProvider("m4j", null, directory)</code>.
     */
    public TempFileStorageProvider(File directory) {
        this(DEFAULT_PREFIX, null, directory);
    }

    /**
     * Creates a new <code>TempFileStorageProvider</code> using the given
     * values.
     *
     * @param prefix
     *            prefix for generating the temporary file's name; must be at
     *            least three characters long.
     * @param suffix
     *            suffix for generating the temporary file's name; may be
     *            <code>null</code> to use the suffix <code>".tmp"</code>.
     * @param directory
     *            the directory in which the file is to be created, or
     *            <code>null</code> if the default temporary-file directory is
     *            to be used (specified by the system property
     *            <code>java.io.tmpdir</code>).
     * @throws IllegalArgumentException
     *             if the given prefix is less than three characters long or the
     *             given directory does not exist and cannot be created (if it
     *             is not <code>null</code>).
     */
    public TempFileStorageProvider(String prefix, String suffix, File directory) {
        if (prefix == null || prefix.length() < 3)
            throw new IllegalArgumentException("invalid prefix");

        if (directory != null) {
            try {
                FileUtils.forceMkdir(directory);
            } catch (IOException e) {
                throw new IllegalArgumentException("invalid directory");
            }
        }

        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
    }

    public StorageOutputStream createStorageOutputStream() throws IOException {
        File file;
        if (directory == null) {
            file = Files.createTempFile(prefix, suffix).toFile();
        } else {
            file = Files.createTempFile(directory.toPath(), prefix, suffix).toFile();
        }
        register(file);

        return new TempFileStorageOutputStream(file);
    }

    private static void register(File file) {
        if (SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
            try {
                Runtime.getRuntime().addShutdownHook(
                        new Thread(TempFileStorageProvider::deleteLiveFiles, "mime4j-temp-file-cleanup"));
            } catch (IllegalStateException | SecurityException e) {
                // shutting down, or not allowed: the files are only deleted by their storage
            }
        }
        LIVE_FILES.add(file);
    }

    private static void deleteLiveFiles() {
        for (File file : LIVE_FILES) {
            tryDelete(file);
        }
    }

    /**
     * Deletes the given file.
     *
     * @return whether the file no longer exists
     */
    private static boolean tryDelete(File file) {
        if (file.delete() || !file.exists()) {
            LIVE_FILES.remove(file);
            return true;
        }
        return false;
    }

    /**
     * Number of temporary files created and not deleted yet. Package private, for tests.
     */
    static int liveFileCount() {
        return LIVE_FILES.size();
    }

    /**
     * Whether the given temporary file is still registered for deletion. Package private, for tests.
     */
    static boolean isRegistered(File file) {
        return LIVE_FILES.contains(file);
    }

    private static final class TempFileStorageOutputStream extends
            StorageOutputStream {
        private final File file;
        private OutputStream out;

        public TempFileStorageOutputStream(File file) throws IOException {
            this.file = file;
            this.out = new FileOutputStream(file);
        }

        @Override
        public void close() throws IOException {
            super.close();
            out.close();
        }

        @Override
        protected void write0(byte[] buffer, int offset, int length)
                throws IOException {
            out.write(buffer, offset, length);
        }

        @Override
        protected Storage toStorage0() throws IOException {
            // out has already been closed because toStorage calls close
            return new TempFileStorage(file);
        }
    }

    private static final class TempFileStorage implements Storage {

        private volatile File file;

        public TempFileStorage(File file) {
            this.file = file;
        }

        public void delete() {
            // deleting a file might not immediately succeed if there are still
            // streams left open (especially under Windows). so we keep track of
            // the files that could not be deleted (up to a bound) and try to
            // delete them again each time this method gets invoked; whatever
            // is left is deleted at shutdown.
            File toDelete;
            synchronized (this) {
                toDelete = file;
                file = null;
            }
            if (toDelete != null && !tryDelete(toDelete)
                    && PENDING_DELETIONS.size() < MAX_PENDING_DELETIONS) {
                PENDING_DELETIONS.add(toDelete);
            }

            for (File pending : PENDING_DELETIONS) {
                // whoever removes it from the set retries it
                if (pending != toDelete && PENDING_DELETIONS.remove(pending) && !tryDelete(pending)) {
                    PENDING_DELETIONS.add(pending);
                }
            }
        }

        public InputStream getInputStream() throws IOException {
            File current = file;
            if (current == null)
                throw new IllegalStateException("storage has been deleted");

            return new BufferedInputStream(new FileInputStream(current));
        }

    }

}
