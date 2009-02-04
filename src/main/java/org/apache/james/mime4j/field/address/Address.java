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

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

import org.apache.james.mime4j.field.address.parser.AddressListParser;
import org.apache.james.mime4j.field.address.parser.ParseException;

/**
 * The abstract base for classes that represent RFC2822 addresses. This includes
 * groups and mailboxes.
 */
public abstract class Address implements Serializable {

    private static final long serialVersionUID = 634090661990433426L;

    /**
     * Adds any mailboxes represented by this address into the given List. Note
     * that this method has default (package) access, so a doAddMailboxesTo
     * method is needed to allow the behavior to be overridden by subclasses.
     */
    final void addMailboxesTo(List<Mailbox> results) {
        doAddMailboxesTo(results);
    }

    /**
     * Adds any mailboxes represented by this address into the given List. Must
     * be overridden by concrete subclasses.
     */
    protected abstract void doAddMailboxesTo(List<Mailbox> results);

    /**
     * Formats the address as a human readable string, not including the route.
     * The resulting string is intended for display purposes only and cannot be
     * used for transport purposes.
     * 
     * @return a string representation of this address intended to be displayed
     * @see #getDisplayString(boolean)
     */
    public final String getDisplayString() {
        return getDisplayString(false);
    }

    /**
     * Formats the address as a human readable string, not including the route.
     * The resulting string is intended for display purposes only and cannot be
     * used for transport purposes.
     * 
     * For example, if the unparsed address was
     * 
     * <"Joe Cheng"@joecheng.com>
     * 
     * this method would return
     * 
     * <Joe Cheng@joecheng.com>
     * 
     * which is not valid for transport; the local part would need to be
     * re-quoted.
     * 
     * @param includeRoute
     *            <code>true</code> if the route should be included if it
     *            exists, <code>false</code> otherwise.
     * @return a string representation of this address intended to be displayed.
     */
    public abstract String getDisplayString(boolean includeRoute);

    /**
     * Returns a string representation of this address that can be used for
     * transport purposes. The route is never included in this representation
     * because routes are obsolete and RFC 5322 states that obsolete syntactic
     * forms MUST NOT be generated.
     * 
     * @return a string representation of this address intended for transport
     *         purposes.
     */
    public abstract String getEncodedString();

    /**
     * Parses the specified raw string into an address.
     * 
     * @param rawAddressString
     *            string to parse.
     * @return an <code>Address</code> object for the specified string.
     * @throws IllegalArgumentException
     *             if the raw string does not represent a single address.
     */
    public static Address parse(String rawAddressString) {
        AddressListParser parser = new AddressListParser(new StringReader(
                rawAddressString));
        try {
            return Builder.getInstance().buildAddress(parser.parseAddress());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return getDisplayString(false);
    }

}