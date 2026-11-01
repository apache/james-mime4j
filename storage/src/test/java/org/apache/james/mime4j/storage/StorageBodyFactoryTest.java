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

package org.apache.james.mime4j.storage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class StorageBodyFactoryTest {

    @Test
    public void textBodyShouldFallBackToUsAsciiForACharsetThatCannotEncode() throws Exception {
        for (String decodeOnly : new String[] {"ISO-2022-CN", "x-JISAutoDetect"}) {
            StorageBodyFactory factory = new StorageBodyFactory(new MemoryStorageProvider(), DecodeMonitor.SILENT);
            TextBody body = factory.textBody("okay", decodeOnly);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            body.writeTo(out);

            Assert.assertEquals("us-ascii", body.getMimeCharset().toLowerCase(Locale.ROOT));
            Assert.assertEquals("okay", new String(out.toByteArray(), StandardCharsets.US_ASCII));
            Assert.assertEquals("okay", new String(ContentUtil.buffer(body.getInputStream()),
                StandardCharsets.US_ASCII));
        }
    }

    @Test
    public void textBodyShouldWarnWhenFallingBackForACharsetThatCannotEncode() {
        final List<String> warnings = new ArrayList<>();
        DecodeMonitor monitor = new DecodeMonitor() {
            @Override
            public boolean warn(String error, String dropDesc) {
                warnings.add(error);
                return false;
            }

            @Override
            public boolean isListening() {
                return true;
            }
        };
        new StorageBodyFactory(new MemoryStorageProvider(), monitor).textBody("okay", "ISO-2022-CN");

        Assert.assertEquals(1, warnings.size());
        Assert.assertTrue(warnings.get(0), warnings.get(0).contains("ISO-2022-CN"));
    }

    @Test
    public void textBodyShouldKeepACharsetThatCanEncode() {
        TextBody body = new StorageBodyFactory(new MemoryStorageProvider(), DecodeMonitor.SILENT)
            .textBody("okay", "ISO-8859-1");
        Assert.assertEquals("ISO-8859-1", body.getMimeCharset());
    }
}
