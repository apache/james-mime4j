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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.util.MessageUtils;

import junit.framework.TestCase;

public class HeaderTest extends TestCase {

    public static final String SUBJECT = "Subject: test";

    public static final String TO = "To: anyuser <any@user>";

    public void testHeader() {
        Header header = new Header();
        header.addField(Field.parse(SUBJECT));
        header.addField(Field.parse(TO));

        assertNotNull("Subject found", header.getField("Subject"));
        assertNotNull("To found", header.getField("To"));

        assertEquals("Headers equals", SUBJECT + "\r\n" + TO + "\r\n", header
                .toString());
    }
    
    static final int SWISS_GERMAN_HELLO [] = {
        0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4
    };
        
    private static String constructString(int [] unicodeChars) {
        StringBuffer buffer = new StringBuffer();
        if (unicodeChars != null) {
            for (int i = 0; i < unicodeChars.length; i++) {
                buffer.append((char)unicodeChars[i]); 
            }
        }
        return buffer.toString();
    }

    public void testWriteInStrictMode() throws Exception {
        String hello = constructString(SWISS_GERMAN_HELLO);
        Header header = new Header();
        header.addField(Field.parse("Hello: " + hello));
        
        Field field = header.getField("Hello");
        assertNotNull(field);
        assertEquals(hello, field.getBody());
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        header.writeTo(buffer, MessageUtils.STRICT_IGNORE);
        String s = buffer.toString(MessageUtils.ASCII.name());
        
        assertEquals("Hello: Gr?ezi_z?m?\r\n\r\n", s);

        buffer.reset();
        
        try {
            header.writeTo(buffer, MessageUtils.STRICT_ERROR);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }
    
    public void testWriteInLenientMode() throws Exception {
        String hello = constructString(SWISS_GERMAN_HELLO);
        Header header = new Header();
        header.addField(Field.parse("Hello: " + hello));
        header.addField(Field.parse("Content-type: text/plain; charset=" + 
                MessageUtils.ISO_8859_1.name()));
        
        Field field = header.getField("Hello");
        assertNotNull(field);
        assertEquals(hello, field.getBody());
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        header.writeTo(buffer, MessageUtils.LENIENT);
        String s = buffer.toString(MessageUtils.ISO_8859_1.name());
        
        assertEquals("Hello: " + hello + "\r\n" +
        		"Content-type: text/plain; charset=ISO-8859-1\r\n\r\n", s);
    }
    
}
