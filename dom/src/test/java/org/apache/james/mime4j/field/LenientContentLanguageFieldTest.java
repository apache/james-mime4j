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

package org.apache.james.mime4j.field;

import java.util.List;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentLanguageField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.TestCase;

public class LenientContentLanguageFieldTest extends TestCase {

    static ContentLanguageField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentLanguageFieldLenientImpl.PARSER.parse(rawField, null);
    }

    public void testGetLanguage() throws Exception {
        ContentLanguageField f = parse("Content-Language: en, de");
        List<String> langs = f.getLanguages();
        assertNotNull(langs);
        assertEquals(2, langs.size());
        assertEquals("en", langs.get(0));
        assertEquals("de", langs.get(1));
    }

    public void testGetLanguageEmpty() throws Exception {
        ContentLanguageField f = parse("Content-Language: ");
        List<String> langs = f.getLanguages();
        assertNotNull(langs);
        assertEquals(0, langs.size());
    }

    public void testGetLanguageWithComments() throws Exception {
        ContentLanguageField f = parse("Content-Language: en (yada yada), (blah blah)de");
        List<String> langs = f.getLanguages();
        assertNotNull(langs);
        assertEquals(2, langs.size());
        assertEquals("en", langs.get(0));
        assertEquals("de", langs.get(1));
    }

    public void testGetLanguageWithUnderscore() throws Exception {
        ContentLanguageField f = parse("Content-Language: en, en_GB (Great Britain)");
        List<String> langs = f.getLanguages();
        assertNotNull(langs);
        assertEquals(2, langs.size());
        assertEquals("en", langs.get(0));
        assertEquals("en_GB", langs.get(1));
    }

    public void testGetLanguageWithEmptyElement() throws Exception {
        ContentLanguageField f = parse("Content-Language: en,, de,");
        List<String> langs = f.getLanguages();
        assertNotNull(langs);
        assertEquals(2, langs.size());
        assertEquals("en", langs.get(0));
        assertEquals("de", langs.get(1));
    }

}
