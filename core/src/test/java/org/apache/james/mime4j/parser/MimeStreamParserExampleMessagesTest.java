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

package org.apache.james.mime4j.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;

/**
 * Creates a TestSuite running the test for each .msg file in the test resouce folder.
 * Allow running of a single test from Unit testing GUIs
 */
public class MimeStreamParserExampleMessagesTest extends TestCase {

    private File file;


    public MimeStreamParserExampleMessagesTest(String testName) throws URISyntaxException {
        this(testName, MimeStreamParserExampleMessagesTestSuite.getFile(testName));
    }

    public MimeStreamParserExampleMessagesTest(String name, File testFile) {
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
        MimeStreamParser parser = null;
        TestHandler handler = null;
        MimeEntityConfig config = new MimeEntityConfig();
        config.setMaxLineLen(-1);
        parser = new MimeStreamParser(config);
        handler = new TestHandler();
        
        System.out.println("Parsing " + file.getName());
        parser.setContentHandler(handler);
        parser.parse(new FileInputStream(file));
        
        String result = handler.sb.toString();
        String xmlFile = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')) + ".xml";
        String xmlFileMime4j = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')) + ".mime4j.xml";
        
        try {
            String expected = IOUtils.toString(new FileInputStream(xmlFile), "ISO8859-1");
            assertEquals("Error parsing " + file.getName(), expected, result);
        } catch (FileNotFoundException e) {
            FileOutputStream fos = new FileOutputStream(xmlFileMime4j);
            fos.write(result.getBytes());
            fos.flush();
            fos.close();
            fail("XML file not found: generated a file with the expected result!");
        }
    }

    public static Test suite() throws IOException, URISyntaxException {
        return new MimeStreamParserExampleMessagesTestSuite();
    }

    
    static class MimeStreamParserExampleMessagesTestSuite extends TestSuite {

        private static final String TESTS_FOLDER = "/testmsgs";

        public MimeStreamParserExampleMessagesTestSuite() throws IOException, URISyntaxException {
            super();
            URL resource = MimeStreamParserExampleMessagesTestSuite.class.getResource(TESTS_FOLDER);
            if (resource != null) {
				File dir = new File(resource.toURI());
	            File[] files = dir.listFiles();
	            
	            for (File f : files) {
	                if (f.getName().toLowerCase().endsWith(".msg")) {
	                    addTest(new MimeStreamParserExampleMessagesTest(f.getName().substring(0, f.getName().length()-4), f));
	                }
	            }
            }
        }
        
        public static File getFile(String name) throws URISyntaxException {
            return new File(MimeStreamParserExampleMessagesTestSuite.class.getResource(TESTS_FOLDER+File.separator+name+".msg").toURI());
        }

    }
}
