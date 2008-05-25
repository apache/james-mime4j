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
package org.apache.james.mime4j.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.util.MessageUtils;

import junit.framework.TestCase;

public class MessageWriteToTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSimpleMailStrictIgnore() throws Exception {
        Message message = createMessage(ExampleMail.RFC822_SIMPLE_BYTES);
        assertFalse("Not multipart", message.isMultipart());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out, MessageUtils.STRICT_IGNORE);
    }
    
    public void testSimpleMailStrictError() throws Exception {
        Message message = createMessage(ExampleMail.RFC822_SIMPLE_BYTES);
        assertFalse("Not multipart", message.isMultipart());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out, MessageUtils.STRICT_ERROR);
    }
    
    public void testSimpleMailLenient() throws Exception {
        Message message = createMessage(ExampleMail.RFC822_SIMPLE_BYTES);
        assertFalse("Not multipart", message.isMultipart());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out, MessageUtils.LENIENT);
    }
    
    private Message createMessage(byte[] octets) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(octets);
        Message message = new Message(in);
        return message;
    }
}
