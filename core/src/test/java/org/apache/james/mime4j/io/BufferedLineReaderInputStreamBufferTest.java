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

import org.apache.james.mime4j.io.BufferedLineReaderInputStream;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class BufferedLineReaderInputStreamBufferTest extends TestCase {

    public void testInvalidInput() throws Exception {
        String text = "blah blah yada yada";
        byte[] b1 = text.getBytes("US-ASCII");
        String pattern = "blah";
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        
        assertEquals('b', inbuffer.read());
        assertEquals('l', inbuffer.read());
        
        try {
            inbuffer.charAt(1);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.charAt(20);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, -1, 3);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, 1, 3);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, 2, -1);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf(b2, 2, 18);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        assertEquals(5, inbuffer.indexOf(b2, 2, 17));
        try {
            inbuffer.indexOf((byte)' ', -1, 3);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf((byte)' ', 1, 3);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf((byte)' ', 2, -1);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            inbuffer.indexOf((byte)' ', 2, 18);
            fail("IndexOutOfBoundsException should have been thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
        assertEquals(10, inbuffer.indexOf((byte)'y', 2, 17));
    }
      
    public void testBasicOperations() throws Exception {
        String text = "bla bla yada yada haha haha";
        byte[] b1 = text.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        assertEquals(0, inbuffer.pos());
        assertEquals(27, inbuffer.limit());
        assertEquals(27, inbuffer.length());

        inbuffer.read();
        inbuffer.read();

        assertEquals(2, inbuffer.pos());
        assertEquals(27, inbuffer.limit());
        assertEquals(25, inbuffer.length());
        
        byte[] tmp1 = new byte[3];
        assertEquals(3, inbuffer.read(tmp1));

        assertEquals(5, inbuffer.pos());
        assertEquals(27, inbuffer.limit());
        assertEquals(22, inbuffer.length());
        
        byte[] tmp2 = new byte[22];
        assertEquals(22, inbuffer.read(tmp2));

        assertEquals(27, inbuffer.pos());
        assertEquals(27, inbuffer.limit());
        assertEquals(0, inbuffer.length());

        assertEquals(-1, inbuffer.read(tmp1));
        assertEquals(-1, inbuffer.read(tmp1));
        assertEquals(-1, inbuffer.read());
        assertEquals(-1, inbuffer.read());
    }

    public void testPatternMatching1() throws Exception {
        String text = "blabla d is the word";
        String pattern = "d";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(7, i);
    }
    
    public void testPatternMatching2() throws Exception {
        String text = "disddisdissdsidsidsiid";
        String pattern = "siid";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(18, i);
    }
    
    public void testPatternMatching3() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "blah";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(-1, i);
    }
    
    public void testPatternMatching4() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "bla";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(0, i);
    }

    public void testPatternOutOfBound() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern1 = "bla bla";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern1.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        byte[] tmp = new byte[3];
        inbuffer.read(tmp);
        int i = inbuffer.indexOf(b2, inbuffer.pos(), inbuffer.length());
        assertEquals(-1, i);
        i = inbuffer.indexOf(b2, inbuffer.pos(), inbuffer.length() - 1);
        assertEquals(-1, i);
    }

    public void testCharOutOfBound() throws Exception {
        String text = "zzz blah blah blah ggg";
        byte[] b1 = text.getBytes("US-ASCII");
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        byte[] tmp = new byte[3];
        inbuffer.read(tmp);
        int i = inbuffer.indexOf((byte)'z', inbuffer.pos(), inbuffer.length());
        assertEquals(-1, i);
        i = inbuffer.indexOf((byte)'g', inbuffer.pos(), inbuffer.length() - 3);
        assertEquals(-1, i);
    }
    
    public void test0xFFInBinaryStream() throws Exception {
        byte[] b1 = new byte[] {1, 2, 3, (byte) 0xff, 10, 1, 2, 3};
        byte[] b2 = new byte[] {10};
        BufferedLineReaderInputStream inbuffer = new BufferedLineReaderInputStream(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(4, i);
    }
}
