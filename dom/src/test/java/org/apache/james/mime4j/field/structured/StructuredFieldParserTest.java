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

import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.james.mime4j.field.structured.parser.StructuredFieldParser;

public class StructuredFieldParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleField() throws Exception {
        final String string = "Field Value";
        assertEquals(string, parse(string));
    }
    
    public void testTrim() throws Exception {
        final String string = "Field Value";
        assertEquals(string, parse("    \t\r\n" + string + "  \t\r\n  "));
    }
    
    public void testFolding() throws Exception {
        assertEquals("Field Value", parse("Field \t\r\n  Value"));
    }
    
    public void testQuotedString() throws Exception {
        assertEquals("Field    Value", parse("\"Field    Value\""));
        assertEquals("Field\t\r\nValue", parse("\"Field\t\r\nValue\""));
        assertEquals("Field\t\r\nValue", parse("\"Field\t\r\n       \t       Value\""));
    }
    
    public void testComments() throws Exception {
        assertEquals("Field", parse("Fi(This is a comment)eld"));
        assertEquals("Field Value", parse("Fi(This is a comment)eld (A (very (nested) )comment)Value"));
    }
    
    public void testQuotedInComments() throws Exception {
        assertEquals("Fi(This is a comment)eld", parse("\"Fi(This is a comment)eld\""));
        assertEquals("Field Value", parse("Fi(This is a comment)eld (A (very (nested) )comment)Value"));
    }
    
    private String parse(String in) throws Exception {
        StructuredFieldParser parser = new StructuredFieldParser(new StringReader(in));
        parser.setFoldingPreserved(true);
        return parser.parse();
    }
}
