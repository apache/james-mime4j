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
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RawFieldParserTest extends TestCase {

    public void testBasicParsing() throws Exception {
        String s = "raw: stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);
        
        RawFieldParser parser = new RawFieldParser();
        
        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertFalse(field.isUsedObsoleteSyntax());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingObsoleteSyntax() throws Exception {
        String s = "raw  \t  : stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);
        
        RawFieldParser parser = new RawFieldParser();
        
        RawField field = parser.parseField(raw);
        Assert.assertSame(raw, field.getRaw());
        Assert.assertEquals("raw", field.getName());
        Assert.assertEquals("stuff;  more stuff", field.getBody());
        Assert.assertTrue(field.isUsedObsoleteSyntax());
        Assert.assertEquals(s, field.toString());
    }

    public void testParsingInvalidSyntax1() throws Exception {
        String s = "raw    stuff;\r\n  more stuff";
        ByteSequence raw = ContentUtil.encode(s);
        
        RawFieldParser parser = new RawFieldParser();
        
        try {
            parser.parseField(raw);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

    public void testParsingInvalidSyntax2() throws Exception {
        String s = "raw    \t \t";
        ByteSequence raw = ContentUtil.encode(s);
        
        RawFieldParser parser = new RawFieldParser();
        
        try {
            parser.parseField(raw);
            fail("MimeException should have been thrown");
        } catch (MimeException expected) {
        }
    }

}
