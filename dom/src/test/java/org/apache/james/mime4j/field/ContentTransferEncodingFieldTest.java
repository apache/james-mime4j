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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.field.ContentTransferEncodingFieldImpl;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.TestCase;

public class ContentTransferEncodingFieldTest extends TestCase {

    static ContentTransferEncodingField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentTransferEncodingFieldImpl.PARSER.parse(rawField, null);
    }

    public void testGetEncoding() throws Exception {
        ContentTransferEncodingField f = parse("Content-Transfer-Encoding: 8bit");
        assertEquals("8bit", f.getEncoding());

        f = parse("Content-Transfer-Encoding:    BaSE64   ");
        assertEquals("base64", f.getEncoding());

        f = parse("Content-Transfer-Encoding:       ");
        assertEquals("", f.getEncoding());

        f = parse("Content-Transfer-Encoding:");
        assertEquals("", f.getEncoding());
    }

    public void testGetEncodingStatic() throws Exception {
        ContentTransferEncodingField f = parse("Content-Transfer-Encoding: 8bit");
        assertEquals("8bit", ContentTransferEncodingFieldImpl.getEncoding(f));

        f = null;
        assertEquals("7bit", ContentTransferEncodingFieldImpl.getEncoding(f));

        f = parse("Content-Transfer-Encoding:       ");
        assertEquals("7bit", ContentTransferEncodingFieldImpl.getEncoding(f));

        f = parse("Content-Transfer-Encoding:");
        assertEquals("7bit", ContentTransferEncodingFieldImpl.getEncoding(f));
    }

}
