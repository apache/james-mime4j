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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.field.address.ParseException;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class AddressTest extends TestCase {

    public void testExceptionTree() {
        // make sure that our ParseException extends MimeException.
        assertTrue(MimeException.class.isAssignableFrom(ParseException.class));
    }

    public void testNullConstructorAndBadUsage() {
        AddressList al = new AddressList(null, false);
        assertEquals(0, al.size());

        try {
            al.get(-1);
            fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            al.get(0);
            fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }
    }


    public void testEmptyDomainList() {
        DomainList dl = new DomainList(null);
        assertEquals(0, dl.size());

        try {
            dl.get(-1);
            fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            dl.get(0);
            fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void testDomainList() {
        List<String> al = new ArrayList<String>();
        al.add("example.com");

        // changing the list passed does not change DomainList's state
        DomainList dl = new DomainList(al);
        assertEquals(1, dl.size());
        al.add("foo.example.com");
        assertEquals(1, dl.size());

        // cloned arraylist
        DomainList dlcopy = new DomainList(al);
        assertEquals(2, dlcopy.size());
        al.add("bar.example.com");
        assertEquals(2, dlcopy.size());

        // check route string
        assertEquals("@example.com,@foo.example.com", dlcopy.toRouteString());
    }


    public void testEmptyMailboxList() {
        MailboxList ml = new MailboxList(null, false);
        assertEquals(0, ml.size());

        try {
            ml.get(-1);
            fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            ml.get(0);
            fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void testMailboxList() {
        List<Mailbox> al = new ArrayList<Mailbox>();
        al.add(new Mailbox("local","example.com"));

        // shared arraylist
        MailboxList ml = new MailboxList(al, true);
        assertEquals(1, ml.size());
        al.add(new Mailbox("local2", "foo.example.com"));
        assertEquals(2, ml.size());

        // cloned arraylist
        MailboxList mlcopy = new MailboxList(al, false);
        assertEquals(2, mlcopy.size());
        al.add(new Mailbox("local3", "bar.example.com"));
        assertEquals(2, mlcopy.size());
    }

    public void testMailboxEquals() throws Exception {
        Mailbox m1 = new Mailbox("john.doe", "acme.org");
        Mailbox m2 = new Mailbox("john doe", "acme.org");
        Mailbox m3 = new Mailbox("john.doe", "Acme.Org");
        Mailbox m4 = new Mailbox("john.doe", null);
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(m2));
        assertTrue(m1.equals(m3));
        assertFalse(m1.equals(m4));
        assertFalse(m1.equals(null));
    }

    public void testMailboxHashCode() throws Exception {
        Mailbox m1 = new Mailbox("john.doe", "acme.org");
        Mailbox m2 = new Mailbox("john doe", "acme.org");
        Mailbox m3 = new Mailbox("john.doe", "Acme.Org");
        Mailbox m4 = new Mailbox("john.doe", null);
        assertTrue(m1.hashCode() == m1.hashCode());
        assertFalse(m1.hashCode() == m2.hashCode());
        assertTrue(m1.hashCode() == m3.hashCode());
        assertFalse(m1.hashCode() == m4.hashCode());
    }

}
