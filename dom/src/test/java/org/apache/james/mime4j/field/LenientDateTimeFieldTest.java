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

import java.util.Date;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

import junit.framework.TestCase;

public class LenientDateTimeFieldTest extends TestCase {

    static DateTimeField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return DateTimeFieldLenientImpl.PARSER.parse(rawField, null);
    }

    public void testDateDST() throws Exception  {
        DateTimeField f = parse("Date: Wed, 16 Jul 2008 17:12:33 +0200");
        assertEquals(new Date(1216221153000L), f.getDate());
    }

    public void testDateDSTNoDayOfWeek() throws Exception  {
        DateTimeField f = parse("Date: 16 Jul 2008 17:12:33 +0200");
        assertEquals(new Date(1216221153000L), f.getDate());
    }
    
    public void testdd() throws Exception {
        DateTimeField f = parse("Date: Thu, 01 Jan 1970 12:00:00 +0000");
        assertEquals(43200000L, f.getDate().getTime());
    }

    public void testMime4j219() throws Exception {
        DateTimeField f = parse("Date: Tue, 17 Jul 2012 22:23:35.882 0000");
        assertEquals(1342563815882L,f.getDate().getTime());
    }

    public void testDateWithExtraLeadingWhiteSpace() throws Exception {
        DateTimeField f = parse("Date:  Wed, 28 Mar 2007 13:32:39 +1000");
        assertEquals(1175052759000L, f.getDate().getTime());
    }

}
