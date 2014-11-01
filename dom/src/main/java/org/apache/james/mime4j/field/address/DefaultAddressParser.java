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

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.io.InputStreams;

/**
 * Default (strict) builder for {@link Address} and its subclasses.
 */
public class DefaultAddressParser implements AddressParser {

    public static final DefaultAddressParser DEFAULT = new DefaultAddressParser();

    protected DefaultAddressParser() {
        super();
    }

    /**
     * Parses the specified raw string into an address.
     *
     * @param text
     *            string to parse.
     * @param monitor the DecodeMonitor to be used while parsing/decoding
     * @return an <code>Address</code> object for the specified string.
     * @throws ParseException if the raw string does not represent a single address.
     */
    public Address parseAddress(CharSequence text, DecodeMonitor monitor) throws ParseException {
        AddressListParser parser = new AddressListParser(InputStreams.create(text, Charsets.UTF_8));
        return Builder.getInstance().buildAddress(parser.parseAddress(), monitor);
    }

    public Address parseAddress(CharSequence text) throws ParseException {
        return parseAddress(text, DecodeMonitor.STRICT);
    }

    /**
     * Parse the address list string, such as the value of a From, To, Cc, Bcc,
     * Sender, or Reply-To header.
     *
     * The string MUST be unfolded already.
     * @param monitor the DecodeMonitor to be used while parsing/decoding
     */
    public AddressList parseAddressList(CharSequence text, DecodeMonitor monitor)
            throws ParseException {
        AddressListParser parser = new AddressListParser(InputStreams.create(text, Charsets.UTF_8));
        return Builder.getInstance().buildAddressList(parser.parseAddressList(), monitor);
    }

    public AddressList parseAddressList(CharSequence text) throws ParseException {
        return parseAddressList(text, DecodeMonitor.STRICT);
    }

    /**
     * Parses the specified raw string into a mailbox address.
     *
     * @param text
     *            string to parse.
     * @param monitor the DecodeMonitor to be used while parsing/decoding.
     * @return a <code>Mailbox</code> object for the specified string.
     * @throws ParseException
     *             if the raw string does not represent a single mailbox
     *             address.
     */
    public Mailbox parseMailbox(CharSequence text, DecodeMonitor monitor) throws ParseException {
        AddressListParser parser = new AddressListParser(InputStreams.create(text, Charsets.UTF_8));
        return Builder.getInstance().buildMailbox(parser.parseMailbox(), monitor);
    }

    public Mailbox parseMailbox(CharSequence text) throws ParseException {
        return parseMailbox(text, DecodeMonitor.STRICT);
    }

    /**
     * Parses the specified raw string into a group address.
     *
     * @param text
     *            string to parse.
     * @return a <code>Group</code> object for the specified string.
     * @throws ParseException
     *             if the raw string does not represent a single group address.
     */
    public Group parseGroup(CharSequence text, DecodeMonitor monitor) throws ParseException {
        Address address = parseAddress(text, monitor);
        if (!(address instanceof Group))
            throw new ParseException("Not a group address");

        return (Group) address;
    }

    public Group parseGroup(CharSequence text) throws ParseException {
        return parseGroup(text, DecodeMonitor.STRICT);
    }

}
