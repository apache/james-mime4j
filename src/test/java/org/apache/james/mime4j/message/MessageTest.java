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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.field.Field;

/**
 * 
 *
 * 
 * @version $Id: MessageTest.java,v 1.4 2004/10/02 12:41:11 ntherning Exp $
 */
public class MessageTest extends TestCase {
    private Header headerTextPlain = null;
    private Header headerMessageRFC822 = null;
    private Header headerEmpty = null;
    private Header headerMultipartMixed = null;
    private Header headerMultipartDigest = null;

    @Override
    public void setUp() throws Exception {
        headerTextPlain = new Header();
        headerMessageRFC822 = new Header();
        headerEmpty = new Header();
        headerMultipartMixed = new Header();
        headerMultipartDigest = new Header();
        
        headerTextPlain.addField(
                Field.parse("Content-Type: text/plain"));
        headerMessageRFC822.addField(
                Field.parse("Content-Type: message/RFC822"));
        headerMultipartMixed.addField(
                Field.parse("Content-Type: multipart/mixed; boundary=foo"));
        headerMultipartDigest.addField(
                Field.parse("Content-Type: multipart/digest; boundary=foo"));
    }
    
    public void testGetParts() {
    }

    public void testGetMimeType() {
        Message parent = null;
        Message child = null;
        
        parent = new Message();
        child = new Message();
        child.setParent(parent);
        parent.setHeader(headerMultipartDigest);
        child.setHeader(headerEmpty);
        assertEquals("multipart/digest, empty", "message/rfc822", 
                child.getMimeType());
        child.setHeader(headerTextPlain);
        assertEquals("multipart/digest, text/plain", "text/plain", 
                child.getMimeType());
        child.setHeader(headerMessageRFC822);
        assertEquals("multipart/digest, message/rfc822", "message/rfc822", 
                child.getMimeType());
        
        parent = new Message();
        child = new Message();
        child.setParent(parent);
        parent.setHeader(headerMultipartMixed);
        child.setHeader(headerEmpty);
        assertEquals("multipart/mixed, empty", "text/plain", 
                child.getMimeType());
        child.setHeader(headerTextPlain);
        assertEquals("multipart/mixed, text/plain", "text/plain", 
                child.getMimeType());
        child.setHeader(headerMessageRFC822);
        assertEquals("multipart/mixed, message/rfc822", "message/rfc822", 
                child.getMimeType());
        
        child = new Message();
        child.setHeader(headerEmpty);
        assertEquals("null, empty", "text/plain", child.getMimeType());
        child.setHeader(headerTextPlain);
        assertEquals("null, text/plain", "text/plain", child.getMimeType());
        child.setHeader(headerMessageRFC822);
        assertEquals("null, message/rfc822", "message/rfc822", 
                child.getMimeType());
    }

    public void testIsMultipart() {
        Message m = new Message();
        
        m.setHeader(headerEmpty);
        assertTrue("empty", !m.isMultipart());
        
        m.setHeader(headerTextPlain);
        assertTrue("text/plain", !m.isMultipart());
        
        m.setHeader(headerMultipartDigest);
        assertTrue("multipart/digest", m.isMultipart());
        
        m.setHeader(headerMultipartMixed);
        assertTrue("multipart/mixed", m.isMultipart());
    }
    
    public void testWriteTo() throws Exception {
        byte[] inputByte = getRawMessageAsByteArray();

        Message m = new Message(new ByteArrayInputStream(inputByte));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        m.writeTo(out, Mode.LENIENT);

        InputStream output = new ByteArrayInputStream(out.toByteArray());

        int b = -1;
        int i = 0;
        while ((b = output.read()) != -1) {
            assertEquals("same byte", b, inputByte[i]);
            i++;
        }
    }

    public void testAddHeaderWriteTo() throws Exception {
        String headerName = "testheader";
        String headerValue = "testvalue";
        String testheader = headerName + ": " + headerValue;

        byte[] inputByte = getRawMessageAsByteArray();

        Message m = new Message(new ByteArrayInputStream(inputByte));
        m.getHeader().addField(Field.parse(testheader));

        assertEquals("header added", m.getHeader().getField(headerName)
                .getBody(), headerValue);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.writeTo(out, Mode.LENIENT);
        List<?> lines = IOUtils.readLines((new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out
                        .toByteArray())))));

        assertTrue("header added", lines.contains(testheader));
    }

    public void testDisposeGetsPropagatedToBody() throws Exception {
        DummyBody body1 = new DummyBody();
        BodyPart part1 = new BodyPart();
        part1.setHeader(headerEmpty);
        part1.setBody(body1);

        DummyBody body2 = new DummyBody();
        BodyPart part2 = new BodyPart();
        part2.setHeader(headerEmpty);
        part2.setBody(body2);

        Multipart mp = new Multipart("mixed");
        mp.addBodyPart(part1);
        mp.addBodyPart(part2);

        Message m = new Message();
        m.setHeader(headerMultipartMixed);
        m.setBody(mp);

        assertFalse(body1.disposed);
        assertFalse(body2.disposed);

        m.dispose();

        assertTrue(body1.disposed);
        assertTrue(body2.disposed);
    }

    private byte[] getRawMessageAsByteArray() {
        StringBuilder header = new StringBuilder();
        StringBuilder body = new StringBuilder();
        StringBuilder complete = new StringBuilder();

        header.append("Date: Wed, 21 Feb 2007 11:09:27 +0100\r\n");
        header.append("From: Test <test@test>\r\n");
        header.append("To: Norman Maurer <nm@byteaction.de>\r\n");
        header.append("Subject: Testmail\r\n");
        header
                .append("Content-Type: text/plain; charset=ISO-8859-15; format=flowed\r\n");
        header.append("Content-Transfer-Encoding: 8bit\r\n\r\n");
        body.append("testbody\r\n");
        complete.append(header);
        complete.append(body);

        return complete.toString().getBytes();
    }

    private static final class DummyBody extends SingleBody {

        public boolean disposed = false;

        public void writeTo(OutputStream out, Mode mode) throws IOException {
            out.write("dummy".getBytes("US-ASCII"));
        }

        @Override
        public void dispose() {
            disposed = true;
        }

    }

}
