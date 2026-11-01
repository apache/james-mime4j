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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.stream.Field;

/**
 * Date-time field such as <code>Date</code> or <code>Resent-Date</code>.
 */
public class DateTimeFieldLenientImpl extends AbstractField implements DateTimeField {
    private static final int INITIAL_YEAR = 1970;
    public static final DateTimeFormatter RFC_5322 = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .parseLenient()
        .optionalStart()
            .appendText(DAY_OF_WEEK, dayOfWeek())
            .appendLiteral(", ")
        .optionalEnd()
        .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral(' ')
        .appendText(MONTH_OF_YEAR, monthOfYear())
        .appendLiteral(' ')
        .appendValueReduced(YEAR, 2, 4, INITIAL_YEAR)
        .appendLiteral(' ')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
        .optionalEnd()
        .optionalStart()
            .appendLiteral('.')
            .appendValue(MILLI_OF_SECOND, 3)
        .optionalEnd()
        .optionalStart()
            .appendLiteral(' ')
            .appendOffset("+HHMM", "GMT")
        .optionalEnd()
        .optionalStart()
            .appendLiteral(' ')
            .appendOffsetId()
        .optionalEnd()
        .optionalStart()
            .appendLiteral(' ')
            .appendText(OFFSET_SECONDS, obsoleteZones())
        .optionalEnd()
        .optionalStart()
            .appendLiteral(' ')
            .appendText(OFFSET_SECONDS, obsoleteDaylightZones())
        .optionalEnd()
        .toFormatter()
        .withZone(ZoneId.of("GMT"))
        .withResolverStyle(ResolverStyle.LENIENT)
        .withResolverFields(DAY_OF_MONTH, MONTH_OF_YEAR, YEAR, HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE, MILLI_OF_SECOND, OFFSET_SECONDS)
        .withLocale(Locale.US);

    private static Map<Long, String> monthOfYear() {
        HashMap<Long, String> result = new HashMap<>();
        result.put(1L, "Jan");
        result.put(2L, "Feb");
        result.put(3L, "Mar");
        result.put(4L, "Apr");
        result.put(5L, "May");
        result.put(6L, "Jun");
        result.put(7L, "Jul");
        result.put(8L, "Aug");
        result.put(9L, "Sep");
        result.put(10L, "Oct");
        result.put(11L, "Nov");
        result.put(12L, "Dec");
        return result;
    }

    /**
     * RFC 5322 obs-zone. Split in two maps as several names share an offset.
     * UT, GMT and military zones (taken as -0000) default to GMT.
     */
    private static Map<Long, String> obsoleteZones() {
        HashMap<Long, String> result = new HashMap<>();
        result.put(-4L * 3600, "EDT");
        result.put(-5L * 3600, "EST");
        result.put(-6L * 3600, "CST");
        result.put(-7L * 3600, "MST");
        result.put(-8L * 3600, "PST");
        return result;
    }

    private static Map<Long, String> obsoleteDaylightZones() {
        HashMap<Long, String> result = new HashMap<>();
        result.put(-5L * 3600, "CDT");
        result.put(-6L * 3600, "MDT");
        result.put(-7L * 3600, "PDT");
        return result;
    }

    private static Map<Long, String> dayOfWeek() {
        HashMap<Long, String> result = new HashMap<>();
        result.put(1L, "Mon");
        result.put(2L, "Tue");
        result.put(3L, "Wed");
        result.put(4L, "Thu");
        result.put(5L, "Fri");
        result.put(6L, "Sat");
        result.put(7L, "Sun");
        return result;
    }

    private boolean parsed = false;
    private Date date;

    private DateTimeFieldLenientImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    public Date getDate() {
        if (!parsed) {
            parse();
        }
        return date;
    }

    private void parse() {
        parsed = true;
        date = null;
        String body = getBody();
        if (body == null) {
            return;
        }
        String text = body.trim();
        date = parseCommonForm(text);
        if (date == null) {
            date = parseWithFormatter(text);
        }
    }

    static Date parseWithFormatter(final String text) {
        if (!mayBeRfc5322(text)) {
            return null;
        }
        try {
            return Date.from(Instant.from(RFC_5322.parse(normalizeShortYear(text), new ParsePosition(0))));
        } catch (Exception e) {
            // Ignore: invalid date
            return null;
        }
    }

    /**
     * The form nearly every mail carries, <code>[Ddd, ]d Mon yyyy HH:MM:SS +HHMM</code>, without the
     * optional sections and the lenient resolution of {@link #RFC_5322}: an out of range value
     * fails here and goes to {@link #RFC_5322}.
     */
    private static final DateTimeFormatter SIMPLE_FORM = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .optionalStart()
            .appendText(DAY_OF_WEEK, dayOfWeek())
            .appendLiteral(", ")
        .optionalEnd()
        .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral(' ')
        .appendText(MONTH_OF_YEAR, monthOfYear())
        .appendLiteral(' ')
        .appendValue(YEAR, 4)
        .appendLiteral(' ')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .appendLiteral(' ')
        .appendOffset("+HHMM", "+0000")
        .toFormatter()
        .withResolverStyle(ResolverStyle.STRICT)
        // As RFC_5322: the day of the week is not checked against the date
        .withResolverFields(DAY_OF_MONTH, MONTH_OF_YEAR, YEAR, HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE, OFFSET_SECONDS)
        .withLocale(Locale.US);

    /**
     * Parses the value with {@link #SIMPLE_FORM} when it has that form, optionally followed by a
     * comment such as <code> (UTC)</code> that {@link #RFC_5322} does not read either.
     *
     * @return the date, or <code>null</code> when the value is not of that form: {@link #RFC_5322}
     * then decides.
     */
    static Date parseCommonForm(final String text) {
        if (!hasSimpleForm(text)) {
            return null;
        }
        try {
            return Date.from(Instant.from(SIMPLE_FORM.parse(text, new ParsePosition(0))));
        } catch (DateTimeException e) {
            // e.g. an out of range value, which RFC_5322 resolves leniently
            return null;
        }
    }

    /**
     * Cheap check of the shape of {@link #SIMPLE_FORM}, so that other values go to
     * {@link #RFC_5322} without paying for a parse exception. Only the kind of each character is
     * checked (letter, digit or separator), not the values: {@link #SIMPLE_FORM} checks them.
     * <pre>
     * Tue, 26 Apr 2022 02:27:54 +0000 (UTC)
     * |    |  |   |    |  |  |  |     |
     * |    |  |   |    |  |  |  |     optional trailing comment, ignored
     * |    |  |   |    |  |  |  zone offset: sign and 4 digits
     * |    |  |   |    |  |  seconds: 2 digits
     * |    |  |   |    |  minutes: 2 digits
     * |    |  |   |    hours: 2 digits
     * |    |  |   year: 4 digits
     * |    |  month: 3 letters
     * |    day of month: 1 or 2 digits
     * optional day of week: 3 letters, a comma and a space
     * </pre>
     */
    private static boolean hasSimpleForm(final String s) {
        // p is the position of the next expected token
        int p = 0;

        // Optional day of week "Tue, "
        if (s.length() > 4 && s.charAt(3) == ',') {
            if (!isLetter(s, 0) || !isLetter(s, 1) || !isLetter(s, 2) || !isChar(s, 4, ' ')) {
                return false;
            }
            p = 5;
        }

        // Day of month "26" or "6"
        if (!isDigit(s, p)) {
            return false;
        }
        p += isDigit(s, p + 1) ? 2 : 1;

        // Month " Apr "
        if (!isChar(s, p, ' ') || !isLetter(s, p + 1) || !isLetter(s, p + 2) || !isLetter(s, p + 3)
            || !isChar(s, p + 4, ' ')) {
            return false;
        }
        p += 5;

        // Year "2022 "
        if (!isDigits(s, p, 4) || !isChar(s, p + 4, ' ')) {
            return false;
        }
        p += 5;

        // Time "02:27:54 "
        if (!isDigits(s, p, 2) || !isChar(s, p + 2, ':')
            || !isDigits(s, p + 3, 2) || !isChar(s, p + 5, ':')
            || !isDigits(s, p + 6, 2) || !isChar(s, p + 8, ' ')) {
            return false;
        }
        p += 9;

        // Zone offset "+0000" or "-0700"
        if (!(isChar(s, p, '+') || isChar(s, p, '-')) || !isDigits(s, p + 1, 4)) {
            return false;
        }
        p += 5;

        // Then the end of the value, or a comment " (UTC)" that RFC_5322 does not read either
        return p == s.length() || (isChar(s, p, ' ') && isChar(s, p + 1, '('));
    }

    private static boolean isChar(final String s, final int pos, final char c) {
        return pos < s.length() && s.charAt(pos) == c;
    }

    private static boolean isDigit(final String s, final int pos) {
        return pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9';
    }

    private static boolean isDigits(final String s, final int pos, final int count) {
        for (int i = 0; i < count; i++) {
            if (!isDigit(s, pos + i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLetter(final String s, final int pos) {
        if (pos >= s.length()) {
            return false;
        }
        char c = s.charAt(pos);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * The lenient formatter reads a 1 or 3 digit year literally: map it as RFC 5322 section 4.3
     * and the strict parser do (1 digit: 2000 + year, 3 digits: 1900 + year).
     */
    private static String normalizeShortYear(final String text) {
        int pos = 0;
        if (text.length() > 4 && text.charAt(3) == ',') {
            // day-of-week ", "
            pos = 5;
        }
        int dayStart = pos;
        while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
            pos++;
        }
        // " Mon "
        if (pos == dayStart || pos + 5 >= text.length() || text.charAt(pos) != ' ' || text.charAt(pos + 4) != ' ') {
            return text;
        }
        int yearStart = pos + 5;
        int yearEnd = yearStart;
        while (yearEnd < text.length() && Character.isDigit(text.charAt(yearEnd))) {
            yearEnd++;
        }
        int digits = yearEnd - yearStart;
        if (digits != 1 && digits != 3) {
            return text;
        }
        int year = Integer.parseInt(text.substring(yearStart, yearEnd));
        year += digits == 1 ? 2000 : 1900;
        return text.substring(0, yearStart) + year + text.substring(yearEnd);
    }

    /**
     * Cheap pre-check that rejects obviously invalid values such as <code>unknown</code> or an
     * empty string without paying for a parse exception. A RFC 5322 date always carries a
     * <code>HH:MM</code> time, hence a colon.
     */
    private static boolean mayBeRfc5322(final String text) {
        return text.indexOf(':') > 0;
    }

    public static final FieldParser<DateTimeField> PARSER = new FieldParser<DateTimeField>() {

        public DateTimeField parse(final Field rawField, final DecodeMonitor monitor) {
            return new DateTimeFieldLenientImpl(rawField, monitor);
        }

    };

}
