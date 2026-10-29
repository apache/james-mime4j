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

package org.apache.james.mime4j.mboxiterator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MboxIteratorEdgeCasesTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private List<String> messages(String mbox, int maxMessageSize) throws IOException {
        File file = folder.newFile();
        Files.write(file.toPath(), mbox.getBytes(StandardCharsets.US_ASCII));
        List<String> result = new ArrayList<>();
        for (CharBufferWrapper message : MboxIterator.fromFile(file).maxMessageSize(maxMessageSize).build()) {
            result.add(message.toString());
        }
        return result;
    }

    @Test
    public void fromLineEndingTheFileShouldGiveAnEmptyMessage() throws IOException {
        Assert.assertEquals(Arrays.asList(""), messages("From a@b F2011", 1024));
    }

    @Test
    public void lastFromLineEndingTheFileShouldGiveAnEmptyMessage() throws IOException {
        Assert.assertEquals(Arrays.asList("body\n", ""), messages("From a@b 2011\nbody\nFrom c@d 2012", 1024));
    }

    @Test
    public void crlfLineEndsShouldNotLeaveAStrayLineFeed() throws IOException {
        Assert.assertEquals(Arrays.asList("Subject: a\r\n\r\nbody\r\n", "Subject: b\r\n"),
            messages("From a@b 2011\r\nSubject: a\r\n\r\nbody\r\nFrom c@d 2012\r\nSubject: b\r\n", 1024));
    }

    @Test
    public void lfLineEndsShouldStillBeSupported() throws IOException {
        Assert.assertEquals(Arrays.asList("Subject: a\n", "Subject: b\n"),
            messages("From a@b 2011\nSubject: a\nFrom c@d 2012\nSubject: b\n", 1024));
    }

    private static String repeat(char c, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(c);
        }
        return result.toString();
    }

    @Test
    public void lineCutByTheBufferShouldNotBeTakenForAFromLine() throws IOException {
        // "From c@d 2012" ends at char 64, where the first decoded buffer ends
        String body = repeat('x', 19) + "\nFrom c@d 2012x\nmore\n";
        Assert.assertEquals(Arrays.asList("m0\n", body), messages("From a@b 2011\nm0\nFrom e@f 2010\n" + body, 64));
    }

    @Test
    public void fromLineCutByTheBufferShouldStillSplitMessages() throws IOException {
        // "From c@d 2012" ends at char 64, where the first decoded buffer ends
        String first = repeat('x', 36) + "\n";
        Assert.assertEquals(Arrays.asList(first, "more\n"), messages("From a@b 2011\n" + first + "From c@d 2012\nmore\n", 64));
    }

    @Test
    public void firstLineCutByTheFirstBufferShouldNotBeAFromLine() throws IOException {
        // the first 64 chars end with 4 digits, the whole line does not
        String mbox = "From " + repeat('a', 55) + "2003abc\nbody\n";
        try {
            Assert.assertEquals(Arrays.asList(), messages(mbox, 64));
        } catch (IllegalArgumentException e) {
            // "does not contain From_ lines": acceptable
        }
    }

    @Test
    public void laterLineCutByTheFirstBufferShouldBeExaminedAgain() throws IOException {
        // "From a@b 2011" ends at char 64, after 50 chars of garbage: it is a From_ line
        Assert.assertEquals(Arrays.asList("body\n"), messages(repeat('y', 50) + "\nFrom a@b 2011\nbody\n", 64));
        // "From a@b 2011" ends at char 64, but the line goes on: it is not one
        try {
            Assert.assertEquals(Arrays.asList(), messages(repeat('y', 50) + "\nFrom a@b 2011x\nbody\n", 64));
        } catch (IllegalArgumentException e) {
            // "does not contain From_ lines": acceptable
        }
    }
}
