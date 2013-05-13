/****************************************************************
((TextBody) b).getInputStream()((TextBody) b).getInputStream()((TextBody) b).getInputStream()((TextBody) b).getInputStream()((TextBody) b).getInputStream()((TextBody) b).getInputStream()((TextBody) b).getInputStream() * Licensed to the Apache Software Foundation (ASF) under one   *
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.ExampleMessageTestCase;
import org.apache.james.mime4j.ExampleMessageTestCaseFactory;
import org.apache.james.mime4j.ExampleMessageTestSuiteBuilder;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.util.CharsetUtil;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * Test round-trip of all sample messages
 */
@RunWith(AllTests.class)
public class ExampleMessagesRoundtripTest extends ExampleMessageTestCase {

    public static TestSuite suite() throws IOException {
        ExampleMessageTestSuiteBuilder testSuiteBuilder = new ExampleMessageTestSuiteBuilder(
                new ExampleMessageTestCaseFactory() {

                    public ExampleMessageTestCase create(final File file, final URL resource) throws IOException {
                        return new ExampleMessagesRoundtripTest(file, resource);
                    }

                });
        return testSuiteBuilder.build();
    }

    public ExampleMessagesRoundtripTest(final File file, final URL resource) {
        super(file, resource);
    }

    @Override
    protected void runTest() throws Exception {
        MimeConfig config = getConfig();

        Message inputMessage;
        InputStream msgstream = getResource().openStream();
        try {
            DefaultMessageBuilder msgbuilder = new DefaultMessageBuilder();
            msgbuilder.setMimeEntityConfig(config);
            inputMessage = msgbuilder.parseMessage(msgstream);
        } finally {
            msgstream.close();
        }

        DefaultMessageWriter msgwriter = new DefaultMessageWriter();

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        msgwriter.writeMessage(inputMessage, outstream);

        String result = new String(outstream.toByteArray(), CharsetUtil.ISO_8859_1.name());

        URL outFile = new URL(getResourceBase() + ".out");
        try {
            String expected;
            InputStream contentstream = outFile.openStream();
            try {
                expected = IOUtils.toString(contentstream, CharsetUtil.ISO_8859_1.name());
            } finally {
                contentstream.close();
            }
            assertEquals(expected, result);
        } catch (FileNotFoundException e) {
            // Create expected content template to the current directory
            File expectedFileTemplate = new File(getFilenameBase() + ".out.expected");
            FileOutputStream templatestream = new FileOutputStream(expectedFileTemplate);
            try {
                IOUtils.write(result, templatestream, CharsetUtil.ISO_8859_1.name());
            } finally {
                templatestream.close();
            }
            Assert.fail("Expected file created.");
        }
    }

}
