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

import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.log4j.BasicConfigurator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Creates a TestSuite running the test for each .msg file in the test resouce folder.
 * Allow running of a single test from Unit testing GUIs
 */
public class ExampleMessagesRoundtripTest extends TestCase {

    private File file;


    public ExampleMessagesRoundtripTest(String testName) {
        this(testName, ExampleMessagesRountripTestSuite.getFile(testName));
    }

    public ExampleMessagesRoundtripTest(String name, File testFile) {
        super(name);
        this.file = testFile;
    }

    @Override
    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }
   
    @Override
    protected void runTest() throws Throwable {
        MimeEntityConfig config = new MimeEntityConfig();
        config.setMaxLineLen(-1);
        Message inputMessage = new Message(new FileInputStream(file), config);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inputMessage.writeTo(out);
        
        String msgoutFile = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')) + ".out";
        String msgoutFileMime4j = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')) + ".mime4j.out";
        
        try {
            ByteArrayOutputStream expectedstream = new ByteArrayOutputStream();
            CodecUtil.copy(new FileInputStream(msgoutFile), expectedstream);
            assertEquals("Wrong Expected result", new String(expectedstream.toByteArray()), new String(out.toByteArray()));
        } catch (FileNotFoundException e) {
            FileOutputStream fos = new FileOutputStream(msgoutFileMime4j);
            fos.write(out.toByteArray());
            fos.flush();
            fos.close();
            fail("Expected file not found: generated a file with the expected result!");
        }
    }

    public static Test suite() throws IOException {
        return new ExampleMessagesRountripTestSuite();
    }

    
    static class ExampleMessagesRountripTestSuite extends TestSuite {

        private static final File TESTS_FOLDER = new File("src/test/resources/testmsgs");

        public ExampleMessagesRountripTestSuite() throws IOException {
            super();
            File dir = TESTS_FOLDER;
            File[] files = dir.listFiles();
            
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".msg")) {
                    addTest(new ExampleMessagesRoundtripTest(f.getName().substring(0, f.getName().length()-4), f));
                }
            }
        }
        
        public static File getFile(String name) {
            return new File(TESTS_FOLDER.getAbsolutePath()+File.separator+name+".msg");
        }

    }
}
