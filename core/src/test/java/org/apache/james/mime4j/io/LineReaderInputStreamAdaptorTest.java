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

import org.apache.james.mime4j.io.LineReaderInputStreamAdaptor;
import org.apache.james.mime4j.io.MaxLineLimitException;
import org.apache.james.mime4j.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public class LineReaderInputStreamAdaptorTest extends TestCase {

    public void testBasicOperations() throws Exception {
        String text = "ah blahblah";
        byte[] b1 = text.getBytes("US-ASCII");
        
        LineReaderInputStreamAdaptor instream = new LineReaderInputStreamAdaptor(
                new ByteArrayInputStream(b1)); 
        
        assertEquals((byte)'a', instream.read());
        assertEquals((byte)'h', instream.read());
        assertEquals((byte)' ', instream.read());

        byte[] tmp1 = new byte[4];
        assertEquals(4, instream.read(tmp1));
        assertEquals(4, instream.read(tmp1));

        assertEquals(-1, instream.read(tmp1));
        assertEquals(-1, instream.read(tmp1));
        assertEquals(-1, instream.read());
        assertEquals(-1, instream.read());
    }

    public void testBasicReadLine() throws Exception {
        
        String[] teststrs = new String[5];
        teststrs[0] = "Hello\r\n";
        teststrs[1] = "This string should be much longer than the size of the input buffer " +
                "which is only 16 bytes for this test\r\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("123456789 ");
        }
        sb.append("and stuff like that\r\n");
        teststrs[2] = sb.toString();
        teststrs[3] = "\r\n";
        teststrs[4] = "And goodbye\r\n";

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        
        for (String teststr : teststrs) {
            outstream.write(teststr.getBytes("US-ASCII"));
        }
        byte[] raw = outstream.toByteArray();
        
        LineReaderInputStreamAdaptor instream = new LineReaderInputStreamAdaptor(
                new ByteArrayInputStream(raw)); 
        
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
        
        String teststr = "\n\n\r\n\r\r\n\n\n\n\n\n";
        byte[] raw = teststr.getBytes("US-ASCII");
        
        LineReaderInputStreamAdaptor instream = new LineReaderInputStreamAdaptor(
                new ByteArrayInputStream(raw)); 
        
        ByteArrayBuffer linebuf = new ByteArrayBuffer(8); 
        linebuf.clear();
        instream.readLine(linebuf);
        String s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);
        
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

        linebuf.clear();
        instream.readLine(linebuf);
        s = new String(linebuf.toByteArray(), "US-ASCII");
        assertEquals("\n", s);

        assertEquals(-1, instream.readLine(linebuf));
        assertEquals(-1, instream.readLine(linebuf));
    }

    public void testReadEmptyLineMaxLimit() throws Exception {
        
        String teststr = "1234567890\r\n";
        byte[] raw = teststr.getBytes("US-ASCII");
        
        LineReaderInputStreamAdaptor instream1 = new LineReaderInputStreamAdaptor(
                new ByteArrayInputStream(raw), 13); 
        ByteArrayBuffer linebuf = new ByteArrayBuffer(8); 
        linebuf.clear();
        instream1.readLine(linebuf);

        LineReaderInputStreamAdaptor instream2 = new LineReaderInputStreamAdaptor(
                new ByteArrayInputStream(raw), 12); 
        linebuf.clear();
        try {
            instream2.readLine(linebuf);
            fail("MaxLineLimitException should have been thrown");
        } catch (MaxLineLimitException ex) {
        }
    }

}
