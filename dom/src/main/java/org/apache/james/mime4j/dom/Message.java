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

import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;

public interface Message extends Entity, Body {

    /**
     * Returns the value of the <i>Message-ID</i> header field of this message
     * or <code>null</code> if it is not present.
     *
     * @return the identifier of this message.
     */
    String getMessageId();

    /**
     * Creates and sets a new <i>Message-ID</i> header field for this message.
     * A <code>Header</code> is created if this message does not already have
     * one.
     *
     * @param hostname
     *            host name to be included in the identifier or
     *            <code>null</code> if no host name should be included.
     */
    void createMessageId(String hostname);

    /**
     * Returns the (decoded) value of the <i>Subject</i> header field of this
     * message or <code>null</code> if it is not present.
     *
     * @return the subject of this message.
     */
    String getSubject();

    /**
     * Sets the <i>Subject</i> header field for this message. The specified
     * string may contain non-ASCII characters, in which case it gets encoded as
     * an 'encoded-word' automatically. A <code>Header</code> is created if
     * this message does not already have one.
     *
     * @param subject
     *            subject to set or <code>null</code> to remove the subject
     *            header field.
     */
    void setSubject(String subject);

    /**
     * Returns the value of the <i>Date</i> header field of this message as
     * <code>Date</code> object or <code>null</code> if it is not present.
     *
     * @return the date of this message.
     */
    Date getDate();

    /**
     * Sets the <i>Date</i> header field for this message. This method uses the
     * default <code>TimeZone</code> of this host to encode the specified
     * <code>Date</code> object into a string.
     *
     * @param date
     *            date to set or <code>null</code> to remove the date header
     *            field.
     */
    void setDate(Date date);

    /**
     * Sets the <i>Date</i> header field for this message. The specified
     * <code>TimeZone</code> is used to encode the specified <code>Date</code>
     * object into a string.
     *
     * @param date
     *            date to set or <code>null</code> to remove the date header
     *            field.
     * @param zone
     *            a time zone.
     */
    void setDate(Date date, TimeZone zone);

    /**
     * Returns the value of the <i>Sender</i> header field of this message as
     * <code>Mailbox</code> object or <code>null</code> if it is not
     * present.
     *
     * @return the sender of this message.
     */
    Mailbox getSender();

    /**
     * Sets the <i>Sender</i> header field of this message to the specified
     * mailbox address.
     *
     * @param sender
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    void setSender(Mailbox sender);

    /**
     * Returns the value of the <i>From</i> header field of this message as
     * <code>MailboxList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the from field of this message.
     */
    MailboxList getFrom();

    /**
     * Sets the <i>From</i> header field of this message to the specified
     * mailbox address.
     *
     * @param from
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    void setFrom(Mailbox from);

    /**
     * Sets the <i>From</i> header field of this message to the specified
     * mailbox addresses.
     *
     * @param from
     *            addresses to set or <code>null</code> or no arguments to
     *            remove the header field.
     */
    void setFrom(Mailbox... from);

    /**
     * Sets the <i>From</i> header field of this message to the specified
     * mailbox addresses.
     *
     * @param from
     *            addresses to set or <code>null</code> or an empty collection
     *            to remove the header field.
     */
    void setFrom(Collection<Mailbox> from);

    /**
     * Returns the value of the <i>To</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the to field of this message.
     */
    AddressList getTo();

    /**
     * Sets the <i>To</i> header field of this message to the specified
     * address.
     *
     * @param to
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    void setTo(Address to);

    /**
     * Sets the <i>To</i> header field of this message to the specified
     * addresses.
     *
     * @param to
     *            addresses to set or <code>null</code> or no arguments to
     *            remove the header field.
     */
    void setTo(Address... to);

    /**
     * Sets the <i>To</i> header field of this message to the specified
     * addresses.
     *
     * @param to
     *            addresses to set or <code>null</code> or an empty collection
     *            to remove the header field.
     */
    void setTo(Collection<Address> to);

    /**
     * Returns the value of the <i>Cc</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the cc field of this message.
     */
    AddressList getCc();

    /**
     * Sets the <i>Cc</i> header field of this message to the specified
     * address.
     *
     * @param cc
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    void setCc(Address cc);

    /**
     * Sets the <i>Cc</i> header field of this message to the specified
     * addresses.
     *
     * @param cc
     *            addresses to set or <code>null</code> or no arguments to
     *            remove the header field.
     */
    void setCc(Address... cc);

    /**
     * Sets the <i>Cc</i> header field of this message to the specified
     * addresses.
     *
     * @param cc
     *            addresses to set or <code>null</code> or an empty collection
     *            to remove the header field.
     */
    void setCc(Collection<Address> cc);

    /**
     * Returns the value of the <i>Bcc</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the bcc field of this message.
     */
    AddressList getBcc();

    /**
     * Sets the <i>Bcc</i> header field of this message to the specified
     * address.
     *
     * @param bcc
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    void setBcc(Address bcc);

    /**
     * Sets the <i>Bcc</i> header field of this message to the specified
     * addresses.
     *
     * @param bcc
     *            addresses to set or <code>null</code> or no arguments to
     *            remove the header field.
     */
    void setBcc(Address... bcc);

    /**
     * Sets the <i>Bcc</i> header field of this message to the specified
     * addresses.
     *
     * @param bcc
     *            addresses to set or <code>null</code> or an empty collection
     *            to remove the header field.
     */
    void setBcc(Collection<Address> bcc);

    /**
     * Returns the value of the <i>Reply-To</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the reply to field of this message.
     */
    AddressList getReplyTo();

    /**
     * Sets the <i>Reply-To</i> header field of this message to the specified
     * address.
     *
     * @param replyTo
     *            address to set or <code>null</code> to remove the header
     *            field.
     */
    void setReplyTo(Address replyTo);

    /**
     * Sets the <i>Reply-To</i> header field of this message to the specified
     * addresses.
     *
     * @param replyTo
     *            addresses to set or <code>null</code> or no arguments to
     *            remove the header field.
     */
    void setReplyTo(Address... replyTo);

    /**
     * Sets the <i>Reply-To</i> header field of this message to the specified
     * addresses.
     *
     * @param replyTo
     *            addresses to set or <code>null</code> or an empty collection
     *            to remove the header field.
     */
    void setReplyTo(Collection<Address> replyTo);

}
