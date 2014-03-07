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

    private RawFieldParser parser;

    @Override
    protected void setUp() throws Exception {
        parser = new RawFieldParser();
    }

    public void testBasicTokenParsing() throws Exception {
        String s = "   raw: \" some stuff \"";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        parser.skipWhiteSpace(raw, cursor);

        Assert.assertFalse(cursor.atEnd());
        Assert.assertEquals(3, cursor.getPos());

        StringBuilder strbuf1 = new StringBuilder();
        parser.copyContent(raw, cursor, RawFieldParser.INIT_BITSET(':'), strbuf1);

        Assert.assertFalse(cursor.atEnd());
        Assert.assertEquals(6, cursor.getPos());
        Assert.assertEquals("raw", strbuf1.toString());
        Assert.assertEquals(':', raw.byteAt(cursor.getPos()));
        cursor.updatePos(cursor.getPos() + 1);

        parser.skipWhiteSpace(raw, cursor);

        Assert.assertFalse(cursor.atEnd());
        Assert.assertEquals(8, cursor.getPos());

        StringBuilder strbuf2 = new StringBuilder();
        parser.copyQuotedContent(raw, cursor, strbuf2);

        Assert.assertTrue(cursor.atEnd());
        Assert.assertEquals(" some stuff ", strbuf2.toString());

        parser.copyQuotedContent(raw, cursor, strbuf2);
        Assert.assertTrue(cursor.atEnd());

        parser.skipWhiteSpace(raw, cursor);
        Assert.assertTrue(cursor.atEnd());
    }

    public void testTokenParsingWithQuotedPairs() throws Exception {
        String s = "raw: \"\\\"some\\stuff\\\\\"";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        parser.skipWhiteSpace(raw, cursor);

        Assert.assertFalse(cursor.atEnd());
        Assert.assertEquals(0, cursor.getPos());

        StringBuilder strbuf1 = new StringBuilder();
        parser.copyContent(raw, cursor, RawFieldParser.INIT_BITSET(':'), strbuf1);

        Assert.assertFalse(cursor.atEnd());
        Assert.assertEquals("raw", strbuf1.toString());
        Assert.assertEquals(':', raw.byteAt(cursor.getPos()));
        cursor.updatePos(cursor.getPos() + 1);

        parser.skipWhiteSpace(raw, cursor);

        Assert.assertFalse(cursor.atEnd());

        StringBuilder strbuf2 = new StringBuilder();
        parser.copyQuotedContent(raw, cursor, strbuf2);

        Assert.assertTrue(cursor.atEnd());
        Assert.assertEquals("\"some\\stuff\\", strbuf2.toString());
    }

    public void testTokenParsingIncompleteQuote() throws Exception {
        String s = "\"stuff and more stuff  ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        StringBuilder strbuf1 = new StringBuilder();
        parser.copyQuotedContent(raw, cursor, strbuf1);
        Assert.assertEquals("stuff and more stuff  ", strbuf1.toString());
    }

    public void testSkipComments() throws Exception {
        String s = "(some (((maybe))human readable) stuff())";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        parser.skipComment(raw, cursor);
        Assert.assertTrue(cursor.atEnd());
    }

    public void testSkipCommentsWithQuotedPairs() throws Exception {
        String s = "(some (((\\)maybe))human readable\\() stuff())";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        parser.skipComment(raw, cursor);
        Assert.assertTrue(cursor.atEnd());
    }

    public void testTokenParsingTokensWithUnquotedBlanks() throws Exception {
        String s = "  stuff and   \tsome\tmore  stuff  ;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseToken(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuff and some more stuff", result);
    }

    public void testTokenParsingTokensWithComments() throws Exception {
        String s = " (blah-blah)  stuff(blah-blah) and some mo(blah-blah)re  stuff (blah-blah) ;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseToken(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuff and some more stuff", result);
    }

    public void testTokenParsingMixedValuesAndQuotedValues() throws Exception {
        String s = "  stuff and    \" some more \"   \"stuff  ;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseValue(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuff and  some more  stuff  ;", result);
    }

    public void testTokenParsingMixedValuesAndQuotedValues2() throws Exception {
        String s = "stuff\"more\"stuff;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseValue(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuffmorestuff", result);
    }

    public void testTokenParsingQuotedValuesWithComments() throws Exception {
        String s = " (blah blah)  \"(stuff)(and)(some)(more)(stuff)\" (yada yada) ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseValue(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("(stuff)(and)(some)(more)(stuff)", result);
    }

    public void testBasicParsing() throws Exception {
        String s = "raw: stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingNoBlankAfterColon() throws Exception {
        String s = "raw:stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingObsoleteSyntax() throws Exception {
        String s = "raw  \t  : stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingInvalidSyntax1() throws Exception {
        String s = "raw    stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        try {
            parser.parseField(raw);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testParsingInvalidSyntax2() throws Exception {
        String s = "raw    \t \t";
        ByteSequence raw = ContentUtil.encode(s);

        try {
            parser.parseField(raw);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testNameValueParseBasics() {
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

        s = "test  = text(text of some kind)/stuff(stuff of some kind)";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("text/stuff", param.getValue());

        s = "test  = \"  stuff\\\"\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        assertEquals("test", param.getName());
        assertEquals("  stuff\"", param.getValue());

        s = "test  = \"  stuff\\\\\\\"\"";
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
        assertEquals("stuff", params.get(3).getValue());
        assertEquals(buf.length(), cursor.getPos());
        assertTrue(cursor.atEnd());
    }

    public void testNameValueListParseEmpty() {
        ByteSequence buf = ContentUtil.encode("    ");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);
        assertEquals(0, params.size());
    }

    public void testNameValueListParseEscaped() {
        ByteSequence buf = ContentUtil.encode(
          "test1 =  \"\\\"stuff\\\"\"; test2= \"\\\\\"; test3 = \"stuff; stuff\"");
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

    public void testRawBodyParseWithComments() {
        ByteSequence buf = ContentUtil.encode(
                "  text/(nothing special)plain ; charset=(latin)ISO-8859-1; "
                + "boundary=foo(bar);");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        assertNotNull(body);
        assertEquals("text/plain", body.getValue());
        List<NameValuePair> params = body.getParams();
        assertEquals(2, params.size());
        assertEquals("charset", params.get(0).getName());
        assertEquals("ISO-8859-1", params.get(0).getValue());
        assertEquals("boundary", params.get(1).getName());
        assertEquals("foo", params.get(1).getValue());
    }

    public void testRawBodyParseEmptyParam() {
        ByteSequence buf = ContentUtil.encode(
                "multipart/alternative;; boundary=\"boundary\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        assertNotNull(body);
        assertEquals("multipart/alternative", body.getValue());
        List<NameValuePair> params = body.getParams();
        assertEquals(2, params.size());
        assertEquals("", params.get(0).getName());
        assertEquals(null, params.get(0).getValue());
        assertEquals("boundary", params.get(1).getName());
        assertEquals("boundary", params.get(1).getValue());
    }

    public void testRawBodyParseFolded() {
        ByteSequence buf = ContentUtil.encode(
                "multipart/alternative; boundary=\"simple\r\n boundary\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        assertNotNull(body);
        assertEquals("multipart/alternative", body.getValue());
        List<NameValuePair> params = body.getParams();
        assertEquals(1, params.size());
        assertEquals("boundary", params.get(0).getName());
        assertEquals("simple boundary", params.get(0).getValue());
    }

}
