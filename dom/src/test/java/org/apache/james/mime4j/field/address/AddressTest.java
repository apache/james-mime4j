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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AddressTest {

    @Test
    public void testExceptionTree() {
        // make sure that our ParseException extends MimeException.
        Assert.assertTrue(MimeException.class.isAssignableFrom(ParseException.class));
    }

    @Test
    public void testNullConstructorAndBadUsage() {
        AddressList al = new AddressList(null, false);
        Assert.assertEquals(0, al.size());

        try {
            al.get(-1);
            Assert.fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            al.get(0);
            Assert.fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }
    }


    @Test
    public void testEmptyDomainList() {
        DomainList dl = new DomainList(null, false);
        Assert.assertEquals(0, dl.size());

        try {
            dl.get(-1);
            Assert.fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            dl.get(0);
            Assert.fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testDomainList() {
        List<String> al = new ArrayList<String>();
        al.add("example.com");

        // changing the list passed does not change DomainList's state
        DomainList dl = new DomainList(al);
        Assert.assertEquals(1, dl.size());
        al.add("foo.example.com");
        Assert.assertEquals(1, dl.size());

        // cloned arraylist
        DomainList dlcopy = new DomainList(al);
        Assert.assertEquals(2, dlcopy.size());
        al.add("bar.example.com");
        Assert.assertEquals(2, dlcopy.size());

        // check route string
        Assert.assertEquals("@example.com,@foo.example.com", dlcopy.toRouteString());
    }


    @Test
    public void testEmptyMailboxList() {
        MailboxList ml = new MailboxList(null, false);
        Assert.assertEquals(0, ml.size());

        try {
            ml.get(-1);
            Assert.fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            ml.get(0);
            Assert.fail("Expected index out of bound exception!");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testMailboxList() {
        List<Mailbox> al = new ArrayList<Mailbox>();
        al.add(new Mailbox("local", "example.com"));

        // shared arraylist
        MailboxList ml = new MailboxList(al, true);
        Assert.assertEquals(1, ml.size());
        al.add(new Mailbox("local2", "foo.example.com"));
        Assert.assertEquals(2, ml.size());

        // cloned arraylist
        MailboxList mlcopy = new MailboxList(al, false);
        Assert.assertEquals(2, mlcopy.size());
        al.add(new Mailbox("local3", "bar.example.com"));
        Assert.assertEquals(2, mlcopy.size());
    }

    @Test
    public void testMailboxEquals() throws Exception {
        Mailbox m1 = new Mailbox("john.doe", "acme.org");
        Mailbox m2 = new Mailbox("john doe", "acme.org");
        Mailbox m3 = new Mailbox("john.doe", "Acme.Org");
        Mailbox m4 = new Mailbox("john.doe", null);
        Assert.assertTrue(m1.equals(m1));
        Assert.assertFalse(m1.equals(m2));
        Assert.assertTrue(m1.equals(m3));
        Assert.assertFalse(m1.equals(m4));
        Assert.assertFalse(m1 == null);
    }

    @Test
    public void testMailboxHashCode() throws Exception {
        Mailbox m1 = new Mailbox("john.doe", "acme.org");
        Mailbox m2 = new Mailbox("john doe", "acme.org");
        Mailbox m3 = new Mailbox("john.doe", "Acme.Org");
        Mailbox m4 = new Mailbox("john.doe", null);
        Assert.assertTrue(m1.hashCode() == m1.hashCode());
        Assert.assertFalse(m1.hashCode() == m2.hashCode());
        Assert.assertTrue(m1.hashCode() == m3.hashCode());
        Assert.assertFalse(m1.hashCode() == m4.hashCode());
    }

    @Test
    public void equalMailboxesShouldHaveEqualHashCodesForNonAsciiDomains() {
        String[][] pairs = {
            {"\u0130nfo.example", "info.example"},   // dotted capital I vs i
            {"\u0131nfo.example", "Info.example"},   // dotless i vs I
            {"\u03C3\u03C2.gr", "\u03C3\u03C3.gr"},     // final sigma vs sigma
            {"\u03A3\u03A3.gr", "\u03C3\u03C2.gr"},     // capital sigmas vs sigma, final sigma
            {"\u017Fome.example", "some.example"},   // long s vs s
            {"\u00B5.example", "\u03BC.example"},     // micro sign vs mu
            {"\uD801\uDC00.example", "\uD801\uDC28.example"}, // Deseret capital vs small long i
        };
        for (String[] p : pairs) {
            Mailbox m1 = new Mailbox("a", p[0]);
            Mailbox m2 = new Mailbox("a", p[1]);
            if (m1.equals(m2)) {
                Assert.assertEquals(p[0] + " / " + p[1], m1.hashCode(), m2.hashCode());
            }
        }
        Assert.assertEquals(new Mailbox("a", "\u0130nfo.example"), new Mailbox("a", "info.example"));
    }

    @Test
    public void hashSetShouldDeduplicateParsedMailboxesWithNonAsciiDomains() {
        List<Mailbox> mailboxes = LenientAddressParser.DEFAULT
            .parseAddressList("a@\u0130nfo.example, a@info.example, a@\u017Fome.example, a@SOME.example").flatten();
        Assert.assertEquals(mailboxes.get(0), mailboxes.get(1));
        Assert.assertEquals(mailboxes.get(2), mailboxes.get(3));
        Assert.assertEquals(2, new HashSet<>(mailboxes).size());
    }

    @Test
    public void mailboxesDifferingOnlyByLocalPartCaseShouldNotBeEqual() {
        Assert.assertNotEquals(new Mailbox("A", "example.com"), new Mailbox("a", "example.com"));
        Assert.assertEquals(new Mailbox("a", "EXAMPLE.com").hashCode(), new Mailbox("a", "example.COM").hashCode());
    }
}
