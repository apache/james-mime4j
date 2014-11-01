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

package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
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
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.NameValuePair;

/**
 * {@link org.apache.james.mime4j.dom.Message} builder.
 */
public class MessageBuilder extends AbstractEntityBuilder {

    private MimeConfig config;
    private DecodeMonitor monitor;
    private BodyDescriptorBuilder bodyDescBuilder;
    private FieldParser<?> fieldParser;
    private BodyFactory bodyFactory;
    private boolean flatMode;
    private boolean rawContent;

    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    public static MessageBuilder createCopy(Message other) {
        return new MessageBuilder().copy(other);
    }

    public static MessageBuilder read(final InputStream is) throws IOException {
        return new MessageBuilder().parse(is);
    }

    /**
     * Sets MIME configuration.
     *
     * @param config the configuration.
     */
    public MessageBuilder use(MimeConfig config) {
        this.config = config;
        return this;
    }

    /**
     * Sets {@link org.apache.james.mime4j.codec.DecodeMonitor} that will be
     * used to handle malformed data when executing {@link #parse(java.io.InputStream)}.
     *
     * @param monitor the decoder monitor.
     */
    public MessageBuilder use(DecodeMonitor monitor) {
        this.monitor = monitor;
        return this;
    }

    /**
     * Sets {@link org.apache.james.mime4j.stream.BodyDescriptorBuilder} that will be
     * used to generate body descriptors when executing {@link #parse(java.io.InputStream)}.
     *
     * @param bodyDescBuilder the body descriptor builder.
     */
    public MessageBuilder use(BodyDescriptorBuilder bodyDescBuilder) {
        this.bodyDescBuilder = bodyDescBuilder;
        return this;
    }

    /**
     * Sets {@link org.apache.james.mime4j.dom.FieldParser} that will be
     * used to generate parse message fields when executing {@link #parse(java.io.InputStream)}.
     *
     * @param fieldParser the field parser.
     */
    public MessageBuilder use(FieldParser<?> fieldParser) {
        this.fieldParser = fieldParser;
        return this;
    }

    /**
     * Sets {@link org.apache.james.mime4j.message.BodyFactory} that will be
     * used to generate message body.
     *
     * @param bodyFactory the body factory.
     */
    public MessageBuilder use(BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
        return this;
    }

    /**
     * Enables flat parsing mode for {@link #parse(java.io.InputStream)} operation.
     */
    public MessageBuilder enableFlatMode() {
        this.flatMode = true;
        return this;
    }

    /**
     * Disables flat parsing mode for {@link #parse(java.io.InputStream)} operation.
     */
    public MessageBuilder disableFlatMode() {
        this.flatMode = false;
        return this;
    }

    /**
     * Enables automatic content decoding for {@link #parse(java.io.InputStream)} operation.
     */
    public MessageBuilder enableContentDecoding() {
        this.rawContent = false;
        return this;
    }

    /**
     * Enables disable content decoding for {@link #parse(java.io.InputStream)} operation.
     */
    public MessageBuilder disableContentDecoding() {
        this.rawContent = true;
        return this;
    }

    @Override
    public MessageBuilder setField(Field field) {
        super.setField(field);
        return this;
    }

    @Override
    public MessageBuilder addField(Field field) {
        super.addField(field);
        return this;
    }

    @Override
    public MessageBuilder removeFields(String name) {
        super.removeFields(name);
        return this;
    }

    @Override
    public MessageBuilder clearFields() {
        super.clearFields();
        return this;
    }

    @Override
    public MessageBuilder setContentTransferEncoding(String contentTransferEncoding) {
        super.setContentTransferEncoding(contentTransferEncoding);
        return this;
    }

    @Override
    public MessageBuilder setContentType(String mimeType, NameValuePair... parameters) {
        super.setContentType(mimeType, parameters);
        return this;
    }

    @Override
    public MessageBuilder setContentDisposition(String dispositionType) {
        super.setContentDisposition(dispositionType);
        return this;
    }

    @Override
    public MessageBuilder setContentDisposition(String dispositionType, String filename) {
        super.setContentDisposition(dispositionType, filename);
        return this;
    }

    @Override
    public MessageBuilder setContentDisposition(String dispositionType, String filename, long size) {
        super.setContentDisposition(dispositionType, filename, size);
        return this;
    }

    @Override
    public MessageBuilder setContentDisposition(String dispositionType, String filename, long size,
                                                Date creationDate, Date modificationDate, Date readDate) {
        super.setContentDisposition(dispositionType, filename, size, creationDate, modificationDate, readDate);
        return this;
    }

    @Override
    public MessageBuilder setBody(Body body) {
        super.setBody(body);
        return this;
    }

    @Override
    public MessageBuilder setBody(TextBody textBody) {
        super.setBody(textBody);
        return this;
    }

    @Override
    public MessageBuilder setBody(BinaryBody binaryBody) {
        super.setBody(binaryBody);
        return this;
    }

    @Override
    public MessageBuilder setBody(Multipart multipart) {
        super.setBody(multipart);
        return this;
    }

    @Override
    public MessageBuilder setBody(Message message) {
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
    public MessageBuilder setBody(String text, Charset charset) throws IOException {
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
    public MessageBuilder setBody(String text, String subtype, Charset charset) throws IOException {
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
    public MessageBuilder setBody(byte[] bin, String mimeType) throws IOException {
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
        Field field = obtainField(FieldName.MESSAGE_ID);
        return field != null ? field.getBody() : null;
    }

    /**
     * Generates and sets message ID for this message.
     *
     * @param hostname
     *            host name to be included in the identifier or
     *            <code>null</code> if no host name should be included.
     */
    public MessageBuilder generateMessageId(String hostname) {
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
    public MessageBuilder setMessageId(String messageId) {
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
        UnstructuredField field = obtainField(FieldName.SUBJECT);
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
    public MessageBuilder setSubject(String subject) {
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
        DateTimeField field = obtainField(FieldName.DATE);
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
    public MessageBuilder setDate(Date date) {
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
    public MessageBuilder setDate(Date date, TimeZone zone) {
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
        return getMailbox(FieldName.SENDER);
    }

    /**
     * Sets <i>Sender</i> header field of this message to the specified
     * mailbox address.
     *
     * @param sender
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    public MessageBuilder setSender(Mailbox sender) {
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
    public MessageBuilder setSender(String sender) throws ParseException {
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
        return getMailboxList(FieldName.FROM);
    }

    /**
     * Sets <i>From</i> header field of this message to the specified
     * mailbox address.
     *
     * @param from
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    public MessageBuilder setFrom(Mailbox from) {
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
    public MessageBuilder setFrom(String from) throws ParseException {
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
    public MessageBuilder setFrom(Mailbox... from) {
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
    public MessageBuilder setFrom(String... from) throws ParseException {
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
    public MessageBuilder setFrom(Collection<Mailbox> from) {
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
        return getAddressList(FieldName.TO);
    }

    /**
     * Sets <i>To</i> header field of this message to the specified
     * address.
     *
     * @param to
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    public MessageBuilder setTo(Address to) {
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
    public MessageBuilder setTo(String to) throws ParseException {
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
    public MessageBuilder setTo(Address... to) {
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
    public MessageBuilder setTo(String... to) throws ParseException {
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
    public MessageBuilder setTo(Collection<? extends Address> to) {
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
        return getAddressList(FieldName.CC);
    }

    /**
     * Sets <i>Cc</i> header field of this message to the specified
     * address.
     *
     * @param cc
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    public MessageBuilder setCc(Address cc) {
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
    public MessageBuilder setCc(Address... cc) {
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
    public MessageBuilder setCc(Collection<? extends Address> cc) {
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
        return getAddressList(FieldName.BCC);
    }

    /**
     * Sets <i>Bcc</i> header field of this message to the specified
     * address.
     *
     * @param bcc
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    public MessageBuilder setBcc(Address bcc) {
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
    public MessageBuilder setBcc(Address... bcc) {
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
    public MessageBuilder setBcc(Collection<? extends Address> bcc) {
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
        return getAddressList(FieldName.REPLY_TO);
    }

    /**
     * Sets <i>Reply-To</i> header field of this message to the specified
     * address.
     *
     * @param replyTo
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    public MessageBuilder setReplyTo(Address replyTo) {
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
    public MessageBuilder setReplyTo(Address... replyTo) {
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
    public MessageBuilder setReplyTo(Collection<? extends Address> replyTo) {
        return setAddressList(FieldName.REPLY_TO, replyTo);
    }

    private Mailbox getMailbox(String fieldName) {
        MailboxField field = obtainField(fieldName);
        return field != null ? field.getMailbox() : null;
    }

    private MessageBuilder setMailbox(String fieldName, Mailbox mailbox) {
        if (mailbox == null) {
            removeFields(fieldName);
        } else {
            setField(Fields.mailbox(fieldName, mailbox));
        }
        return this;
    }

    private MessageBuilder setMailbox(String fieldName, String mailbox) throws ParseException {
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

    private MessageBuilder setMailboxList(String fieldName, Mailbox mailbox) {
        return setMailboxList(fieldName, mailbox == null ? null : Collections.singleton(mailbox));
    }

    private MessageBuilder setMailboxList(String fieldName, String mailbox) throws ParseException {
        return setMailboxList(fieldName, mailbox == null ? null : DefaultAddressParser.DEFAULT.parseMailbox(mailbox));
    }

    private MessageBuilder setMailboxList(String fieldName, Mailbox... mailboxes) {
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

    private MessageBuilder setMailboxList(String fieldName, String... mailboxes) throws ParseException {
        return setMailboxList(fieldName, parseMailboxes(mailboxes));
    }

    private MessageBuilder setMailboxList(String fieldName, Collection<Mailbox> mailboxes) {
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

    private MessageBuilder setAddressList(String fieldName, Address address) {
        return setAddressList(fieldName, address == null ? null : Collections.singleton(address));
    }

    private MessageBuilder setAddressList(String fieldName, String address) throws ParseException {
        return setAddressList(fieldName, address == null ? null : DefaultAddressParser.DEFAULT.parseMailbox(address));
    }

    private MessageBuilder setAddressList(String fieldName, Address... addresses) {
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

    private MessageBuilder setAddressList(String fieldName, String... addresses) throws ParseException {
        return setAddressList(fieldName, parseAddresses(addresses));
    }

    private MessageBuilder setAddressList(String fieldName, Collection<? extends Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            removeFields(fieldName);
        } else {
            setField(Fields.addressList(fieldName, addresses));
        }
        return this;
    }

    public MessageBuilder copy(Message other) {
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
            body = MessageBuilder.createCopy((Message) otherBody).build();
        } else if (otherBody instanceof Multipart) {
            body = MultipartBuilder.createCopy((Multipart) otherBody).build();
        } else if (otherBody instanceof SingleBody) {
            body = ((SingleBody) otherBody).copy();
        }
        setBody(body);
        return this;
    }

    public MessageBuilder parse(final InputStream is) throws IOException {
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
        if (!containsField(FieldName.MIME_VERSION)) {
            header.setField(Fields.version("1.0"));
        }
        for (Field field : getFields()) {
            header.addField(field);
        }
        if (!containsField(FieldName.DATE)) {
            header.setField(Fields.date(new Date()));
        }

        message.setBody(getBody());

        return message;
    }

}
