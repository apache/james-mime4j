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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MimeTokenStreamTest {

    MimeTokenStream stream;

    @Before
    public void setUp() throws Exception {
        stream = new MimeTokenStream();
    }

    @Test
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
        Assert.assertEquals(MimeTokenStream.stateToString(expected), MimeTokenStream.stateToString(stream.next()));
    }

    @Test
    public void aCommentInAFieldNameShouldNotRenameTheField() throws Exception {
        String message = "Fr(x)om: ceo@example.com\r\nFrom: sender@attacker.example\r\n(!!!: x\r\nSubject: hello\r\n\r\nbody\r\n";
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(new ByteArrayInputStream(message.getBytes(StandardCharsets.US_ASCII)));

        List<String> names = new ArrayList<>();
        for (EntityState state = stream.getState(); state != EntityState.T_END_HEADER; state = stream.next()) {
            if (state == EntityState.T_FIELD) {
                names.add(stream.getField().getName());
            }
        }
        Assert.assertEquals(Arrays.asList("Fr(x)om", "From", "(!!!", "Subject"), names);
    }

    private static List<EntityState> statesReadingAtStartMultipart(MimeConfig config, int len) throws Exception {
        String msg = "Content-Type: multipart/mixed; boundary=b\r\n"
            + "\r\n"
            + "--b\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "hello\r\n"
            + "--b--\r\n";

        MimeTokenStream stream = new MimeTokenStream(config);
        stream.parse(new ByteArrayInputStream(msg.getBytes(StandardCharsets.US_ASCII)));
        List<EntityState> states = new ArrayList<>();
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state = stream.next()) {
            states.add(state);
            if (state == EntityState.T_START_MULTIPART) {
                int read = stream.getInputStream().read(new byte[16], 0, len);
                if (len == 0) {
                    Assert.assertEquals(0, read);
                }
            }
        }
        return states;
    }

    @Test
    public void zeroLengthReadAtStartMultipartShouldNotSkipTheParts() throws Exception {
        for (MimeConfig config : new MimeConfig[] {MimeConfig.DEFAULT, MimeConfig.PERMISSIVE}) {
            List<EntityState> states = statesReadingAtStartMultipart(config, 0);
            Assert.assertTrue("body part lost: " + states, states.contains(EntityState.T_START_BODYPART));
            Assert.assertTrue("body part lost: " + states, states.contains(EntityState.T_BODY));
        }
    }

    @Test
    public void readingTheRawMultipartBodyShouldSkipTheParts() throws Exception {
        List<EntityState> states = statesReadingAtStartMultipart(MimeConfig.DEFAULT, 1);
        Assert.assertFalse(states.toString(), states.contains(EntityState.T_START_BODYPART));
        Assert.assertTrue(states.toString(), states.contains(EntityState.T_END_MULTIPART));
    }
}
