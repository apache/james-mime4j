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

import junit.framework.Assert;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RawFieldParserTest {

    private RawFieldParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new RawFieldParser();
    }

    @Test
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

    @Test
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

    @Test
    public void testTokenParsingIncompleteQuote() throws Exception {
        String s = "\"stuff and more stuff  ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        StringBuilder strbuf1 = new StringBuilder();
        parser.copyQuotedContent(raw, cursor, strbuf1);
        Assert.assertEquals("stuff and more stuff  ", strbuf1.toString());
    }

    @Test
    public void testSkipComments() throws Exception {
        String s = "(some (((maybe))human readable) stuff())";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        parser.skipComment(raw, cursor);
        Assert.assertTrue(cursor.atEnd());
    }

    @Test
    public void testSkipCommentsWithQuotedPairs() throws Exception {
        String s = "(some (((\\)maybe))human readable\\() stuff())";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        parser.skipComment(raw, cursor);
        Assert.assertTrue(cursor.atEnd());
    }

    @Test
    public void testTokenParsingTokensWithUnquotedBlanks() throws Exception {
        String s = "  stuff and   \tsome\tmore  stuff  ;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseToken(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuff and some more stuff", result);
    }

    @Test
    public void testTokenParsingTokensWithComments() throws Exception {
        String s = " (blah-blah)  stuff(blah-blah) and some mo(blah-blah)re  stuff (blah-blah) ;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseToken(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuff and some more stuff", result);
    }

    @Test
    public void testTokenParsingMixedValuesAndQuotedValues() throws Exception {
        String s = "  stuff and    \" some more \"   \"stuff  ;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseValue(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuff and  some more  stuff  ;", result);
    }

    @Test
    public void testTokenParsingMixedValuesAndQuotedValues2() throws Exception {
        String s = "stuff\"more\"stuff;";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseValue(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("stuffmorestuff", result);
    }

    @Test
    public void testTokenParsingQuotedValuesWithComments() throws Exception {
        String s = " (blah blah)  \"(stuff)(and)(some)(more)(stuff)\" (yada yada) ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        String result = parser.parseValue(raw, cursor, RawFieldParser.INIT_BITSET(';'));
        Assert.assertEquals("(stuff)(and)(some)(more)(stuff)", result);
    }

    @Test
    public void testBasicParsing() throws Exception {
        String s = "raw: stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    @Test
    public void testParsingNoBlankAfterColon() throws Exception {
        String s = "raw:stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    @Test
    public void testParsingObsoleteSyntax() throws Exception {
        String s = "raw  \t  : stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertEquals(s, field.toString());
    }

    @Test
    public void testParsingInvalidSyntax1() throws Exception {
        String s = "raw    stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);

        try {
            parser.parseField(raw);
            org.junit.Assert.fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    @Test
    public void testParsingInvalidSyntax2() throws Exception {
        String s = "raw    \t \t";
        ByteSequence raw = ContentUtil.encode(s);

        try {
            parser.parseField(raw);
            org.junit.Assert.fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    @Test
    public void testNameValueParseBasics() {
        String s = "test";
        ByteSequence buf = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        NameValuePair param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals(null, param.getValue());
        org.junit.Assert.assertEquals(s.length(), cursor.getPos());
        org.junit.Assert.assertTrue(cursor.atEnd());

        s = "test;";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals(null, param.getValue());
        org.junit.Assert.assertEquals(s.length(), cursor.getPos());
        org.junit.Assert.assertTrue(cursor.atEnd());

        s = "test=stuff";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("stuff", param.getValue());
        org.junit.Assert.assertEquals(s.length(), cursor.getPos());
        org.junit.Assert.assertTrue(cursor.atEnd());

        s = "   test  =   stuff ";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("stuff", param.getValue());
        org.junit.Assert.assertEquals(s.length(), cursor.getPos());
        org.junit.Assert.assertTrue(cursor.atEnd());

        s = "   test  =   stuff ;1234";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("stuff", param.getValue());
        org.junit.Assert.assertEquals(s.length() - 4, cursor.getPos());
        org.junit.Assert.assertFalse(cursor.atEnd());

        s = "test  = \"stuff\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("stuff", param.getValue());

        s = "test  = text(text of some kind)/stuff(stuff of some kind)";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("text/stuff", param.getValue());

        s = "test  = \"  stuff\\\"\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("  stuff\"", param.getValue());

        s = "test  = \"  stuff\\\\\\\"\"";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals("  stuff\\\"", param.getValue());

        s = "  test";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("test", param.getName());
        org.junit.Assert.assertEquals(null, param.getValue());

        s = "  ";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("", param.getName());
        org.junit.Assert.assertEquals(null, param.getValue());

        s = " = stuff ";
        buf = ContentUtil.encode(s);
        cursor = new ParserCursor(0, s.length());

        param = parser.parseParameter(buf, cursor);
        org.junit.Assert.assertEquals("", param.getName());
        org.junit.Assert.assertEquals("stuff", param.getValue());
    }

    @Test
    public void testNameValueListParseBasics() {
        ByteSequence buf = ContentUtil.encode(
                "test; test1 =  stuff   ; test2 =  \"stuff; stuff\"; test3=\"stuff");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);

        org.junit.Assert.assertEquals("test", params.get(0).getName());
        org.junit.Assert.assertEquals(null, params.get(0).getValue());
        org.junit.Assert.assertEquals("test1", params.get(1).getName());
        org.junit.Assert.assertEquals("stuff", params.get(1).getValue());
        org.junit.Assert.assertEquals("test2", params.get(2).getName());
        org.junit.Assert.assertEquals("stuff; stuff", params.get(2).getValue());
        org.junit.Assert.assertEquals("test3", params.get(3).getName());
        org.junit.Assert.assertEquals("stuff", params.get(3).getValue());
        org.junit.Assert.assertEquals(buf.length(), cursor.getPos());
        org.junit.Assert.assertTrue(cursor.atEnd());
    }

    @Test
    public void testNameValueListParseEmpty() {
        ByteSequence buf = ContentUtil.encode("    ");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);
        org.junit.Assert.assertEquals(0, params.size());
    }

    @Test
    public void testNameValueListParseEscaped() {
        ByteSequence buf = ContentUtil.encode(
                "test1 =  \"\\\"stuff\\\"\"; test2= \"\\\\\"; test3 = \"stuff; stuff\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);
        org.junit.Assert.assertEquals(3, params.size());
        org.junit.Assert.assertEquals("test1", params.get(0).getName());
        org.junit.Assert.assertEquals("\"stuff\"", params.get(0).getValue());
        org.junit.Assert.assertEquals("test2", params.get(1).getName());
        org.junit.Assert.assertEquals("\\", params.get(1).getValue());
        org.junit.Assert.assertEquals("test3", params.get(2).getName());
        org.junit.Assert.assertEquals("stuff; stuff", params.get(2).getValue());
    }

    @Test
    public void testRawBodyParse() {
        ByteSequence buf = ContentUtil.encode(
                "  text/plain ; charset=ISO-8859-1; "
                        + "boundary=foo; param1=value1; param2=\"value2\"; param3=value3");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        org.junit.Assert.assertNotNull(body);
        org.junit.Assert.assertEquals("text/plain", body.getValue());
        List<NameValuePair> params = body.getParams();
        org.junit.Assert.assertEquals(5, params.size());
        org.junit.Assert.assertEquals("charset", params.get(0).getName());
        org.junit.Assert.assertEquals("ISO-8859-1", params.get(0).getValue());
        org.junit.Assert.assertEquals("boundary", params.get(1).getName());
        org.junit.Assert.assertEquals("foo", params.get(1).getValue());
        org.junit.Assert.assertEquals("param1", params.get(2).getName());
        org.junit.Assert.assertEquals("value1", params.get(2).getValue());
        org.junit.Assert.assertEquals("param2", params.get(3).getName());
        org.junit.Assert.assertEquals("value2", params.get(3).getValue());
        org.junit.Assert.assertEquals("param3", params.get(4).getName());
        org.junit.Assert.assertEquals("value3", params.get(4).getValue());
    }

    @Test
    public void testRawBodyParseWithComments() {
        ByteSequence buf = ContentUtil.encode(
                "  text/(nothing special)plain ; charset=(latin)ISO-8859-1; "
                        + "boundary=foo(bar);");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        org.junit.Assert.assertNotNull(body);
        org.junit.Assert.assertEquals("text/plain", body.getValue());
        List<NameValuePair> params = body.getParams();
        org.junit.Assert.assertEquals(2, params.size());
        org.junit.Assert.assertEquals("charset", params.get(0).getName());
        org.junit.Assert.assertEquals("ISO-8859-1", params.get(0).getValue());
        org.junit.Assert.assertEquals("boundary", params.get(1).getName());
        org.junit.Assert.assertEquals("foo", params.get(1).getValue());
    }

    @Test
    public void testRawBodyParseEmptyParam() {
        ByteSequence buf = ContentUtil.encode(
                "multipart/alternative;; boundary=\"boundary\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        org.junit.Assert.assertNotNull(body);
        org.junit.Assert.assertEquals("multipart/alternative", body.getValue());
        List<NameValuePair> params = body.getParams();
        org.junit.Assert.assertEquals(2, params.size());
        org.junit.Assert.assertEquals("", params.get(0).getName());
        org.junit.Assert.assertEquals(null, params.get(0).getValue());
        org.junit.Assert.assertEquals("boundary", params.get(1).getName());
        org.junit.Assert.assertEquals("boundary", params.get(1).getValue());
    }

    @Test
    public void testRawBodyParseFolded() {
        ByteSequence buf = ContentUtil.encode(
                "multipart/alternative; boundary=\"simple\r\n boundary\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        RawBody body = parser.parseRawBody(buf, cursor);
        org.junit.Assert.assertNotNull(body);
        org.junit.Assert.assertEquals("multipart/alternative", body.getValue());
        List<NameValuePair> params = body.getParams();
        org.junit.Assert.assertEquals(1, params.size());
        org.junit.Assert.assertEquals("boundary", params.get(0).getName());
        org.junit.Assert.assertEquals("simple boundary", params.get(0).getValue());
    }

    @Test
    public void testRegressionForContentDispositionParsingASCIIonly() {
        ByteSequence buf = ContentUtil.encode(
                "Content-Disposition: form-data; name=\"filedata\"; filename=\"Sanity a.doc\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);

        org.junit.Assert.assertEquals(3, params.size());
        org.junit.Assert.assertEquals("Content-Disposition: form-data", params.get(0).getName());
        org.junit.Assert.assertEquals(null, params.get(0).getValue());
        org.junit.Assert.assertEquals("name", params.get(1).getName());
        org.junit.Assert.assertEquals("filedata", params.get(1).getValue());
        org.junit.Assert.assertEquals("filename", params.get(2).getName());
        org.junit.Assert.assertEquals("Sanity a.doc", params.get(2).getValue());
   }

    @Test
    public void testRegressionForContentDispositionParsingUTF8() {
        ByteSequence buf = ContentUtil.encode(
                "Content-Disposition: form-data; name=\"filedata\"; filename=\"Sanity ä.doc\"");
        ParserCursor cursor = new ParserCursor(0, buf.length());
        List<NameValuePair> params = parser.parseParameters(buf, cursor);

        org.junit.Assert.assertEquals(3, params.size());
        org.junit.Assert.assertEquals("Content-Disposition: form-data", params.get(0).getName());
        org.junit.Assert.assertEquals(null, params.get(0).getValue());
        org.junit.Assert.assertEquals("name", params.get(1).getName());
        org.junit.Assert.assertEquals("filedata", params.get(1).getValue());
        org.junit.Assert.assertEquals("filename", params.get(2).getName());
        org.junit.Assert.assertEquals("Sanity ä.doc", params.get(2).getValue());
   }


}
