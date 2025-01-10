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

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ExtractPartWithDifferentLengthsTest {
    @Test
    public void testExtractPartWithDifferentLengths() throws Exception {
        StringBuilder partBuilder = new StringBuilder();
        for (int i = 1; i <= 5000; i++) {
            partBuilder.append(i % 80 == 0 ? "\n" : "A");
            String part = partBuilder.toString();
            String mimeMessage = createMimeMultipart(part);

            String extracted = extractPart(mimeMessage);
            Assert.assertEquals(part, extracted);
        }
    }

    private String createMimeMultipart(String part) {
        return "Content-type: multipart/mixed; boundary=QvEgqhjEnYxz\n"
            + "\n"
            + "--QvEgqhjEnYxz\n"
            + "Content-Type: text/plain\n"
            + "\n"
            + part
            + "\r\n"
            + "--QvEgqhjEnYxz--\n";
    }

    private String extractPart(String mimeMessage) throws
        MimeException, IOException {
        String[] resultWrapper = new String[1];

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void body(BodyDescriptor bd, InputStream is) throws IOException {
                resultWrapper[0] = new String(IOUtils.toString(is,
                    StandardCharsets.UTF_8).getBytes());
            }
        });
        parser.parse(new ByteArrayInputStream(mimeMessage.getBytes()));
        return resultWrapper[0];
    }
}
