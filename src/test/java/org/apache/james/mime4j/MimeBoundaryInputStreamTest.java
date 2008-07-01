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

package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeBoundaryInputStream;


import junit.framework.TestCase;

/**
 * 
 *
 * 
 * @version $Id: MimeBoundaryInputStreamTest.java,v 1.2 2004/10/02 12:41:11 ntherning Exp $
 */
public class MimeBoundaryInputStreamTest extends TestCase {

    public void testBasicReading() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n--boundary--";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes("US-ASCII"));
        
        InputBuffer buffer = new InputBuffer(bis, 4096); 
        
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
        
        InputBuffer buffer = new InputBuffer(bis, 20); 
        
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
        
        InputBuffer buffer = new InputBuffer(bis, 20); 
        
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
        
        InputBuffer buffer = new InputBuffer(bis, 4096); 
        
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
        
        InputBuffer buffer = new InputBuffer(bis, 4096); 
        
        MimeBoundaryInputStream mime1 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\nLine 2", read(mime1, 5));
        
        assertFalse(mime1.isLastPart());
        
        MimeBoundaryInputStream mime2 = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 3\r\nLine 4\r\n", read(mime2, 5));

        assertFalse(mime2.isLastPart());
    }
    
    private String readByOneByte(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        int b = 0;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    private String read(InputStream is, int bufsize) throws IOException {
        StringBuffer sb = new StringBuffer();
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
        InputBuffer buffer = new InputBuffer(bis, 4096); 
        
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals(-1, stream.read());
        
        text = "\r\n--boundary\r\n";
        
        bis = new ByteArrayInputStream(text.getBytes());
        buffer = new InputBuffer(bis, 4096); 
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
        InputBuffer buffer = new InputBuffer(bis, 4096); 
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
        InputBuffer buffer = new InputBuffer(bis, 4096); 
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals("Line 1\r\n", read(stream, 100));
        
        text = "--boundaryyada\r\n";
        
        bis = new ByteArrayInputStream(text.getBytes());
        buffer = new InputBuffer(bis, 4096); 
        stream = new MimeBoundaryInputStream(buffer, "boundary");
        assertEquals(-1, stream.read());
    }    
}
