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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.ExampleMessageTestCase;
import org.apache.james.mime4j.ExampleMessageTestCaseFactory;
import org.apache.james.mime4j.ExampleMessageTestSuiteBuilder;
import org.apache.james.mime4j.field.FieldsTest;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.util.CharsetUtil;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * Test parsing into DOM of all sample messages
 */
@RunWith(AllTests.class)
public class MessageParserTest extends ExampleMessageTestCase {

    public static TestSuite suite() throws IOException {
        ExampleMessageTestSuiteBuilder testSuiteBuilder = new ExampleMessageTestSuiteBuilder(
                new ExampleMessageTestCaseFactory() {

                    public ExampleMessageTestCase create(final File file, final URL resource) throws IOException {
                        return new MessageParserTest(file, resource);
                    }

                });
        return testSuiteBuilder.build();
    }

    public MessageParserTest(final File file, final URL resource) {
        super(file, resource);
    }

    @Override
    protected void runTest() throws IOException {
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

        String resourceBase = getResourceBase();
        URL decodedFile = new URL(resourceBase + "_decoded.xml");

        String result = getStructure(inputMessage, resourceBase, "1");
        try {
            String expected;
            InputStream contentstream = decodedFile.openStream();
            try {
                expected = IOUtils.toString(contentstream, CharsetUtil.ISO_8859_1.name());
            } finally {
                contentstream.close();
            }
            Assert.assertEquals(expected, result);
        } catch (FileNotFoundException ex) {
            // Create expected content template to the current directory
            File expectedFileTemplate = new File(getFilenameBase() + "_decoded.xml.expected");
            FileOutputStream templatestream = new FileOutputStream(expectedFileTemplate);
            try {
                IOUtils.write(result, templatestream, CharsetUtil.ISO_8859_1.name());
            } finally {
                templatestream.close();
            }
            Assert.fail("Expected file created.");
        }
    }

    private String escape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        return s.replaceAll(">", "&gt;");
    }

    private String getStructure(
            final Entity e, final String resourceBase, final String id) throws IOException {

        StringBuilder sb = new StringBuilder();

        if (e instanceof MessageImpl) {
            sb.append("<message>\r\n");
        } else {
            sb.append("<body-part>\r\n");
        }

        sb.append("<header>\r\n");
        for (Field field : e.getHeader().getFields()) {
            sb.append("<field>\r\n").append(escape(FieldsTest.decode(field))).append("</field>\r\n");
        }
        sb.append("</header>\r\n");

        if (e.getBody() instanceof Multipart) {
            sb.append("<multipart>\r\n");

            Multipart multipart = (Multipart) e.getBody();
            List<Entity> parts = multipart.getBodyParts();

            if (multipart.getPreamble() != null) {
                sb.append("<preamble>\r\n");
                sb.append(escape(multipart.getPreamble()));
                sb.append("</preamble>\r\n");
            }

            int i = 1;
            for (Entity bodyPart : parts) {
                sb.append(getStructure(bodyPart, resourceBase, id + "_" + (i++)));
            }

            if (multipart.getEpilogue() != null) {
                sb.append("<epilogue>\r\n");
                sb.append(escape(multipart.getEpilogue()));
                sb.append("</epilogue>\r\n");
            }

            sb.append("</multipart>\r\n");

        } else if (e.getBody() instanceof MessageImpl) {
            sb.append(getStructure((MessageImpl) e.getBody(), resourceBase, id + "_1"));
        } else {
            Body b = e.getBody();
            String suffix = "_decoded_" + id + (b instanceof TextBody ? ".txt" : ".bin");
            String filename = getFilenameBase() + suffix;

            String tag = b instanceof TextBody ? "text-body" : "binary-body";
            sb.append("<").append(tag).append(" name=\"").append(filename).append("\"/>\r\n");
            URL expectedUrl = new URL(getResourceBase() + suffix);

            if (b instanceof TextBody) {
                String charset = e.getCharset();
                if (charset == null) {
                    charset = CharsetUtil.ISO_8859_1.name();
                }

                String result = IOUtils.toString(((TextBody) b).getReader());
                try {
                    String expected;
                    InputStream contentstream = expectedUrl.openStream();
                    try {
                        expected = IOUtils.toString(contentstream, charset);
                    } finally {
                        contentstream.close();
                    }
                    Assert.assertEquals(filename, expected, result);
                } catch (FileNotFoundException ex) {
                    // Create expected content template to the current directory
                    File expectedFileTemplate = new File(filename + ".expected");
                    FileOutputStream templatestream = new FileOutputStream(expectedFileTemplate);
                    try {
                        IOUtils.copy(((TextBody) b).getInputStream(), templatestream);
                    } finally {
                        templatestream.close();
                    }
                    Assert.fail("Expected file created.");
                }
            } else {
                try {
                    InputStream contentstream = expectedUrl.openStream();
                    try {
                        assertEqualsBinary(filename, contentstream,
                                ((BinaryBody) b).getInputStream());
                    } finally {
                        contentstream.close();
                    }
                } catch (FileNotFoundException ex) {
                    // Create expected content template to the current directory
                    File expectedFileTemplate = new File(filename + ".expected");
                    FileOutputStream templatestream = new FileOutputStream(expectedFileTemplate);
                    try {
                        IOUtils.copy(((BinaryBody) b).getInputStream(), templatestream);
                    } finally {
                        templatestream.close();
                    }
                    Assert.fail("Expected file created.");
                }
            }
        }

        if (e instanceof MessageImpl) {
            sb.append("</message>\r\n");
        } else {
            sb.append("</body-part>\r\n");
        }

        return sb.toString();
    }

    private void assertEqualsBinary(String msg, InputStream a, InputStream b)
            throws IOException {

        int pos = 0;
        while (true) {
            int b1 = a.read();
            int b2 = b.read();
            Assert.assertEquals(msg + " (Position " + (++pos) + ")", b1, b2);

            if (b1 == -1 || b2 == -1) {
                break;
            }
        }
    }
}
