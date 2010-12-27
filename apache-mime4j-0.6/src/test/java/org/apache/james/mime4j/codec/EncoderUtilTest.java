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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.apache.james.mime4j.codec.EncoderUtil.Encoding;
import org.apache.james.mime4j.codec.EncoderUtil.Usage;
import org.apache.james.mime4j.util.CharsetUtil;

public class EncoderUtilTest extends TestCase {

    public void testEncodeAddressDisplayName() throws Exception {
        assertEquals("\"\"", EncoderUtil.encodeAddressDisplayName(""));
        assertEquals("test", EncoderUtil.encodeAddressDisplayName("test"));
        assertEquals(" test ", EncoderUtil.encodeAddressDisplayName(" test "));
        assertEquals(" test\ttest ", EncoderUtil
                .encodeAddressDisplayName(" test\ttest "));
        assertEquals("\"test()\"", EncoderUtil
                .encodeAddressDisplayName("test()"));
        assertEquals("\"John Q. Public\"", EncoderUtil
                .encodeAddressDisplayName("John Q. Public"));
        assertEquals("\"Giant; \\\"Big\\\" Box\"", EncoderUtil
                .encodeAddressDisplayName("Giant; \"Big\" Box"));
        assertEquals("=?ISO-8859-1?Q?Semmelbr=F6sel?=", EncoderUtil
                .encodeAddressDisplayName("Semmelbr\366sel"));
        // dollar sign as to be encoded as =24 when used as a word in a phrase
        assertEquals("=?UTF-8?Q?Dollar_=24_Euro_=E2=82=AC?=", EncoderUtil
                .encodeAddressDisplayName("Dollar $ Euro \u20ac"));
    }

    public void testEncodeAddressLocalPart() throws Exception {
        assertEquals("john.wayne", EncoderUtil
                .encodeAddressLocalPart("john.wayne"));
        assertEquals("\"clint eastwood\"", EncoderUtil
                .encodeAddressLocalPart("clint eastwood"));
    }

    public void testEncodeHeaderParameter() throws Exception {
        assertEquals("p=test", EncoderUtil.encodeHeaderParameter("p", "test"));
        assertEquals("p=\"test test\"", EncoderUtil.encodeHeaderParameter("p",
                "test test"));
        assertEquals("p=\"=test\"", EncoderUtil.encodeHeaderParameter("p",
                "=test"));
        assertEquals("p=\"\\\\test\"", EncoderUtil.encodeHeaderParameter("p",
                "\\test"));
        assertEquals("p=\"\\\"\\\\\\\"\"", EncoderUtil.encodeHeaderParameter(
                "p", "\"\\\""));
    }

    public void testHasToBeEncoded() throws Exception {
        assertFalse(EncoderUtil.hasToBeEncoded("", 0));
        assertFalse(EncoderUtil.hasToBeEncoded("only ascii characters", 0));

        assertTrue(EncoderUtil.hasToBeEncoded("non-printable ascii: \010", 0));
        assertTrue(EncoderUtil.hasToBeEncoded("non-ascii: \u20ac", 0));

        assertFalse(EncoderUtil.hasToBeEncoded("123456789012345678901234567",
                50));
        assertTrue(EncoderUtil.hasToBeEncoded("1234567890123456789012345678",
                50));
        assertFalse(EncoderUtil.hasToBeEncoded(
                "\t12345678901234567890123456789", 50));
    }

    public void testEncodeEncodedWordDetectCharset() throws Exception {
        assertTrue(EncoderUtil
                .encodeEncodedWord("only ascii", Usage.TEXT_TOKEN).startsWith(
                        "=?US-ASCII?"));
        assertTrue(EncoderUtil.encodeEncodedWord("latin 1: \344",
                Usage.TEXT_TOKEN).startsWith("=?ISO-8859-1?"));
        assertTrue(EncoderUtil.encodeEncodedWord("unicode: \u20ac",
                Usage.TEXT_TOKEN).startsWith("=?UTF-8?"));
    }

    public void testEncodeEncodedWordForceCharset() throws Exception {
        assertTrue(EncoderUtil.encodeEncodedWord("only ascii",
                Usage.TEXT_TOKEN, 0, CharsetUtil.UTF_8, null).startsWith(
                "=?UTF-8?"));
    }

    public void testEncodeEncodedWordDetectEncoding() throws Exception {
        assertTrue(EncoderUtil
                .encodeEncodedWord("only ascii", Usage.TEXT_TOKEN).startsWith(
                        "=?US-ASCII?Q?"));
        assertTrue(EncoderUtil.encodeEncodedWord("\344\344\344\344\344",
                Usage.TEXT_TOKEN).startsWith("=?ISO-8859-1?B?"));
    }

    public void testEncodeEncodedWordForceEncoding() throws Exception {
        assertTrue(EncoderUtil.encodeEncodedWord("only ascii",
                Usage.TEXT_TOKEN, 0, null, Encoding.B).startsWith(
                "=?US-ASCII?B?"));
    }

    public void testEncodeEncodedWordSplit() throws Exception {
        String sixty = "123456789012345678901234567890123456789012345678901234567890";

        String expected = "=?US-ASCII?Q?" + sixty + "?=";
        assertEquals(expected, EncoderUtil.encodeEncodedWord(sixty,
                Usage.TEXT_TOKEN, 0, null, Encoding.Q));
        assertEquals(75, expected.length());

        String sixtyOne = sixty + "1";
        String encodedSixtyOne = EncoderUtil.encodeEncodedWord(sixtyOne,
                Usage.TEXT_TOKEN, 0, null, Encoding.Q);
        assertTrue(encodedSixtyOne.contains("?= =?US-ASCII?Q?"));
    }

    public void testEncodeEncodedWord() throws Exception {
        assertEquals("=?US-ASCII?Q??=", EncoderUtil.encodeEncodedWord("",
                Usage.TEXT_TOKEN, 0, null, Encoding.Q));

        assertEquals("=?US-ASCII?Q?testing_123?=", EncoderUtil
                .encodeEncodedWord("testing 123", Usage.TEXT_TOKEN, 0, null,
                        Encoding.Q));

        assertEquals("=?US-ASCII?B?dGVzdGluZyAxMjM=?=", EncoderUtil
                .encodeEncodedWord("testing 123", Usage.TEXT_TOKEN, 0, null,
                        Encoding.B));

        assertEquals("=?windows-1252?Q?100_=80?=", EncoderUtil
                .encodeEncodedWord("100 \u20ac", Usage.TEXT_TOKEN, 0, Charset
                        .forName("Cp1252"), Encoding.Q));

        assertEquals("=?windows-1252?B?MTAwIIA=?=", EncoderUtil
                .encodeEncodedWord("100 \u20ac", Usage.TEXT_TOKEN, 0, Charset
                        .forName("Cp1252"), Encoding.B));
    }

    public void testEncodeB() throws Exception {
        assertEquals("", encodeB(""));
        assertEquals("YQ==", encodeB("a"));
        assertEquals("YWI=", encodeB("ab"));
        assertEquals("YWJj", encodeB("abc"));
        assertEquals("YWJjZA==", encodeB("abcd"));
        assertEquals("YWJjZGU=", encodeB("abcde"));
        assertEquals("YWJjZGVm", encodeB("abcdef"));
        assertEquals("YWJjZGVmZw==", encodeB("abcdefg"));
        assertEquals("YWJjZGVmZ2g=", encodeB("abcdefgh"));
        assertEquals("YWJjZGVmZ2hp", encodeB("abcdefghi"));
        assertEquals("DQoMCQ==", encodeB("\r\n\f\t"));
        assertEquals("LT0/VGhhdCdzIGEgdGVzdD89LQ==",
                encodeB("-=?That's a test?=-"));
    }

    public void testEncodeQRegular() throws Exception {
        byte[] b = new byte[132];
        for (int i = 0; i < 132; i++) {
            b[i] = (byte) i;
        }

        String expected = "=00=01=02=03=04=05=06=07=08=09=0A=0B=0C=0D=0E=0F"
                + "=10=11=12=13=14=15=16=17=18=19=1A=1B=1C=1D=1E=1F_!\"#$%&"
                + "'()*+,-./0123456789:;<=3D>=3F@ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "[\\]^=5F`abcdefghijklmnopqrstuvwxyz{|}~=7F=80=81=82=83";
        assertEquals(expected, EncoderUtil.encodeQ(b, Usage.TEXT_TOKEN));
    }

    public void testEncodeQRestricted() throws Exception {
        byte[] b = new byte[136];
        for (int i = 0; i < 136; i++) {
            b[i] = (byte) i;
        }

        String expected = "=00=01=02=03=04=05=06=07=08=09=0A=0B=0C=0D=0E=0F"
                + "=10=11=12=13=14=15=16=17=18=19=1A=1B=1C=1D=1E=1F_!=22=23"
                + "=24=25=26=27=28=29*+=2C-=2E/0123456789=3A=3B=3C=3D=3E=3F"
                + "=40ABCDEFGHIJKLMNOPQRSTUVWXYZ=5B=5C=5D=5E=5F=60abcdefghi"
                + "jklmnopqrstuvwxyz=7B=7C=7D=7E=7F=80=81=82=83=84=85=86=87";
        assertEquals(expected, EncoderUtil.encodeQ(b, Usage.WORD_ENTITY));
    }

    private String encodeB(String s) {
        try {
            return EncoderUtil.encodeB(s.getBytes("us-ascii"));
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

}
