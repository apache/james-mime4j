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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.james.mime4j.decoder.EncoderUtil;
import org.apache.james.mime4j.util.MimeUtil;

public class Fields {
    private Fields() {
    }

    public static ContentTypeField contentType(String contentType) {
        int usedCharacters = Field.CONTENT_TYPE.length() + 2;
        String body = MimeUtil.fold(contentType, usedCharacters);

        return (ContentTypeField) Field.parse(Field.CONTENT_TYPE, body);
    }

    public static ContentTypeField contentType(String mimeType,
            Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return (ContentTypeField) Field.parse(Field.CONTENT_TYPE, mimeType);
        } else {
            StringBuilder sb = new StringBuilder(mimeType);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append("; ");
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(quote(entry.getValue()));
            }
            String contentType = sb.toString();
            return contentType(contentType);
        }
    }

    public static ContentTransferEncodingField contentTransferEncoding(
            String contentTransferEncoding) {
        return (ContentTransferEncodingField) Field.parse(
                Field.CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
    }

    public static DateTimeField date(String fieldValue) {
        return date(Field.DATE, fieldValue);
    }

    public static DateTimeField date(String fieldName, String fieldValue) {
        return (DateTimeField) Field.parse(fieldName, fieldValue);
    }

    public static DateTimeField date(Date date) {
        return date(Field.DATE, date, null);
    }

    public static DateTimeField date(String fieldName, Date date) {
        return date(fieldName, date, null);
    }

    public static DateTimeField date(String fieldName, Date date, TimeZone zone) {
        DateFormat df = RFC822_DATE_FORMAT.get();
        if (zone != null) {
            df.setTimeZone(zone);
        }
        return date(fieldName, df.format(date));
    }

    public static Field messageId(String hostname) {
        return Field.parse(Field.MESSAGE_ID, MimeUtil
                .createUniqueMessageId(hostname));
    }

    public static UnstructuredField subject(String subject) {
        int usedCharacters = Field.SUBJECT.length() + 2;
        String encoded = EncoderUtil.encodeIfNecessary(subject,
                EncoderUtil.Usage.TEXT_TOKEN, usedCharacters);
        String rawValue = MimeUtil.fold(encoded, usedCharacters);

        return (UnstructuredField) Field.parse(Field.SUBJECT, rawValue);
    }

    private static String quote(String value) {
        for (int idx = 0; idx < value.length(); idx++) {
            if (isSpecial(value.charAt(idx))) {
                value = value.replaceAll("[\\\"]", "\\\\$0");
                return "\"" + value + "\"";
            }
        }

        return value;
    }

    private static boolean isSpecial(char ch) {
        final String tspecials = "()<>@,;:\\\"/[]?="; // rfc 2045
        return ch < 32 || ch >= 127 || tspecials.indexOf(ch) != -1;
    }

    private static final ThreadLocal<DateFormat> RFC822_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new Rfc822DateFormat();
        }
    };

    private static final class Rfc822DateFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;

        public Rfc822DateFormat() {
            super("EEE, d MMM yyyy HH:mm:ss ", Locale.US);
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo,
                FieldPosition pos) {
            StringBuffer sb = super.format(date, toAppendTo, pos);

            int zoneMillis = calendar.get(GregorianCalendar.ZONE_OFFSET);
            int dstMillis = calendar.get(GregorianCalendar.DST_OFFSET);
            int minutes = (zoneMillis + dstMillis) / 1000 / 60;

            if (minutes < 0) {
                sb.append('-');
                minutes = -minutes;
            } else {
                sb.append('+');
            }

            sb.append(String.format("%02d%02d", minutes / 60, minutes % 60));

            return sb;
        }
    }

}
