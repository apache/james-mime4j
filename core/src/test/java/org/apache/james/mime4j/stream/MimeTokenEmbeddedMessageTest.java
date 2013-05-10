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

package org.apache.james.mime4j.stream;

import org.apache.james.mime4j.ExampleMail;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MimeTokenEmbeddedMessageTest {

    MimeTokenStream stream;

    @Before
    public void setUp() throws Exception {
        stream = new MimeTokenStream();
        InputStream in = new ByteArrayInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_BYTES);
        stream.parse(in);
    }

    @Test
    public void testWhenRecurseShouldVisitInnerMailsAndInnerMultiparts() throws Exception {
        stream.setRecursionMode(RecursionMode.M_RECURSE);

        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_START_MULTIPART);

        nextIs(EntityState.T_PREAMBLE);

        // PART ONE
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream("Rhubarb!\r\n");
        nextIs(EntityState.T_END_BODYPART);

        // PART TWO
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream("987654321AHPLA\r\n");
        nextIs(EntityState.T_END_BODYPART);

        // PART THREE
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_START_MESSAGE);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_START_MULTIPART);
        nextIs(EntityState.T_PREAMBLE);
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream("Custard!\r\n");
        nextIs(EntityState.T_END_BODYPART);
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream("CUSTARDCUSTARDCUSTARD\r\n");
        nextIs(EntityState.T_END_BODYPART);
        nextIs(EntityState.T_END_MULTIPART);
        nextIs(EntityState.T_END_MESSAGE);
        nextIs(EntityState.T_END_BODYPART);

        // PART FOUR
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_START_MULTIPART);
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MULTIPART_MIXED);
        nextIs(EntityState.T_END_MULTIPART);
        nextIs(EntityState.T_END_BODYPART);
        nextIs(EntityState.T_EPILOGUE);
        nextIs(EntityState.T_END_MULTIPART);
        nextIs(EntityState.T_END_MESSAGE);
        nextIs(EntityState.T_END_OF_STREAM);
    }


    @Test
    public void testWhenFlatAtStartShouldIgnoreMultipartStructure() throws Exception {
        stream.setRecursionMode(RecursionMode.M_FLAT);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_BODY);

        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_BODY);

        nextIs(EntityState.T_END_MESSAGE);
    }

    @Test
    public void testWhenFlatShouldIgnoreInnerMailsAndInnerMultiparts() throws Exception {
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);

        nextIs(EntityState.T_START_MULTIPART);

        stream.setRecursionMode(RecursionMode.M_FLAT);
        nextIs(EntityState.T_PREAMBLE);

        // PART ONE
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream("Rhubarb!\r\n");
        nextIs(EntityState.T_END_BODYPART);

        // PART TWO
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream("987654321AHPLA\r\n");
        nextIs(EntityState.T_END_BODYPART);

        // PART THREE
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MAIL);
        nextIs(EntityState.T_END_BODYPART);

        // PART FOUR
        nextIs(EntityState.T_START_BODYPART);
        nextIs(EntityState.T_START_HEADER);
        nextIs(EntityState.T_FIELD);
        nextIs(EntityState.T_END_HEADER);
        nextIs(EntityState.T_BODY);
        checkInputStream(ExampleMail.MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MULTIPART_MIXED);
        nextIs(EntityState.T_END_BODYPART);
        nextIs(EntityState.T_EPILOGUE);
        nextIs(EntityState.T_END_MULTIPART);
    }

    private void checkInputStream(String expected) throws Exception {
        InputStream inputStream = stream.getInputStream();
        int next = inputStream.read();
        int i = 0;
        while (next != -1) {
            Assert.assertEquals("@" + i, expected.charAt(i++), (char) next);
            next = inputStream.read();
        }
        Assert.assertEquals(expected.length(), i);
    }

    private void nextIs(EntityState state) throws Exception {
        Assert.assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(stream.next()));
    }
}
