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
import org.apache.james.mime4j.parser.MimeTokenStream;
import org.apache.james.mime4j.parser.RecursionMode;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class MimeStreamTokenMessageRfc822Test extends TestCase {

    MimeTokenStream stream;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        stream = new MimeTokenStream();
        stream.parse(new ByteArrayInputStream(ExampleMail.MIME_RFC822_SIMPLE_BYTES));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testShouldParseMessageRFC822CorrectWithDefaultConfiguration() throws Exception {
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MESSAGE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_OF_STREAM);
    }
    
    public void testShouldParseMessageRFC822CorrectWithNoRecurse() throws Exception {
        stream.setRecursionMode(RecursionMode.M_NO_RECURSE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_OF_STREAM);
    }
    
    public void testShouldParseMessageRFC822CorrectWithFlat() throws Exception {
        stream.setRecursionMode(RecursionMode.M_FLAT);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_OF_STREAM);
    }
    
    private void nextIs(int state) throws Exception {
        assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(stream.next()));
    }
}
