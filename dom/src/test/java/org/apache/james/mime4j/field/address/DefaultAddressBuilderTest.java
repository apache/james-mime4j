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
import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.field.address.ParseException;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class DefaultAddressBuilderTest extends TestCase {

    private AddressBuilder parser;

    @Override
    protected void setUp() throws Exception {
        parser = AddressBuilder.DEFAULT;
    }

    public void testParseMailbox() throws ParseException {
        Mailbox mailbox1 = parser.parseMailbox("John Doe <jdoe@machine(comment).  example>");
        assertEquals("John Doe", mailbox1.getName());
        assertEquals("jdoe", mailbox1.getLocalPart());
        assertEquals("machine.example", mailbox1.getDomain());

        Mailbox mailbox2 = parser.parseMailbox("Mary Smith \t    \t\t  <mary@example.net>");
        assertEquals("Mary Smith", mailbox2.getName());
        assertEquals("mary", mailbox2.getLocalPart());
        assertEquals("example.net", mailbox2.getDomain());

        Mailbox mailbox3 = parser.parseMailbox("john.doe@acme.org");
        assertNull(mailbox3.getName());
        assertEquals("john.doe@acme.org", mailbox3.getAddress());

        Mailbox mailbox4 = parser.parseMailbox("Mary Smith <mary@example.net>");
        assertEquals("Mary Smith", mailbox4.getName());
        assertEquals("mary@example.net", mailbox4.getAddress());

        // non-ascii should be allowed in quoted strings
        Mailbox mailbox5 = parser.parseMailbox(
                "\"Hans M\374ller\" <hans.mueller@acme.org>");
        assertEquals("Hans M\374ller", mailbox5.getName());
        assertEquals("hans.mueller@acme.org", mailbox5.getAddress());

    }

    public void testParseMailboxObsoleteSynatax() throws ParseException {
        Mailbox mailbox1 = parser.parseMailbox("< (route)(obsolete) " +
                "@host1.domain1 , @host2 . domain2:  foo@bar.org>");
        assertEquals(null, mailbox1.getName());
        assertEquals("foo", mailbox1.getLocalPart());
        assertEquals("bar.org", mailbox1.getDomain());
        DomainList domainList = mailbox1.getRoute();
        assertNotNull(domainList);
        assertEquals(2, domainList.size());
        assertEquals("host1.domain1", domainList.get(0));
        assertEquals("host2.domain2", domainList.get(1));
    }

    public void testParseInvalidMailbox() throws Exception {
        try {
            parser.parseMailbox("g: Mary Smith <mary@example.net>;");
            fail();
        } catch (ParseException expected) {
        }
        try {
            parser.parseMailbox("Mary Smith <mary@example.net>, hans.mueller@acme.org");
            fail();
        } catch (ParseException expected) {
        }
    }

    public void testParseAddressList() throws ParseException {
        AddressList addrList1 = parser.parseAddressList("John Doe <jdoe@machine(comment).  example>");
        assertEquals(1, addrList1.size());
        Mailbox mailbox1 = (Mailbox)addrList1.get(0);
        assertEquals("John Doe", mailbox1.getName());
        assertEquals("jdoe", mailbox1.getLocalPart());
        assertEquals("machine.example", mailbox1.getDomain());

        AddressList addrList2 = parser.parseAddressList("Mary Smith \t    \t\t  <mary@example.net>");
        assertEquals(1, addrList2.size());
        Mailbox mailbox2 = (Mailbox)addrList2.get(0);
        assertEquals("Mary Smith", mailbox2.getName());
        assertEquals("mary", mailbox2.getLocalPart());
        assertEquals("example.net", mailbox2.getDomain());
    }

    public void testEmptyGroup() throws ParseException {
        AddressList addrList = parser.parseAddressList("undisclosed-recipients:;");
        assertEquals(1, addrList.size());
        Group group = (Group)addrList.get(0);
        assertEquals(0, group.getMailboxes().size());
        assertEquals("undisclosed-recipients", group.getName());
    }

    public void testMessyGroupAndMailbox() throws ParseException {
        AddressList addrList = parser.parseAddressList(
                "Marketing  folks :  Jane Smith < jane @ example . net >," +
                " \" Jack \\\"Jackie\\\" Jones \" < jjones@example.com > (comment(comment)); ,, (comment)  ," +
                " <@example . net,@example(ignore\\)).com:(ignore)john@(ignore)example.net>");
        assertEquals(2, addrList.size());

        Group group = (Group)addrList.get(0);
        assertEquals("Marketing  folks", group.getName());
        assertEquals(2, group.getMailboxes().size());

        Mailbox mailbox1 = group.getMailboxes().get(0);
        Mailbox mailbox2 = group.getMailboxes().get(1);

        assertEquals("Jane Smith", mailbox1.getName());
        assertEquals("jane", mailbox1.getLocalPart());
        assertEquals("example.net", mailbox1.getDomain());

        assertEquals(" Jack \"Jackie\" Jones ", mailbox2.getName());
        assertEquals("jjones", mailbox2.getLocalPart());
        assertEquals("example.com", mailbox2.getDomain());

        Mailbox mailbox = (Mailbox)addrList.get(1);
        assertEquals("john", mailbox.getLocalPart());
        assertEquals("example.net", mailbox.getDomain());
        assertEquals(2, mailbox.getRoute().size());
        assertEquals("example.net", mailbox.getRoute().get(0));
        assertEquals("example.com", mailbox.getRoute().get(1));
    }

    public void testEmptyAddressList() throws ParseException {
        assertEquals(0, parser.parseAddressList("  \t   \t ").size());
        assertEquals(0, parser.parseAddressList("  \t  ,  , , ,,, , \t ").size());
    }

    public void testSimpleForm() throws ParseException {
        AddressList addrList = parser.parseAddressList("\"a b c d e f g\" (comment) @example.net");
        assertEquals(1, addrList.size());
        Mailbox mailbox = (Mailbox)addrList.get(0);
        assertEquals("a b c d e f g", mailbox.getLocalPart());
        assertEquals("example.net", mailbox.getDomain());
    }

    public void testFlatten() throws ParseException {
        AddressList addrList = parser.parseAddressList("dev : one@example.com, two@example.com; , ,,, marketing:three@example.com ,four@example.com;, five@example.com");
        assertEquals(3, addrList.size());
        assertEquals(5, addrList.flatten().size());
    }

    public void testTortureTest() throws ParseException {

        // Source: http://mailformat.dan.info/headers/from.html
        // (Commented out pending confirmation of legality--I think the local-part is illegal.)
        // AddressList.parse("\"Guy Macon\" <guymacon+\" http://www.guymacon.com/ \"00@spamcop.net>");

        // Taken mostly from RFC822.

        // Just make sure these are recognized as legal address lists;
        // there shouldn't be any aspect of the RFC that is tested here
        // but not in the other unit tests.

        parser.parseAddressList("Alfred Neuman <Neuman@BBN-TENEXA>");
        parser.parseAddressList("Neuman@BBN-TENEXA");
        parser.parseAddressList("\"George, Ted\" <Shared@Group.Arpanet>");
        parser.parseAddressList("Wilt . (the Stilt) Chamberlain@NBA.US");

        // NOTE: In RFC822 8.1.5, the following example did not have "Galloping Gourmet"
        // in double-quotes.  I can only assume this was a typo, since 6.2.4 specifically
        // disallows spaces in unquoted local-part.
        parser.parseAddressList("     Gourmets:  Pompous Person <WhoZiWhatZit@Cordon-Bleu>," +
                "                Childs@WGBH.Boston, \"Galloping Gourmet\"@" +
                "                ANT.Down-Under (Australian National Television)," +
                "                Cheapie@Discount-Liquors;," +
                "       Cruisers:  Port@Portugal, Jones@SEA;," +
                "         Another@Somewhere.SomeOrg");

        // NOTE: In RFC822 8.3.3, the following example ended with a lone ">" after
        // Tops-20-Host.  I can only assume this was a typo, since 6.1 clearly shows
        // ">" requires a matching "<".
        parser.parseAddressList("Important folk:" +
                "                   Tom Softwood <Balsa@Tree.Root>," +
                "                   \"Sam Irving\"@Other-Host;," +
                "                 Standard Distribution:" +
                "                   /main/davis/people/standard@Other-Host," +
                "                   \"<Jones>standard.dist.3\"@Tops-20-Host;");

        // The following are from a Usenet post by Dan J. Bernstein:
        // http://groups.google.com/groups?selm=1996Aug1418.21.01.28081%40koobera.math.uic.edu
        parser.parseAddressList("\":sysmail\"@  Some-Group.\t         Some-Org, Muhammed.(I am  the greatest) Ali @(the)Vegas.WBA");
        parser.parseAddressList("me@home.com (comment (nested (deeply\\))))");
        parser.parseAddressList("mailing list: me@home.com, route two <you@work.com>, them@play.com ;");

    }

    public void testLexicalError() {
        // ensure that TokenMgrError doesn't get thrown
        try {
            parser.parseAddressList(")");
            fail("Expected parsing error");
        }
        catch (ParseException e) {
        }
    }

    public void testAddressList() throws ParseException {
        AddressList addlist = parser.parseAddressList("foo@example.com, bar@example.com, third@example.com");
        List<Address> al = new ArrayList<Address>();
        al.add(addlist.get(0));

        // shared arraylist
        AddressList dl = new AddressList(al, true);
        assertEquals(1, dl.size());
        al.add(addlist.get(1));
        assertEquals(2, dl.size());

        // cloned arraylist
        AddressList dlcopy = new AddressList(al, false);
        assertEquals(2, dlcopy.size());
        al.add(addlist.get(2));
        assertEquals(2, dlcopy.size());

        // check route string
        assertEquals(2, dlcopy.flatten().size());
    }

    public void testParseAddress() throws Exception {
        Address address = parser.parseAddress("Mary Smith <mary@example.net>");
        assertTrue(address instanceof Mailbox);
        assertEquals("Mary Smith", ((Mailbox) address).getName());
        assertEquals("mary@example.net", ((Mailbox) address).getAddress());

        address = parser.parseAddress("group: Mary Smith <mary@example.net>;");
        assertTrue(address instanceof Group);
        assertEquals("group", ((Group) address).getName());
        assertEquals("Mary Smith", ((Group) address).getMailboxes().get(0)
                .getName());
        assertEquals("mary@example.net", ((Group) address).getMailboxes()
                .get(0).getAddress());
    }

    public void testParseInvalidAddress() throws Exception {
        try {
            parser.parseGroup("john.doe@acme.org, jane.doe@acme.org");
            fail();
        } catch (ParseException expected) {
        }
    }

    public void testParseGroup() throws Exception {
        Group group = parser.parseGroup(
                "group: john.doe@acme.org, Mary Smith <mary@example.net>;");
        assertEquals("group", group.getName());

        MailboxList mailboxes = group.getMailboxes();
        assertEquals(2, mailboxes.size());

        Mailbox mailbox1 = mailboxes.get(0);
        assertNull(mailbox1.getName());
        assertEquals("john.doe@acme.org", mailbox1.getAddress());

        Mailbox mailbox2 = mailboxes.get(1);
        assertEquals("Mary Smith", mailbox2.getName());
        assertEquals("mary@example.net", mailbox2.getAddress());
    }

    public void testParseInvalidGroup() throws Exception {
        try {
            parser.parseGroup("john.doe@acme.org");
            fail();
        } catch (ParseException expected) {
        }

        try {
            parser.parseGroup("g1: john.doe@acme.org;, g2: mary@example.net;");
            fail();
        } catch (ParseException expected) {
        }
    }

}
