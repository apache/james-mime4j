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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class QuotedPrintableInputStreamTest {

    @Test
    public void testBasicDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("=e1=e2=E3=E4\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("\u00e1\u00e2\u00e3\u00e4\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeBufferWrapping() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream(
                "=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n=e1=e2=E3=E4\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("\u00e1\u00e2\u00e3\u00e4\r\n\u00e1\u00e2\u00e3\u00e4\r\n\u00e1\u00e2\u00e3" +
                "\u00e4\r\n\u00e1\u00e2\u00e3\u00e4\r\n\u00e1\u00e2\u00e3\u00e4\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testInvalidValueDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("=e1=g2=E3=E4\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("\u00e1=g2\u00e3\u00e4\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeTrailingBlanks() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("   =e1 =e2  =E3\t=E4  \t \t    \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("   \u00e1 \u00e2  \u00e3\t\u00e4\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testCanonicalSoftBreakDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("Soft line   =\r\nHard line   \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   Hard line\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testInvalidCR() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("Invalid=\rCR\rHard line   \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        // TODO is this what we really expect from decoding a stream including CR with no LF?
        Assert.assertEquals("Invalid=\rCR\rHard line\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testSoftBreakLoneLFDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("Soft line   =\nHard line   \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   Hard line\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testSoftBreakTrailingBalnksDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("Soft line   = \t \r\nHard line   \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   Hard line\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testBrokenSoftBreakDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("Soft line   =\rHard line   \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("Soft line   =\rHard line\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testEscapedEQDecode() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("width==340 height=3d200\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("width=340 height=200\r\n", new String(read(decoder), "ISO8859-1"));
        // TODO this could be even decoded as width=40 height=200.
    }

    @Test
    public void testBrokenEscapedEQDecode() throws IOException, UnsupportedEncodingException {
        /*
         * This isn't valid qp (==) but it is known to occur in certain
         * messages, especially spam.
         */
        ByteArrayInputStream bis = new ByteArrayInputStream("width==\r\n340 height=3d200\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("width=340 height=200\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testSpacesBeforeEOL() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("some \r\n spaced\t\r\ncontent \t \r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals("some\r\n spaced\r\ncontent\r\n", new String(read(decoder), "ISO8859-1"));
    }


    @Test
    public void testDecodeEndOfStream1() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("01234567".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234567", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeEndOfStream2() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("012345\r".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("012345", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeEndOfStream3() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("012345\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("012345\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeEndOfStream4() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("01234= ".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeEndOfStream5() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("01234=\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodeEndOfStream6() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream("01234\r\n".getBytes("US-ASCII"));
        QuotedPrintableInputStream decoder = new QuotedPrintableInputStream(6, bis, false);
        Assert.assertEquals("01234\r\n", new String(read(decoder), "ISO8859-1"));
    }

    @Test
    public void testDecodePrematureClose() throws IOException, UnsupportedEncodingException {
        ByteArrayInputStream bis;
        QuotedPrintableInputStream decoder;

        bis = new ByteArrayInputStream("=e1=e2=E3=E4\r\n".getBytes("US-ASCII"));
        decoder = new QuotedPrintableInputStream(bis);
        Assert.assertEquals('\u00e1', decoder.read());
        Assert.assertEquals('\u00e2', decoder.read());
        decoder.close();

        try {
            decoder.read();
            Assert.fail();
        } catch (IOException expected) {
        }
    }

    private byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            bos.write(b);
        }
        return bos.toByteArray();
    }
}
