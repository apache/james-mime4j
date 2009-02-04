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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.AddressListField;
import org.apache.james.mime4j.field.DateTimeField;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.MailboxField;
import org.apache.james.mime4j.field.MailboxListField;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.StorageProvider;

/**
 * Represents a MIME message. The following code parses a stream into a
 * <code>Message</code> object.
 * 
 * <pre>
 * Message msg = new Message(new FileInputStream(&quot;mime.msg&quot;));
 * </pre>
 */
public class Message extends Entity implements Body {

    /**
     * Creates a new empty <code>Message</code>.
     */
    public Message() {
    }

    /**
     * Creates a new <code>Message</code> from the specified
     * <code>Message</code>. The <code>Message</code> instance is
     * initialized with copies of header and body of the specified
     * <code>Message</code>. The parent entity of the new message is
     * <code>null</code>.
     * 
     * @param other
     *            message to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link Message}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public Message(Message other) {
        super(other);
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance.
     * 
     * @param is
     *            the stream to parse.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message(InputStream is) throws IOException, MimeIOException {
        this(is, null, DefaultStorageProvider.getInstance());
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig}.
     * 
     * @param is
     *            the stream to parse.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message(InputStream is, MimeEntityConfig config) throws IOException,
            MimeIOException {
        this(is, config, DefaultStorageProvider.getInstance());
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig} and {@link StorageProvider}.
     * 
     * @param is
     *            the stream to parse.
     * @param config
     *            {@link MimeEntityConfig} to use.
     * @param storageProvider
     *            {@link StorageProvider} to use for storing text and binary
     *            message bodies.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public Message(InputStream is, MimeEntityConfig config,
            StorageProvider storageProvider) throws IOException,
            MimeIOException {
        try {
            MimeStreamParser parser = new MimeStreamParser(config);
            parser.setContentHandler(new MessageBuilder(this, storageProvider));
            parser.parse(is);
        } catch (MimeException e) {
            throw new MimeIOException(e);
        }
    }

    /**
     * Write the content to the given output stream using the
     * {@link MessageWriter#STRICT_ERROR STRICT_ERROR} message writer.
     * 
     * @param out
     *            the output stream to write to.
     * @throws IOException
     *             in case of an I/O error
     * @throws MimeIOException
     *             in case of a MIME protocol violation
     * @see MessageWriter
     */
    public void writeTo(OutputStream out) throws IOException, MimeIOException {
        MessageWriter.STRICT_ERROR.writeEntity(this, out);
    }

    /**
     * Returns the value of the <i>Message-ID</i> header field of this message
     * or <code>null</code> if it is not present.
     * 
     * @return the identifier of this message.
     */
    public String getMessageId() {
        Field field = obtainField(Field.MESSAGE_ID);
        if (field == null)
            return null;

        return field.getBody();
    }

    /**
     * Creates and sets a new <i>Message-ID</i> header field for this message.
     * A <code>Header</code> is created if this message does not already have
     * one.
     * 
     * @param hostname
     *            host name to be included in the identifier or
     *            <code>null</code> if no host name should be included.
     */
    public void createMessageId(String hostname) {
        Header header = obtainHeader();

        header.setField(Fields.messageId(hostname));
    }

    /**
     * Returns the (decoded) value of the <i>Subject</i> header field of this
     * message or <code>null</code> if it is not present.
     * 
     * @return the subject of this message.
     */
    public String getSubject() {
        UnstructuredField field = obtainField(Field.SUBJECT);
        if (field == null)
            return null;

        return field.getValue();
    }

    /**
     * Sets the <i>Subject</i> header field for this message. The specified
     * string may contain non-ASCII characters, in which case it gets encoded as
     * an 'encoded-word' automatically. A <code>Header</code> is created if
     * this message does not already have one.
     * 
     * @param subject
     *            subject to set.
     */
    public void setSubject(String subject) {
        Header header = obtainHeader();

        if (subject == null) {
            header.removeFields(Field.SUBJECT);
        } else {
            header.setField(Fields.subject(subject));
        }
    }

    /**
     * Returns the value of the <i>Date</i> header field of this message as
     * <code>Date</code> object or <code>null</code> if it is not present.
     * 
     * @return the date of this message.
     */
    public Date getDate() {
        DateTimeField dateField = obtainField(Field.DATE);
        if (dateField == null)
            return null;

        return dateField.getDate();
    }

    /**
     * Sets the <i>Date</i> header field for this message. This method uses the
     * default <code>TimeZone</code> of this host to encode the specified
     * <code>Date</code> object into a string.
     * 
     * @param date
     *            date to set.
     */
    public void setDate(Date date) {
        setDate(date, null);
    }

    /**
     * Sets the <i>Date</i> header field for this message. The specified
     * <code>TimeZone</code> is used to encode the specified <code>Date</code>
     * object into a string.
     * 
     * @param date
     *            date to set.
     * @param zone
     *            a time zone.
     */
    public void setDate(Date date, TimeZone zone) {
        Header header = obtainHeader();

        if (date == null) {
            header.removeFields(Field.DATE);
        } else {
            header.setField(Fields.date(Field.DATE, date, zone));
        }
    }

    public Mailbox getSender() {
        return getMailbox(Field.SENDER);
    }

    public void setSender(Mailbox sender) {
        setMailbox(Field.SENDER, sender);
    }

    public MailboxList getFrom() {
        return getMailboxList(Field.FROM);
    }

    public void setFrom(Mailbox from) {
        setMailboxList(Field.FROM, from);
    }

    public void setFrom(Mailbox... from) {
        setMailboxList(Field.FROM, from);
    }

    public void setFrom(Collection<Mailbox> from) {
        setMailboxList(Field.FROM, from);
    }

    public AddressList getTo() {
        return getAddressList(Field.TO);
    }

    public void setTo(Address to) {
        setAddressList(Field.TO, to);
    }

    public void setTo(Address... to) {
        setAddressList(Field.TO, to);
    }

    public void setTo(Collection<Address> to) {
        setAddressList(Field.TO, to);
    }

    public AddressList getCc() {
        return getAddressList(Field.CC);
    }

    public void setCc(Address cc) {
        setAddressList(Field.CC, cc);
    }

    public void setCc(Address... cc) {
        setAddressList(Field.CC, cc);
    }

    public void setCc(Collection<Address> cc) {
        setAddressList(Field.CC, cc);
    }

    public AddressList getBcc() {
        return getAddressList(Field.BCC);
    }

    public void setBcc(Address bcc) {
        setAddressList(Field.BCC, bcc);
    }

    public void setBcc(Address... bcc) {
        setAddressList(Field.BCC, bcc);
    }

    public void setBcc(Collection<Address> bcc) {
        setAddressList(Field.BCC, bcc);
    }

    public AddressList getReplyTo() {
        return getAddressList(Field.REPLY_TO);
    }

    public void setReplyTo(Address replyTo) {
        setAddressList(Field.REPLY_TO, replyTo);
    }

    public void setReplyTo(Address... replyTo) {
        setAddressList(Field.REPLY_TO, replyTo);
    }

    public void setReplyTo(Collection<Address> replyTo) {
        setAddressList(Field.REPLY_TO, replyTo);
    }

    private Mailbox getMailbox(String fieldName) {
        MailboxField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getMailbox();
    }

    private void setMailbox(String fieldName, Mailbox mailbox) {
        Header header = obtainHeader();

        if (mailbox == null) {
            header.removeFields(fieldName);
        } else {
            header.setField(Fields.mailbox(fieldName, mailbox));
        }
    }

    private MailboxList getMailboxList(String fieldName) {
        MailboxListField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getMailboxList();
    }

    private void setMailboxList(String fieldName, Mailbox mailbox) {
        setMailboxList(fieldName, mailbox == null ? null : Collections
                .singleton(mailbox));
    }

    private void setMailboxList(String fieldName, Mailbox... mailboxes) {
        setMailboxList(fieldName, mailboxes == null ? null : Arrays
                .asList(mailboxes));
    }

    private void setMailboxList(String fieldName, Collection<Mailbox> mailboxes) {
        Header header = obtainHeader();

        if (mailboxes == null || mailboxes.isEmpty()) {
            header.removeFields(fieldName);
        } else {
            header.setField(Fields.mailboxList(fieldName, mailboxes));
        }
    }

    private AddressList getAddressList(String fieldName) {
        AddressListField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getAddressList();
    }

    private void setAddressList(String fieldName, Address address) {
        setAddressList(fieldName, address == null ? null : Collections
                .singleton(address));
    }

    private void setAddressList(String fieldName, Address... addresses) {
        setAddressList(fieldName, addresses == null ? null : Arrays
                .asList(addresses));
    }

    private void setAddressList(String fieldName, Collection<Address> addresses) {
        Header header = obtainHeader();

        if (addresses == null || addresses.isEmpty()) {
            header.removeFields(fieldName);
        } else {
            header.setField(Fields.addressList(fieldName, addresses));
        }
    }

}
