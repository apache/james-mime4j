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

import java.util.List;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RawFieldParserTest extends TestCase {

    public void testBasicParsing() throws Exception {
        String s = "raw: stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawFieldParser parser = new RawFieldParser();

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingObsoleteSyntax() throws Exception {
        String s = "raw  \t  : stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawFieldParser parser = new RawFieldParser();

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingInvalidSyntax1() throws Exception {
        String s = "raw    stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawFieldParser parser = new RawFieldParser();

        try {
            parser.parseField(raw);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testParsingInvalidSyntax2() throws Exception {
        String s = "raw    \t \t";
        ByteSequence raw = ContentUtil.encode(s);

        RawFieldParser parser = new RawFieldParser();

        try {
            parser.parseField(raw);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testNameValueParseBasics() {
        RawFieldParser parser = new RawFieldParser();
        String s = "test";
        ByteSequence buf = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        NameValuePair param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals(null, param.getValue());
        assertEquals(s.length(), cursor.getPos());
        assertTrue(cursor.atEnd());

        s = "test;";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals(null, param.getValue());
        assertEquals(s.length(), cursor.getPos());
        assertTrue(cursor.atEnd());

        s = "test=stuff";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("stuff", param.getValue());
        assertEquals(s.length(), cursor.getPos());
        assertTrue(cursor.atEnd());

        s = "   test  =   stuff ";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("stuff", param.getValue());
        assertEquals(s.length(), cursor.getPos());
        assertTrue(cursor.atEnd());

        s = "   test  =   stuff ;1234";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("stuff", param.getValue());
        assertEquals(s.length() - 4, cursor.getPos());
        assertFalse(cursor.atEnd());

        s = "test  = \"stuff\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("stuff", param.getValue());

        s = "test  = \"  stuff\\\"\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("  stuff\"", param.getValue());

        s = "test  = \"  stuff\\\\\"\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("  stuff\\\"", param.getValue());

        s = "  test";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals(null, param.getValue());

        s = "  ";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("", param.getName());
        assertEquals(null, param.getValue());

        s = " = stuff ";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("", param.getName());
        assertEquals("stuff", param.getValue());
    }

    public void testNameValueListParseBasics() {
        RawFieldParser parser = new RawFieldParser();
        ByteSequence buf = ContentUtil.encode(
                "test; test1 =  stuff   ; test2 =  \"stuff; stuff\"; test3=\"stuff");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);

        assertEquals("test", params.get(0).getName());
        assertEquals(null, params.get(0).getValue());
        assertEquals("test1", params.get(1).getName());
        assertEquals("stuff", params.get(1).getValue());
        assertEquals("test2", params.get(2).getName());
        assertEquals("stuff; stuff", params.get(2).getValue());
        assertEquals("test3", params.get(3).getName());
        assertEquals("\"stuff", params.get(3).getValue());
        assertEquals(buf.length(), cursor.getPos());
        assertTrue(cursor.atEnd());
    }

    public void testNameValueListParseEmpty() {
        ByteSequence buf = ContentUtil.encode("    ");
        RawFieldParser parser = new RawFieldParser();
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);
        assertEquals(0, params.size());
    }

    public void testNameValueListParseEscaped() {
        ByteSequence buf = ContentUtil.encode(
          "test1 =  \"\\\"stuff\\\"\"; test2= \"\\\\\"; test3 = \"stuff; stuff\"");
        RawFieldParser parser = new RawFieldParser();
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);
        assertEquals(3, params.size());
        assertEquals("test1", params.get(0).getName());
        assertEquals("\"stuff\"", params.get(0).getValue());
        assertEquals("test2", params.get(1).getName());
        assertEquals("\\", params.get(1).getValue());
        assertEquals("test3", params.get(2).getName());
        assertEquals("stuff; stuff", params.get(2).getValue());
    }

    public void testRawBodyParse() {
        ByteSequence buf = ContentUtil.encode(
                "  text/plain ; charset=ISO-8859-1; "
                + "boundary=foo; param1=value1; param2=\"value2\"; param3=value3");
        RawFieldParser parser = new RawFieldParser();
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        assertNotNull(body);
        assertEquals("text/plain", body.getValue());
        List<NameValuePair> params = body.getParams();
        assertEquals(5, params.size());
        assertEquals("charset", params.get(0).getName());
        assertEquals("ISO-8859-1", params.get(0).getValue());
        assertEquals("boundary", params.get(1).getName());
        assertEquals("foo", params.get(1).getValue());
        assertEquals("param1", params.get(2).getName());
        assertEquals("value1", params.get(2).getValue());
        assertEquals("param2", params.get(3).getName());
        assertEquals("value2", params.get(3).getValue());
        assertEquals("param3", params.get(4).getName());
        assertEquals("value3", params.get(4).getValue());
    }

}