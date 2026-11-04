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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentLocationField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.TestCase;

public class ContentLocationFieldTest extends TestCase {

    static ContentLocationField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentLocationFieldImpl.PARSER.parse(rawField, null);
    }

    public void testGetSimpleLocation() throws Exception {
        ContentLocationField f = parse("Content-Location: stuff");
        String location = f.getLocation();
        assertEquals("stuff", location);
    }

    public void testGetQuotedLocation() throws Exception {
        ContentLocationField f = parse("Content-Location: \" stuff \"");
        String location = f.getLocation();
        assertEquals("stuff", location);
    }

    public void testGetLocationWithBlanks() throws Exception {
        ContentLocationField f = parse("Content-Location: this / that \t/what not");
        String location = f.getLocation();
        assertEquals("this/that/whatnot", location);
    }

    public void testGetLocationWithCommens() throws Exception {
        ContentLocationField f = parse("Content-Location: this(blah) / that (yada) /what not");
        String location = f.getLocation();
        assertEquals("this/that/whatnot", location);
    }

    public void testQuotedPairInComment() throws Exception {
        ContentLocationField f = parse("Content-Location: http://example.com/(a\\) b)c");
        assertEquals("http://example.com/c", f.getLocation());
        assertNull(f.getParseException());
    }

    public void testQuotedPairInCommentShouldAgreeWithTheLenientParser() throws Exception {
        String[] values = {"http://example.com/(a\\) b)c", "a(\\()b", "a(x\\\\)b", "a(\\))b", "a((\\)))b"};
        for (String value : values) {
            ContentLocationField strict = parse("Content-Location: " + value);
            ContentLocationField lenient = LenientContentLocationFieldTest.parse("Content-Location: " + value);
            assertEquals(value, lenient.getLocation(), strict.getLocation());
        }
    }

    public void testQuotedPairShouldNotSwallowTheRestOfTheValue() throws Exception {
        // a quoted pair escapes one character: the closing quote and what follows are still read
        ContentLocationField f = parse("Content-Location: \"a\\\"b\" (c\\) d) e");
        assertNotNull(f.getLocation());
        assertTrue(f.getLocation(), f.getLocation().endsWith("e"));
        assertNull(f.getParseException());
    }

    public void testQuotedPairFollowedByTextInQuotedString() throws Exception {
        ContentLocationField f = parse("Content-Location: \"a\\\"b\"");
        assertNull(f.getParseException());
        assertEquals("a\"b", f.getLocation());

        f = parse("Content-Location: \"http://example.com/a\\\\b.html\"");
        assertNull(f.getParseException());
        assertEquals("http://example.com/a\\b.html", f.getLocation());
    }

    public void testQuotedPairInQuotedStringShouldAgreeWithTheLenientParser() throws Exception {
        String[] values = {"\"a\\\"b\"", "\"http://example.com/a\\\\b.html\"", "\"ab\\\"\""};
        for (String value : values) {
            ContentLocationField strict = parse("Content-Location: " + value);
            ContentLocationField lenient = LenientContentLocationFieldTest.parse("Content-Location: " + value);
            assertEquals(value, lenient.getLocation(), strict.getLocation());
        }
    }
}
