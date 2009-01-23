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

package org.apache.james.mime4j.field.address;

import java.util.List;

import org.apache.james.mime4j.codec.EncoderUtil;

/**
 * A named group of zero or more mailboxes.
 */
public class Group extends Address {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final MailboxList mailboxList;

    /**
     * @param name
     *            The group name.
     * @param mailboxes
     *            The mailboxes in this group.
     */
    public Group(String name, MailboxList mailboxes) {
        if (name == null)
            throw new IllegalArgumentException();
        if (mailboxes == null)
            throw new IllegalArgumentException();

        this.name = name;
        this.mailboxList = mailboxes;
    }

    /**
     * Returns the group name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the mailboxes in this group.
     */
    public MailboxList getMailboxes() {
        return mailboxList;
    }

    @Override
    public String getDisplayString(boolean includeRoute) {
        StringBuilder sb = new StringBuilder();

        for (Mailbox mailbox : mailboxList) {
            if (sb.length() == 0) {
                sb.append(name);
                sb.append(':');
            } else {
                sb.append(',');
            }

            sb.append(mailbox.getDisplayString(includeRoute));
        }

        sb.append(";");

        return sb.toString();
    }

    @Override
    public String getEncodedString() {
        StringBuilder buf = new StringBuilder();

        for (Mailbox mailbox : mailboxList) {
            if (buf.length() == 0) {
                buf.append(EncoderUtil.encodeAddressDisplayName(name));
                buf.append(':');
            } else {
                buf.append(',');
            }
            buf.append(mailbox.getEncodedString());
        }

        buf.append(';');

        return buf.toString();
    }

    @Override
    protected void doAddMailboxesTo(List<Mailbox> results) {
        for (Mailbox mailbox : mailboxList) {
            results.add(mailbox);
        }
    }

}
