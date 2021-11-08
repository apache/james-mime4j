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

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.stream.Field;

/**
 * Date-time field such as <code>Date</code> or <code>Resent-Date</code>.
 */
public class DateTimeFieldLenientImpl extends AbstractField implements DateTimeField {
    private static final boolean JAVA_8_COMPATIBILITY_MODE = System.getProperty("java.version").startsWith("1.8");

    private static final String[] DEFAULT_DATE_FORMATS = {
        "EEE, dd MMM yy HH:mm:ss ZZZZ",
        "dd MMM yy HH:mm:ss ZZZZ",
        "EEE, dd MMM yy HH:mm:ss.SSS 0000",
        "EEE, dd MMM yy HH:mm:ss 0000",
        "EEE, dd MMM yyyy HH:mm:ss ZZZZ",
        "dd MMM yyyy HH:mm:ss ZZZZ",
        "EEE, dd MMM yyyy HH:mm:ss.SSS 0000",
        "EEE, dd MMM yyyy HH:mm:ss 0000",
        "EEE, dd MMM yy HH:mm:ss X",
        "dd MMM yy HH:mm:ss X",
        "EEE, dd MMM yy HH:mm:ss.SSS X",
        "EEE, dd MMM yy HH:mm:ss X",
        "EEE, dd MMM yyyy HH:mm:ss X",
        "dd MMM yyyy HH:mm:ss X",
        "EEE, dd MMM yyyy HH:mm:ss.SSS X",
        "EEE, dd MMM yyyy HH:mm:ss X",
    };
    public static final List<String> DATE_PATTERNS_FOR_JAVA_8 = Collections.unmodifiableList(Arrays.asList(DEFAULT_DATE_FORMATS));

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
        if (body != null) {
            body = body.trim();
        }
        try {
            if (JAVA_8_COMPATIBILITY_MODE) {
                compatibilityWithJava8(body);
            } else {
                date = Date.from(Instant.from(RFC_5322.parse(body, new ParsePosition(0))));
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void compatibilityWithJava8(String body) {
        for (String datePattern : DATE_PATTERNS_FOR_JAVA_8) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(datePattern, Locale.US);
                parser.setTimeZone(TimeZone.getTimeZone("GMT"));
                parser.setLenient(true);
                date = parser.parse(body);
                break;
            } catch (ParseException ignore) {
            }
        }
    }

    public static final FieldParser<DateTimeField> PARSER = new FieldParser<DateTimeField>() {

        public DateTimeField parse(final Field rawField, final DecodeMonitor monitor) {
            return new DateTimeFieldLenientImpl(rawField, monitor);
        }

    };

}
