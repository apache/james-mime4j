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

package org.apache.james.mime4j;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestSuite;

/**
 * Creates a TestSuite running the test for each sample .msg file in the test resource folder.
 */
public class ExampleMessageTestSuiteBuilder {

    private final ExampleMessageTestCaseFactory testFactory;

    public ExampleMessageTestSuiteBuilder(final ExampleMessageTestCaseFactory testFactory) {
        super();
        this.testFactory = testFactory;
    }

    public TestSuite build() throws IOException {
        TestSuite suite = new TestSuite();
        addAllTests(suite);
        return suite;
    }

    private void addAllTests(
            final TestSuite testSuite) throws IOException {
        addTests(testSuite, "/testmsgs");
        addTests(testSuite, "/mimetools-testmsgs");
    }

    private void addTests(
            final TestSuite testSuite,
            final String testsFolder) throws IOException {
        URL resource = ExampleMessageTestSuiteBuilder.class.getResource(testsFolder);
        if (resource != null) {
            if (resource.getProtocol().equalsIgnoreCase("file")) {
                File dir = new File(resource.getFile());
                File[] files = dir.listFiles();

                for (File f : files) {
                    if (f.getName().endsWith(".msg")) {
                        testSuite.addTest(this.testFactory.create(
                                f, f.toURI().toURL()));
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
                        testSuite.addTest(testFactory.create(
                                f, new URL("jar:file:" + jar.getName() + "!" + s)));
                    }
                }
            }
        }
    }

}
