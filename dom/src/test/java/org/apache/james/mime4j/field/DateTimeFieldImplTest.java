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

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DateTimeFieldImplTest {

    private TimeZone timeZone;

    @Before
    public void setup() {
        timeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    @After
    public void tearDown() {
        TimeZone.setDefault(timeZone);
    }
    
    @Test
    public void parseShouldReturnYearWhen4Digits() throws Exception {
        DateTimeField field = parse("Date: Sun, 13 May 1917 14:18:52Z");
        assertEquals("Sun May 13 14:18:52 UTC 1917", field.getDate().toString());
    }

    @Test
    public void parseShouldAddCenturyWhen2Digits() throws Exception {
        DateTimeField field = parse("Date: Sat, 13 May 17 14:18:52Z");
        assertEquals("Sat May 13 14:18:52 UTC 2017", field.getDate().toString());
    }

    @Test
    public void parseShouldAddPreviousCenturyWhen2DigitsAndMoreThan70s() throws Exception {
        DateTimeField field = parse("Date: Wed, 13 May 87 14:18:52Z");
        assertEquals("Wed May 13 14:18:52 UTC 1987", field.getDate().toString());
    }

    @Test
    public void dayIsDependentFromTheDateNotFromTheGivenDay() throws Exception {
        DateTimeField field = parse("Date: Mon, 13 May 17 14:18:52Z");
        assertEquals("Sat May 13 14:18:52 UTC 2017", field.getDate().toString());
    }

    private DateTimeField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return DateTimeFieldImpl.PARSER.parse(rawField, null);
    }
}
