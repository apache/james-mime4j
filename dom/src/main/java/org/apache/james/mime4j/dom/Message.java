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

package org.apache.james.mime4j.dom;

import java.util.Date;

import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.MailboxField;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.dom.field.ParseException;
import org.apache.james.mime4j.dom.field.UnstructuredField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.LenientFieldParser;
import org.apache.james.mime4j.field.address.DefaultAddressParser;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.internal.AbstractEntityBuilder;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MultipartBuilder;
import org.apache.james.mime4j.internal.ParserStreamContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.NameValuePair;

/**
 * An MIME message (as defined in RFC 2045).
 */
public interface Message extends Entity, Body {

    /**
     * Returns the value of the <i>Message-ID</i> header field of this message
     * or <code>null</code> if it is not present.
     *
     * @return the identifier of this message.
     */
    String getMessageId();

    /**
     * Returns the (decoded) value of the <i>Subject</i> header field of this
     * message or <code>null</code> if it is not present.
     *
     * @return the subject of this message.
     */
    String getSubject();

    /**
     * Returns the value of the <i>Date</i> header field of this message as
     * <code>Date</code> object or <code>null</code> if it is not present.
     *
     * @return the date of this message.
     */
    Date getDate();

    /**
     * Returns the value of the <i>Sender</i> header field of this message as
     * <code>Mailbox</code> object or <code>null</code> if it is not
     * present.
     *
     * @return the sender of this message.
     */
    Mailbox getSender();

    /**
     * Returns the value of the <i>From</i> header field of this message as
     * <code>MailboxList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the from field of this message.
     */
    MailboxList getFrom();

    /**
     * Returns the value of the <i>To</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the to field of this message.
     */
    AddressList getTo();

    /**
     * Returns the value of the <i>Cc</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the cc field of this message.
     */
    AddressList getCc();

    /**
     * Returns the value of the <i>Bcc</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the bcc field of this message.
     */
    AddressList getBcc();

    /**
     * Returns the value of the <i>Reply-To</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the reply to field of this message.
     */
    AddressList getReplyTo();

    class Builder extends AbstractEntityBuilder {
        private MimeConfig config;
        private DecodeMonitor monitor;
        private BodyDescriptorBuilder bodyDescBuilder;
        private FieldParser<?> fieldParser;
        private BodyFactory bodyFactory;
        private boolean flatMode;
        private boolean rawContent;

        private Builder() {
            super();
        }

        public static Builder of() {
            return new Builder();
        }

        public static Builder of(Message other) {
            return new Builder().copy(other);
        }

        public static Builder of(final InputStream is) throws IOException {
            return new Builder().parse(is);
        }

        /**
         * Sets MIME configuration.
         *
         * @param config the configuration.
         */
        public Builder use(MimeConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Sets {@link org.apache.james.mime4j.codec.DecodeMonitor} that will be
         * used to handle malformed data when executing {@link #parse(java.io.InputStream)}.
         *
         * @param monitor the decoder monitor.
         */
        public Builder use(DecodeMonitor monitor) {
            this.monitor = monitor;
            return this;
        }

        /**
         * Sets {@link org.apache.james.mime4j.stream.BodyDescriptorBuilder} that will be
         * used to generate body descriptors when executing {@link #parse(java.io.InputStream)}.
         *
         * @param bodyDescBuilder the body descriptor builder.
         */
        public Builder use(BodyDescriptorBuilder bodyDescBuilder) {
            this.bodyDescBuilder = bodyDescBuilder;
            return this;
        }

        /**
         * Sets {@link org.apache.james.mime4j.dom.FieldParser} that will be
         * used to generate parse message fields when executing {@link #parse(java.io.InputStream)}.
         *
         * @param fieldParser the field parser.
         */
        public Builder use(FieldParser<?> fieldParser) {
            this.fieldParser = fieldParser;
            return this;
        }

        /**
         * Sets {@link org.apache.james.mime4j.message.BodyFactory} that will be
         * used to generate message body.
         *
         * @param bodyFactory the body factory.
         */
        public Builder use(BodyFactory bodyFactory) {
            this.bodyFactory = bodyFactory;
            return this;
        }

        /**
         * Enables flat parsing mode for {@link #parse(java.io.InputStream)} operation.
         */
        public Builder enableFlatMode() {
            this.flatMode = true;
            return this;
        }

        /**
         * Disables flat parsing mode for {@link #parse(java.io.InputStream)} operation.
         */
        public Builder disableFlatMode() {
            this.flatMode = false;
            return this;
        }

        /**
         * Enables automatic content decoding for {@link #parse(java.io.InputStream)} operation.
         */
        public Builder enableContentDecoding() {
            this.rawContent = false;
            return this;
        }

        /**
         * Enables disable content decoding for {@link #parse(java.io.InputStream)} operation.
         */
        public Builder disableContentDecoding() {
            this.rawContent = true;
            return this;
        }

        public Builder copy(Message other) {
            if (other == null) {
                return this;
            }
            clearFields();
            final Header otherHeader = other.getHeader();
            if (otherHeader != null) {
                final List<Field> otherFields = otherHeader.getFields();
                for (Field field: otherFields) {
                    addField(field);
                }
            }
            Body body = null;
            Body otherBody = other.getBody();
            if (otherBody instanceof Message) {
                body = Builder.of((Message) otherBody).build();
            } else if (otherBody instanceof Multipart) {
                body = MultipartBuilder.createCopy((Multipart) otherBody).build();
            } else if (otherBody instanceof SingleBody) {
                body = ((SingleBody) otherBody).copy();
            }
            setBody(body);
            return this;
        }

        @Override
        public Builder setField(Field field) {
            super.setField(field);
            return this;
        }

        @Override
        public Builder addField(Field field) {
            super.addField(field);
            return this;
        }

        @Override
        public Builder removeFields(String name) {
            super.removeFields(name);
            return this;
        }

        @Override
        public Builder clearFields() {
            super.clearFields();
            return this;
        }

        @Override
        public Builder setContentTransferEncoding(String contentTransferEncoding) {
            super.setContentTransferEncoding(contentTransferEncoding);
            return this;
        }

        @Override
        public Builder setContentType(String mimeType, NameValuePair... parameters) {
            super.setContentType(mimeType, parameters);
            return this;
        }

        @Override
        public Builder setContentDisposition(String dispositionType) {
            super.setContentDisposition(dispositionType);
            return this;
        }

        @Override
        public Builder setContentDisposition(String dispositionType, String filename) {
            super.setContentDisposition(dispositionType, filename);
            return this;
        }

        @Override
        public Builder setContentDisposition(String dispositionType, String filename, long size) {
            super.setContentDisposition(dispositionType, filename, size);
            return this;
        }

        @Override
        public Builder setContentDisposition(String dispositionType, String filename, long size,
                                                    Date creationDate, Date modificationDate, Date readDate) {
            super.setContentDisposition(dispositionType, filename, size, creationDate, modificationDate, readDate);
            return this;
        }

        @Override
        public Builder setBody(Body body) {
            super.setBody(body);
            return this;
        }

        @Override
        public Builder setBody(TextBody textBody) {
            super.setBody(textBody);
            return this;
        }

        @Override
        public Builder setBody(BinaryBody binaryBody) {
            super.setBody(binaryBody);
            return this;
        }

        @Override
        public Builder setBody(Multipart multipart) {
            super.setBody(multipart);
            return this;
        }

        @Override
        public Builder setBody(Message message) {
            super.setBody(message);
            return this;
        }

        /**
         * Sets text of this message with the charset.
         *
         * @param text
         *            the text.
         * @param charset
         *            the charset of the text.
         */
        public Builder setBody(String text, Charset charset) throws IOException {
            return setBody(text, null, charset);
        }

        /**
         * Sets text of this message with the given MIME subtype and charset.
         *
         * @param text
         *            the text.
         * @param charset
         *            the charset of the text.
         * @param subtype
         *            the text subtype (e.g. &quot;plain&quot;, &quot;html&quot; or
         *            &quot;xml&quot;).
         */
        public Builder setBody(String text, String subtype, Charset charset) throws IOException {
            String mimeType = "text/" + (subtype != null ? subtype : "plain");
            if (charset != null) {
                setField(Fields.contentType(mimeType, new NameValuePair("charset", charset.name())));
            } else {
                setField(Fields.contentType(mimeType));
            }
            Body textBody;
            if (bodyFactory != null) {
                textBody = bodyFactory.textBody(
                    InputStreams.create(text, charset),
                    charset != null ? charset.name() : null);
            } else {
                textBody = BasicBodyFactory.INSTANCE.textBody(text, charset);
            }
            return setBody(textBody);
        }

        /**
         * Sets binary content of this message with the given MIME type.
         *
         * @param bin
         *            the body.
         * @param mimeType
         *            the MIME media type of the specified body
         *            (&quot;type/subtype&quot;).
         */
        public Builder setBody(byte[] bin, String mimeType) throws IOException {
            setField(Fields.contentType(mimeType != null ? mimeType : "application/octet-stream"));
            Body binBody;
            if (bodyFactory != null) {
                binBody = bodyFactory.binaryBody(InputStreams.create(bin));
            } else {
                binBody = BasicBodyFactory.INSTANCE.binaryBody(bin);
            }
            return setBody(binBody);
        }

        /**
         * Returns the value of the <i>Message-ID</i> header field of this message
         * or <code>null</code> if it is not present.
         *
         * @return the identifier of this message.
         */
        public String getMessageId() {
            Field field = obtainField(FieldName.MESSAGE_ID_LOWERCASE);
            return field != null ? field.getBody() : null;
        }

        /**
         * Generates and sets message ID for this message.
         *
         * @param hostname
         *            host name to be included in the identifier or
         *            <code>null</code> if no host name should be included.
         */
        public Builder generateMessageId(String hostname) {
            if (hostname == null) {
                removeFields(FieldName.MESSAGE_ID);
            } else {
                setField(Fields.generateMessageId(hostname));
            }
            return this;
        }

        /**
         * Sets message ID for this message.
         *
         * @param messageId
         *            the message ID.
         */
        public Builder setMessageId(String messageId) {
            if (messageId == null) {
                removeFields(FieldName.MESSAGE_ID);
            } else {
                setField(Fields.messageId(messageId));
            }
            return this;
        }

        /**
         * Returns the (decoded) value of the <i>Subject</i> header field of this
         * message or <code>null</code> if it is not present.
         *
         * @return the subject of this message.
         */
        public String getSubject() {
            UnstructuredField field = obtainField(FieldName.SUBJECT_LOWERCASE);
            return field != null ? field.getValue() : null;
        }

        /**
         * Sets <i>Subject</i> header field for this message. The specified
         * string may contain non-ASCII characters, in which case it gets encoded as
         * an 'encoded-word' automatically.
         *
         * @param subject
         *            subject to set or <code>null</code> to remove the subject
         *            header field.
         */
        public Builder setSubject(String subject) {
            if (subject == null) {
                removeFields(FieldName.SUBJECT);
            } else {
                setField(Fields.subject(subject));
            }
            return this;
        }

        /**
         * Returns the value of the <i>Date</i> header field of this message as
         * <code>Date</code> object or <code>null</code> if it is not present.
         *
         * @return the date of this message.
         */
        public Date getDate() {
            DateTimeField field = obtainField(FieldName.DATE_LOWERCASE);
            return field != null ? field.getDate() : null;
        }

        /**
         * Sets <i>Date</i> header field for this message. This method uses the
         * default <code>TimeZone</code> of this host to encode the specified
         * <code>Date</code> object into a string.
         *
         * @param date
         *            date to set or <code>null</code> to remove the date header
         *            field.
         */
        public Builder setDate(Date date) {
            return setDate(date, null);
        }

        /**
         * Sets <i>Date</i> header field for this message. The specified
         * <code>TimeZone</code> is used to encode the specified <code>Date</code>
         * object into a string.
         *
         * @param date
         *            date to set or <code>null</code> to remove the date header
         *            field.
         * @param zone
         *            a time zone.
         */
        public Builder setDate(Date date, TimeZone zone) {
            if (date == null) {
                removeFields(FieldName.DATE);
            } else {
                setField(Fields.date(FieldName.DATE, date, zone));
            }
            return this;
        }

        /**
         * Returns the value of the <i>Sender</i> header field of this message as
         * <code>Mailbox</code> object or <code>null</code> if it is not
         * present.
         *
         * @return the sender of this message.
         */
        public Mailbox getSender() {
            return getMailbox(FieldName.SENDER_LOWERCASE);
        }

        /**
         * Sets <i>Sender</i> header field of this message to the specified
         * mailbox address.
         *
         * @param sender
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setSender(Mailbox sender) {
            return setMailbox(FieldName.SENDER, sender);
        }

        /**
         * Sets <i>Sender</i> header field of this message to the specified
         * mailbox address.
         *
         * @param sender
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setSender(String sender) throws ParseException {
            return setMailbox(FieldName.SENDER, sender);
        }

        /**
         * Returns the value of the <i>From</i> header field of this message as
         * <code>MailboxList</code> object or <code>null</code> if it is not
         * present.
         *
         * @return value of the from field of this message.
         */
        public MailboxList getFrom() {
            return getMailboxList(FieldName.FROM_LOWERCASE);
        }

        /**
         * Sets <i>From</i> header field of this message to the specified
         * mailbox address.
         *
         * @param from
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setFrom(Mailbox from) {
            return setMailboxList(FieldName.FROM, from);
        }

        /**
         * Sets <i>From</i> header field of this message to the specified
         * mailbox address.
         *
         * @param from
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setFrom(String from) throws ParseException {
            return setMailboxList(FieldName.FROM, from);
        }

        /**
         * Sets <i>From</i> header field of this message to the specified
         * mailbox addresses.
         *
         * @param from
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setFrom(Mailbox... from) {
            return setMailboxList(FieldName.FROM, from);
        }

        /**
         * Sets <i>From</i> header field of this message to the specified
         * mailbox addresses.
         *
         * @param from
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setFrom(String... from) throws ParseException {
            return setMailboxList(FieldName.FROM, from);
        }

        /**
         * Sets <i>From</i> header field of this message to the specified
         * mailbox addresses.
         *
         * @param from
         *            addresses to set or <code>null</code> or an empty collection
         *            to remove the header field.
         */
        public Builder setFrom(Collection<Mailbox> from) {
            return setMailboxList(FieldName.FROM, from);
        }

        /**
         * Returns the value of the <i>To</i> header field of this message as
         * <code>AddressList</code> object or <code>null</code> if it is not
         * present.
         *
         * @return value of the to field of this message.
         */
        public AddressList getTo() {
            return getAddressList(FieldName.TO_LOWERCASE);
        }

        /**
         * Sets <i>To</i> header field of this message to the specified
         * address.
         *
         * @param to
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setTo(Address to) {
            return setAddressList(FieldName.TO, to);
        }

        /**
         * Sets <i>To</i> header field of this message to the specified
         * address.
         *
         * @param to
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setTo(String to) throws ParseException {
            return setAddressList(FieldName.TO, to);
        }

        /**
         * Sets <i>To</i> header field of this message to the specified
         * addresses.
         *
         * @param to
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setTo(Address... to) {
            return setAddressList(FieldName.TO, to);
        }

        /**
         * Sets <i>To</i> header field of this message to the specified
         * addresses.
         *
         * @param to
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setTo(String... to) throws ParseException {
            return setAddressList(FieldName.TO, to);
        }

        /**
         * Sets <i>To</i> header field of this message to the specified
         * addresses.
         *
         * @param to
         *            addresses to set or <code>null</code> or an empty collection
         *            to remove the header field.
         */
        public Builder setTo(Collection<? extends Address> to) {
            return setAddressList(FieldName.TO, to);
        }

        /**
         * Returns the value of the <i>Cc</i> header field of this message as
         * <code>AddressList</code> object or <code>null</code> if it is not
         * present.
         *
         * @return value of the cc field of this message.
         */
        public AddressList getCc() {
            return getAddressList(FieldName.CC_LOWERCASE);
        }

        /**
         * Sets <i>Cc</i> header field of this message to the specified
         * address.
         *
         * @param cc
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setCc(Address cc) {
            return setAddressList(FieldName.CC, cc);
        }

        /**
         * Sets <i>Cc</i> header field of this message to the specified
         * addresses.
         *
         * @param cc
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setCc(Address... cc) {
            return setAddressList(FieldName.CC, cc);
        }

        /**
         * Sets <i>Cc</i> header field of this message to the specified
         * addresses.
         *
         * @param cc
         *            addresses to set or <code>null</code> or an empty collection
         *            to remove the header field.
         */
        public Builder setCc(Collection<? extends Address> cc) {
            return setAddressList(FieldName.CC, cc);
        }

        /**
         * Returns the value of the <i>Bcc</i> header field of this message as
         * <code>AddressList</code> object or <code>null</code> if it is not
         * present.
         *
         * @return value of the bcc field of this message.
         */
        public AddressList getBcc() {
            return getAddressList(FieldName.BCC_LOWERCASE);
        }

        /**
         * Sets <i>Bcc</i> header field of this message to the specified
         * address.
         *
         * @param bcc
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setBcc(Address bcc) {
            return setAddressList(FieldName.BCC, bcc);
        }

        /**
         * Sets <i>Bcc</i> header field of this message to the specified
         * addresses.
         *
         * @param bcc
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setBcc(Address... bcc) {
            return setAddressList(FieldName.BCC, bcc);
        }

        /**
         * Sets <i>Bcc</i> header field of this message to the specified
         * addresses.
         *
         * @param bcc
         *            addresses to set or <code>null</code> or an empty collection
         *            to remove the header field.
         */
        public Builder setBcc(Collection<? extends Address> bcc) {
            return setAddressList(FieldName.BCC, bcc);
        }

        /**
         * Returns the value of the <i>Reply-To</i> header field of this message as
         * <code>AddressList</code> object or <code>null</code> if it is not
         * present.
         *
         * @return value of the reply to field of this message.
         */
        public AddressList getReplyTo() {
            return getAddressList(FieldName.REPLY_TO_LOWERCASE);
        }

        /**
         * Sets <i>Reply-To</i> header field of this message to the specified
         * address.
         *
         * @param replyTo
         *            address to set or <code>null</code> to remove the header
         *            field.
         */
        public Builder setReplyTo(Address replyTo) {
            return setAddressList(FieldName.REPLY_TO, replyTo);
        }

        /**
         * Sets <i>Reply-To</i> header field of this message to the specified
         * addresses.
         *
         * @param replyTo
         *            addresses to set or <code>null</code> or no arguments to
         *            remove the header field.
         */
        public Builder setReplyTo(Address... replyTo) {
            return setAddressList(FieldName.REPLY_TO, replyTo);
        }

        /**
         * Sets <i>Reply-To</i> header field of this message to the specified
         * addresses.
         *
         * @param replyTo
         *            addresses to set or <code>null</code> or an empty collection
         *            to remove the header field.
         */
        public Builder setReplyTo(Collection<? extends Address> replyTo) {
            return setAddressList(FieldName.REPLY_TO, replyTo);
        }

        public Builder parse(final InputStream is) throws IOException {
            MimeConfig currentConfig = config != null ? config : MimeConfig.DEFAULT;
            boolean strict = currentConfig.isStrictParsing();
            DecodeMonitor currentMonitor = monitor != null ? monitor : strict ? DecodeMonitor.STRICT : DecodeMonitor.SILENT;
            BodyDescriptorBuilder currentBodyDescBuilder = bodyDescBuilder != null ? bodyDescBuilder :
                new DefaultBodyDescriptorBuilder(null, fieldParser != null ? fieldParser :
                    strict ? DefaultFieldParser.getParser() : LenientFieldParser.getParser(), currentMonitor);
            BodyFactory currentBodyFactory = bodyFactory != null ? bodyFactory : new BasicBodyFactory(!strict);
            MimeStreamParser parser = new MimeStreamParser(currentConfig, currentMonitor, currentBodyDescBuilder);

            Message message = new MessageImpl();
            parser.setContentHandler(new ParserStreamContentHandler(message, currentBodyFactory));
            parser.setContentDecoding(!rawContent);
            if (flatMode) {
                parser.setFlat();
            }
            try {
                parser.parse(is);
            } catch (MimeException e) {
                throw new MimeIOException(e);
            }
            clearFields();
            final List<Field> fields = message.getHeader().getFields();
            for (Field field: fields) {
                addField(field);
            }
            setBody(message.getBody());
            return this;
        }

        public Message build() {
            MessageImpl message = new MessageImpl();
            HeaderImpl header = new HeaderImpl();
            message.setHeader(header);
            if (!containsField(FieldName.MIME_VERSION_LOWERCASE)) {
                header.setField(Fields.version("1.0"));
            }
            for (Field field : getFields()) {
                header.addField(field);
            }

            message.setBody(getBody());

            return message;
        }

        private Mailbox getMailbox(String fieldName) {
            MailboxField field = obtainField(fieldName);
            return field != null ? field.getMailbox() : null;
        }

        private Builder setMailbox(String fieldName, Mailbox mailbox) {
            if (mailbox == null) {
                removeFields(fieldName);
            } else {
                setField(Fields.mailbox(fieldName, mailbox));
            }
            return this;
        }

        private Builder setMailbox(String fieldName, String mailbox) throws ParseException {
            if (mailbox == null) {
                removeFields(fieldName);
            } else {
                setField(Fields.mailbox(fieldName, DefaultAddressParser.DEFAULT.parseMailbox(mailbox)));
            }
            return this;
        }

        private MailboxList getMailboxList(String fieldName) {
            MailboxListField field = obtainField(fieldName);
            return field != null ? field.getMailboxList() : null;
        }

        private Builder setMailboxList(String fieldName, Mailbox mailbox) {
            return setMailboxList(fieldName, mailbox == null ? null : Collections.singleton(mailbox));
        }

        private Builder setMailboxList(String fieldName, String mailbox) throws ParseException {
            return setMailboxList(fieldName, mailbox == null ? null : DefaultAddressParser.DEFAULT.parseMailbox(mailbox));
        }

        private Builder setMailboxList(String fieldName, Mailbox... mailboxes) {
            return setMailboxList(fieldName, mailboxes == null ? null : Arrays.asList(mailboxes));
        }

        private List<Mailbox> parseMailboxes(String... mailboxes) throws ParseException {
            if (mailboxes == null || mailboxes.length == 0) {
                return null;
            } else {
                List<Mailbox> list = new ArrayList<Mailbox>();
                for (String mailbox: mailboxes) {
                    list.add(DefaultAddressParser.DEFAULT.parseMailbox(mailbox));
                }
                return list;
            }
        }

        private Builder setMailboxList(String fieldName, String... mailboxes) throws ParseException {
            return setMailboxList(fieldName, parseMailboxes(mailboxes));
        }

        private Builder setMailboxList(String fieldName, Collection<Mailbox> mailboxes) {
            if (mailboxes == null || mailboxes.isEmpty()) {
                removeFields(fieldName);
            } else {
                setField(Fields.mailboxList(fieldName, mailboxes));
            }
            return this;
        }

        private AddressList getAddressList(String fieldName) {
            AddressListField field = obtainField(fieldName);
            return field != null? field.getAddressList() : null;
        }

        private Builder setAddressList(String fieldName, Address address) {
            return setAddressList(fieldName, address == null ? null : Collections.singleton(address));
        }

        private Builder setAddressList(String fieldName, String address) throws ParseException {
            return setAddressList(fieldName, address == null ? null : DefaultAddressParser.DEFAULT.parseMailbox(address));
        }

        private Builder setAddressList(String fieldName, Address... addresses) {
            return setAddressList(fieldName, addresses == null ? null : Arrays.asList(addresses));
        }

        private List<Address> parseAddresses(String... addresses) throws ParseException {
            if (addresses == null || addresses.length == 0) {
                return null;
            } else {
                List<Address> list = new ArrayList<Address>();
                for (String address: addresses) {
                    list.add(DefaultAddressParser.DEFAULT.parseAddress(address));
                }
                return list;
            }
        }

        private Builder setAddressList(String fieldName, String... addresses) throws ParseException {
            return setAddressList(fieldName, parseAddresses(addresses));
        }

        private Builder setAddressList(String fieldName, Collection<? extends Address> addresses) {
            if (addresses == null || addresses.isEmpty()) {
                removeFields(fieldName);
            } else {
                setField(Fields.addressList(fieldName, addresses));
            }
            return this;
        }
    }
}
