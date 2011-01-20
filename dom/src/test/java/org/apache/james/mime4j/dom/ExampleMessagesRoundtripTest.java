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

package org.apache.james.mime4j.dom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.MimeBuilder;
import org.apache.james.mime4j.stream.MimeEntityConfig;

/**
 * Creates a TestSuite running the test for each .msg file in the test resouce folder.
 * Allow running of a single test from Unit testing GUIs
 */
public class ExampleMessagesRoundtripTest extends TestCase {

    private URL url;

    public ExampleMessagesRoundtripTest(String name, URL url) {
        super(name);
        this.url = url;
    }

    @Override
    protected void runTest() throws Throwable {
        MimeEntityConfig config = new MimeEntityConfig();
        if (getName().startsWith("malformedHeaderStartsBody")) {
            config.setMalformedHeaderStartsBody(true);
        }
        config.setMaxLineLen(-1);
        Message inputMessage = MimeBuilder.DEFAULT.parse(url.openStream(), config);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inputMessage.writeTo(out);
        
        String s = url.toString();
        URL msgout = new URL(s.substring(0, s.lastIndexOf('.')) + ".out");
        
        ByteArrayOutputStream expectedstream = new ByteArrayOutputStream();
        CodecUtil.copy(msgout.openStream(), expectedstream);
        assertEquals("Wrong Expected result", new String(expectedstream.toByteArray()), new String(out.toByteArray()));
    }

    public static Test suite() throws IOException, URISyntaxException {
        return new ExampleMessagesRountripTestSuite();
    }

    
    static class ExampleMessagesRountripTestSuite extends TestSuite {

        private static final String TESTS_FOLDER = "/testmsgs";

        public ExampleMessagesRountripTestSuite() throws IOException, URISyntaxException {
            super();
            URL resource = ExampleMessagesRountripTestSuite.class.getResource(TESTS_FOLDER);
            if (resource != null) {
                if (resource.getProtocol().equalsIgnoreCase("file")) {
                    File dir = new File(resource.toURI());
                    File[] files = dir.listFiles();
                    
                    for (File f : files) {
                        if (f.getName().endsWith(".msg")) {
                            addTest(new ExampleMessagesRoundtripTest(f.getName(), 
                                    f.toURL()));
                        }
                    }
                } else if (resource.getProtocol().equalsIgnoreCase("jar")) {
                    JarURLConnection conn = (JarURLConnection) resource.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (Enumeration<JarEntry> it = jar.entries(); it.hasMoreElements(); ) {
                        JarEntry entry = it.nextElement();
                        String s = "/" + entry.toString();
                        File f = new File(s);
                        if (s.startsWith(TESTS_FOLDER) && s.endsWith(".msg")) {
                            addTest(new ExampleMessagesRoundtripTest(f.getName(), 
                                    new URL("jar:file:" + jar.getName() + "!" + s)));
                        }
                    }
                }
            }
        }
        
    }
}
