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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class DateTimeTest {

    static Date parse(final String s) throws MimeException {
        ByteArrayInputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII));
        return DateTime.parse(stream).asDate();
    }

    @Test
    public void testDateDST() throws Exception {
        Date f = parse("Wed, 16 Jul 2008 17:12:33 +0200");
        Assert.assertEquals(new Date(1216221153000L), f);
    }

    @Test
    public void extraPDTShouldBeTolerated() throws Exception {
        Date f = parse("Wed, 16 Jul 2008 17:12:33 +0200 (PDT)");
        Assert.assertEquals(new Date(1216221153000L), f);
    }

    @Ignore
    @Test
    public void extraCharsShouldBeTolerated() throws Exception {
        Date f = parse("Thu, 4 Oct 2001 20:12:26 -0700 (PDT),Thu, 4 Oct 2001 20:12:26 -0700");
        Assert.assertEquals(new Date(1002251546000L), f);
    }

    @Test
    public void parseShouldSupportPartialYears() throws Exception {
        Date f = parse("Wed, 16 Jul 08 17:12:33 +0200");
        Assert.assertEquals(new Date(1216221153000L), f);
    }

    @Test
    public void parseShouldSupportPartialYearsFromLastCentury() throws Exception {
        Date field1 = parse("16 Jul 99 17:12:33 +0200");
        Date field2 = parse("16 Jul 1999 17:12:33 +0200");
        Assert.assertEquals(field2.getDate(), field1.getDate());
    }

    @Test
    public void testDateDSTNoDayOfWeek() throws Exception {
        Date f = parse("16 Jul 2008 17:12:33 +0200");
        Assert.assertEquals(new Date(1216221153000L), f);
    }

    @Test
    public void testdd() throws Exception {
        Date f = parse("Thu, 01 Jan 1970 12:00:00 +0000");
        Assert.assertEquals(43200000L, f.getTime());
    }

    @Test
    public void parseShouldAcceptWrongDayOfWeek() throws Exception {
        // Should be Thu
        Date f = parse("Fri, 01 Jan 1970 12:00:00 +0000");
        Assert.assertEquals(43200000L, f.getTime());
    }

    @Ignore
    @Test
    public void testMime4j219() throws Exception {
        Date f = parse("Tue, 17 Jul 2012 22:23:35.882 0000");
        Assert.assertEquals(1342563815882L, f.getTime());
    }

    @Test
    public void testDateWithExtraLeadingWhiteSpace() throws Exception {
        Date f = parse(" Wed, 28 Mar 2007 13:32:39 +1000");
        Assert.assertEquals(1175052759000L, f.getTime());
    }

    @Test
    public void testDateWhenGeneralTimezone() throws Exception {
        Date f = parse("Fri, 05 Jan 2018 16:18:28 Z");
        Assert.assertEquals(1515169108000L, f.getTime());
    }

    @Test
    public void parseShouldSupportUppercaseDayOfWeek() throws Exception {
        Date f = parse("WED, 10 Aug 2022 20:00:00 +0200");
        Assert.assertEquals(1660154400000L, f.getTime());
    }

    @Test
    public void parseShouldSupportLowercaseDayOfWeek() throws Exception {
        Date f = parse("wed, 10 Aug 2022 20:00:00 +0200");
        Assert.assertEquals(1660154400000L, f.getTime());
    }

    @Test
    public void parseShouldSupportUppercaseMonth() throws Exception {
        Date f = parse("Wed, 10 AUG 2022 20:00:00 +0200");
        Assert.assertEquals(1660154400000L, f.getTime());
    }

    @Test
    public void parseShouldSupportLowercaseMonth() throws Exception {
        Date f = parse("Wed, 10 aug 2022 20:00:00 +0200");
        Assert.assertEquals(1660154400000L, f.getTime());
    }

    @Test
    public void parseShouldSupportMixedCaseDate() throws Exception {
        Date f = parse("WeD, 10 aUg 2022 20:00:00 +0200");
        Assert.assertEquals(1660154400000L, f.getTime());
    }

}
