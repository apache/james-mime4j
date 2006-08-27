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

package org.mime4j.field.address;

import junit.framework.TestCase;
import org.mime4j.field.address.parser.ParseException;

public class AddressTest extends TestCase {

    public void testParse1() throws ParseException {
        AddressList addrList = AddressList.parse("John Doe <jdoe@machine(comment).  example>");
        assertEquals(1, addrList.size());
        NamedMailbox mailbox = (NamedMailbox)addrList.get(0);
        assertEquals("John Doe", mailbox.getName());
        assertEquals("jdoe", mailbox.getLocalPart());
        assertEquals("machine.example", mailbox.getDomain());
    }

    public void testParse2() throws ParseException {
        AddressList addrList = AddressList.parse("Mary Smith \t    \t\t  <mary@example.net>");
        assertEquals(1, addrList.size());
        NamedMailbox mailbox = (NamedMailbox)addrList.get(0);
        assertEquals("Mary Smith", mailbox.getName());
        assertEquals("mary", mailbox.getLocalPart());
        assertEquals("example.net", mailbox.getDomain());
    }

    public void testEmptyGroup() throws ParseException {
        AddressList addrList = AddressList.parse("undisclosed-recipients:;");
        assertEquals(1, addrList.size());
        Group group = (Group)addrList.get(0);
        assertEquals(0, group.getMailboxes().size());
        assertEquals("undisclosed-recipients", group.getName());
    }

    public void testMessyGroupAndMailbox() throws ParseException {
        AddressList addrList = AddressList.parse("Marketing  folks :  Jane Smith < jane @ example . net >, \" Jack \\\"Jackie\\\" Jones \" < jjones@example.com > (comment(comment)); ,, (comment)  , <@example . net,@example(ignore\\)).com:(ignore)john@(ignore)example.net>");
        assertEquals(2, addrList.size());

        Group group = (Group)addrList.get(0);
        assertEquals("Marketing  folks", group.getName());
        assertEquals(2, group.getMailboxes().size());

        NamedMailbox namedMailbox1 = (NamedMailbox)group.getMailboxes().get(0);
        NamedMailbox namedMailbox2 = (NamedMailbox)group.getMailboxes().get(1);

        assertEquals("Jane Smith", namedMailbox1.getName());
        assertEquals("jane", namedMailbox1.getLocalPart());
        assertEquals("example.net", namedMailbox1.getDomain());

        assertEquals(" Jack \"Jackie\" Jones ", namedMailbox2.getName());
        assertEquals("jjones", namedMailbox2.getLocalPart());
        assertEquals("example.com", namedMailbox2.getDomain());

        Mailbox mailbox = (Mailbox)addrList.get(1);
        assertFalse(mailbox instanceof NamedMailbox);
        assertEquals("john", mailbox.getLocalPart());
        assertEquals("example.net", mailbox.getDomain());
        assertEquals(2, mailbox.getRoute().size());
        assertEquals("example.net", mailbox.getRoute().get(0));
        assertEquals("example.com", mailbox.getRoute().get(1));
    }

    public void testEmptyAddressList() throws ParseException {
        assertEquals(0, AddressList.parse("  \t   \t ").size());
        assertEquals(0, AddressList.parse("  \t  ,  , , ,,, , \t ").size());
    }

    public void testSimpleForm() throws ParseException {
        AddressList addrList = AddressList.parse("\"a b c d e f g\" (comment) @example.net");
        assertEquals(1, addrList.size());
        Mailbox mailbox = (Mailbox)addrList.get(0);
        assertEquals("a b c d e f g", mailbox.getLocalPart());
        assertEquals("example.net", mailbox.getDomain());
    }

    public void testFlatten() throws ParseException {
        AddressList addrList = AddressList.parse("dev : one@example.com, two@example.com; , ,,, marketing:three@example.com ,four@example.com;, five@example.com");
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

        AddressList.parse("Alfred Neuman <Neuman@BBN-TENEXA>");
        AddressList.parse("Neuman@BBN-TENEXA");
        AddressList.parse("\"George, Ted\" <Shared@Group.Arpanet>");
        AddressList.parse("Wilt . (the Stilt) Chamberlain@NBA.US");

        // NOTE: In RFC822 8.1.5, the following example did not have "Galloping Gourmet"
        // in double-quotes.  I can only assume this was a typo, since 6.2.4 specifically
        // disallows spaces in unquoted local-part.
        AddressList.parse("     Gourmets:  Pompous Person <WhoZiWhatZit@Cordon-Bleu>," +
                "                Childs@WGBH.Boston, \"Galloping Gourmet\"@" +
                "                ANT.Down-Under (Australian National Television)," +
                "                Cheapie@Discount-Liquors;," +
                "       Cruisers:  Port@Portugal, Jones@SEA;," +
                "         Another@Somewhere.SomeOrg");

        // NOTE: In RFC822 8.3.3, the following example ended with a lone ">" after
        // Tops-20-Host.  I can only assume this was a typo, since 6.1 clearly shows
        // ">" requires a matching "<".
        AddressList.parse("Important folk:" +
                "                   Tom Softwood <Balsa@Tree.Root>," +
                "                   \"Sam Irving\"@Other-Host;," +
                "                 Standard Distribution:" +
                "                   /main/davis/people/standard@Other-Host," +
                "                   \"<Jones>standard.dist.3\"@Tops-20-Host;");

        // The following are from a Usenet post by Dan J. Bernstein:
        // http://groups.google.com/groups?selm=1996Aug1418.21.01.28081%40koobera.math.uic.edu
        AddressList.parse("\":sysmail\"@  Some-Group.\t         Some-Org, Muhammed.(I am  the greatest) Ali @(the)Vegas.WBA");
        AddressList.parse("me@home.com (comment (nested (deeply\\))))");
        AddressList.parse("mailing list: me@home.com, route two <you@work.com>, them@play.com ;");

    }

    public void testLexicalError() {
        // ensure that TokenMgrError doesn't get thrown
        try {
            AddressList.parse(")");
        }
        catch (ParseException e) {

        }
    }
}
