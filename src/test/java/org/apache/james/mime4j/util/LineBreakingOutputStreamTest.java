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

package org.apache.james.mime4j.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

/**
 * Test for LineBreakingOutputStream
 */
public class LineBreakingOutputStreamTest extends TestCase {
    
    public void testLongLine() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream os = new LineBreakingOutputStream(out, 4);
        os.write("gsdfklgsjdhflkgjhsdfklgjhsdlkfjghksdljfhgslkdjfhgklsjdhf".getBytes());
        os.flush();
        os.close();
        String expected = "gsdf\r\nklgs\r\njdhf\r\nlkgj\r\nhsdf\r\nklgj\r\nhsdl\r\nkfjg\r\nhksd\r\nljfh\r\ngslk\r\ndjfh\r\ngkls\r\njdhf";
        assertEquals(new String(out.toByteArray()), expected);
    }
    
    public void testLineLengthEqualInput() throws IOException {
        String input = "gsdfklgsjdhflkgjhsdfklgjhsdlkfjghksdljfhgslkdjfhgklsjdhf";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream os = new LineBreakingOutputStream(out, input.length());
        os.write(input.getBytes());
        os.flush();
        os.close();
        assertEquals(new String(out.toByteArray()), input);
    }
    
    public void testLineLengthLongerThanInput() throws IOException {
        String input = "gsdfklgsjdhflkgjhsdfklgjhsdlkfjghksdljfhgslkdjfhgklsjdhf";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream os = new LineBreakingOutputStream(out, input.length()+1);
        os.write(input.getBytes());
        os.flush();
        os.close();
        assertEquals(new String(out.toByteArray()), input);
    }
    
    public void testLineLengthShorterThanInput() throws IOException {
        String input = "gsdfklgsjdhflkgjhsdfklgjhsdlkfjghksdljfhgslkdjfhgklsjdhf";
        String expected = "gsdfklgsjdhflkgjhsdfklgjhsdlkfjghksdljfhgslkdjfhgklsjdh\r\nf";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream os = new LineBreakingOutputStream(out, input.length()-1);
        os.write(input.getBytes());
        os.flush();
        os.close();
        assertEquals(new String(out.toByteArray()), expected);
    }
    
    public void testZeroLengthInput() throws IOException {
        String input = "";
        String expected = "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream os = new LineBreakingOutputStream(out, 10);
        os.write(input.getBytes());
        os.flush();
        os.close();
        assertEquals(new String(out.toByteArray()), expected);
    }

}
