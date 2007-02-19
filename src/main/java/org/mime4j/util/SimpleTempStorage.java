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

package org.mime4j.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * 
 * @version $Id: SimpleTempStorage.java,v 1.2 2004/10/02 12:41:11 ntherning Exp $
 */
public class SimpleTempStorage extends TempStorage {
    private static Log log = LogFactory.getLog(SimpleTempStorage.class);
    
    private TempPath rootPath = null;
    private Random random = new Random();
    
    /**
     * Creates a new <code>SimpleTempStorageManager</code> instance.
     */
    public SimpleTempStorage() {
        rootPath = new SimpleTempPath(System.getProperty("java.io.tmpdir"));
    }
    
    private TempPath createTempPath(TempPath parent, String prefix) 
            throws IOException {
        
        if (prefix == null) {
            prefix = "";
        }
        
        File p = null;
        int count = 1000;
        do {
            long n = Math.abs(random.nextLong());
            p = new File(parent.getAbsolutePath(), prefix + n);
            count--;
        } while (p.exists() && count > 0);
        
        if (p.exists() || !p.mkdirs()) {
            log.error("Unable to mkdirs on " + p.getAbsolutePath());
            throw new IOException("Creating dir '" 
                                    + p.getAbsolutePath() + "' failed."); 
        }
        
        return new SimpleTempPath(p);
    }
    
    private TempFile createTempFile(TempPath parent, String prefix, 
                                    String suffix) throws IOException {
        
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = ".tmp";
        }
        
        File f = null;
        
        int count = 1000;
        synchronized (this) {
            do {
                long n = Math.abs(random.nextLong());
                f = new File(parent.getAbsolutePath(), prefix + n + suffix);
                count--;
            } while (f.exists() && count > 0);
            
            if (f.exists()) {
                throw new IOException("Creating temp file failed: "
                                         + "Unable to find unique file name");
            }
            
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new IOException("Creating dir '" 
                                        + f.getAbsolutePath() + "' failed."); 
            }
        }
        
        return new SimpleTempFile(f);
    }
    
    public TempPath getRootTempPath() {
        return rootPath;
    }

    private class SimpleTempPath implements TempPath {
        private File path = null;
        
        private SimpleTempPath(String path) {
            this.path = new File(path);
        }
        
        private SimpleTempPath(File path) {
            this.path = path;
        }
        
        public TempFile createTempFile() throws IOException {
            return SimpleTempStorage.this.createTempFile(this, null, null);
        }

        public TempFile createTempFile(String prefix, String suffix) 
                throws IOException {
            
            return SimpleTempStorage.this.createTempFile(this, prefix, suffix);
        }

        public TempFile createTempFile(String prefix, String suffix, 
                                       boolean allowInMemory) 
            throws IOException {
            
            return SimpleTempStorage.this.createTempFile(this, prefix, suffix);
        }
        
        public String getAbsolutePath() {
            return path.getAbsolutePath();
        }

        public void delete() {
        }

        public TempPath createTempPath() throws IOException {
            return SimpleTempStorage.this.createTempPath(this, null);
        }

        public TempPath createTempPath(String prefix) throws IOException {
            return SimpleTempStorage.this.createTempPath(this, prefix);
        }
        
    }
    
    private class SimpleTempFile implements TempFile {
        private File file = null;
        
        private SimpleTempFile(String file) {
            this.file = new File(file);
            this.file.deleteOnExit();
        }
        
        private SimpleTempFile(File file) {
            this.file = file;
            this.file.deleteOnExit();
        }

        public InputStream getInputStream() throws IOException {
            return new BufferedInputStream(new FileInputStream(file));
        }

        public OutputStream getOutputStream() throws IOException {
            return new BufferedOutputStream(new FileOutputStream(file));
        }

        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        public void delete() {
        }

        public boolean isInMemory() {
            return false;
        }

        public long length() {
            return file.length();
        }
        
    }
}
