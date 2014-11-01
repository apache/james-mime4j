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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Group;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Lenient (tolerant to non-critical format violations) builder for {@link Address}
 * and its subclasses.
 */
public class LenientAddressParser implements AddressParser {

    private static final int AT                = '@';
    private static final int OPENING_BRACKET   = '<';
    private static final int CLOSING_BRACKET   = '>';
    private static final int COMMA             = ',';
    private static final int COLON             = ':';
    private static final int SEMICOLON         = ';';

    private static final BitSet AT_AND_CLOSING_BRACKET = RawFieldParser.INIT_BITSET(AT, CLOSING_BRACKET);
    private static final BitSet CLOSING_BRACKET_ONLY   = RawFieldParser.INIT_BITSET(CLOSING_BRACKET);
    private static final BitSet COMMA_ONLY             = RawFieldParser.INIT_BITSET(COMMA);
    private static final BitSet COLON_ONLY             = RawFieldParser.INIT_BITSET(COLON);
    private static final BitSet SEMICOLON_ONLY         = RawFieldParser.INIT_BITSET(SEMICOLON);

    public static final LenientAddressParser DEFAULT = new LenientAddressParser(DecodeMonitor.SILENT);

    private final DecodeMonitor monitor;
    private final RawFieldParser parser;

    protected LenientAddressParser(final DecodeMonitor monitor) {
        super();
        this.monitor = monitor;
        this.parser = new RawFieldParser();
    }

    String parseDomain(final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        StringBuilder dst = new StringBuilder();
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (delimiters != null && delimiters.get(current)) {
                break;
            } else if (CharsetUtil.isWhitespace(current)) {
                this.parser.skipWhiteSpace(buf, cursor);
            } else if (current == '(') {
                this.parser.skipComment(buf, cursor);
            } else {
                this.parser.copyContent(buf, cursor, delimiters, dst);
            }
        }
        return dst.toString();
    }

    DomainList parseRoute(final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        BitSet bitset = RawFieldParser.INIT_BITSET(COMMA, COLON);
        if (delimiters != null) {
            bitset.or(delimiters);
        }
        List<String> domains = null;
        for (;;) {
            this.parser.skipAllWhiteSpace(buf, cursor);
            if (cursor.atEnd()) {
                break;
            }
            int pos = cursor.getPos();
            int current = (char) (buf.byteAt(pos) & 0xff);
            if (current == AT) {
                cursor.updatePos(pos + 1);
            } else {
                break;
            }
            String s = parseDomain(buf, cursor, bitset);
            if (s != null && s.length() > 0) {
                if (domains == null) {
                    domains = new ArrayList<String>();
                }
                domains.add(s);
            }
            if (cursor.atEnd()) {
                break;
            }
            pos = cursor.getPos();
            current = (char) (buf.byteAt(pos) & 0xff);
            if (current == COMMA) {
                cursor.updatePos(pos + 1);
            } else if (current == COLON) {
                cursor.updatePos(pos + 1);
                break;
            } else {
                break;
            }
        }
        return domains != null ? new DomainList(domains) : null;
    }

    private Mailbox createMailbox(
            final String name, final DomainList route, final String localPart, final String domain) {
        return new Mailbox(
                name != null ? DecoderUtil.decodeEncodedWords(name, this.monitor) : null, 
                        route, localPart, domain);
    }
    
    Mailbox parseMailboxAddress(
            final String openingText, final ByteSequence buf, final ParserCursor cursor) {
        if (cursor.atEnd()) {
            return createMailbox(null, null, openingText, null);
        }
        int pos = cursor.getPos();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current == OPENING_BRACKET) {
            cursor.updatePos(pos + 1);
        } else {
            return createMailbox(null, null, openingText, null);
        }
        DomainList domainList = parseRoute(buf, cursor, CLOSING_BRACKET_ONLY);
        String localPart = this.parser.parseValue(buf, cursor, AT_AND_CLOSING_BRACKET);
        if (cursor.atEnd()) {
            return createMailbox(openingText, domainList, localPart, null);
        }
        pos = cursor.getPos();
        current = (char) (buf.byteAt(pos) & 0xff);
        if (current == AT) {
            cursor.updatePos(pos + 1);
        } else {
            return createMailbox(openingText, domainList, localPart, null);
        }
        String domain = parseDomain(buf, cursor, CLOSING_BRACKET_ONLY);
        if (cursor.atEnd()) {
            return createMailbox(openingText, domainList, localPart, domain);
        }
        pos = cursor.getPos();
        current = (char) (buf.byteAt(pos) & 0xff);
        if (current == CLOSING_BRACKET) {
            cursor.updatePos(pos + 1);
        } else {
            return createMailbox(openingText, domainList, localPart, domain);
        }
        while (!cursor.atEnd()) {
            pos = cursor.getPos();
            current = (char) (buf.byteAt(pos) & 0xff);
            if (CharsetUtil.isWhitespace(current)) {
                this.parser.skipWhiteSpace(buf, cursor);
            } else if (current == '(') {
                this.parser.skipComment(buf, cursor);
            } else {
                break;
            }
        }
        return createMailbox(openingText, domainList, localPart, domain);
    }

    private Mailbox createMailbox(final String localPart) {
        if (localPart != null && localPart.length() > 0) {
            return new Mailbox(null, null, localPart, null);
        } else {
            return null;
        }
    }

    public Mailbox parseMailbox(
            final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        BitSet bitset = RawFieldParser.INIT_BITSET(AT, OPENING_BRACKET);
        if (delimiters != null) {
            bitset.or(delimiters);
        }
        String openingText = this.parser.parseValue(buf, cursor, bitset);
        if (cursor.atEnd()) {
            return createMailbox(openingText);
        }
        int pos = cursor.getPos();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current == OPENING_BRACKET) {
            // name <localPart @ domain> form
            return parseMailboxAddress(openingText, buf, cursor);
        } else if (current == AT) {
            // localPart @ domain form
            cursor.updatePos(pos + 1);
            String domain = parseDomain(buf, cursor, delimiters);
            return new Mailbox(null, null, openingText, domain);
        } else {
            return createMailbox(openingText);
        }
    }

    public Mailbox parseMailbox(final CharSequence text) {
        ByteSequence raw = ContentUtil.encode(text);
        ParserCursor cursor = new ParserCursor(0, text.length());
        return parseMailbox(raw, cursor, null);
    }

    List<Mailbox> parseMailboxes(
            final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        BitSet bitset = RawFieldParser.INIT_BITSET(COMMA);
        if (delimiters != null) {
            bitset.or(delimiters);
        }
        List<Mailbox> mboxes = new ArrayList<Mailbox>();
        while (!cursor.atEnd()) {
            int pos = cursor.getPos();
            int current = (char) (buf.byteAt(pos) & 0xff);
            if (delimiters != null && delimiters.get(current)) {
                break;
            } else if (current == COMMA) {
                cursor.updatePos(pos + 1);
            } else {
                Mailbox mbox = parseMailbox(buf, cursor, bitset);
                if (mbox != null) {
                    mboxes.add(mbox);
                }
            }
        }
        return mboxes;
    }

    public Group parseGroup(final ByteSequence buf, final ParserCursor cursor) {
        String name = this.parser.parseToken(buf, cursor, COLON_ONLY);
        if (cursor.atEnd()) {
            return new Group(name, Collections.<Mailbox>emptyList());
        }
        int pos = cursor.getPos();
        int current = (char) (buf.byteAt(pos) & 0xff);
        if (current == COLON) {
            cursor.updatePos(pos + 1);
        }
        List<Mailbox> mboxes = parseMailboxes(buf, cursor, SEMICOLON_ONLY);
        return new Group(name, mboxes);
    }

    public Group parseGroup(final CharSequence text) {
        ByteSequence raw = ContentUtil.encode(text);
        ParserCursor cursor = new ParserCursor(0, text.length());
        return parseGroup(raw, cursor);
    }

    public Address parseAddress(
            final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        BitSet bitset = RawFieldParser.INIT_BITSET(COLON, AT, OPENING_BRACKET);
        if (delimiters != null) {
            bitset.or(delimiters);
        }
        String openingText = this.parser.parseValue(buf, cursor, bitset);
        if (cursor.atEnd()) {
            return createMailbox(openingText);
        }
        int pos = cursor.getPos();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current == OPENING_BRACKET) {
            // name <localPart @ domain> form
            return parseMailboxAddress(openingText, buf, cursor);
        } else if (current == AT) {
            // localPart @ domain form
            cursor.updatePos(pos + 1);
            String domain = parseDomain(buf, cursor, delimiters);
            return new Mailbox(null, null, openingText, domain);
        } else if (current == COLON) {
            // group-name: localPart @ domain, name <localPart @ domain>; form
            cursor.updatePos(pos + 1);
            List<Mailbox> mboxes = parseMailboxes(buf, cursor, SEMICOLON_ONLY);
            if (!cursor.atEnd()) {
                pos = cursor.getPos();
                current = (char) (buf.byteAt(pos) & 0xff);
                if (current == SEMICOLON) {
                    cursor.updatePos(pos + 1);
                }
            }
            return new Group(openingText, mboxes);
        } else {
            return createMailbox(openingText);
        }
    }

    public Address parseAddress(final CharSequence text) {
        ByteSequence raw = ContentUtil.encode(text);
        ParserCursor cursor = new ParserCursor(0, text.length());
        return parseAddress(raw, cursor, null);
    }

    public AddressList parseAddressList(final ByteSequence buf, final ParserCursor cursor) {
        List<Address> addresses = new ArrayList<Address>();
        while (!cursor.atEnd()) {
            int pos = cursor.getPos();
            int current = (char) (buf.byteAt(pos) & 0xff);
            if (current == COMMA) {
                cursor.updatePos(pos + 1);
            } else {
                Address address = parseAddress(buf, cursor, COMMA_ONLY);
                if (address != null) {
                    addresses.add(address);
                }
            }
        }
        return new AddressList(addresses, false);
    }

    public AddressList parseAddressList(final CharSequence text) {
        ByteSequence raw = ContentUtil.encode(text);
        ParserCursor cursor = new ParserCursor(0, text.length());
        return parseAddressList(raw, cursor);
    }

}
