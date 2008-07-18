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

package org.apache.james.mime4j.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.james.mime4j.util.SimpleTempStorage;
import org.apache.james.mime4j.util.TempFile;
import org.apache.james.mime4j.util.TempPath;

import junit.framework.TestCase;

/**
 * 
 *
 * 
 * @version $Id: SimpleTempStorageTest.java,v 1.2 2004/10/02 12:41:12 ntherning Exp $
 */
public class SimpleTempStorageTest extends TestCase {

    public void testGetRootTempPath() {
        SimpleTempStorage man = new SimpleTempStorage();
        TempPath path = man.getRootTempPath();
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        assertEquals(tmpdir.getAbsolutePath(), path.getAbsolutePath());
    }

    public void testCreateTempPath() throws IOException {
        SimpleTempStorage man = new SimpleTempStorage();
        TempPath path = man.getRootTempPath().createTempPath();
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        assertTrue(path.getAbsolutePath().startsWith(tmpdir.getAbsolutePath()));
        
        String fileName = path.getAbsolutePath().substring(
                path.getAbsolutePath().lastIndexOf(File.separatorChar) + 1);
        assertTrue("Unexpected chars in file name " + fileName, 
                    fileName.matches("^[0-9]+$"));
        assertTrue("Temp dir doesn't exist", 
                   new File(path.getAbsolutePath()).exists());
    }

    public void testCreateTempPathString() throws IOException {
        SimpleTempStorage man = new SimpleTempStorage();
        TempPath path = man.getRootTempPath().createTempPath("test_prefix");
        File tmpdir = new File(System.getProperty("java.io.tmpdir"), 
                               "test_prefix");
        assertTrue(path.getAbsolutePath().startsWith(tmpdir.getAbsolutePath()));
        
        String fileName = path.getAbsolutePath().substring(
                path.getAbsolutePath().lastIndexOf(File.separatorChar) + 1);
        assertTrue("Unexpected chars in file name " + fileName,
                    fileName.matches("^test_prefix[0-9]+$"));
        assertTrue("Temp dir doesn't exist", 
                   new File(path.getAbsolutePath()).exists());
    }

    public void testCreateTempFile() throws IOException {
        SimpleTempStorage man = new SimpleTempStorage();
        TempPath path = man.getRootTempPath().createTempPath();
        TempFile file = path.createTempFile();
        assertTrue(file.getAbsolutePath().startsWith(path.getAbsolutePath()));
        
        String fileName = file.getAbsolutePath().substring(
                file.getAbsolutePath().lastIndexOf(File.separatorChar) + 1);
        assertTrue("Unexpected chars in file name " + fileName, 
                    fileName.matches("^[0-9]+\\.tmp$"));
        assertTrue("Temp file doesn't exist", 
                   new File(file.getAbsolutePath()).exists());
        
        String s = "A short string";
        OutputStream out = file.getOutputStream();
        out.write(s.getBytes());
        out.close();

        byte[] buffer = new byte[s.length() * 2];
        InputStream in = file.getInputStream();
        int i = 0;
        for (int data; (data = in.read()) != -1; i++) {
            buffer[i] = (byte) data;
        }
        in.close();
        assertTrue(s.length() == i);
        assertEquals(s, new String(buffer, 0, i));
    }
    
    public void testCreateTempFileStringString() throws IOException {
        SimpleTempStorage man = new SimpleTempStorage();
        TempPath path = man.getRootTempPath().createTempPath();
        TempFile file = path.createTempFile("test_prefix", ".suffix");
        assertTrue(file.getAbsolutePath().startsWith(path.getAbsolutePath()));
        
        String fileName = file.getAbsolutePath().substring(
                file.getAbsolutePath().lastIndexOf(File.separatorChar) + 1);
        assertTrue("Unexpected chars in file name " + fileName, 
                    fileName.matches("^test_prefix[0-9]+\\.suffix$"));
        assertTrue("Temp file doesn't exist", 
                   new File(file.getAbsolutePath()).exists());
    }    
}
