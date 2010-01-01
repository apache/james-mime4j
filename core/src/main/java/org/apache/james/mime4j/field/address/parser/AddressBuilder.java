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

package org.apache.james.mime4j.field.address.parser;

import java.io.StringReader;

import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Group;
import org.apache.james.mime4j.field.address.Mailbox;

public class AddressBuilder {

    /**
     * Parses the specified raw string into an address.
     * 
     * @param rawAddressString
     *            string to parse.
     * @return an <code>Address</code> object for the specified string.
     * @throws IllegalArgumentException
     *             if the raw string does not represent a single address.
     */
    public static Address parseAddress(String rawAddressString) {
        AddressListParser parser = new AddressListParser(new StringReader(
                rawAddressString));
        try {
            return Builder.getInstance().buildAddress(parser.parseAddress());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse the address list string, such as the value of a From, To, Cc, Bcc,
     * Sender, or Reply-To header.
     * 
     * The string MUST be unfolded already.
     */
    public static AddressList parseAddressList(String rawAddressList)
            throws ParseException {
        AddressListParser parser = new AddressListParser(new StringReader(
                rawAddressList));
        return Builder.getInstance().buildAddressList(parser.parseAddressList());
    }


    /**
     * Test console for AddressList
     */
    public static void main(String[] args) throws Exception {
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                String line = reader.readLine();
                if (line.length() == 0 || line.toLowerCase().equals("exit")
                        || line.toLowerCase().equals("quit")) {
                    System.out.println("Goodbye.");
                    return;
                }
                AddressList list = AddressBuilder.parseAddressList(line);
                list.print();
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(300);
            }
        }
    }

    /**
     * Parses the specified raw string into a mailbox address.
     * 
     * @param rawMailboxString
     *            string to parse.
     * @return a <code>Mailbox</code> object for the specified string.
     * @throws IllegalArgumentException
     *             if the raw string does not represent a single mailbox
     *             address.
     */
    public static Mailbox parseMailbox(String rawMailboxString) {
        AddressListParser parser = new AddressListParser(new StringReader(
                rawMailboxString));
        try {
            return Builder.getInstance().buildMailbox(parser.parseMailbox());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parses the specified raw string into a group address.
     * 
     * @param rawGroupString
     *            string to parse.
     * @return a <code>Group</code> object for the specified string.
     * @throws IllegalArgumentException
     *             if the raw string does not represent a single group address.
     */
    public static Group parseGroup(String rawGroupString) {
        Address address = parseAddress(rawGroupString);
        if (!(address instanceof Group))
            throw new IllegalArgumentException("Not a group address");
    
        return (Group) address;
    }

}
