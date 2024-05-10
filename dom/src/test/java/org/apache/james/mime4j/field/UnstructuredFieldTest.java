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

package org.apache.james.mime4j.field;

import java.nio.charset.StandardCharsets;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.UnstructuredField;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.junit.Assert;
import org.junit.Test;

public class UnstructuredFieldTest {

    @Test
    public void testGetBody() throws Exception {
        UnstructuredField f;

        f = (UnstructuredField) DefaultFieldParser.parse("Subject: Yada\r\n yada yada\r\n");
        Assert.assertEquals("Testing folding value 1", "Yada yada yada", f.getValue());

        f = (UnstructuredField) DefaultFieldParser.parse("Subject:  \r\n\tyada");
        Assert.assertEquals("Testing folding value 2", " \tyada", f.getValue());

        f = (UnstructuredField) DefaultFieldParser.parse("Subject:yada");
        Assert.assertEquals("Testing value without a leading ' '", "yada", f.getValue());
    }

    @Test
    public void testUnfoldWithEqualSign() throws Exception {
        UnstructuredField f = (UnstructuredField) DefaultFieldParser.parse("\n" +
            "References: <CAMpLFpB=uu_mqGwf5RToWqCfkd9cmZKBoJ782872YDgfp1d2sA@mail.gmail.com>\n" +
            " <CAMpLFpCVygEwb+t=FmD6TqiDLrQHkREvh=_2=ZinF8WH1-yxbQ@mail.gmail.com>\r\n");
        Assert.assertEquals("<CAMpLFpB=uu_mqGwf5RToWqCfkd9cmZKBoJ782872YDgfp1d2sA@mail.gmail.com> <CAMpLFpCVygEwb+t=FmD6TqiDLrQHkREvh=_2=ZinF8WH1-yxbQ@mail.gmail.com>", f.getValue());
    }

    @Test
    public void testGetBodyUtf8() throws Exception {
        UnstructuredField f;

        byte[] data = "Subject: Счет для ООО \"СТАНЦИЯ ВИРТУАЛЬНАЯ\" от ООО \"Цифровые системы\"".getBytes(StandardCharsets.UTF_8);

        f = (UnstructuredField) DefaultFieldParser.parse(new ByteArrayBuffer(data, true), DecodeMonitor.SILENT);
        Assert.assertEquals("Testing UTF8 value 1", "Счет для ООО \"СТАНЦИЯ ВИРТУАЛЬНАЯ\" от ООО \"Цифровые системы\"", f.getValue());
    }

}
