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

import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultAddressFormatterTest {

    private AddressFormatter formatter;

    @Before
    public void setUp() throws Exception {
        formatter = AddressFormatter.DEFAULT;
    }

    @Test
    public void testGroupSerialization() {
        List<Mailbox> al = new ArrayList<Mailbox>();
        al.add(new Mailbox("test", "example.com"));
        al.add(new Mailbox("Foo!", "foo", "example.com"));
        DomainList dl = new DomainList(new ArrayList<String>(
                Arrays.asList("foo.example.com")));
        Mailbox mailbox = new Mailbox("Foo Bar", dl, "foo2", "example.com");
        Assert.assertSame(dl, mailbox.getRoute());
        al.add(mailbox);
        Group g = new Group("group", new MailboxList(al, false));
        String s = formatter.format(g, false);
        Assert.assertEquals("group: test@example.com, Foo! <foo@example.com>, Foo Bar <foo2@example.com>;", s);
    }

    @Test
    public void testMailboxGetEncodedString() throws Exception {
        Mailbox m1 = new Mailbox("john.doe", "acme.org");
        Assert.assertEquals("john.doe@acme.org", formatter.encode(m1));
        Mailbox m2 = new Mailbox("john doe", "acme.org");
        Assert.assertEquals("\"john doe\"@acme.org", formatter.encode(m2));
        Mailbox m3 = new Mailbox("John Doe", "john.doe", "acme.org");
        Assert.assertEquals("John Doe <john.doe@acme.org>", formatter.encode(m3));
        Mailbox m4 = new Mailbox("John Doe @Home", "john.doe", "acme.org");
        Assert.assertEquals("\"John Doe @Home\" <john.doe@acme.org>", formatter.encode(m4));
        Mailbox m5 = new Mailbox("Hans M\374ller", "hans.mueller", "acme.org");
        Assert.assertEquals("=?ISO-8859-1?Q?Hans_M=FCller?= <hans.mueller@acme.org>", formatter.encode(m5));
    }

    @Test
    public void testGroupGetEncodedString() throws Exception {
        List<Mailbox> al = new ArrayList<Mailbox>();
        al.add(new Mailbox("test", "example.com"));
        al.add(new Mailbox("Foo!", "foo", "example.com"));
        al.add(new Mailbox("Hans M\374ller", "hans.mueller", "acme.org"));
        Group g = new Group("group @work", new MailboxList(al, false));
        Assert.assertEquals("\"group @work\": test@example.com, "
                + "Foo! <foo@example.com>, =?ISO-8859-1?Q?Hans_M=FCller?="
                + " <hans.mueller@acme.org>;", formatter.encode(g));
    }

    @Test
    public void testEmptyGroupGetEncodedString() throws Exception {
        MailboxList emptyMailboxes = new MailboxList(null, true);
        Group g = new Group("Undisclosed recipients", emptyMailboxes);
        Assert.assertEquals("Undisclosed recipients:;", formatter.encode(g));
    }

}
