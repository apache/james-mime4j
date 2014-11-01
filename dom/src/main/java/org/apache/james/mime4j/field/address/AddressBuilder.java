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

import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;

/**
 * Either a lenient {@link LenientAddressBuilder} or strict {@link AddressBuilder} builder for {@link Address} and its subclasses.
 * 
 */
public interface SharedAddressBuilder {

    /**
     * Parses the specified raw string into an address.
     *
     * @param rawAddressString
     *            string to parse.
     * @return an <code>Address</code> object for the specified string.
     * @throws ParseException if the raw string does not represent a single address.
     */
    public abstract Address parseAddress(String rawAddressString) throws ParseException;

    /**
     * Parse the address list string, such as the value of a From, To, Cc, Bcc,
     * Sender, or Reply-To header.
     *
     * The string MUST be unfolded already.
     * @param rawAddressList
     *            string to parse.
     */
    public abstract AddressList parseAddressList(String rawAddressList) throws ParseException;

    /**
     * Parses the specified raw string into a mailbox address.
     *
     * @param rawMailboxString
     *            string to parse.
     * @return a <code>Mailbox</code> object for the specified string.
     * @throws ParseException
     *             if the raw string does not represent a single mailbox
     *             address.
     */
    public abstract Mailbox parseMailbox(String rawMailboxString) throws ParseException;

    /**
     * Parses the specified raw string into a group address.
     *
     * @param rawGroupString
     *            string to parse.
     * @return a <code>Group</code> object for the specified string.
     * @throws ParseException
     *             if the raw string does not represent a single group address.
     */
    public abstract Group parseGroup(String rawGroupString) throws ParseException;

}
