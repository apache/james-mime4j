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
}
