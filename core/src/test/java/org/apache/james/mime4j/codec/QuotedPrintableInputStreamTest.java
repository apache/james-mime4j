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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.io.InputStreams;
import org.junit.Assert;
import org.junit.Test;

public class QuotedPrintableInputStreamTest {

    private static String readText(final InputStream is) throws IOException {
        return IOUtils.toString(is, Charsets.ISO_8859_1.name());
    }

    private static String readTextByOne(final InputStream is) throws IOException {
        StringBuilder buf = new StringBuilder();
        Reader reader = new InputStreamReader(is, Charsets.ISO_8859_1.name());
        int ch;
        while ((ch = reader.read()) != -1) {
            buf.append((char) ch);
        }
        return buf.toString();
    }

    @Test
    public void testBasicDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("=e1=e2=E3=E4\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("\u00e1\u00e2\u00e3\u00e4\r\n", readText(decoder));
    }

    @Test
    public void testDecodeBufferWrapping() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("\u00e1\u00e2\u00e3\u00e4\r\n\u00e1\u00e2\u00e3\u00e4\r\n\u00e1\u00e2\u00e3" +
                "\u00e4\r\n\u00e1\u00e2\u00e3\u00e4\r\n\u00e1\u00e2\u00e3\u00e4\r\n", readText(decoder));
    }

    @Test
    public void testInvalidValueDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("=e1=g2=E3=E4\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("\u00e1=g2\u00e3\u00e4\r\n", readText(decoder));
    }

    @Test
    public void testDecodeTrailingBlanks() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("   =e1 =e2  =E3\t=E4  \t \t    \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("   \u00e1 \u00e2  \u00e3\t\u00e4\r\n", readText(decoder));
    }

    @Test
    public void testCanonicalSoftBreakDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("Soft line   =\r\nHard line   \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   Hard line\r\n", readText(decoder));
    }

    @Test
    public void testSoftBreakStrictMode() throws IOException {
        String input = "<?xml version=3D\"1.0\" standalone=3D\"no\"?>\r\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/=\r\n" +
                "SVG/1.1/DTD/svg11.dtd\" >\r\n";
        String expected = "<?xml version=\"1.0\" standalone=\"no\"?>\r\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/" +
                "SVG/1.1/DTD/svg11.dtd\" >\r\n";
        InputStream bis = InputStreams.createAscii(input);
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis, DecodeMonitor.STRICT);
        Assert.assertEquals(expected, readText(decoder));
    }

    @Test
    public void testInvalidCR() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("Invalid=\rCR\rHard line   \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        // TODO is this what we really expect from decoding a stream including CR with no LF?
        Assert.assertEquals("Invalid=\rCR\rHard line\r\n", readText(decoder));
    }

    @Test
    public void testInvalidCRStrictMode() throws IOException {
        InputStream bis = InputStreams.createAscii("Invalid=\rCR\rHard line   \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis, DecodeMonitor.STRICT);
        try {
            readText(decoder);
            Assert.fail("IOException should have been thrown");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testSoftBreakLoneLFDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("Soft line   =\nHard line   \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   Hard line\r\n", readText(decoder));
    }

    @Test
    public void testSpaceBeforeSoftBreakStrictMode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("text before eq sign =\r\n text after LF");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis,DecodeMonitor.STRICT);
        Assert.assertEquals("text before eq sign  text after LF", readTextByOne(decoder));
    }

    @Test
    public void testSoftBreakTrailingBalnksDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("Soft line   = \t \r\nHard line   \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   Hard line\r\n", readText(decoder));
    }

    @Test
    public void testBrokenSoftBreakDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("Soft line   =\rHard line   \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   =\rHard line\r\n", readText(decoder));
    }

    @Test
    public void testEscapedEQDecode() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("width==340 height=3d200\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("width=340 height=200\r\n", readText(decoder));
        // TODO this could be even decoded as width=40 height=200.
    }

    @Test
    public void testBrokenEscapedEQDecode() throws IOException, UnsupportedEncodingException {
        /*
         * This isn't valid qp (==) but it is known to occur in certain
         * messages, especially spam.
         */
        InputStream bis = InputStreams.createAscii("width==\r\n340 height=3d200\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("width=340 height=200\r\n", readText(decoder));
    }

    @Test
    public void testSpacesBeforeEOL() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("some \r\n spaced\t\r\ncontent \t \r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("some\r\n spaced\r\ncontent\r\n", readText(decoder));
    }


    @Test
    public void testDecodeEndOfStream1() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("01234567");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234567", readText(decoder));
    }

    @Test
    public void testDecodeEndOfStream2() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("012345\r");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("012345", readText(decoder));
    }

    @Test
    public void testDecodeEndOfStream3() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("012345\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("012345\r\n", readText(decoder));
    }

    @Test
    public void testDecodeEndOfStream4() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("01234= ");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234", readText(decoder));
    }

    @Test
    public void testDecodeEndOfStream5() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("01234=\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234", readText(decoder));
    }

    @Test
    public void testDecodeEndOfStream6() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("01234\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234\r\n", readText(decoder));
    }

    @Test
    public void testDecodePrematureClose() throws IOException, UnsupportedEncodingException {
        InputStream bis = InputStreams.createAscii("=e1=e2=E3=E4\r\n");
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals('\u00e1', decoder.read());
        Assert.assertEquals('\u00e2', decoder.read());
        decoder.close();

        try {
            decoder.read();
            Assert.fail();
        } catch (IOException expected) {
        }
    }


    @Test
    public void closingTwiceShouldNotMakeTwoLaterStreamsShareABuffer() throws Exception {
        QuotedPrintableInputStream closed = new QuotedPrintableInputStream(InputStreams.createAscii("abc"), false);
        closed.close();
        closed.close();

        String first = "first=20text with  some   blanks=\r\n and more";
        String second = "SECOND=20TEXT WITH  SOME   BLANKS=\r\n AND MORE";
        QuotedPrintableInputStream in1 = new QuotedPrintableInputStream(InputStreams.createAscii(first), false);
        QuotedPrintableInputStream in2 = new QuotedPrintableInputStream(InputStreams.createAscii(second), false);
        StringBuilder out1 = new StringBuilder();
        StringBuilder out2 = new StringBuilder();
        int b1 = 0;
        int b2 = 0;
        while (b1 != -1 || b2 != -1) {
            if (b1 != -1 && (b1 = in1.read()) != -1) {
                out1.append((char) b1);
            }
            if (b2 != -1 && (b2 = in2.read()) != -1) {
                out2.append((char) b2);
            }
        }

        Assert.assertEquals("first text with  some   blanks and more", out1.toString());
        Assert.assertEquals("SECOND TEXT WITH  SOME   BLANKS AND MORE", out2.toString());
    }

    private static String decodeReadingAtMost(String encoded, int readSize) throws IOException {
        InputStream source = new FilterInputStream(InputStreams.createAscii(encoded)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, readSize));
            }
        };
        try (QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(source, false)) {
            return new String(IOUtils.toByteArray(decoder), "ISO-8859-1");
        }
    }

    @Test
    public void decodingShouldNotDependOnReadSizes() throws Exception {
        for (String encoded : new String[] {"7bY==\r\nYLY", "7bY==\nYLY", "a=3D=\r\nb", "a=\r\nb==c", "=4", "=", "a=\r"}) {
            String whole = decodeReadingAtMost(encoded, Integer.MAX_VALUE);
            for (int readSize = 1; readSize < 6; readSize++) {
                Assert.assertEquals(encoded + " read by " + readSize, whole, decodeReadingAtMost(encoded, readSize));
            }
        }
    }
}
