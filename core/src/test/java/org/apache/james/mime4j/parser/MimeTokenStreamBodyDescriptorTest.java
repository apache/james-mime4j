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

import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.parser.MimeTokenStream;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class MimeTokenStreamBodyDescriptorTest extends TestCase {

    MimeTokenStream parser;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new MimeTokenStream();
        parser.parse(new ByteArrayInputStream(ExampleMail.MIME_MULTIPART_ALTERNATIVE_BYTES));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testShouldReturnValidDescriptorForPreamble() throws Exception {
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_MULTIPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_PREAMBLE), MimeTokenStream.stateToString(parser.next()));
        BodyDescriptor descriptor = parser.getBodyDescriptor();
        assertNotNull(descriptor);
        assertEquals("1729", descriptor.getBoundary());
        assertEquals( "multipart/alternative", descriptor.getMimeType());
    }
    
    public void testShouldReturnValidDescriptorForEpilogue() throws Exception {
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_MULTIPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_PREAMBLE), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_BODYPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_BODYPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_BODYPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_BODYPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_BODYPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_START_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_FIELD), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_HEADER), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_BODY), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_END_BODYPART), MimeTokenStream.stateToString(parser.next()));
        assertEquals(MimeTokenStream.stateToString(MimeTokenStream.T_EPILOGUE), MimeTokenStream.stateToString(parser.next()));

        BodyDescriptor descriptor = parser.getBodyDescriptor();
        assertNotNull(descriptor);
        assertEquals("1729", descriptor.getBoundary());
        assertEquals( "multipart/alternative", descriptor.getMimeType());
    }
}
