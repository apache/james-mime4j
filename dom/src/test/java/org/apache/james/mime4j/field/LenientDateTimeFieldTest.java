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

import java.time.Instant;
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
    public void parseShouldAcceptWrongDayOfWeek() throws Exception {
        // Should be Thu
        DateTimeField f = parse("Date: Fri, 01 Jan 1970 12:00:00 +0000");
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

    @Test
    public void obsoleteZonesShouldBeHonoured() throws Exception {
        Assert.assertEquals(Instant.parse("1997-11-21T14:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 EST").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T13:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 EDT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T15:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 CST").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T14:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 CDT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T16:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 MST").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T15:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 MDT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1901-01-02T17:22:32Z"), parse("Date: 2 Jan 1901 09:22:32 PST").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T16:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 PDT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T09:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 UT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T09:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 GMT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T09:55:06Z"), parse("Date: Fri, 21 Nov 1997 09:55:06 Z").getDate().toInstant());
    }

    @Test
    public void numericOffsetShouldWinOverAFollowingZoneName() throws Exception {
        Assert.assertEquals(Instant.parse("2008-07-16T15:12:33Z"), parse("Date: Wed, 16 Jul 2008 17:12:33 +0200 PDT").getDate().toInstant());
    }

    @Test
    public void shortYearsShouldBeMappedAsTheStrictParserDoes() throws Exception {
        Assert.assertEquals(Instant.parse("2000-10-04T10:00:00Z"), parse("Date: 4 Oct 0 10:00:00 +0000").getDate().toInstant());
        Assert.assertEquals(Instant.parse("2004-10-04T10:00:00Z"), parse("Date: Mon, 4 Oct 4 10:00:00 +0000").getDate().toInstant());
        Assert.assertEquals(Instant.parse("2069-10-04T10:00:00Z"), parse("Date: 4 Oct 69 10:00:00 +0000").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1970-10-04T10:00:00Z"), parse("Date: 4 Oct 70 10:00:00 +0000").getDate().toInstant());
        Assert.assertEquals(Instant.parse("2000-10-04T10:00:00Z"), parse("Date: 4 Oct 100 10:00:00 +0000").getDate().toInstant());
        Assert.assertEquals(Instant.parse("2899-10-04T10:00:00Z"), parse("Date: 4 Oct 999 10:00:00 +0000").getDate().toInstant());
        Assert.assertEquals(Instant.parse("2001-10-04T10:00:00Z"), parse("Date: 4 Oct 2001 10:00:00 +0000").getDate().toInstant());
    }

    @Test
    public void zoneAfterFoldingWhiteSpaceShouldBeHonoured() throws Exception {
        Assert.assertEquals(Instant.parse("1997-11-21T15:55:06Z"),
            parse("Date: Fri, 21 Nov 1997 09:55:06  -0600").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T14:55:06Z"),
            parse("Date: Fri, 21 Nov 1997 09:55:06  EST").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T15:55:06Z"),
            parse("Date: Fri, 21 Nov 1997 09:55:06\t-0600").getDate().toInstant());
    }

    @Test
    public void obsoleteZoneWithoutSpaceShouldBeHonoured() throws Exception {
        // RFC 5322 4.3: obs-zone has no leading FWS, obs-second is [CFWS] 2DIGIT [CFWS]
        Assert.assertEquals(Instant.parse("1997-11-21T13:55:06Z"),
            parse("Date: Fri, 21 Nov 1997 09:55:06EDT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("2000-10-04T05:02:00Z"),
            parse("Date: 4 Oct 0 1:2EDT").getDate().toInstant());
    }

    @Test
    public void partlyReadOffsetShouldNotGiveADate() throws Exception {
        Assert.assertNull(parse("Date: 32 Feb 3416118 48:84:32 +1178").getDate());
        Assert.assertNull(parse("Date: 21 Nov 1997 09:55:06 +11789").getDate());
    }

    @Test
    public void trailingTextAfterTheZoneShouldStillBeTolerated() throws Exception {
        Assert.assertEquals(Instant.parse("1997-11-21T15:55:06Z"),
            parse("Date: Fri, 21 Nov 1997 09:55:06 -0600 (CST)").getDate().toInstant());
    }

    @Test
    public void outOfRangeNumericOffsetShouldNotBeReadAsSeconds() throws Exception {
        Date date = parse("Date: 21 Nov 1997 09:55:06 +9900").getDate();
        // 9900 seconds = +02:45
        Assert.assertNotEquals(Instant.parse("1997-11-21T07:10:06Z"), date == null ? null : date.toInstant());
        Assert.assertNull(date);
        Assert.assertNull(parse("Date: 1 Jan 2020 25:61:61 +9999").getDate());
        Assert.assertNull(parse("Date: 32 Jan 0 00:54 +6357").getDate());
    }

    @Test
    public void obsoleteZonesShouldStillBeHonoured() throws Exception {
        Assert.assertEquals(Instant.parse("1997-11-21T16:55:06Z"),
            parse("Date: 21 Nov 1997 09:55:06 PDT").getDate().toInstant());
        Assert.assertEquals(Instant.parse("1997-11-21T14:55:06Z"),
            parse("Date: 21 Nov 1997 09:55:06 est").getDate().toInstant());
    }
}
