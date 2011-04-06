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

import junit.framework.TestCase;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.field.Field;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MimeWriter;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ContentUtil;

public class HeaderTest extends TestCase {

    public static final String SUBJECT = "Subject: test";

    public static final String TO = "To: anyuser <any@user>";

    public void testHeader() throws Exception {
        Header header = new HeaderImpl();
        header.addField(DefaultFieldParser.parse(SUBJECT));
        header.addField(DefaultFieldParser.parse(TO));

        assertNotNull("Subject found", header.getField("Subject"));
        assertNotNull("To found", header.getField("To"));

        assertEquals("Headers equals", SUBJECT + "\r\n" + TO + "\r\n", header
                .toString());
    }
    
    private static final String SWISS_GERMAN_HELLO = "Gr\374ezi_z\344m\344";

    public void testWriteSpecialCharacters() throws Exception {
        String hello = SWISS_GERMAN_HELLO;
        Header header = new HeaderImpl();
        header.addField(DefaultFieldParser.parse("Hello: " + hello));
        
        Field field = header.getField("Hello");
        assertNotNull(field);
        // field.getBody is already a 7 bit ASCII string, after MIME4J-151
        // assertEquals(hello, field.getBody());
        assertEquals(SWISS_GERMAN_HELLO, field.getBody());
        
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        
        MimeWriter.DEFAULT.writeHeader(header, outstream);
        byte[] b = outstream.toByteArray();
        ByteArrayBuffer buf = new ByteArrayBuffer(b.length);
        buf.append(b, 0, b.length);
        String s = ContentUtil.decode(buf);
        
        assertEquals("Hello: " + SWISS_GERMAN_HELLO + "\r\n\r\n", s);
    }

    public void testRemoveFields() throws Exception {
        Header header = new HeaderImpl();
        header.addField(DefaultFieldParser.parse("Received: from foo by bar for james"));
        header.addField(DefaultFieldParser.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(DefaultFieldParser.parse("ReCeIvEd: from bar by foo for james"));

        assertEquals(3, header.getFields().size());
        assertEquals(2, header.getFields("received").size());
        assertEquals(1, header.getFields("Content-Type").size());

        assertEquals(2, header.removeFields("rEcEiVeD"));

        assertEquals(1, header.getFields().size());
        assertEquals(0, header.getFields("received").size());
        assertEquals(1, header.getFields("Content-Type").size());

        assertEquals("Content-type", header.getFields().get(0).getName());
    }

    public void testRemoveNonExistantField() throws Exception {
        Header header = new HeaderImpl();
        header.addField(DefaultFieldParser.parse("Received: from foo by bar for james"));
        header.addField(DefaultFieldParser.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(DefaultFieldParser.parse("ReCeIvEd: from bar by foo for james"));

        assertEquals(0, header.removeFields("noSuchField"));

        assertEquals(3, header.getFields().size());
        assertEquals(2, header.getFields("received").size());
        assertEquals(1, header.getFields("Content-Type").size());
    }

    public void testSetField() throws Exception {
        Header header = new HeaderImpl();
        header.addField(DefaultFieldParser.parse("From: mime4j@james.apache.org"));
        header.addField(DefaultFieldParser.parse("Received: from foo by bar for james"));
        header.addField(DefaultFieldParser.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(DefaultFieldParser.parse("ReCeIvEd: from bar by foo for james"));

        header.setField(DefaultFieldParser.parse("received: from nobody by noone for james"));

        assertEquals(3, header.getFields().size());
        assertEquals(1, header.getFields("received").size());

        assertEquals("From", header.getFields().get(0).getName());
        assertEquals("received", header.getFields().get(1).getName());
        assertEquals("Content-type", header.getFields().get(2).getName());
    }

    public void testSetNonExistantField() throws Exception {
        Header header = new HeaderImpl();
        header.addField(DefaultFieldParser.parse("Received: from foo by bar for james"));
        header.addField(DefaultFieldParser.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(DefaultFieldParser.parse("ReCeIvEd: from bar by foo for james"));

        header.setField(DefaultFieldParser.parse("Message-ID: <msg9901@apache.org>"));

        assertEquals(4, header.getFields().size());
        assertEquals(1, header.getFields("message-id").size());

        assertEquals("Message-ID", header.getFields().get(3).getName());
    }
    
}
