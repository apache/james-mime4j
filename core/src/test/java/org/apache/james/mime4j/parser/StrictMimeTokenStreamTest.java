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

import org.apache.james.mime4j.parser.Event;
import org.apache.james.mime4j.parser.MimeParseEventException;
import org.apache.james.mime4j.parser.MimeTokenStream;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class StrictMimeTokenStreamTest extends TestCase {
    
    private static final String HEADER_ONLY = "From: foo@abr.com\r\nSubject: A subject\r\n";
    private static final String CORRECT_HEADERS = HEADER_ONLY + "\r\n";
    
    public void testUnexpectedEndOfHeaders() throws Exception {
        
        MimeTokenStream parser = MimeTokenStream.createStrictValidationStream();
        
        parser.parse(new ByteArrayInputStream(HEADER_ONLY.getBytes()));
        
        assertEquals("Headers start", MimeTokenStream.T_START_HEADER, parser.next());
        assertEquals("Field", MimeTokenStream.T_FIELD, parser.next());
        try {
            parser.next();
            fail("Expected exception to be thrown");
        } catch (MimeParseEventException e) {
            assertEquals("Premature end of headers", Event.HEADERS_PREMATURE_END, e.getEvent());
        }
     }
    
    public void testCorrectEndOfHeaders() throws Exception {
        
        MimeTokenStream parser = MimeTokenStream.createStrictValidationStream();
        
        parser.parse(new ByteArrayInputStream(CORRECT_HEADERS.getBytes()));
        
        assertEquals("Headers start", MimeTokenStream.T_START_HEADER, parser.next());
        assertEquals("From header", MimeTokenStream.T_FIELD, parser.next());
        assertEquals("Subject header", MimeTokenStream.T_FIELD, parser.next());
        assertEquals("End message", MimeTokenStream.T_END_HEADER, parser.next());
     }
}
