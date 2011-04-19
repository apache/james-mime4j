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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.MimeEntityConfig;

/**
 * Creates a TestSuite running the test for each .msg file in the test resouce folder.
 * Allow running of a single test from Unit testing GUIs
 */
public class MimeStreamParserExampleMessagesTest extends TestCase {

    private URL url;

    public MimeStreamParserExampleMessagesTest(String name, URL url) {
        super(name);
        this.url = url;
    }

    @Override
    protected void runTest() throws Throwable {
        MimeStreamParser parser = null;
        TestHandler handler = null;
        MimeEntityConfig config = new MimeEntityConfig();
        if (getName().startsWith("malformedHeaderStartsBody")) {
            config.setMalformedHeaderStartsBody(true);
        }
        config.setMaxLineLen(-1);
        parser = new MimeStreamParser(config);
        handler = new TestHandler();
        
        parser.setContentHandler(handler);
        parser.parse(url.openStream());
        
        String result = handler.sb.toString();
        
        String s = url.toString();
        String prefix = s.substring(0, s.lastIndexOf('.'));
        URL xmlFileUrl = new URL(prefix + ".xml");
        try {
	        InputStream openStream = xmlFileUrl.openStream();
			String expected = IOUtils.toString(openStream, "ISO8859-1");
	        assertEquals(expected, result);
        } catch (FileNotFoundException e) {
        	IOUtils.write(result, new FileOutputStream(xmlFileUrl.getPath()+".expected"));
        	fail("Expected file created.");
        }
    }

    public static Test suite() throws IOException, URISyntaxException {
        return new MimeStreamParserExampleMessagesTestSuite();
    }

    static class MimeStreamParserExampleMessagesTestSuite extends TestSuite {

        public MimeStreamParserExampleMessagesTestSuite() throws IOException, URISyntaxException {
            addTests("/testmsgs");
            addTests("/mimetools-testmsgs");
        }

		private void addTests(String testsFolder) throws URISyntaxException,
				MalformedURLException, IOException {
			URL resource = MimeStreamParserExampleMessagesTestSuite.class.getResource(testsFolder);
            if (resource != null) {
                if (resource.getProtocol().equalsIgnoreCase("file")) {
                    File dir = new File(resource.toURI());
                    File[] files = dir.listFiles();
                    
                    for (File f : files) {
                        if (f.getName().endsWith(".msg")) {
                            addTest(new MimeStreamParserExampleMessagesTest(f.getName(), 
                                    f.toURI().toURL()));
                        }
                    }
                } else if (resource.getProtocol().equalsIgnoreCase("jar")) {
                    JarURLConnection conn = (JarURLConnection) resource.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (Enumeration<JarEntry> it = jar.entries(); it.hasMoreElements(); ) {
                        JarEntry entry = it.nextElement();
                        String s = "/" + entry.toString();
                        File f = new File(s);
                        if (s.startsWith(testsFolder) && s.endsWith(".msg")) {
                            addTest(new MimeStreamParserExampleMessagesTest(f.getName(), 
                                    new URL("jar:file:" + jar.getName() + "!" + s)));
                        }
                    }
                }
            }
		}

    }
}
