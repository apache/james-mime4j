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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Requalification of #44 on origin/master: with CRLF line ends, no message starts with a stray LF. */
public class RequalificationMboxTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private List<String> messages(String mbox, int maxMessageSize) throws Exception {
        File file = folder.newFile();
        Files.write(file.toPath(), mbox.getBytes(StandardCharsets.US_ASCII));
        List<String> result = new ArrayList<>();
        for (CharBufferWrapper message : MboxIterator.fromFile(file).maxMessageSize(maxMessageSize).build()) {
            result.add(message.toString());
        }
        return result;
    }

    @Test
    public void issue44_crlfMessagesShouldNotStartWithALineFeed() throws Exception {
        String[] fromLines = {"From a@b 2011", "From MAILER-DAEMON@x Mon Jan  1 00:00:00 2024",
            "From bob@example.com Tue Apr 26 02:27:54 2022"};
        StringBuilder failures = new StringBuilder();
        for (String from : fromLines) {
            for (int count : new int[] {1, 2, 5}) {
                for (int bodySize : new int[] {0, 10, 5000}) {
                    StringBuilder mbox = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        mbox.append(from).append("\r\nSubject: m").append(i).append("\r\n\r\n");
                        for (int j = 0; j < bodySize; j++) {
                            mbox.append(j % 70 == 69 ? "\r\n" : "x");
                        }
                        mbox.append("\r\n");
                    }
                    List<String> messages = messages(mbox.toString(), 64 * 1024);
                    if (messages.size() != count) {
                        failures.append(from).append(" count=").append(count).append(" size=").append(bodySize)
                            .append(": ").append(messages.size()).append(" messages\n");
                    }
                    for (String message : messages) {
                        if (!message.startsWith("Subject: m")) {
                            failures.append(from).append(" count=").append(count).append(" size=").append(bodySize)
                                .append(": starts with ").append(message.substring(0, Math.min(12, message.length()))
                                    .replace("\r", "\\r").replace("\n", "\\n")).append('\n');
                        }
                    }
                }
            }
        }
        Assert.assertEquals("", failures.toString());
    }
}
