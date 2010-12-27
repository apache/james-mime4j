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

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

public class DecoderUtilTest extends TestCase {

    @Override
    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }
    /*
    public void testDecodeEncodedWords() {
        String s = "=?ISO-2022-JP?B?GyRCTCQbKEobJEI+NRsoShskQkJ6GyhKGyRCOS0bKEo=?= "
                 + "=?ISO-2022-JP?B?GyRCOXAbKEobJEIiKBsoShskQiU1GyhKGyRCJSQbKEo=?= "
                 + "=?ISO-2022-JP?B?GyRCJUkbKEobJEIlUxsoShskQiU4GyhKGyRCJU0bKEo=?= "  
                 + "=?ISO-2022-JP?B?GyRCJTkbKEobJEIkThsoShskQjdoGyhKGyRCRGobKEo=?= "
                 + "=?ISO-2022-JP?B?GyRCSEcbKEobJEIkRxsoShskQiQ5GyhKGyRCISobKEo=?=";      
        
        s = DecoderUtil.decodeEncodedWords(s);
        System.out.println(s);
    }*/
    
    public void testDecodeB() throws UnsupportedEncodingException {
        String s = DecoderUtil.decodeB("VGhpcyBpcyB0aGUgcGxhaW4gd"
                    + "GV4dCBtZXNzYWdlIQ==", "ISO8859-1");
        assertEquals("This is the plain text message!", s);
    }
    

    public void testDecodeQ() throws UnsupportedEncodingException {
        String s = DecoderUtil.decodeQ("=e1_=e2=09=E3_=E4_", 
                                                         "ISO8859-1");
        assertEquals("\u00e1 \u00e2\t\u00e3 \u00e4 ", s);
    }
    
    public void testDecodeEncodedWords() {
        assertEquals("", DecoderUtil.decodeEncodedWords(""));
        assertEquals("Yada yada", DecoderUtil.decodeEncodedWords("Yada yada"));
        assertEquals("  \u00e1\u00e2\u00e3\t\u00e4", 
                DecoderUtil.decodeEncodedWords("=?iso-8859-1?Q?_=20=e1=e2=E3=09=E4?="));
        assertEquals("Word 1 '  \u00e2\u00e3\t\u00e4'. Word 2 '  \u00e2\u00e3\t\u00e4'", 
                DecoderUtil.decodeEncodedWords("Word 1 '=?iso-8859-1?Q?_=20=e2=E3=09=E4?="
                        + "'. Word 2 '=?iso-8859-1?q?_=20=e2=E3=09=E4?='"));
        assertEquals("=?iso-8859-YADA?Q?_=20=t1=e2=E3=09=E4?=", 
                DecoderUtil.decodeEncodedWords("=?iso-8859-YADA?Q?_=20=t1=e2=E3=09=E4?="));
        assertEquals("A short text", 
                DecoderUtil.decodeEncodedWords("=?US-ASCII?B?QSBzaG9ydCB0ZXh0?="));
        assertEquals("A short text again!", 
                DecoderUtil.decodeEncodedWords("=?US-ASCII?b?QSBzaG9ydCB0ZXh0IGFnYWluIQ==?="));

        // invalid encoded words should be returned unchanged
        assertEquals("=?iso8859-1?Q?=", DecoderUtil.decodeEncodedWords("=?iso8859-1?Q?="));
        assertEquals("=?iso8859-1?b?=", DecoderUtil.decodeEncodedWords("=?iso8859-1?b?="));
        assertEquals("=?ISO-8859-1?Q?", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?"));
        assertEquals("=?ISO-8859-1?R?abc?=", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?R?abc?="));

        // encoded-text requires at least one character according to rfc 2047
        assertEquals("=?ISO-8859-1?Q??=", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q??="));
        assertEquals("=?ISO-8859-1?B??=", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?B??="));
        
        // white space between encoded words should be removed (MIME4J-104)
        assertEquals("a", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?="));
        assertEquals("a b", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= b"));
        assertEquals("ab", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= =?ISO-8859-1?Q?b?="));
        assertEquals("ab", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?=  =?ISO-8859-1?Q?b?="));
        assertEquals("ab", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?=\r\n  =?ISO-8859-1?Q?b?="));
        assertEquals("a b", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a_b?="));
        assertEquals("a b", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= =?ISO-8859-2?Q?_b?="));

        // non white space between encoded words should be retained
        assertEquals("a b c", DecoderUtil.decodeEncodedWords("=?ISO-8859-1?Q?a?= b =?ISO-8859-1?Q?c?="));

        // text before and after encoded words should be retained
        assertEquals(" a b c ", DecoderUtil.decodeEncodedWords(" =?ISO-8859-1?Q?a?= b =?ISO-8859-1?Q?c?= "));
        assertEquals("! a b c !", DecoderUtil.decodeEncodedWords("! =?ISO-8859-1?Q?a?= b =?ISO-8859-1?Q?c?= !"));
        
        // Bug detected on June 7, 2005. Decoding the following string caused
        // OutOfMemoryError.
        assertEquals("=3?!!\\=?\"!g6P\"!Xp:\"!", DecoderUtil.decodeEncodedWords("=3?!!\\=?\"!g6P\"!Xp:\"!"));
    }    
}
