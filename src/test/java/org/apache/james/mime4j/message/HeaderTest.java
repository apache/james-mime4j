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

import junit.framework.TestCase;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.CharsetUtil;

public class HeaderTest extends TestCase {

    public static final String SUBJECT = "Subject: test";

    public static final String TO = "To: anyuser <any@user>";

    public void testHeader() throws Exception {
        Header header = new Header();
        header.addField(AbstractField.parse(SUBJECT));
        header.addField(AbstractField.parse(TO));

        assertNotNull("Subject found", header.getField("Subject"));
        assertNotNull("To found", header.getField("To"));

        assertEquals("Headers equals", SUBJECT + "\r\n" + TO + "\r\n", header
                .toString());
    }
    
    private static final String SWISS_GERMAN_HELLO = "Gr\374ezi_z\344m\344";

    public void testWriteSpecialCharacters() throws Exception {
        String hello = SWISS_GERMAN_HELLO;
        Header header = new Header();
        header.addField(AbstractField.parse("Hello: " + hello));
        
        Field field = header.getField("Hello");
        assertNotNull(field);
        assertEquals(hello, field.getBody());
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        MessageWriter.DEFAULT.writeHeader(header, buffer);
        String s = buffer.toString(CharsetUtil.US_ASCII.name());
        
        assertEquals("Hello: Gr?ezi_z?m?\r\n\r\n", s);
    }

    public void testRemoveFields() throws Exception {
        Header header = new Header();
        header.addField(AbstractField.parse("Received: from foo by bar for james"));
        header.addField(AbstractField.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(AbstractField.parse("ReCeIvEd: from bar by foo for james"));

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
        Header header = new Header();
        header.addField(AbstractField.parse("Received: from foo by bar for james"));
        header.addField(AbstractField.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(AbstractField.parse("ReCeIvEd: from bar by foo for james"));

        assertEquals(0, header.removeFields("noSuchField"));

        assertEquals(3, header.getFields().size());
        assertEquals(2, header.getFields("received").size());
        assertEquals(1, header.getFields("Content-Type").size());
    }

    public void testSetField() throws Exception {
        Header header = new Header();
        header.addField(AbstractField.parse("From: mime4j@james.apache.org"));
        header.addField(AbstractField.parse("Received: from foo by bar for james"));
        header.addField(AbstractField.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(AbstractField.parse("ReCeIvEd: from bar by foo for james"));

        header.setField(AbstractField.parse("received: from nobody by noone for james"));

        assertEquals(3, header.getFields().size());
        assertEquals(1, header.getFields("received").size());

        assertEquals("From", header.getFields().get(0).getName());
        assertEquals("received", header.getFields().get(1).getName());
        assertEquals("Content-type", header.getFields().get(2).getName());
    }

    public void testSetNonExistantField() throws Exception {
        Header header = new Header();
        header.addField(AbstractField.parse("Received: from foo by bar for james"));
        header.addField(AbstractField.parse("Content-type: text/plain; charset=US-ASCII"));
        header.addField(AbstractField.parse("ReCeIvEd: from bar by foo for james"));

        header.setField(AbstractField.parse("Message-ID: <msg9901@apache.org>"));

        assertEquals(4, header.getFields().size());
        assertEquals(1, header.getFields("message-id").size());

        assertEquals("Message-ID", header.getFields().get(3).getName());
    }
    
}
