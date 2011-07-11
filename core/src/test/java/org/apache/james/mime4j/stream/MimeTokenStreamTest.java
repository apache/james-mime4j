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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.james.mime4j.ExampleMail;

public class MimeTokenStreamTest extends TestCase {

    MimeTokenStream stream;

    @Override
    public void setUp() throws Exception {
        stream = new MimeTokenStream();
    }

    public void testSetRecursionModeBeforeParse() throws Exception {
        stream.setRecursionMode(RecursionMode.M_NO_RECURSE);
        stream.parse(new ByteArrayInputStream(ExampleMail.MAIL_WITH_RFC822_PART_BYTES));
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_START_MULTIPART);
        checkNextIs(EntityState.T_PREAMBLE);
        checkNextIs(EntityState.T_START_BODYPART);
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_BODY);
        checkNextIs(EntityState.T_END_BODYPART);
        checkNextIs(EntityState.T_START_BODYPART);
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_BODY);
        checkNextIs(EntityState.T_END_BODYPART);
        checkNextIs(EntityState.T_START_BODYPART);
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_BODY);
        checkNextIs(EntityState.T_END_BODYPART);
        checkNextIs(EntityState.T_START_BODYPART);
        checkNextIs(EntityState.T_START_HEADER);
        checkNextIs(EntityState.T_FIELD);
        checkNextIs(EntityState.T_END_HEADER);
        checkNextIs(EntityState.T_BODY);
        checkNextIs(EntityState.T_END_BODYPART);
        checkNextIs(EntityState.T_EPILOGUE);
        checkNextIs(EntityState.T_END_MULTIPART);
        checkNextIs(EntityState.T_END_MESSAGE);
        checkNextIs(EntityState.T_END_OF_STREAM);
    }

    private void checkNextIs(EntityState expected) throws Exception {
        assertEquals(MimeTokenStream.stateToString(expected), MimeTokenStream.stateToString(stream.next()));
    }
}
