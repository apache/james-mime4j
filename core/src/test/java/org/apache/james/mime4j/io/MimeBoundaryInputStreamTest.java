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

package org.apache.james.mime4j.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.io.BufferedLineReaderInputStream;
import org.apache.james.mime4j.io.LineReaderInputStream;
import org.apache.james.mime4j.io.MimeBoundaryInputStream;
import org.apache.james.mime4j.util.ByteArrayBuffer;


import junit.framework.TestCase;

public class MimeBoundaryInputStreamTest extends TestCase {

    public void testBasicReading() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n--boundary--";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\nLine 2", read(mime1, 5));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 3\r\nLine 4", read(mime2, 5));

        assertTrue(mime2.isLastPart());
    }
    
    public void testLenientLineDelimiterReading() throws IOException {
        String text = "Line 1\r\nLine 2\n--boundary\n" +
                "Line 3\r\nLine 4\n--boundary--\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\nLine 2", read(mime1, 5));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 3\r\nLine 4", read(mime2, 5));

        assertTrue(mime2.isLastPart());
    }
    
    public void testBasicReadingSmallBuffer1() throws IOException {
        String text = "yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada\r\n--boundary\r\n" +
                "blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah\r\n--boundary--";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 20); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada", 
                read(mime1, 10));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah", 
                read(mime2, 10));

        assertTrue(mime2.isLastPart());
    }
    
    public void testBasicReadingSmallBuffer2() throws IOException {
        String text = "yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada\r\n--boundary\r\n" +
                "blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah\r\n--boundary--";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 20); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        
        assertEquals("yadayadayadayadayadayadayadayadayadayadayadayadayadayadayadayada", 
                read(mime1, 25));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("blahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblahblah", 
                read(mime2, 25));

        assertTrue(mime2.isLastPart());
    }
    
    public void testBasicReadingByOneByte() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n--boundary--";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\nLine 2", readByOneByte(mime1));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 3\r\nLine 4", readByOneByte(mime2));

        assertTrue(mime2.isLastPart());
    }
    
    /**
     * Tests that a CRLF immediately preceding a boundary isn't included in
     * the stream.
     */
    public void testCRLFPrecedingBoundary() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n\r\n--boundary\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\nLine 2", read(mime1, 5));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 3\r\nLine 4\r\n", read(mime2, 5));

        assertFalse(mime2.isLastPart());
    }
    
    private String readByOneByte(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b = 0;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    private String read(InputStream is, int bufsize) throws IOException {
        StringBuilder sb = new StringBuilder();
        int l;
        byte[] tmp = new byte[bufsize];
        while ((l = is.read(tmp)) != -1) {
            for (int i = 0; i < l; i++) {
                sb.append((char) tmp[i]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Tests that a stream containing only a boundary is empty.
     */
    public void testImmediateBoundary() throws IOException {
        String text = "--boundary\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals(-1, stream.read());
        
        text = "\r\n--boundary\r\n";
        
        bis = new ByteArrayInputStream(text.getBytes());
        buffer = new BufferedLineReaderInputStream(bis, 4096); 
        stream = 
            new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals(-1, stream.read());
    }
    
    /**
     * Tests that hasMoreParts behave as expected.
     */
    public void testHasMoreParts() throws IOException {
        String text = "--boundary--\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals(-1, stream.read());
        assertTrue(stream.isLastPart());
    }
    
    /**
     * Tests that a stream containing only a boundary is empty.
     */
    public void testPrefixIsBoundary() throws IOException {
        String text = "Line 1\r\n\r\n--boundaryyada\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 4096); 
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\n", read(stream, 100));
        
        text = "--boundaryyada\r\n";
        
        bis = new ByteArrayInputStream(text.getBytes());
        buffer = new BufferedLineReaderInputStream(bis, 4096); 
        stream = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals(-1, stream.read());
    }
    
    
    public void testBasicReadLine() throws Exception {
        
        String[] teststrs = new String[5];
        teststrs[0] = "Hello\r\n";
        teststrs[1] = "This string should be much longer than the size of the input buffer " +
                "which is only 20 bytes for this test\r\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("123456789 ");
        }
        sb.append("and stuff like that\r\n");
        teststrs[2] = sb.toString();
        teststrs[3] = "\r\n";
        teststrs[4] = "And goodbye\r\n";

        String term = "\r\n--1234\r\n";
        
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        
        for (String teststr : teststrs) {
            outstream.write(teststr.getBytes("US-ASCII"));
        }
        outstream.write(term.getBytes("US-ASCII"));
        byte[] raw = outstream.toByteArray();
        
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(raw), 20); 
        LineReaderInputStream instream = new MimeBoundaryInputStream(inbuffer, "1234"); 
        
        ByteArrayBuffer linebuf = new ByteArrayBuffer(8); 
        for (String teststr : teststrs) {
            linebuf.clear();
            instream.readLine(linebuf);
            String s = new String(linebuf.toByteArray(), "US-ASCII");
            assertEquals(teststr, s);
        }
        assertEquals(-1, instream.readLine(linebuf));
        assertEquals(-1, instream.readLine(linebuf));
    }
    
    public void testReadEmptyLine() throws Exception {
        
        String teststr = "01234567890123456789\n\n\r\n\r\r\n\n\n\n\n\n--1234\r\n";
        byte[] raw = teststr.getBytes("US-ASCII");
        
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(raw), 20); 
        LineReaderInputStream instream = new MimeBoundaryInputStream(inbuffer, "1234"); 
        
        ByteArrayBuffer linebuf = new ByteArrayBuffer(8); 
        linebuf.clear();
        instream.readLine(linebuf);
        String s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("01234567890123456789\n", s);
        
        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);
        
        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\r\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\r\r\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);

        assertEquals(-1, instream.readLine(linebuf));
        assertEquals(-1, instream.readLine(linebuf));
    }
    
    public void testboundaryLongerThanBuffer() throws IOException {
        String text = "--looooooooooooooooooooooooooong-boundary\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        BufferedLineReaderInputStream buffer = new BufferedLineReaderInputStream(bis, 10); 
        
        try {
            new MimeBoundaryInputStream(buffer, "looooooooooooooooooooooooooong-boundary");
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

}
