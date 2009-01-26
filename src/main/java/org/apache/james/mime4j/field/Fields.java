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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.util.MimeUtil;

public class Fields {

    private Fields() {
    }

    public static ContentTypeField contentType(String contentType) {
        int usedCharacters = Field.CONTENT_TYPE.length() + 2;
        String body = MimeUtil.fold(contentType, usedCharacters);

        return parse(ContentTypeField.class, Field.CONTENT_TYPE, body);
    }

    public static ContentTypeField contentType(String mimeType,
            Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return parse(ContentTypeField.class, Field.CONTENT_TYPE, mimeType);
        } else {
            StringBuilder sb = new StringBuilder(mimeType);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append("; ");
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(EncoderUtil.encodeContentTypeParameterValue(entry
                        .getValue()));
            }
            String contentType = sb.toString();
            return contentType(contentType);
        }
    }

    public static ContentTransferEncodingField contentTransferEncoding(
            String contentTransferEncoding) {
        return parse(ContentTransferEncodingField.class,
                Field.CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
    }

    public static DateTimeField date(String fieldValue) {
        return date(Field.DATE, fieldValue);
    }

    public static DateTimeField date(String fieldName, String fieldValue) {
        return parse(DateTimeField.class, fieldName, fieldValue);
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

        return parse(UnstructuredField.class, Field.SUBJECT, rawValue);
    }

    public static MailboxField sender(Mailbox mailbox) {
        return mailbox(Field.SENDER, mailbox);
    }

    public static MailboxListField from(Mailbox mailbox) {
        return mailboxList(Field.FROM, Collections.singleton(mailbox));
    }

    public static MailboxListField from(Mailbox... mailboxes) {
        return mailboxList(Field.FROM, Arrays.asList(mailboxes));
    }

    public static MailboxListField from(Iterable<Mailbox> mailboxes) {
        return mailboxList(Field.FROM, mailboxes);
    }

    public static AddressListField to(Address address) {
        return addressList(Field.TO, Collections.singleton(address));
    }

    public static AddressListField to(Address... addresses) {
        return addressList(Field.TO, Arrays.asList(addresses));
    }

    public static AddressListField to(Iterable<Address> addresses) {
        return addressList(Field.TO, addresses);
    }

    public static AddressListField cc(Address address) {
        return addressList(Field.CC, Collections.singleton(address));
    }

    public static AddressListField cc(Address... addresses) {
        return addressList(Field.CC, Arrays.asList(addresses));
    }

    public static AddressListField cc(Iterable<Address> addresses) {
        return addressList(Field.CC, addresses);
    }

    public static AddressListField bcc(Address address) {
        return addressList(Field.BCC, Collections.singleton(address));
    }

    public static AddressListField bcc(Address... addresses) {
        return addressList(Field.BCC, Arrays.asList(addresses));
    }

    public static AddressListField bcc(Iterable<Address> addresses) {
        return addressList(Field.BCC, addresses);
    }

    public static AddressListField replyTo(Address address) {
        return addressList(Field.REPLY_TO, Collections.singleton(address));
    }

    public static AddressListField replyTo(Address... addresses) {
        return addressList(Field.REPLY_TO, Arrays.asList(addresses));
    }

    public static AddressListField replyTo(Iterable<Address> addresses) {
        return addressList(Field.REPLY_TO, addresses);
    }

    /**
     * Creates a mailbox field from the specified field name and mailbox
     * address. Valid field names are <code>Sender</code> and
     * <code>Resent-Sender</code>.
     * 
     * @param fieldName
     *            the name of the mailbox field (<code>Sender</code> or
     *            <code>Resent-Sender</code>).
     * @param mailbox
     *            mailbox address for the field value.
     * @return the newly created mailbox field.
     */
    public static MailboxField mailbox(String fieldName, Mailbox mailbox) {
        String value = encodeAddresses(Collections.singleton(mailbox));

        String folded = MimeUtil.fold(value, fieldName.length() + 2);
        return parse(MailboxField.class, fieldName, folded);
    }

    /**
     * Creates a mailbox-list field from the specified field name and mailbox
     * addresses. Valid field names are <code>From</code> and
     * <code>Resent-From</code>.
     * 
     * @param fieldName
     *            the name of the mailbox field (<code>From</code> or
     *            <code>Resent-From</code>).
     * @param mailboxes
     *            mailbox addresses for the field value.
     * @return the newly created mailbox-list field.
     */
    public static MailboxListField mailboxList(String fieldName,
            Iterable<Mailbox> mailboxes) {
        String value = encodeAddresses(mailboxes);

        String folded = MimeUtil.fold(value, fieldName.length() + 2);
        return parse(MailboxListField.class, fieldName, folded);
    }

    /**
     * Creates an address-list field from the specified field name and mailbox
     * or group addresses. Valid field names are <code>To</code>,
     * <code>Cc</code>, <code>Bcc</code>, <code>Reply-To</code>,
     * <code>Resent-To</code>, <code>Resent-Cc</code> and
     * <code>Resent-Bcc</code>.
     * 
     * @param fieldName
     *            the name of the mailbox field (<code>To</code>,
     *            <code>Cc</code>, <code>Bcc</code>, <code>Reply-To</code>,
     *            <code>Resent-To</code>, <code>Resent-Cc</code> or
     *            <code>Resent-Bcc</code>).
     * @param addresses
     *            mailbox or group addresses for the field value.
     * @return the newly created address-list field.
     */
    public static AddressListField addressList(String fieldName,
            Iterable<Address> addresses) {
        String value = encodeAddresses(addresses);

        String folded = MimeUtil.fold(value, fieldName.length() + 2);
        return parse(AddressListField.class, fieldName, folded);
    }

    private static <F extends Field> F parse(Class<F> fieldClass,
            String fieldName, String fieldBody) {
        Field field = Field.parse(fieldName, fieldBody);
        if (!fieldClass.isInstance(field)) {
            throw new IllegalArgumentException("Illegal field name: "
                    + fieldName);
        }

        return fieldClass.cast(field);
    }

    private static String encodeAddresses(Iterable<? extends Address> addresses) {
        StringBuilder sb = new StringBuilder();

        for (Address address : addresses) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getEncodedString());
        }

        return sb.toString();
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
