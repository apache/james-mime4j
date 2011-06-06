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

import junit.framework.TestCase;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

public class LenientMimeVersionParserTest extends TestCase {

    static MimeVersionField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return MimeVersionFieldLenientImpl.PARSER.parse(rawField, null);
    }
    
    static void check(String input, int expectedMajorVersion, int expectedMinorVersion) throws Exception {
        MimeVersionField f = parse("MIME-Version: " + input);
        assertEquals("Major version number", expectedMajorVersion, f.getMajorVersion());
        assertEquals("Minor version number", expectedMinorVersion, f.getMinorVersion());
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
    
    public void testMalformed1() throws Exception {
        MimeVersionField f = parse("MIME-Version: 5  ");
        assertEquals(5, f.getMajorVersion());
        assertEquals(MimeVersionFieldImpl.DEFAULT_MINOR_VERSION, f.getMinorVersion());
        assertNull(f.getParseException());
    }
    
    public void testMalformed2() throws Exception {
        MimeVersionField f = parse("MIME-Version: 5.  ");
        assertEquals(5, f.getMajorVersion());
        assertEquals(MimeVersionFieldImpl.DEFAULT_MINOR_VERSION, f.getMinorVersion());
        assertNull(f.getParseException());
    }
    
    public void testMalformed3() throws Exception {
        MimeVersionField f = parse("MIME-Version: .5  ");
        assertEquals(MimeVersionFieldImpl.DEFAULT_MAJOR_VERSION, f.getMajorVersion());
        assertEquals(5, f.getMinorVersion());
        assertNull(f.getParseException());
    }
    
    public void testMalformed4() throws Exception {
        MimeVersionField f = parse("MIME-Version: crap ");
        assertEquals(MimeVersionFieldImpl.DEFAULT_MAJOR_VERSION, f.getMajorVersion());
        assertEquals(MimeVersionFieldImpl.DEFAULT_MINOR_VERSION, f.getMinorVersion());
        assertNull(f.getParseException());
    }
    
}
