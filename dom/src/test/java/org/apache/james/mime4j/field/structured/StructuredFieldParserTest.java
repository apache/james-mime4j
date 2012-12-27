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

package org.apache.james.mime4j.field.structured;

import org.apache.james.mime4j.field.structured.parser.StructuredFieldParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class StructuredFieldParserTest {

    @Test
    public void testSimpleField() throws Exception {
        final String string = "Field Value";
        Assert.assertEquals(string, parse(string));
    }

    @Test
    public void testTrim() throws Exception {
        final String string = "Field Value";
        Assert.assertEquals(string, parse("    \t\r\n" + string + "  \t\r\n  "));
    }

    @Test
    public void testFolding() throws Exception {
        Assert.assertEquals("Field Value", parse("Field \t\r\n  Value"));
    }

    @Test
    public void testQuotedString() throws Exception {
        Assert.assertEquals("Field    Value", parse("\"Field    Value\""));
        Assert.assertEquals("Field\t\r\nValue", parse("\"Field\t\r\nValue\""));
        Assert.assertEquals("Field\t\r\nValue", parse("\"Field\t\r\n       \t       Value\""));
    }

    @Test
    public void testComments() throws Exception {
        Assert.assertEquals("Field", parse("Fi(This is a comment)eld"));
        Assert.assertEquals("Field Value", parse("Fi(This is a comment)eld (A (very (nested) )comment)Value"));
    }

    @Test
    public void testQuotedInComments() throws Exception {
        Assert.assertEquals("Fi(This is a comment)eld", parse("\"Fi(This is a comment)eld\""));
        Assert.assertEquals("Field Value", parse("Fi(This is a comment)eld (A (very (nested) )comment)Value"));
    }

    private String parse(String in) throws Exception {
        StructuredFieldParser parser = new StructuredFieldParser(new StringReader(in));
        parser.setFoldingPreserved(true);
        return parser.parse();
    }
}
