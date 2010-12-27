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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

public class MimeTokenEmbeddedMessageTest extends TestCase {
    
    MimeTokenStream stream;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        stream = new MimeTokenStream();
        InputStream in = new ByteArrayInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_BYTES);
        stream.parse(in);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWhenRecurseShouldVisitInnerMailsAndInnerMultiparts() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_RECURSE);

        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        
        nextIs(MimeTokenStream.T_PREAMBLE);

        // PART ONE
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream("Rhubarb!\r\n");
        nextIs(MimeTokenStream.T_END_BODYPART);
        
        // PART TWO
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream("987654321AHPLA\r\n");
        nextIs(MimeTokenStream.T_END_BODYPART);
        
        // PART THREE
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MESSAGE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream("Custard!\r\n");
        nextIs(MimeTokenStream.T_END_BODYPART);
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream("CUSTARDCUSTARDCUSTARD\r\n");
        nextIs(MimeTokenStream.T_END_BODYPART);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);   
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_BODYPART);
        
        // PART FOUR
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MULTIPART);
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MULTIPART_MIXED);
        nextIs(MimeTokenStream.T_END_MULTIPART);
        nextIs(MimeTokenStream.T_END_BODYPART);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextIs(MimeTokenStream.T_END_OF_STREAM);
    }
    
    
    public void testWhenFlatAtStartShouldIgnoreMultipartStructure() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_FLAT);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_BODY);
        
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_BODY);
        
        nextIs(MimeTokenStream.T_END_MESSAGE);
    }
    
    public void testWhenFlatShouldIgnoreInnerMailsAndInnerMultiparts() throws Exception {
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        
        stream.setRecursionMode(MimeTokenStream.M_FLAT);
        nextIs(MimeTokenStream.T_PREAMBLE);

        // PART ONE
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream("Rhubarb!\r\n");
        nextIs(MimeTokenStream.T_END_BODYPART);
        
        // PART TWO
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream("987654321AHPLA\r\n");
        nextIs(MimeTokenStream.T_END_BODYPART);
        
        // PART THREE
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MAIL);
        nextIs(MimeTokenStream.T_END_BODYPART);
        
        // PART FOUR
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MULTIPART_MIXED);
        nextIs(MimeTokenStream.T_END_BODYPART);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
    }
    
    private void checkInputStream(String expected) throws Exception {
        InputStream inputStream = stream.getInputStream();
        int next = inputStream.read();
        int i=0;
        while (next != -1) {
            assertEquals("@" + i, expected.charAt(i++), (char) next);
            next = inputStream.read();
        }
        assertEquals(expected.length(), i);
    }
    
    private void nextIs(int state) throws Exception {
        assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(stream.next()));
    }
}
