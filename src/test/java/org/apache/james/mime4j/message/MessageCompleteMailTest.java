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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.james.mime4j.ExampleMail;

public class MessageCompleteMailTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testMultipartAlternative() throws Exception {
        Message message = createMessage(ExampleMail.MIME_MULTIPART_ALTERNATIVE_BYTES);
        assertTrue("Should be a multipart/alternative mail", message.isMultipart());
        Multipart part = (Multipart)message.getBody();
        assertEquals("alternative", part.getSubType());
    }    
    
    public void testMultipartMixed() throws Exception {
        Message message = createMessage(ExampleMail.MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTES);
        assertTrue("Should be a multipart/mixed mail", message.isMultipart());
        Multipart part = (Multipart)message.getBody();
        assertEquals("mixed", part.getSubType());
    }

    private Message createMessage(byte[] octets) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(octets);
        Message message = new Message(in);
        return message;
    }
}
