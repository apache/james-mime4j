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

import java.util.Date;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.MailboxField;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.dom.field.UnstructuredField;
import org.apache.james.mime4j.stream.Field;

/**
 * Abstract MIME message.
 */
public abstract class AbstractMessage extends AbstractEntity implements Message {

    /**
     * Returns the value of the <i>Message-ID</i> header field of this message
     * or <code>null</code> if it is not present.
     *
     * @return the identifier of this message.
     */
    public String getMessageId() {
        Field field = obtainField(FieldName.MESSAGE_ID);
        if (field == null)
            return null;

        return field.getBody();
    }

    /**
     * Returns the (decoded) value of the <i>Subject</i> header field of this
     * message or <code>null</code> if it is not present.
     *
     * @return the subject of this message.
     */
    public String getSubject() {
        UnstructuredField field = obtainField(FieldName.SUBJECT);
        if (field == null)
            return null;

        return field.getValue();
    }

    /**
     * Returns the value of the <i>Date</i> header field of this message as
     * <code>Date</code> object or <code>null</code> if it is not present.
     *
     * @return the date of this message.
     */
    public Date getDate() {
        DateTimeField dateField = obtainField(FieldName.DATE);
        if (dateField == null)
            return null;

        return dateField.getDate();
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
     * Returns the value of the <i>Reply-To</i> header field of this message as
     * <code>AddressList</code> object or <code>null</code> if it is not
     * present.
     *
     * @return value of the reply to field of this message.
     */
    public AddressList getReplyTo() {
        return getAddressList(FieldName.REPLY_TO);
    }

    private Mailbox getMailbox(String fieldName) {
        MailboxField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getMailbox();
    }

    private MailboxList getMailboxList(String fieldName) {
        MailboxListField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getMailboxList();
    }

    private AddressList getAddressList(String fieldName) {
        AddressListField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getAddressList();
    }

}
