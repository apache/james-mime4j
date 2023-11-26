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

package org.apache.james.mime4j.stream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.RecycledByteArrayBuffer;

import junit.framework.TestCase;

public class DefaultFieldBuilderTest extends TestCase {

    static ByteArrayBuffer line(final String line) throws Exception {
        ByteArrayBuffer buf = new ByteArrayBuffer(line.length());
        byte[] b = line.getBytes("US-ASCII");
        buf.append(b, 0, b.length);
        return buf;
    }

    public void testBasics() throws Exception {
        DefaultFieldBuilder builder = new DefaultFieldBuilder(0);
        builder.reset();
        builder.append(line("raw:   stuff;\r\n"));
        builder.append(line("   more stuff;\r\n"));
        builder.append(line("   a lot more stuff\r\n"));
        RecycledByteArrayBuffer buf = builder.getRaw();
        assertNotNull(buf);
        assertEquals("raw:   stuff;\r\n   more stuff;\r\n   a lot more stuff\r\n",
                new String(buf.toByteArray(), "US-ASCII"));
        RawField field = builder.build();
        assertNotNull(field);
        assertEquals("raw", field.getName());
        assertEquals("  stuff;   more stuff;   a lot more stuff", field.getBody());
        ByteSequence raw = field.getRaw();
        assertNotNull(raw);
        assertEquals("raw:   stuff;\r\n   more stuff;\r\n   a lot more stuff",
                new String(raw.toByteArray(), "US-ASCII"));
    }

    public void testObsoleteSyntax() throws Exception {
        DefaultFieldBuilder builder = new DefaultFieldBuilder(0);
        builder.reset();
        builder.append(line("raw  : stuff;\r\n"));
        builder.append(line("   more stuff;\r\n"));
        builder.append(line("   a lot more stuff\r\n"));
        RecycledByteArrayBuffer buf = builder.getRaw();
        assertNotNull(buf);
        assertEquals("raw  : stuff;\r\n   more stuff;\r\n   a lot more stuff\r\n",
                new String(buf.toByteArray(), "US-ASCII"));
        RawField field = builder.build();
        assertNotNull(field);
        assertEquals("raw", field.getName());
        assertEquals("stuff;   more stuff;   a lot more stuff", field.getBody());
        ByteSequence raw = field.getRaw();
        assertNotNull(raw);
        assertEquals("raw  : stuff;\r\n   more stuff;\r\n   a lot more stuff",
                new String(raw.toByteArray(), "US-ASCII"));
    }

    public void testNoTrailingCRLF() throws Exception {
        DefaultFieldBuilder builder = new DefaultFieldBuilder(0);
        builder.reset();
        builder.append(line("raw:   stuff;\r\n"));
        builder.append(line("   more stuff;\r\n"));
        builder.append(line("   a lot more stuff"));
        RecycledByteArrayBuffer buf = builder.getRaw();
        assertNotNull(buf);
        assertEquals("raw:   stuff;\r\n   more stuff;\r\n   a lot more stuff",
                new String(buf.toByteArray(), "US-ASCII"));
        RawField field = builder.build();
        assertNotNull(field);
        assertEquals("raw", field.getName());
        assertEquals("  stuff;   more stuff;   a lot more stuff", field.getBody());
        ByteSequence raw = field.getRaw();
        assertNotNull(raw);
        assertEquals("raw:   stuff;\r\n   more stuff;\r\n   a lot more stuff",
                new String(raw.toByteArray(), "US-ASCII"));
    }

    public void testIllegalCharsInName() throws Exception {
        DefaultFieldBuilder builder = new DefaultFieldBuilder(0);
        builder.reset();
        builder.append(line("raw stuff: some stuff\r\n"));
        try {
            builder.build();
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testReset() throws Exception {
        DefaultFieldBuilder builder = new DefaultFieldBuilder(0);
        builder.reset();
        builder.append(line("raw: some stuff\r\n"));
        RecycledByteArrayBuffer buf = builder.getRaw();
        assertNotNull(buf);
        assertEquals("raw: some stuff\r\n", new String(buf.toByteArray(), "US-ASCII"));
        builder.reset();
        buf = builder.getRaw();
        assertTrue(buf.isEmpty());
        try {
            builder.build();
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testTooLong() throws Exception {
        DefaultFieldBuilder builder = new DefaultFieldBuilder(20);
        builder.reset();
        builder.append(line("raw: some stuff\r\n"));
        try {
            builder.append(line("toooooooooooooooooooooooooooooooooooooons of stuff\r\n"));
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

}
