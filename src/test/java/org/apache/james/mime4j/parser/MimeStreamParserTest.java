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

import org.apache.james.mime4j.BodyDescriptor;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.log4j.BasicConfigurator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * 
 *
 * 
 * @version $Id: MimeStreamParserTest.java,v 1.6 2005/02/11 10:12:02 ntherning Exp $
 */
public class MimeStreamParserTest extends TestCase {

    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }
        /*
    public void testRootAndBodyStreamsAreSynched() throws IOException {
        File dir = new File("testmsgs");
        File[] files = dir.listFiles();
        
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            
            if (f.getName().toLowerCase().endsWith(".msg")) {
                final RandomAccessFile file = new RandomAccessFile(f, "r");
                final EOLTrackingInputStream rootStream = 
                    new EOLTrackingInputStream(
                       new RandomAccessFileInputStream(file, 0, file.length()));
                
                ContentHandler handler = new AbstractContentHandler() {
                    public void body(BodyDescriptor bd, InputStream expected) throws IOException {
                       int pos = rootStream.getMark();
                       if (expected instanceof RootInputStream) {
                           pos++;
                       }
                       InputStream actual = 
                           new EOLConvertingInputStream(
                                 new RandomAccessFileInputStream(file, pos, file.length()));
                       
                       StringBuffer sb1 = new StringBuffer();
                       StringBuffer sb2 = new StringBuffer();
                       int b = 0;
                       while ((b = expected.read()) != -1) {
                           sb1.append((char) (b & 0xff));
                           sb2.append((char) (actual.read() & 0xff));
                       }
                       assertEquals(sb1.toString(), sb2.toString());
                    }
                };
                
                System.out.println("Testing synch of " + f.getName());
                
                MimeStreamParser parser = new MimeStreamParser();
                parser.setContentHandler(handler);
                parser.parse(rootStream);
            }
        }
    }*/
    
    public void testBoundaryInEpilogue() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("From: foo@bar.com\r\n");
        sb.append("To: someone@else.com\r\n");
        sb.append("Content-type: multipart/something; boundary=myboundary\r\n");
        sb.append("\r\n");
        sb.append("This is the preamble.\r\n");
        sb.append("--myboundary\r\n");
        sb.append("Content-type: text/plain\r\n");
        sb.append("\r\n");
        sb.append("This is the first body.\r\n");
        sb.append("It's completely meaningless.\r\n");
        sb.append("After this line the body ends.\r\n");
        sb.append("\r\n");
        sb.append("--myboundary--\r\n");
        
        StringBuffer epilogue = new StringBuffer();
        epilogue.append("Content-type: text/plain\r\n");
        epilogue.append("\r\n");
        epilogue.append("This is actually the epilogue but it looks like a second body.\r\n");
        epilogue.append("Yada yada yada.\r\n");
        epilogue.append("\r\n");
        epilogue.append("--myboundary--\r\n");
        epilogue.append("This is still the epilogue.\r\n");
        
        sb.append(epilogue.toString());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("US-ASCII"));
        
        final StringBuffer actual = new StringBuffer();
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void epilogue(InputStream is) throws IOException {
                int b;
                while ((b = is.read()) != -1) {
                    actual.append((char) b);
                }
            }
        });
        parser.parse(bais);
        
        assertEquals(epilogue.toString(), actual.toString());
    }
    
    public void testParseOneLineFields() throws Exception {
        StringBuffer sb = new StringBuffer();
        final LinkedList expected = new LinkedList();
        expected.add("From: foo@abr.com");
        sb.append(expected.getLast().toString() + "\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast().toString() + "\r\n");
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
    }
    
    public void testCRWithoutLFInHeader() throws Exception {
        /*
         * Test added because \r:s not followed by \n:s in the header would
         * cause an infinite loop. 
         */
        StringBuffer sb = new StringBuffer();
        final LinkedList expected = new LinkedList();
        expected.add("The-field: This field\r\rcontains CR:s\r\r"
                        + "not\r\n\tfollowed by LF");
        sb.append(expected.getLast().toString() + "\r\n");
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
    }
    
    public void testParseMultiLineFields() throws Exception {
        StringBuffer sb = new StringBuffer();
        final LinkedList expected = new LinkedList();
        expected.add("Received: by netmbx.netmbx.de (/\\==/\\ Smail3.1.28.1)\r\n"
                   + "\tfrom mail.cs.tu-berlin.de with smtp\r\n"
                   + "\tid &lt;m0uWPrO-0004wpC&gt;;"
                        + " Wed, 19 Jun 96 18:12 MES");
        sb.append(expected.getLast().toString() + "\r\n");
        expected.add("Subject: A folded subject\r\n Line 2\r\n\tLine 3");
        sb.append(expected.getLast().toString() + "\r\n");
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
    }
    
    public void testStop() throws Exception {
        final MimeStreamParser parser = new MimeStreamParser();
        TestHandler handler = new TestHandler() {
            public void endHeader() {
                super.endHeader();
                parser.stop();
            }
        };
        parser.setContentHandler(handler);

        String msg = "Subject: Yada yada\r\n"
                   + "From: foo@bar.com\r\n"
                   + "\r\n"
                   + "Line 1\r\n"
                   + "Line 2\r\n";
        String expected = "<message>\r\n"
                        + "<header>\r\n"
                        + "<field>\r\n"
                        + "Subject: Yada yada"
                        + "</field>\r\n"
                        + "<field>\r\n"
                        + "From: foo@bar.com"
                        + "</field>\r\n"
                        + "</header>\r\n"
                        + "<body>\r\n"
                        + "</body>\r\n"
                        + "</message>\r\n";
        
        parser.parse(new ByteArrayInputStream(msg.getBytes()));
        String result = handler.sb.toString();
        
        assertEquals(expected, result);
    }
    
    /*
     * Tests that invalid fields are ignored.
     */
    public void testInvalidFields() throws Exception {
        StringBuffer sb = new StringBuffer();
        final LinkedList expected = new LinkedList();
        sb.append("From - foo@abr.com\r\n");
        expected.add("From: some@one.com");
        sb.append(expected.getLast().toString() + "\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast().toString() + "\r\n");
        sb.append("A line which should be ignored\r\n");
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
    }

    /*
     * Tests that empty streams still generate the expected series of events.
     */
    public void testEmptyStream() throws Exception {
        final LinkedList expected = new LinkedList();
        expected.add("startMessage");
        expected.add("startHeader");
        expected.add("endHeader");
        expected.add("body");
        expected.add("endMessage");
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void body(BodyDescriptor bd, InputStream is) {
                assertEquals((String) expected.removeFirst(), "body");
            }
            
            public void endMultipart() {
                fail("endMultipart shouldn't be called for empty stream");
            }

            public void endBodyPart() {
                fail("endBodyPart shouldn't be called for empty stream");
            }

            public void endHeader() {
                assertEquals((String) expected.removeFirst(), "endHeader");
            }

            public void endMessage() {
                assertEquals((String) expected.removeFirst(), "endMessage");
            }

            public void field(String fieldData) {
                fail("field shouldn't be called for empty stream");
            }

            public void startMultipart(BodyDescriptor bd) {
                fail("startMultipart shouldn't be called for empty stream");
            }

            public void startBodyPart() {
                fail("startBodyPart shouldn't be called for empty stream");
            }

            public void startHeader() {
                assertEquals((String) expected.removeFirst(), "startHeader");
            }

            public void startMessage() {
                assertEquals((String) expected.removeFirst(), "startMessage");
            }
        });
        
        parser.parse(new ByteArrayInputStream(new byte[0]));
        
        assertEquals(0, expected.size());
    }
    
    /*
     * Tests parsing of empty headers.
     */
    public void testEmpyHeader() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append("The body is right here\r\n");
        
        final StringBuffer body = new StringBuffer();
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                fail("No fields should be reported");
            }
            public void body(BodyDescriptor bd, InputStream is) throws IOException {
                int b;
                while ((b = is.read()) != -1) {
                    body.append((char) b);
                }
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals("The body is right here\r\n", body.toString());
    }
    
    /*
     * Tests parsing of empty body.
     */
    public void testEmptyBody() throws Exception {
        StringBuffer sb = new StringBuffer();
        final LinkedList expected = new LinkedList();
        expected.add("From: some@one.com");
        sb.append(expected.getLast().toString() + "\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast().toString() + "\r\n\r\n");
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
            public void body(BodyDescriptor bd, InputStream is) throws IOException {
                assertEquals(-1, is.read());
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
    }
    
    /*
     * Tests that invalid fields are ignored.
     */
    public void testPrematureEOFAfterFields() throws Exception {
        StringBuffer sb = new StringBuffer();
        final LinkedList expected = new LinkedList();
        expected.add("From: some@one.com");
        sb.append(expected.getLast().toString() + "\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast().toString());
        
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
        
        sb = new StringBuffer();
        expected.clear();
        expected.add("From: some@one.com");
        sb.append(expected.getLast().toString() + "\r\n");
        expected.add("Subject: A subject");
        sb.append(expected.getLast().toString() + "\r\n");
        
        parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void field(String fieldData) {
                assertEquals((String) expected.removeFirst(), fieldData);
            }
        });
        
        parser.parse(new ByteArrayInputStream(sb.toString().getBytes()));
        
        assertEquals(0, expected.size());
    }
    
    public void testAutomaticContentDecoding() throws Exception {
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentDecoding(true);
        TestHandler handler = new TestHandler();
        parser.setContentHandler(handler);

        String msg = "Subject: Yada yada\r\n"
                   + "From: foo@bar.com\r\n"
                   + "Content-Type: application/octet-stream\r\n"
                   + "Content-Transfer-Encoding: base64\r\n"
                   + "\r\n"
                   + "V2hvIGF0ZSBteSBjYWtlPwo=";
        String expected = "<message>\r\n"
                        + "<header>\r\n"
                        + "<field>\r\n"
                        + "Subject: Yada yada"
                        + "</field>\r\n"
                        + "<field>\r\n"
                        + "From: foo@bar.com"
                        + "</field>\r\n"
                        + "<field>\r\n"
                        + "Content-Type: application/octet-stream"
                        + "</field>\r\n"
                        + "<field>\r\n"
                        + "Content-Transfer-Encoding: base64"
                        + "</field>\r\n"
                        + "</header>\r\n"
                        + "<body>\r\n"
                        + "Who ate my cake?\n"
                        + "</body>\r\n"
                        + "</message>\r\n";
        
        parser.parse(new ByteArrayInputStream(msg.getBytes()));
        String result = handler.sb.toString();
        
        assertEquals(expected, result);
    }
    
}
