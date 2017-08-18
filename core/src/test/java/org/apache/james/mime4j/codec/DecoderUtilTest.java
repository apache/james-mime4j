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

package org.apache.james.mime4j.codec;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class DecoderUtilTest {

    @Test
    public void testDecodeB() throws UnsupportedEncodingException {
        String s = DecoderUtil.decodeB("VGhpcyBpcyB0aGUgcGxhaW4gd"
                + "GV4dCBtZXNzYWdlIQ==", "ISO8859-1", DecodeMonitor.STRICT);
        Assert.assertEquals("This is the plain text message!", s);
    }

    @Test
    public void testDecodeQ() throws UnsupportedEncodingException {
        String s = DecoderUtil.decodeQ("=e1_=e2=09=E3_=E4_", "ISO8859-1", DecodeMonitor.STRICT);
        Assert.assertEquals("\u00e1 \u00e2\t\u00e3 \u00e4 ", s);
    }

    @Test
    public void testNonEncodedWordsAreIgnored() {
        Assert.assertEquals("", DecoderUtil.decodeEncodedWords(""));
        Assert.assertEquals("Yada yada", DecoderUtil.decodeEncodedWords("Yada yada"));
    }

    @Test
    public void testDecodeSomeEncodedWords() {
        Assert.assertEquals("  \u00e1\u00e2\u00e3\t\u00e4",
                DecoderUtil.decodeEncodedWords("=?iso-8859-1?Q?_=20=e1=e2=E3=09=E4?="));
        Assert.assertEquals("Word 1 '  \u00e2\u00e3\t\u00e4'. Word 2 '  \u00e2\u00e3\t\u00e4'",
                DecoderUtil.decodeEncodedWords("Word 1 '=?iso-8859-1?Q?_=20=e2=E3=09=E4?="
                        + "'. Word 2 '=?iso-8859-1?q?_=20=e2=E3=09=E4?='"));
        Assert.assertEquals("=?iso-8859-YADA?Q?_=20=t1=e2=E3=09=E4?=",
                DecoderUtil.decodeEncodedWords("=?iso-8859-YADA?Q?_=20=t1=e2=E3=09=E4?="));
        Assert.assertEquals("A short text",
                DecoderUtil.decodeEncodedWords("=?US-ASCII?B?QSBzaG9ydCB0ZXh0?="));
        Assert.assertEquals("A short text again!",
                DecoderUtil.decodeEncodedWords("=?US-ASCII?b?QSBzaG9ydCB0ZXh0IGFnYWluIQ==?="));
    }

    @Test
    public void testDecodeJapaneseEncodedWords() {
        String enc = "=?ISO-2022-JP?B?GyRCTCQbKEobJEI+NRsoShskQkJ6GyhKGyRCOS0bKEo=?= "
                + "=?ISO-2022-JP?B?GyRCOXAbKEobJEIiKBsoShskQiU1GyhKGyRCJSQbKEo=?= "
                + "=?ISO-2022-JP?B?GyRCJUkbKEobJEIlUxsoShskQiU4GyhKGyRCJU0bKEo=?= "
                + "=?ISO-2022-JP?B?GyRCJTkbKEobJEIkThsoShskQjdoGyhKGyRCRGobKEo=?= "
                + "=?ISO-2022-JP?B?GyRCSEcbKEobJEIkRxsoShskQiQ5GyhKGyRCISobKEo=?=";

        String dec = DecoderUtil.decodeEncodedWords(enc);
        Assert.assertEquals("\u672A\u627F\u8AFE\u5E83\u544A\u203B\u30B5\u30A4\u30C9\u30D3"
                + "\u30B8\u30CD\u30B9\u306E\u6C7A\u5B9A\u7248\u3067\u3059\uFF01", dec);
    }

    @Test
    public void testDecodeJapaneseEncodedWordsWithFallback() {
        String enc = "=?random?B?GyRCTCQbKEobJEI+NRsoShskQkJ6GyhKGyRCOS0bKEo=?= "
                + "=?garbage?B?GyRCOXAbKEobJEIiKBsoShskQiU1GyhKGyRCJSQbKEo=?= "
                + "=?charset?B?GyRCJUkbKEobJEIlUxsoShskQiU4GyhKGyRCJU0bKEo=?= "
                + "=?name?B?GyRCJTkbKEobJEIkThsoShskQjdoGyhKGyRCRGobKEo=?= "
                + "=?trash?B?GyRCSEcbKEobJEIkRxsoShskQiQ5GyhKGyRCISobKEo=?=";

        String dec = DecoderUtil.decodeEncodedWords(enc, Charset.forName("ISO-2022-JP"));
        Assert.assertEquals("\u672A\u627F\u8AFE\u5E83\u544A\u203B\u30B5\u30A4\u30C9\u30D3"
                + "\u30B8\u30CD\u30B9\u306E\u6C7A\u5B9A\u7248\u3067\u3059\uFF01", dec);
    }

    @Test
    public void testInvalidEncodedWordsAreIgnored() {
        Assert.assertEquals("=?iso8859-1?Q?=", DecoderUtil.decodeEncodedWords("=?iso8859-1?Q?="));
        Assert.assertEquals("=?iso8859-1?b?=", DecoderUtil.decodeEncodedWords("=?iso8859-1?b?="));
        Assert.assertEquals("=?ISO-8859-1?Q?", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?"));
        Assert.assertEquals("=?ISO-8859-1?R?abc?=", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?R?abc?="));
        Assert.assertEquals("test =?ISO-8859-1?R?abc?=", DecoderUtil.decodeEncodedWords("test =?ISO-8859-1?R?abc?="));
    }

    @Test
    public void testEmptyEncodedTextIsIgnored() {
        // encoded-text requires at least one character according to rfc 2047
        // meanwhile in real life there's emails in real world that contain 0
        // characters in encoded-text part. Probably makes sense to decode them anyway
        Assert.assertEquals("", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q??="));
        Assert.assertEquals("", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?B??="));
    }

    // see MIME4J-104
    @Test
    public void testWhiteSpaceBetweenEncodedWordsGetsRemoved() {
        Assert.assertEquals("a", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?="));
        Assert.assertEquals("a b", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= b"));
        Assert.assertEquals("ab", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= =?ISO-8859-1?Q?b?="));
        Assert.assertEquals("ab", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?=  =?ISO-8859-1?Q?b?="));
        Assert.assertEquals("ab", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?=\r\n  =?ISO-8859-1?Q?b?="));
        Assert.assertEquals("a b", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a_b?="));
        Assert.assertEquals("a b", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= =?ISO-8859-2?Q?_b?="));
    }

    // see MIME4J-138
    @Test
    public void testEncodedTextMayStartWithAnEqualsSign() {
        Assert.assertEquals(" foo", DecoderUtil.decodeEncodedWords("=?utf-8?Q?=20foo?="));
        Assert.assertEquals("Re: How to place a view at the bottom with a 100% width",
                DecoderUtil.decodeEncodedWords("=?utf-8?Q?Re:=20How=20to=20place=20a=20view=20at=20the=20bottom?= "
                        + "=?utf-8?Q?=20with=20a=20100%=20width?="));
        Assert.assertEquals("Test \u00fc and more",
                DecoderUtil.decodeEncodedWords("Test =?ISO-8859-1?Q?=FC_?= =?ISO-8859-1?Q?and_more?="));
    }

    // see MIME4J-142
    @Test
    public void testEncodedTextMayContainDollarSign() {
        Assert.assertEquals("variable ${target.nl}", DecoderUtil.decodeEncodedWords("=?utf-8?Q?variable=20${target.nl}?="));
    }

    // see MIME4J-209
    @Test
    public void testEncodedTextMayContainQuestionMark() {
        Assert.assertEquals("?", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q???="));
    }

    @Test
    public void testNonWhiteSpaceBetweenEncodedWordsIsRetained() {
        Assert.assertEquals("a b c", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= b =?ISO-8859-1?Q?c?="));
        Assert.assertEquals("a\rb\nc", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?=\rb\n=?ISO-8859-1?Q?c?="));
    }

    @Test
    public void testTextBeforeAndAfterEncodedWordIsRetained() {
        Assert.assertEquals(" a b c ", DecoderUtil.decodeEncodedWords(" =?ISO-8859-1?Q?a?= b =?ISO-8859-1?Q?c?= "));
        Assert.assertEquals("! a b c !", DecoderUtil.decodeEncodedWords("! =?ISO-8859-1?Q?a?= b =?ISO-8859-1?Q?c?= !"));
    }

    @Test
    public void testFunnyInputDoesNotRaiseOutOfMemoryError() {
        // Bug detected on June 7, 2005. Decoding the following string caused OutOfMemoryError.
        Assert.assertEquals("=3?!!\\=?\"!g6P\"!Xp:\"!", DecoderUtil.decodeEncodedWords("=3?!!\\=?\"!g6P\"!Xp:\"!"));
    }
}
