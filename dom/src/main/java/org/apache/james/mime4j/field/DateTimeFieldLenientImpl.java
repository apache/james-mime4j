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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.DateTimeField;

/**
 * Date-time field such as <code>Date</code> or <code>Resent-Date</code>.
 */
public class DateTimeFieldLenientImpl extends AbstractField implements DateTimeField {

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

    private final List<String> datePatterns;

    private boolean parsed = false;
    private Date date;

    private DateTimeFieldLenientImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
        this.datePatterns = Collections.unmodifiableList(Arrays.asList(DEFAULT_DATE_FORMATS));
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
        for (String datePattern : datePatterns) {
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
