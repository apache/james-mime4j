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

import java.io.ByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.parser.EntityStateMachine;
import org.apache.james.mime4j.parser.EntityStates;
import org.apache.james.mime4j.parser.MimeEntity;
import org.apache.james.mime4j.parser.RecursionMode;
import org.apache.james.mime4j.stream.BufferedLineReaderInputStream;
import org.apache.james.mime4j.stream.RootInputStream;

import junit.framework.TestCase;

public class MimeEntityTest extends TestCase {

    public void testSimpleEntity() throws Exception {
        String message = 
            "To: Road Runner <runner@example.org>\r\n" +
            "From: Wile E. Cayote <wile@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Mail\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "a very important message";
        byte[] raw = message.getBytes("US-ASCII");
        ByteArrayInputStream instream = new ByteArrayInputStream(raw);
        RootInputStream rootStream = new RootInputStream(instream); 
        BufferedLineReaderInputStream rawstream = new BufferedLineReaderInputStream(rootStream, 12); 
        
        MimeEntity entity = new MimeEntity(
                rootStream,
                rawstream,
                null,
                EntityStates.T_START_MESSAGE,
                EntityStates.T_END_MESSAGE);
        
        
        assertEquals(EntityStates.T_START_MESSAGE, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_START_HEADER, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("To", entity.getFieldName());
        assertEquals(" Road Runner <runner@example.org>", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("From", entity.getFieldName());
        assertEquals(" Wile E. Cayote <wile@example.org>", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Date", entity.getFieldName());
        assertEquals(" Tue, 12 Feb 2008 17:34:09 +0000 (GMT)", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Subject", entity.getFieldName());
        assertEquals(" Mail", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Content-Type", entity.getFieldName());
        assertEquals(" text/plain", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_END_HEADER, entity.getState());
        try {
            entity.getFieldName();
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException expected) {
        }
        try {
            entity.getFieldValue();
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException expected) {
        }
        
        entity.advance();
        assertEquals(EntityStates.T_BODY, entity.getState());
        assertEquals("a very important message", IOUtils.toString(entity.getContentStream()));
        entity.advance();
        assertEquals(EntityStates.T_END_MESSAGE, entity.getState());
        try {
            entity.getContentStream();
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException expected) {
        }
        entity.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, entity.getState());
        try {
            entity.advance();
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException expected) {
        }
    }

    public void testMultipartEntity() throws Exception {
        String message = 
            "To: Road Runner <runner@example.org>\r\n" +
            "From: Wile E. Cayote <wile@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Mail\r\n" +
            "Content-Type: multipart/mixed;boundary=1729\r\n" +
            "\r\n" +
            "Hello!\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "\r\n" +
            "blah blah blah\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "\r\n" +
            "yada yada yada\r\n" +
            "--1729--\r\n" +
            "Goodbye!";
        byte[] raw = message.getBytes("US-ASCII");
        ByteArrayInputStream instream = new ByteArrayInputStream(raw);
        RootInputStream rootStream = new RootInputStream(instream); 
        BufferedLineReaderInputStream rawstream = new BufferedLineReaderInputStream(rootStream, 24); 
        
        MimeEntity entity = new MimeEntity(
                rootStream,
                rawstream,
                null,
                EntityStates.T_START_MESSAGE,
                EntityStates.T_END_MESSAGE);
        
        assertEquals(EntityStates.T_START_MESSAGE, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_START_HEADER, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("To", entity.getFieldName());
        assertEquals(" Road Runner <runner@example.org>", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("From", entity.getFieldName());
        assertEquals(" Wile E. Cayote <wile@example.org>", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Date", entity.getFieldName());
        assertEquals(" Tue, 12 Feb 2008 17:34:09 +0000 (GMT)", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Subject", entity.getFieldName());
        assertEquals(" Mail", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Content-Type", entity.getFieldName());
        assertEquals(" multipart/mixed;boundary=1729", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_END_HEADER, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_START_MULTIPART, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_PREAMBLE, entity.getState());
        assertEquals("Hello!", IOUtils.toString(entity.getContentStream()));
        
        EntityStateMachine p1 = entity.advance();
        assertNotNull(p1);
        
        assertEquals(EntityStates.T_START_BODYPART, p1.getState());
        p1.advance();
        assertEquals(EntityStates.T_START_HEADER, p1.getState());
        p1.advance();
        assertEquals(EntityStates.T_FIELD, p1.getState());
        assertEquals("Content-Type", p1.getFieldName());
        assertEquals(" text/plain; charset=US-ASCII", p1.getFieldValue());
        p1.advance();
        assertEquals(EntityStates.T_END_HEADER, p1.getState());
        p1.advance();
        assertEquals(EntityStates.T_BODY, p1.getState());
        assertEquals("blah blah blah", IOUtils.toString(p1.getContentStream()));
        p1.advance();
        assertEquals(EntityStates.T_END_BODYPART, p1.getState());
        p1.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, p1.getState());

        EntityStateMachine p2 = entity.advance();
        assertNotNull(p2);
        
        assertEquals(EntityStates.T_START_BODYPART, p2.getState());
        p2.advance();
        assertEquals(EntityStates.T_START_HEADER, p2.getState());
        p2.advance();
        assertEquals(EntityStates.T_FIELD, p2.getState());
        assertEquals("Content-Type", p2.getFieldName());
        assertEquals(" text/plain; charset=US-ASCII", p2.getFieldValue());
        p2.advance();
        assertEquals(EntityStates.T_END_HEADER, p2.getState());
        p2.advance();
        assertEquals(EntityStates.T_BODY, p2.getState());
        assertEquals("yada yada yada", IOUtils.toString(p2.getContentStream()));
        p2.advance();
        assertEquals(EntityStates.T_END_BODYPART, p2.getState());
        p2.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, p2.getState());

        entity.advance();
        assertEquals(EntityStates.T_EPILOGUE, entity.getState());
        assertEquals("Goodbye!", IOUtils.toString(entity.getContentStream()));
        entity.advance();
        assertEquals(EntityStates.T_END_MULTIPART, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_END_MESSAGE, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, entity.getState());
    }
    
    public void testRawEntity() throws Exception {
        String message = 
            "To: Road Runner <runner@example.org>\r\n" +
            "From: Wile E. Cayote <wile@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Mail\r\n" +
            "Content-Type: multipart/mixed;boundary=1729\r\n" +
            "\r\n" +
            "Hello!\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "\r\n" +
            "blah blah blah\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "\r\n" +
            "yada yada yada\r\n" +
            "--1729--\r\n" +
            "Goodbye!";
        byte[] raw = message.getBytes("US-ASCII");
        ByteArrayInputStream instream = new ByteArrayInputStream(raw);
        RootInputStream rootStream = new RootInputStream(instream); 
        BufferedLineReaderInputStream rawstream = new BufferedLineReaderInputStream(rootStream, 24); 
        
        MimeEntity entity = new MimeEntity(
                rootStream,
                rawstream,
                null,
                EntityStates.T_START_MESSAGE,
                EntityStates.T_END_MESSAGE);
        
        entity.setRecursionMode(RecursionMode.M_RAW);
        
        assertEquals(EntityStates.T_START_MESSAGE, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_START_HEADER, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("To", entity.getFieldName());
        assertEquals(" Road Runner <runner@example.org>", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("From", entity.getFieldName());
        assertEquals(" Wile E. Cayote <wile@example.org>", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Date", entity.getFieldName());
        assertEquals(" Tue, 12 Feb 2008 17:34:09 +0000 (GMT)", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Subject", entity.getFieldName());
        assertEquals(" Mail", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_FIELD, entity.getState());
        assertEquals("Content-Type", entity.getFieldName());
        assertEquals(" multipart/mixed;boundary=1729", entity.getFieldValue());
        entity.advance();
        assertEquals(EntityStates.T_END_HEADER, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_START_MULTIPART, entity.getState());
        
        entity.advance();
        assertEquals(EntityStates.T_PREAMBLE, entity.getState());
        assertEquals("Hello!", IOUtils.toString(entity.getContentStream()));
        
        EntityStateMachine p1 = entity.advance();
        assertNotNull(p1);
        
        assertEquals(EntityStates.T_RAW_ENTITY, p1.getState());
        assertNull(p1.getBodyDescriptor());
        assertNull(p1.getField());
        assertNull(p1.getFieldName());
        assertNull(p1.getFieldValue());
        assertEquals(
                "Content-Type: text/plain; charset=US-ASCII\r\n" +
                "\r\n" +
                "blah blah blah", IOUtils.toString(p1.getContentStream()));
        p1.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, p1.getState());

        EntityStateMachine p2 = entity.advance();
        assertNotNull(p2);
        
        assertEquals(EntityStates.T_RAW_ENTITY, p2.getState());
        assertNull(p2.getBodyDescriptor());
        assertNull(p2.getField());
        assertNull(p2.getFieldName());
        assertNull(p2.getFieldValue());
        assertEquals(
                "Content-Type: text/plain; charset=US-ASCII\r\n" +
                "\r\n" +
                "yada yada yada", IOUtils.toString(p2.getContentStream()));
        p2.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, p2.getState());

        entity.advance();
        assertEquals(EntityStates.T_EPILOGUE, entity.getState());
        assertEquals("Goodbye!", IOUtils.toString(entity.getContentStream()));
        entity.advance();
        assertEquals(EntityStates.T_END_MULTIPART, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_END_MESSAGE, entity.getState());
        entity.advance();
        assertEquals(EntityStates.T_END_OF_STREAM, entity.getState());
    }

}
