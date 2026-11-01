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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.codec.EncoderUtil.Encoding;
import org.apache.james.mime4j.codec.EncoderUtil.Usage;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class EncoderUtilTest {

    @Test
    public void testEncodeAddressDisplayName() throws Exception {
        Assert.assertEquals("\"\"", EncoderUtil.encodeAddressDisplayName(""));
        Assert.assertEquals("test", EncoderUtil.encodeAddressDisplayName("test"));
        Assert.assertEquals("\" test \"", EncoderUtil.encodeAddressDisplayName(" test "));
        Assert.assertEquals("\" test\ttest \"", EncoderUtil
                .encodeAddressDisplayName(" test\ttest "));
        Assert.assertEquals("\"test()\"", EncoderUtil
                .encodeAddressDisplayName("test()"));
        Assert.assertEquals("\"John Q. Public\"", EncoderUtil
                .encodeAddressDisplayName("John Q. Public"));
        Assert.assertEquals("\"Giant; \\\"Big\\\" Box\"", EncoderUtil
                .encodeAddressDisplayName("Giant; \"Big\" Box"));
        Assert.assertEquals("=?ISO-8859-1?Q?Semmelbr=F6sel?=", EncoderUtil
                .encodeAddressDisplayName("Semmelbr\366sel"));
        // dollar sign as to be encoded as =24 when used as a word in a phrase
        Assert.assertEquals("=?UTF-8?Q?Dollar_=24_Euro_=E2=82=AC?=", EncoderUtil
                .encodeAddressDisplayName("Dollar $ Euro \u20ac"));
    }

    @Test
    public void testEncodeAddressLocalPart() throws Exception {
        Assert.assertEquals("john.wayne", EncoderUtil
                .encodeAddressLocalPart("john.wayne"));
        Assert.assertEquals("\"clint eastwood\"", EncoderUtil
                .encodeAddressLocalPart("clint eastwood"));
    }

    @Test
    public void testEncodeHeaderParameter() throws Exception {
        Assert.assertEquals("p=test", EncoderUtil.encodeHeaderParameter("p", "test"));
        Assert.assertEquals("p=\"test test\"", EncoderUtil.encodeHeaderParameter("p",
                "test test"));
        Assert.assertEquals("p=\"=test\"", EncoderUtil.encodeHeaderParameter("p",
                "=test"));
        Assert.assertEquals("p=\"\\\\test\"", EncoderUtil.encodeHeaderParameter("p",
                "\\test"));
        Assert.assertEquals("p=\"\\\"\\\\\\\"\"", EncoderUtil.encodeHeaderParameter(
                "p", "\"\\\""));
    }

    @Test
    public void testHasToBeEncoded() throws Exception {
        Assert.assertFalse(EncoderUtil.hasToBeEncoded("", 0));
        Assert.assertFalse(EncoderUtil.hasToBeEncoded("only ascii characters", 0));

        Assert.assertTrue(EncoderUtil.hasToBeEncoded("non-printable ascii: \010", 0));
        Assert.assertTrue(EncoderUtil.hasToBeEncoded("non-ascii: \u20ac", 0));

        Assert.assertFalse(EncoderUtil.hasToBeEncoded("123456789012345678901234567",
                50));
        Assert.assertTrue(EncoderUtil.hasToBeEncoded("1234567890123456789012345678",
                50));
        Assert.assertFalse(EncoderUtil.hasToBeEncoded(
                "\t12345678901234567890123456789", 50));
    }

    @Test
    public void testEncodeEncodedWordDetectCharset() throws Exception {
        Assert.assertTrue(EncoderUtil
                .encodeEncodedWord("only ascii", Usage.TEXT_TOKEN).startsWith(
                        "=?US-ASCII?"));
        Assert.assertTrue(EncoderUtil.encodeEncodedWord("latin 1: \344",
                Usage.TEXT_TOKEN).startsWith("=?ISO-8859-1?"));
        Assert.assertTrue(EncoderUtil.encodeEncodedWord("unicode: \u20ac",
                Usage.TEXT_TOKEN).startsWith("=?UTF-8?"));
    }

    @Test
    public void testEncodeEncodedWordForceCharset() throws Exception {
        Assert.assertTrue(EncoderUtil.encodeEncodedWord("only ascii",
                Usage.TEXT_TOKEN, 0, Charsets.UTF_8, null).startsWith(
                "=?UTF-8?"));
    }

    @Test
    public void testEncodeEncodedWordDetectEncoding() throws Exception {
        Assert.assertTrue(EncoderUtil
                .encodeEncodedWord("only ascii", Usage.TEXT_TOKEN).startsWith(
                        "=?US-ASCII?Q?"));
        Assert.assertTrue(EncoderUtil.encodeEncodedWord("\344\344\344\344\344",
                Usage.TEXT_TOKEN).startsWith("=?ISO-8859-1?B?"));
    }

    @Test
    public void testEncodeEncodedWordForceEncoding() throws Exception {
        Assert.assertTrue(EncoderUtil.encodeEncodedWord("only ascii",
                Usage.TEXT_TOKEN, 0, null, Encoding.B).startsWith(
                "=?US-ASCII?B?"));
    }

    @Test
    public void testEncodeEncodedWordSplit() throws Exception {
        String sixty = "123456789012345678901234567890123456789012345678901234567890";

        String expected = "=?US-ASCII?Q?" + sixty + "?=";
        Assert.assertEquals(expected, EncoderUtil.encodeEncodedWord(sixty,
                Usage.TEXT_TOKEN, 0, null, Encoding.Q));
        Assert.assertEquals(75, expected.length());

        String sixtyOne = sixty + "1";
        String encodedSixtyOne = EncoderUtil.encodeEncodedWord(sixtyOne,
                Usage.TEXT_TOKEN, 0, null, Encoding.Q);
        Assert.assertTrue(encodedSixtyOne.contains("?= =?US-ASCII?Q?"));
    }
    
    @Test
    public void testEncodeBEncodedWordSplitForUnicode() throws Exception {
        StringBuilder sb = new StringBuilder("z");
        for (int i = 0; i < 10; i++) {
            // Append unicode character 𝕫 10 times.
            sb.append("\uD835\uDD6b");
        }

        String expected = "=?UTF-8?B?evCdlavwnZWr8J2Vq/Cdlas=?= " +
                "=?UTF-8?B?8J2Vq/CdlavwnZWr8J2Vq/CdlavwnZWr?=";
        Assert.assertEquals(expected, EncoderUtil.encodeEncodedWord(sb.toString(),
                Usage.TEXT_TOKEN, 10, null, Encoding.B));
    }

    @Test
    public void testEncodeQEncodedWordSplitForUnicode() throws Exception {
        StringBuilder sb = new StringBuilder("z");
        for (int i = 0; i < 6; i++) {
            // Append unicode character 𝕫 6 times.
            sb.append("\uD835\uDD6b");
        }

        String expected = "=?UTF-8?Q?z=F0=9D=95=AB=F0=9D=95=AB?= " +
                "=?UTF-8?Q?=F0=9D=95=AB=F0=9D=95=AB=F0=9D=95=AB=F0=9D=95=AB?=";
        Assert.assertEquals(expected, EncoderUtil.encodeEncodedWord(sb.toString(),
                Usage.TEXT_TOKEN, 10, null, Encoding.Q));
    }

    @Test
    public void testEncodeEncodedWord() throws Exception {
        Assert.assertEquals("=?US-ASCII?Q??=", EncoderUtil.encodeEncodedWord("",
                Usage.TEXT_TOKEN, 0, null, Encoding.Q));

        Assert.assertEquals("=?US-ASCII?Q?testing_123?=", EncoderUtil
                .encodeEncodedWord("testing 123", Usage.TEXT_TOKEN, 0, null,
                        Encoding.Q));

        Assert.assertEquals("=?US-ASCII?B?dGVzdGluZyAxMjM=?=", EncoderUtil
                .encodeEncodedWord("testing 123", Usage.TEXT_TOKEN, 0, null,
                        Encoding.B));

        Assert.assertEquals("=?windows-1252?Q?100_=80?=", EncoderUtil
                .encodeEncodedWord("100 \u20ac", Usage.TEXT_TOKEN, 0, Charset
                        .forName("Cp1252"), Encoding.Q));

        Assert.assertEquals("=?windows-1252?B?MTAwIIA=?=", EncoderUtil
                .encodeEncodedWord("100 \u20ac", Usage.TEXT_TOKEN, 0, Charset
                        .forName("Cp1252"), Encoding.B));
    }

    @Test
    public void testEncodeB() throws Exception {
        Assert.assertEquals("", encodeB(""));
        Assert.assertEquals("YQ==", encodeB("a"));
        Assert.assertEquals("YWI=", encodeB("ab"));
        Assert.assertEquals("YWJj", encodeB("abc"));
        Assert.assertEquals("YWJjZA==", encodeB("abcd"));
        Assert.assertEquals("YWJjZGU=", encodeB("abcde"));
        Assert.assertEquals("YWJjZGVm", encodeB("abcdef"));
        Assert.assertEquals("YWJjZGVmZw==", encodeB("abcdefg"));
        Assert.assertEquals("YWJjZGVmZ2g=", encodeB("abcdefgh"));
        Assert.assertEquals("YWJjZGVmZ2hp", encodeB("abcdefghi"));
        Assert.assertEquals("DQoMCQ==", encodeB("\r\n\f\t"));
        Assert.assertEquals("LT0/VGhhdCdzIGEgdGVzdD89LQ==",
                encodeB("-=?That's a test?=-"));
    }

    @Test
    public void testEncodeQRegular() throws Exception {
        byte[] b = new byte[132];
        for (int i = 0; i < 132; i++) {
            b[i] = (byte) i;
        }

        String expected = "=00=01=02=03=04=05=06=07=08=09=0A=0B=0C=0D=0E=0F"
                + "=10=11=12=13=14=15=16=17=18=19=1A=1B=1C=1D=1E=1F_!\"#$%&"
                + "'()*+,-./0123456789:;<=3D>=3F@ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "[\\]^=5F`abcdefghijklmnopqrstuvwxyz{|}~=7F=80=81=82=83";
        Assert.assertEquals(expected, EncoderUtil.encodeQ(b, Usage.TEXT_TOKEN));
    }

    @Test
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
        Assert.assertEquals(expected, EncoderUtil.encodeQ(b, Usage.WORD_ENTITY));
    }

    private String encodeB(String s) {
        try {
            return EncoderUtil.encodeB(s.getBytes("us-ascii"));
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    @Test
    public void testEncodeQuotedPrintableLargeInput() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024 * 5; i++) {
            sb.append((char) ('0' + (i % 10)));
        }
        String expected = sb.toString().replaceAll("(\\d{75})", "$1=\r\n");

        InputStream in = InputStreams.createAscii(sb.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EncoderUtil.encodeQBinary(in, out);
        String actual = ContentUtil.toAsciiString(out.toByteArray());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testEncodeQuotedPrintableNonAsciiChars() throws Exception {
        String s = "7bit content with euro \u20AC symbol";
        InputStream in = InputStreams.create(s, Charset.forName("iso-8859-15"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EncoderUtil.encodeQBinary(in, out);
        String actual = new String(out.toByteArray(), "US-ASCII");
        Assert.assertEquals("7bit=20content=20with=20euro=20=A4=20symbol", actual);
    }

    @Test
    public void testBase64OutputStream() throws Exception {
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < 128; i++) {
            sb.append("0123456789ABCDEF");
        }
        String input = sb.toString();
        String output = roundtripUsingOutputStream(input);
        Assert.assertEquals(input, output);
    }

    private String roundtripUsingOutputStream(String input) throws IOException {
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        Base64OutputStream outb64 = new Base64OutputStream(out2, 76);
        ContentUtil.copy(InputStreams.create(input, Charsets.ISO_8859_1), outb64);
        outb64.flush();
        outb64.close();

        InputStream is = new Base64InputStream(InputStreams.create(out2.toByteArray()));
        byte[] buf = ContentUtil.buffer(is);
        return ContentUtil.toAsciiString(buf);
    }

    /**
     * This test is a proof for MIME4J-67
     */
    @Test
    public void testBase64Encoder() throws Exception {
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < 128; i++) {
            sb.append("0123456789ABCDEF");
        }
        String input = sb.toString();
        String output = roundtripUsingEncoder(input);
        Assert.assertEquals(input, output);
    }

    private String roundtripUsingEncoder(String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EncoderUtil.encodeB(InputStreams.createAscii(input), out);

        InputStream is = new Base64InputStream(InputStreams.create(out.toByteArray()));
        byte[] buf = ContentUtil.buffer(is);
        return ContentUtil.toAsciiString(buf);
    }

    @Test
    public void displayNameWithLineBreaksShouldBeEncoded() {
        for (String displayName : new String[] {"y\n", "y\r", "a\r\n b"}) {
            String encoded = EncoderUtil.encodeAddressDisplayName(displayName);

            Assert.assertEquals(encoded, -1, encoded.indexOf('\n'));
            Assert.assertEquals(encoded, -1, encoded.indexOf('\r'));
            Assert.assertEquals(displayName, DecoderUtil.decodeEncodedWords(encoded, DecodeMonitor.SILENT));
        }
    }

    @Test
    public void textHoldingAnEncodedWordLookalikeShouldBeEncoded() {
        for (String text : new String[] {"=?utf-8?q?x?=", "a =?utf-8?q?x?= b", "=?=?=?=?=?", "=?utf-8?q?x?=\"\""}) {
            String displayName = EncoderUtil.encodeAddressDisplayName(text);
            String unstructured = EncoderUtil.encodeIfNecessary(text, Usage.TEXT_TOKEN, 0);

            Assert.assertEquals(text, DecoderUtil.decodeEncodedWords(displayName, DecodeMonitor.SILENT));
            Assert.assertEquals(text, DecoderUtil.decodeEncodedWords(unstructured, DecodeMonitor.SILENT));
        }
    }

    @Test
    public void textWithoutEncodedWordLookalikeShouldNotBeEncoded() {
        Assert.assertEquals("a = b ? c", EncoderUtil.encodeIfNecessary("a = b ? c", Usage.TEXT_TOKEN, 0));
        Assert.assertEquals("a?=b", EncoderUtil.encodeAddressDisplayName("a?=b"));
    }

    @Test
    public void localPartShouldNotHoldRawLineBreaks() {
        Assert.assertEquals("\"\\\\\"", EncoderUtil.encodeAddressLocalPart("\\\r"));
        Assert.assertEquals("\"a b\"", EncoderUtil.encodeAddressLocalPart("a\r\n b"));
    }

    @Test
    public void validDomainsShouldBeKept() {
        Assert.assertEquals("example.com", EncoderUtil.encodeAddressDomain("example.com"));
        Assert.assertEquals("[192.168.0.(1]", EncoderUtil.encodeAddressDomain("[192.168.0.(1]"));
        Assert.assertEquals("éxample.com", EncoderUtil.encodeAddressDomain("éxample.com"));
    }

    @Test
    public void domainsThatWouldChangeTheHeaderShouldBeNeutralized() {
        Assert.assertEquals("example.comotherexample.org", EncoderUtil.encodeAddressDomain("example.com, other@example.org"));
        Assert.assertEquals("", EncoderUtil.encodeAddressDomain(",\u0000"));
        Assert.assertEquals("ab", EncoderUtil.encodeAddressDomain("a b"));
        Assert.assertEquals("a", EncoderUtil.encodeAddressDomain("a>"));
    }

    @Test
    public void harmlessMalformedDomainsShouldBeKept() {
        Assert.assertEquals("[", EncoderUtil.encodeAddressDomain("["));
        Assert.assertEquals("example.com.", EncoderUtil.encodeAddressDomain("example.com."));
        Assert.assertEquals("a..b", EncoderUtil.encodeAddressDomain("a..b"));
    }

    @Test
    public void invalidParameterNamesShouldBeRejected() {
        for (String name : new String[] {"*", "a*b", "*0", "a**", "a*1*2", "a;b", "a=b", "a b", "", "a\""}) {
            try {
                EncoderUtil.encodeHeaderParameter(name, "v");
                Assert.fail("Expecting IllegalArgumentException for " + name);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test
    public void tokenAndRfc2231ParameterNamesShouldBeAccepted() {
        Assert.assertEquals("filename=v", EncoderUtil.encodeHeaderParameter("FileName", "v"));
        Assert.assertEquals("filename*=v", EncoderUtil.encodeHeaderParameter("filename*", "v"));
        Assert.assertEquals("filename*0=v", EncoderUtil.encodeHeaderParameter("filename*0", "v"));
        Assert.assertEquals("filename*1*=v", EncoderUtil.encodeHeaderParameter("filename*1*", "v"));
    }

    @Test
    public void encodeEncodedWordShouldChooseAnEncodableCharsetForADecodeOnlyCharset() {
        for (String name : new String[] {"ISO-2022-CN", "x-JISAutoDetect"}) {
            Charset decodeOnly = Charset.forName(name);
            Assert.assertFalse(decodeOnly.canEncode());

            for (String text : new String[] {"Hi", "Grüße 你好"}) {
                for (Encoding encoding : new Encoding[] {null, Encoding.B, Encoding.Q}) {
                    String encoded = EncoderUtil.encodeEncodedWord(text, Usage.TEXT_TOKEN, 0, decodeOnly, encoding);
                    Assert.assertEquals(text, DecoderUtil.decodeEncodedWords(encoded, DecodeMonitor.SILENT));
                }
            }
        }
    }
}
