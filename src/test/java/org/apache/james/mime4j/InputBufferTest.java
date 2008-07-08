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

import junit.framework.TestCase;

public class InputBufferTest extends TestCase {

    public void testPatternMatching1() throws Exception {
        String text = "blabla d is the word";
        String pattern = "d";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        InputBuffer inbuffer = new InputBuffer(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(7, i);
    }
    
    public void testPatternMatching2() throws Exception {
        String text = "disddisdissdsidsidsiid";
        String pattern = "siid";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        InputBuffer inbuffer = new InputBuffer(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(18, i);
    }
    
    public void testPatternMatching3() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "blah";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        InputBuffer inbuffer = new InputBuffer(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(-1, i);
    }
    
    public void testPatternMatching4() throws Exception {
        String text = "bla bla yada yada haha haha";
        String pattern = "bla";
        byte[] b1 = text.getBytes("US-ASCII");
        byte[] b2 = pattern.getBytes("US-ASCII");
        InputBuffer inbuffer = new InputBuffer(new ByteArrayInputStream(b1), 4096);
        inbuffer.fillBuffer();
        int i = inbuffer.indexOf(b2);
        assertEquals(0, i);
    }
}
