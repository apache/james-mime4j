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
import org.junit.Assert;
import org.junit.Test;

public class LenientDateTimeFieldTest {

    static DateTimeField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return DateTimeFieldLenientImpl.PARSER.parse(rawField, null);
    }

    @Test
    public void testDateDST() throws Exception {
        DateTimeField f = parse("Date: Wed, 16 Jul 2008 17:12:33 +0200");
        Assert.assertEquals(new Date(1216221153000L), f.getDate());
    }

    @Test
    public void extraPDTShouldBeTolerated() throws Exception {
        DateTimeField f = parse("Date: Wed, 16 Jul 2008 17:12:33 +0200 (PDT)");
        Assert.assertEquals(new Date(1216221153000L), f.getDate());
    }

    @Test
    public void extraCharsShouldBeTolerated() throws Exception {
        DateTimeField f = parse("Date: Thu, 4 Oct 2001 20:12:26 -0700 (PDT),Thu, 4 Oct 2001 20:12:26 -0700");
        Assert.assertEquals(new Date(1002251546000L), f.getDate());
    }

    @Test
    public void parseShouldSupportPartialYears() throws Exception {
        DateTimeField f = parse("Date: Wed, 16 Jul 08 17:12:33 +0200");
        Assert.assertEquals(new Date(1216221153000L), f.getDate());
    }

    @Test
    public void parseShouldSupportPartialYearsFromLastCentury() throws Exception {
        DateTimeField field1 = parse("Date: 16 Jul 99 17:12:33 +0200");
        DateTimeField field2 = parse("Date: 16 Jul 1999 17:12:33 +0200");
        Assert.assertEquals(field2.getDate(), field1.getDate());
    }

    @Test
    public void testDateDSTNoDayOfWeek() throws Exception {
        DateTimeField f = parse("Date: 16 Jul 2008 17:12:33 +0200");
        Assert.assertEquals(new Date(1216221153000L), f.getDate());
    }

    @Test
    public void testdd() throws Exception {
        DateTimeField f = parse("Date: Thu, 01 Jan 1970 12:00:00 +0000");
        Assert.assertEquals(43200000L, f.getDate().getTime());
    }

    @Test
    public void testMime4j219() throws Exception {
        DateTimeField f = parse("Date: Tue, 17 Jul 2012 22:23:35.882 0000");
        Assert.assertEquals(1342563815882L, f.getDate().getTime());
    }

    @Test
    public void testDateWithExtraLeadingWhiteSpace() throws Exception {
        DateTimeField f = parse("Date:  Wed, 28 Mar 2007 13:32:39 +1000");
        Assert.assertEquals(1175052759000L, f.getDate().getTime());
    }

    @Test
    public void testDateWhenGeneralTimezone() throws Exception {
        DateTimeField f = parse("Date: Fri, 05 Jan 2018 16:18:28 Z");
        Assert.assertEquals(1515169108000L, f.getDate().getTime());
    }

}
