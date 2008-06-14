/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class MaximalBodyDescriptorTest extends TestCase {

    MimeTokenStream parser;
    
    protected void setUp() throws Exception {
        super.setUp();
        parser = MimeTokenStream.createMaximalDescriptorStream();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMimeVersionDefault() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.RFC822_SIMPLE_BYTES);
        assertEquals(1, descriptor.getMimeMajorVersion());
        assertEquals(0, descriptor.getMimeMinorVersion());
        assertNull(descriptor.getMimeVersionParseException());
    }
    
    public void testMimeVersion() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_ASCII_COMMENT_IN_MIME_VERSION_BYTES);
        assertEquals(2, descriptor.getMimeMajorVersion());
        assertEquals(4, descriptor.getMimeMinorVersion());
        assertNull(descriptor.getMimeVersionParseException());
    }
    
    
    public void testMimeVersionHeaderBreak() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_ASCII_MIME_VERSION_SPANS_TWO_LINES_BYTES);
        assertEquals(4, descriptor.getMimeMajorVersion());
        assertEquals(1, descriptor.getMimeMinorVersion());
        assertNull(descriptor.getMimeVersionParseException());
    }
    
    private MaximalBodyDescriptor describe(byte[] mail) throws Exception {
        ByteArrayInputStream bias = new ByteArrayInputStream(mail);
        parser.parse(bias);
        int state = parser.next();
        while (state != MimeTokenStream.T_BODY && state != MimeTokenStream.T_END_OF_STREAM) 
        {
            state = parser.next();
        }
        assertEquals(MimeTokenStream.T_BODY, state);
        BodyDescriptor descriptor = parser.getBodyDescriptor();
        assertNotNull(descriptor);
        assertTrue("Parser is maximal so body descriptor should be maximal", descriptor instanceof MaximalBodyDescriptor);
        return (MaximalBodyDescriptor) descriptor;
    }
}
