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

import junit.framework.TestCase;

import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

public class LenientAddressBuilderTest extends TestCase {

    private LenientAddressBuilder parser;

    @Override
    protected void setUp() throws Exception {
        parser = LenientAddressBuilder.DEFAULT;
    }

    public void testParseDomain() throws ParseException {
        String s = "machine (comment).  example (dot). com  ; more stuff";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        String domain = parser.parseDomain(raw, cursor, new int[] {';'});
        assertEquals("machine.example.com", domain);
    }

    public void testParseAddress() throws ParseException {
        String s = "<  some  one @ some host . some where . com >";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        Mailbox mailbox = parser.parseMailboxAddress(null, raw, cursor, new int[] {';'});
        assertEquals("some one@somehost.somewhere.com", mailbox.getAddress());
    }

    public void testParseAddressQuotedLocalPart() throws ParseException {
        String s = "<  \"some  one\"   @ some host . some where . com >";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        Mailbox mailbox = parser.parseMailboxAddress(null, raw, cursor, new int[] {';'});
        assertEquals("some  one@somehost.somewhere.com", mailbox.getAddress());
    }

    public void testParseAddressTruncated() throws ParseException {
        String s = "<  some  one  ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        Mailbox mailbox = parser.parseMailboxAddress(null, raw, cursor, new int[] {';'});
        assertEquals("some one", mailbox.getAddress());
    }

    public void testParseAddressTrailingComments() throws ParseException {
        String s = "< someone@somehost.somewhere.com  > (garbage) ; ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        Mailbox mailbox = parser.parseMailboxAddress(null, raw, cursor, new int[] {';'});
        assertEquals("someone@somehost.somewhere.com", mailbox.getAddress());
        assertEquals(';', raw.byteAt(cursor.getPos()));
    }

    public void testParseAddressTrailingGarbage() throws ParseException {
        String s = "< someone@somehost.somewhere.com  > garbage) ; ";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        Mailbox mailbox = parser.parseMailboxAddress(null, raw, cursor, new int[] {';'});
        assertEquals("someone@somehost.somewhere.com", mailbox.getAddress());
        assertEquals('g', raw.byteAt(cursor.getPos()));
    }

    public void testParseRoute() throws ParseException {
        String s = "  @a, @b, @c :me@home";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        DomainList route = parser.parseRoute(raw, cursor);
        assertNotNull(route);
        assertEquals(3, route.size());
        assertEquals("a", route.get(0));
        assertEquals("b", route.get(1));
        assertEquals("c", route.get(2));
        assertEquals('m', raw.byteAt(cursor.getPos()));
    }

    public void testParseNoRoute() throws ParseException {
        String s = "stuff";
        ByteSequence raw = ContentUtil.encode(s);
        ParserCursor cursor = new ParserCursor(0, s.length());

        DomainList route = parser.parseRoute(raw, cursor);
        assertNull(route);
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

    public void testParseMailboxNonASCII() throws ParseException {
        Mailbox mailbox1 = parser.parseMailbox(
                "Hans M\374ller <hans.mueller@acme.org>");
        assertEquals("Hans M\374ller", mailbox1.getName());
        assertEquals("hans.mueller@acme.org", mailbox1.getAddress());
    }

    public void testParsePartialQuotes() throws ParseException {
        Mailbox mailbox1 = parser.parseMailbox(
                "Hans \"M\374ller\" is a good fella <hans.mueller@acme.org>");
        assertEquals("Hans M\374ller is a good fella", mailbox1.getName());
        assertEquals("hans.mueller@acme.org", mailbox1.getAddress());
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

}
