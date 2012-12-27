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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.field.FieldsTest;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;

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
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MessageParserTest extends TestCase {

    private URL url;

    public MessageParserTest(String name, URL url) {
        super(name);
        this.url = url;
    }

    public static Test suite() throws IOException, URISyntaxException {
        return new MessageParserTestSuite();
    }

    static class MessageParserTestSuite extends TestSuite {

        public MessageParserTestSuite() throws IOException, URISyntaxException {
            addTests("/testmsgs");
            addTests("/mimetools-testmsgs");
        }

        private void addTests(String testsFolder) throws URISyntaxException,
                MalformedURLException, IOException {
            URL resource = MessageParserTestSuite.class.getResource(testsFolder);
            if (resource != null) {
                if (resource.getProtocol().equalsIgnoreCase("file")) {
                    File dir = new File(resource.toURI());
                    File[] files = dir.listFiles();

                    for (File f : files) {
                        if (f.getName().endsWith(".msg")) {
                            addTest(new MessageParserTest(f.getName(),
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
                            addTest(new MessageParserTest(f.getName(),
                                    new URL("jar:file:" + jar.getName() + "!" + s)));
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void runTest() throws IOException {
        MimeConfig config = new MimeConfig();
        if (getName().startsWith("malformedHeaderStartsBody")) {
            config.setMalformedHeaderStartsBody(true);
        }
        config.setMaxLineLen(-1);
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(config);
        Message m = builder.parseMessage(url.openStream());

        String s = url.toString();
        String prefix = s.substring(0, s.lastIndexOf('.'));
        URL xmlFileUrl = new URL(prefix + "_decoded.xml");

        String result = getStructure(m, prefix, "1");
        try {
            String expected = IOUtils.toString(xmlFileUrl.openStream(), "ISO8859-1");
            assertEquals(expected, result);
        } catch (FileNotFoundException ex) {
            IOUtils.write(result, new FileOutputStream(xmlFileUrl.getPath() + ".expected"), "ISO8859-1");
            fail("Expected file created.");
        }
    }

    private String escape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        return s.replaceAll(">", "&gt;");
    }

    private String getStructure(Entity e, String prefix, String id)
            throws IOException {

        StringBuilder sb = new StringBuilder();

        if (e instanceof MessageImpl) {
            sb.append("<message>\r\n");
        } else {
            sb.append("<body-part>\r\n");
        }

        sb.append("<header>\r\n");
        for (Field field : e.getHeader().getFields()) {
            sb.append("<field>\r\n"
                    + escape(FieldsTest.decode(field))
                    + "</field>\r\n");
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
                sb.append(getStructure(bodyPart, prefix, id + "_" + (i++)));
            }

            if (multipart.getEpilogue() != null) {
                sb.append("<epilogue>\r\n");
                sb.append(escape(multipart.getEpilogue()));
                sb.append("</epilogue>\r\n");
            }

            sb.append("</multipart>\r\n");

        } else if (e.getBody() instanceof MessageImpl) {
            sb.append(getStructure((MessageImpl) e.getBody(), prefix, id + "_1"));
        } else {
            Body b = e.getBody();
            String s = prefix + "_decoded_" + id
                    + (b instanceof TextBody ? ".txt" : ".bin");
            String tag = b instanceof TextBody ? "text-body" : "binary-body";
            File f = new File(s);
            sb.append("<" + tag + " name=\"" + f.getName() + "\"/>\r\n");
            URL expectedUrl = new URL(s);

            if (b instanceof TextBody) {
                String charset = e.getCharset();
                if (charset == null) {
                    charset = "ISO8859-1";
                }

                String s2 = IOUtils.toString(((TextBody) b).getReader());
                try {
                    String s1 = IOUtils.toString(expectedUrl.openStream(), charset);
                    assertEquals(f.getName(), s1, s2);
                } catch (FileNotFoundException ex) {
                    IOUtils.write(s2, new FileOutputStream(expectedUrl.getPath() + ".expected"));
                    fail("Expected file created.");
                }
            } else {
                try {
                    assertEqualsBinary(f.getName(), expectedUrl.openStream(),
                            ((BinaryBody) b).getInputStream());
                } catch (FileNotFoundException ex) {
                    IOUtils.copy(((BinaryBody) b).getInputStream(), new FileOutputStream(expectedUrl.getPath() + ".expected"));
                    fail("Expected file created.");
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
            assertEquals(msg + " (Position " + (++pos) + ")", b1, b2);

            if (b1 == -1 || b2 == -1) {
                break;
            }
        }
    }
}
