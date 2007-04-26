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
    /**
     * Tests that a CRLF immediately preceding a boundary isn't included in
     * the stream.
     */
    public void testCRLFPrecedingBoundary() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundary\r\n" +
                "Line 3\r\nLine 4\r\n\r\n--boundary\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        
        assertEquals("Line 1\r\nLine 2", 
                     read(new MimeBoundaryInputStream(bis, "boundary")));
        
        assertEquals("Line 3\r\nLine 4\r\n", 
                     read(new MimeBoundaryInputStream(bis, "boundary")));
    }
    
    public void testBigEnoughPushbackBuffer() throws IOException {
        String text = "Line 1\r\nLine 2\r\n--boundar\r\n" +
                "Line 3\r\nLine 4\r\n\r\n--boundary\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        
        assertEquals("Line 1\r\nLine 2\r\n--boundar\r\n" +
                     "Line 3\r\nLine 4\r\n", 
                     read(new MimeBoundaryInputStream(bis, "boundary")));
    }
    
    /**
     * Tests that CR characters are ignored.
     */
    public void testCRIgnored() throws IOException {
        
    }
    
    private String read(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        int b = 0;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        return sb.toString();
    }
    
    /**
     * Tests that a stream containing only a boundary is empty.
     */
    public void testImmediateBoundary() throws IOException {
        String text = "--boundary\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(bis, "boundary");
        assertEquals(-1, stream.read());
        
        text = "\r\n--boundary\r\n";
        
        bis = new ByteArrayInputStream(text.getBytes());
        stream = 
            new MimeBoundaryInputStream(bis, "boundary");
        assertEquals(-1, stream.read());
    }
    
    /**
     * Tests that hasMoreParts behave as expected.
     */
    public void testHasMoreParts() throws IOException {
        String text = "--boundary--\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(bis, "boundary");
        assertFalse(stream.hasMoreParts());
        assertEquals(-1, stream.read());

    }
    
    /**
     * Tests that a stream containing only a boundary is empty.
     */
    public void testPrefixIsBoundary() throws IOException {
        String text = "Line 1\r\n\r\n--boundaryyada\r\n";
        
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        MimeBoundaryInputStream stream = 
            new MimeBoundaryInputStream(bis, "boundary");
        assertEquals("Line 1\r\n", read(stream));
        
        text = "--boundaryyada\r\n";
        
        bis = new ByteArrayInputStream(text.getBytes());
        stream = 
            new MimeBoundaryInputStream(bis, "boundary");
        assertEquals(-1, stream.read());
    }    
}
