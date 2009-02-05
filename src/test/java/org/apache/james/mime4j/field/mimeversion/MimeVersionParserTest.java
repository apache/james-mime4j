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

package org.apache.james.mime4j.field.mimeversion;

import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.james.mime4j.field.mimeversion.parser.MimeVersionParser;
import org.apache.james.mime4j.field.mimeversion.parser.ParseException;

public class MimeVersionParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPlainLine() throws Exception {
        check("2.4", 2, 4);
        check("25.344", 25, 344);
        check("0.1", 0, 1);
        check("123234234.0", 123234234, 0);
    }
    
    public void testLineWithComments() throws Exception {
        check("2(A comment).4", 2, 4);
        check("2(.8).4", 2, 4);
        check("(A comment)2.4", 2, 4);
        check("2.4(A comment)", 2, 4);
        check("2.(A comment)4", 2, 4);
    }
    
    public void testLineWithNestedComments() throws Exception {
        check("2(4.45 ( Another ()comment () blah (Wobble(mix)))Whatever).4", 2, 4);
    }
    
    public void testEmptyLine() throws Exception {
        try {
            parse("(This is just a comment)");
            fail("Expected exception to be thrown");
        } catch (ParseException e) {
            //expected
        }
    }
    
    private void check(String input, int expectedMajorVersion, int expectedMinorVersion) throws Exception {
        MimeVersionParser parser = parse(input);
        assertEquals("Major version number", expectedMajorVersion, parser.getMajorVersion());
        assertEquals("Minor version number", expectedMinorVersion, parser.getMinorVersion());
    }

    private MimeVersionParser parse(String input) throws ParseException {
        MimeVersionParser parser = new MimeVersionParser(new StringReader(input));
        parser.parseAll();
        return parser;
    }
}
