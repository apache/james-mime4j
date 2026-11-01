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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Oracle for {@link DateTimeFieldLenientImpl#parseCommonForm(String)}: whenever the simple form
 * answers, the {@link DateTimeFieldLenientImpl#RFC_5322} formatter must give the same instant.
 */
public class DateTimeFieldLenientFastPathTest {

    private static final String[] DAY_NAMES = {"", "Mon, ", "Tue, ", "sun, ", "FRI, ", "Xyz, ", "Mon,", "Mon , "};
    private static final String[] DAYS = {"0", "1", "01", "9", "28", "29", "30", "31", "32", "99", "123", "-1"};
    private static final String[] MONTHS = {"Jan", "feb", "FEB", "Mar", "Apr", "Jun", "Sep", "Nov", "Dec", "Foo", "Ja"};
    private static final String[] YEARS = {"0000", "0001", "1582", "1900", "1969", "1970", "2000", "2023", "2024", "2100",
        "9999", "99", "00", "5", "123", "12345"};
    private static final String[] TIMES = {"00:00", "23:59", "24:00", "12:60", "1:00", "00:00:00", "23:59:59", "23:59:60",
        "12:34:5", "12:34:56.789", "99:99:99"};
    private static final String[] ZONES = {"", " +0000", " -0000", " +0100", " -1230", " +1759", " +1800", " -1800",
        " +2359", " +0060", " +000", " GMT", " UT", " EST", " PDT", " Z", " +01:00", "  +0000"};
    private static final String[] TRAILERS = {"", " (UTC)", " GMT", " x"};

    private static void assertAgrees(String text, List<String> fastPathTaken) {
        Date fast = DateTimeFieldLenientImpl.parseCommonForm(text);
        if (fast != null) {
            fastPathTaken.add(text);
            Assert.assertEquals(text, DateTimeFieldLenientImpl.parseWithFormatter(text), fast);
        }
    }

    @Test
    public void fastPathShouldAgreeWithTheFormatterOnEveryCombination() {
        List<String> fastPathTaken = new ArrayList<>();
        int count = 0;
        for (String dayName : DAY_NAMES) {
            for (String day : DAYS) {
                for (String month : MONTHS) {
                    for (String year : YEARS) {
                        for (String time : TIMES) {
                            for (String zone : ZONES) {
                                for (String trailer : TRAILERS) {
                                    assertAgrees(dayName + day + " " + month + " " + year + " " + time + zone + trailer, fastPathTaken);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Most combinations are deliberately not of the simple form, but some are
        Assert.assertTrue(fastPathTaken.size() + " of " + count, fastPathTaken.size() > count / 1000);
    }

    @Test
    public void fastPathShouldAgreeWithTheFormatterOnMutatedDates() {
        Random random = new Random(42);
        String alphabet = "0123456789 :+-,()JanFebMarAprDecMonTueGMTUTESTPDTZ.\t";
        String[] seeds = {"Tue, 26 Apr 2022 02:27:54 +0000", "4 Oct 2001 20:12 -0700", "29 Feb 2024 23:59:59",
            "Sun, 31 Dec 9999 23:59:59 -1759 (comment)"};
        List<String> fastPathTaken = new ArrayList<>();
        for (int i = 0; i < 500_000; i++) {
            StringBuilder s = new StringBuilder(seeds[random.nextInt(seeds.length)]);
            int mutations = 1 + random.nextInt(3);
            for (int m = 0; m < mutations; m++) {
                int pos = random.nextInt(s.length() + 1);
                char c = alphabet.charAt(random.nextInt(alphabet.length()));
                switch (random.nextInt(3)) {
                    case 0:
                        s.insert(pos, c);
                        break;
                    case 1:
                        if (pos < s.length()) {
                            s.deleteCharAt(pos);
                        }
                        break;
                    default:
                        if (pos < s.length()) {
                            s.setCharAt(pos, c);
                        }
                }
            }
            assertAgrees(s.toString(), fastPathTaken);
        }
        Assert.assertFalse(fastPathTaken.isEmpty());
    }

    @Test
    public void fastPathShouldServeTheUsualForms() {
        String[] usual = {"Tue, 26 Apr 2022 02:27:54 +0000", "26 Apr 2022 02:27:54 +0000", "tue, 26 APR 2022 02:27:54 -0000",
            "Wed, 16 Jul 2008 17:12:33 +0200 (PDT)", "Mon, 1 Jan 2024 00:00:00 -0500 (EST)", "Thu, 29 Feb 2024 23:59:59 +1759"};
        for (String text : usual) {
            Date fast = DateTimeFieldLenientImpl.parseCommonForm(text);
            Assert.assertNotNull(text, fast);
            Assert.assertEquals(text, DateTimeFieldLenientImpl.parseWithFormatter(text), fast);
        }
        Assert.assertEquals(new Date(1216221153000L), DateTimeFieldLenientImpl.parseCommonForm("Wed, 16 Jul 2008 17:12:33 +0200"));
    }
}
