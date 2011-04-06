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

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RawFieldTest extends TestCase {

    public void testPrivateConstructor() throws Exception {
        String s = "raw: stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);
        RawField field = new RawField(raw, 3, "raw", null);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    public void testPublicConstructor() throws Exception {
        RawField field1 = new RawField("raw", "stuff");
        Assert.assertNull(field1.getRaw());
        Assert.assertEquals("raw", field1.getName());
        Assert.assertEquals("stuff", field1.getBody());
        Assert.assertEquals("raw: stuff", field1.toString());

        RawField field2 = new RawField("raw", null);
        Assert.assertNull(field2.getRaw());
        Assert.assertEquals("raw", field2.getName());
        Assert.assertEquals(null, field2.getBody());
        Assert.assertEquals("raw: ", field2.toString());
    }

}
