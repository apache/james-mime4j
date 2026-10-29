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

package org.apache.james.mime4j.util;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class MimeUtilTest {

    @Test
    public void testFold() {
        Assert.assertEquals("this\r\n is\r\n a\r\n test", MimeUtil.fold("this is a test", 0, 4));
        Assert.assertEquals("this\r\n is a\r\n test", MimeUtil.fold("this is a test", 0, 5));
        Assert.assertEquals("this\r\n is\r\n a\r\n test", MimeUtil.fold("this is a test", 1, 4));
    }

    @Test
    public void testFoldWithDefaultMaxCharacters() {
        Assert.assertEquals("this is\r\n a test", MimeUtil.fold("this is a test", 68));
        Assert.assertEquals("this is\r\n a test", MimeUtil.fold("this is a test", 69));
        Assert.assertEquals("this\r\n is a test", MimeUtil.fold("this is a test", 70));
        Assert.assertEquals("this  \r\n   is a test", MimeUtil.fold(
            "this     is a test", 70));
    }

    @Test
    public void testFoldOverlyLongNonWhitespace() throws Exception {
        String ninety = "1234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890";
        String input = String.format("testing 1 2 %s testing %s", ninety,
            ninety);

        String expected = String.format(
            "testing 1 2\r\n %s\r\n testing\r\n %s", ninety, ninety);

        Assert.assertEquals(expected, MimeUtil.fold(input, 0));
    }

    @Test
    public void testUnfold() throws Exception {
        Assert.assertEquals("", MimeUtil.unfold(""));
        Assert.assertEquals("x", MimeUtil.unfold("x"));
        Assert.assertEquals(" x ", MimeUtil.unfold(" x "));

        Assert.assertEquals("", MimeUtil.unfold("\r"));
        Assert.assertEquals("", MimeUtil.unfold("\n"));
        Assert.assertEquals("", MimeUtil.unfold("\r\n"));

        Assert.assertEquals(" ", MimeUtil.unfold(" \n"));
        Assert.assertEquals(" ", MimeUtil.unfold("\n "));
        Assert.assertEquals(" ", MimeUtil.unfold(" \r\n"));
        Assert.assertEquals(" ", MimeUtil.unfold("\r\n "));

        Assert.assertEquals("this is a test", MimeUtil.unfold("this is\r\n a test"));
        Assert.assertEquals("this is a test", MimeUtil.unfold("this is\r\n a test"));
        Assert.assertEquals("this is a test", MimeUtil.unfold("this\r\n is a test"));
        Assert.assertEquals("this     is a test", MimeUtil
            .unfold("this  \r\n   is a test"));

        Assert.assertEquals("this is a test", MimeUtil
            .unfold("this\r\n is\r\n a\r\n test"));
    }

    private static String repeat(String s, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(s);
        }
        return result.toString();
    }

    private static void assertNoWhitespaceOnlyLine(String s, int usedCharacters) {
        String folded = MimeUtil.fold(s, usedCharacters);

        Assert.assertFalse(folded, folded.startsWith("\r\n"));
        String[] lines = folded.split("\r\n");
        for (int i = 1; i < lines.length; i++) {
            // a continuation line made only of whitespace
            Assert.assertFalse(folded, lines[i].trim().isEmpty());
        }
        Assert.assertEquals(s, MimeUtil.unfold(folded));
    }

    @Test
    public void foldShouldNotProduceWhitespaceOnlyLines() {
        assertNoWhitespaceOnlyLine(repeat("a", 75) + "\t\t\t\t\t", 1);
        assertNoWhitespaceOnlyLine(repeat("\t", 21), 150);
        assertNoWhitespaceOnlyLine(repeat("a", 75) + "     ", 9);
        assertNoWhitespaceOnlyLine(repeat("a", 70) + repeat(" ", 100) + "b", 9);
        assertNoWhitespaceOnlyLine(repeat(" ", 100) + "b", 9);
    }

    @Test
    public void foldShouldStillFoldTextAfterWhitespaceRuns() {
        String folded = MimeUtil.fold(repeat("word ", 40) + "end", 9);

        Assert.assertTrue(folded, folded.contains("\r\n "));
        Assert.assertEquals(repeat("word ", 40) + "end", MimeUtil.unfold(folded));
    }

    private static void assertFormatDateDenotesTheInstant(String instant, String zone) {
        Date date = Date.from(Instant.parse(instant));
        String formatted = MimeUtil.formatDate(date, TimeZone.getTimeZone(zone));

        Instant back = ZonedDateTime.parse(formatted, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        Assert.assertEquals(formatted, date.toInstant(), back);
    }

    @Test
    public void formatDateShouldDenoteTheSameInstantWhenTheOffsetHasSeconds() {
        assertFormatDateDenotesTheInstant("1900-01-16T00:00:00Z", "Africa/Abidjan");
        assertFormatDateDenotesTheInstant("1900-01-01T00:00:00Z", "Europe/Paris");
        assertFormatDateDenotesTheInstant("2024-06-01T12:34:56Z", "Europe/Paris");
        assertFormatDateDenotesTheInstant("2024-06-01T12:34:56Z", "Asia/Kolkata");
    }

    @Test
    public void foldShouldNotBreakRightAfterAFoldAlreadyInTheValue() {
        String value = "X-Custom: " + repeat("x", 70) + "\r\n " + repeat("y", 10) + " z";
        String folded = MimeUtil.fold(value, 0);

        Assert.assertFalse(folded, folded.contains("\r\n\r\n"));
        Assert.assertEquals(MimeUtil.unfold(value), MimeUtil.unfold(folded));

        String tabs = "V\tV\t:\r\n\t\r\n\t\r\n\t\r\n\t:\r\n\t\r\n\t\r\n\t\t\r\n\t:\r\n\t\r\n\t\r\n\t\t\r\n\t:\r\n\t\r\n\t";
        String foldedTabs = MimeUtil.fold("Content-Type: text/plain; name=\"" + tabs + "\"", 0);
        Assert.assertFalse(foldedTabs, foldedTabs.contains("\r\n\r\n"));
    }

    @Test
    public void foldShouldStillFoldLongLinesOfAPrefoldedValue() {
        String value = repeat("word ", 10) + "\r\n " + repeat("word ", 30) + "end";
        String folded = MimeUtil.fold(value, 9);

        for (String line : folded.split("\r\n")) {
            Assert.assertTrue(folded, line.length() <= 76);
        }
        Assert.assertEquals(MimeUtil.unfold(value), MimeUtil.unfold(folded));
    }

    @Test
    public void formatDateShouldWriteTheYearInFull() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");

        Assert.assertEquals("Thu, 1 Jan 17756 00:00:00 +0000", MimeUtil.formatDate(Date.from(Instant.parse("+17756-01-01T00:00:00Z")), gmt));
        Assert.assertEquals("Fri, 1 Jan 0196 00:00:00 +0000", MimeUtil.formatDate(Date.from(Instant.parse("0196-01-01T00:00:00Z")), gmt));
        assertFormatDateDenotesTheInstant("2024-06-01T12:34:56Z", "GMT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatDateShouldRejectNegativeYears() {
        MimeUtil.formatDate(Date.from(Instant.parse("-4964-01-01T00:00:00Z")), TimeZone.getTimeZone("GMT"));
    }
}
