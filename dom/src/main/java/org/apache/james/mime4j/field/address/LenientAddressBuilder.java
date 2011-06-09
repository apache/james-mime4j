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
import java.util.List;

import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;

public class LenientAddressBuilder {

    private static final int AT                = '@';
    private static final int OPENING_BRACKET   = '<';
    private static final int CLOSING_BRACKET   = '>';
    private static final int COMMA             = ',';
    private static final int COLON             = ':';

    private static final int[] AT_AND_OPENING_BRACKET = new int[] { AT, OPENING_BRACKET };
    private static final int[] AT_AND_CLOSING_BRACKET = new int[] { AT, CLOSING_BRACKET };
    private static final int[] CLOSING_BRACKET_ONLY   = new int[] { CLOSING_BRACKET };
    private static final int[] COMMA_AND_COLON        = new int[] { COMMA, COLON };

    public static final LenientAddressBuilder DEFAULT = new LenientAddressBuilder();

    private final RawFieldParser parser;

    protected LenientAddressBuilder() {
        super();
        this.parser = new RawFieldParser();
    }

    String parseDomain(final ByteSequence buf, final ParserCursor cursor, final int[] delimiters) {
        StringBuilder dst = new StringBuilder();
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (RawFieldParser.isOneOf(current, delimiters)) {
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

    DomainList parseRoute(final ByteSequence buf, final ParserCursor cursor) {
        List<String> domains = null;
        while (!cursor.atEnd()) {
            this.parser.skipAllWhiteSpace(buf, cursor);
            int pos = cursor.getPos();
            int current = (char) (buf.byteAt(pos) & 0xff);
            if (current == AT) {
                cursor.updatePos(pos + 1);
            } else {
                break;
            }
            String s = parseDomain(buf, cursor, COMMA_AND_COLON);
            if (s != null && s.length() > 0) {
                if (domains == null) {
                    domains = new ArrayList<String>();
                }
                domains.add(s);
            }
            pos = cursor.getPos();
            current = (char) (buf.byteAt(pos) & 0xff);
            if (current == COMMA) {
                cursor.updatePos(pos + 1);
                continue;
            } else if (current == COLON) {
                cursor.updatePos(pos + 1);
                break;
            } else {
                break;
            }
        }
        return domains != null ? new DomainList(domains, true) : null;
    }

    Mailbox parseMailboxAddress(
            final String openingText,
            final ByteSequence buf, final ParserCursor cursor, final int[] delimiters) {
        if (cursor.atEnd()) {
            return null;
        }
        int pos = cursor.getPos();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current == OPENING_BRACKET) {
            cursor.updatePos(pos + 1);
        } else {
            return new Mailbox(null, null, openingText, null);
        }
        DomainList domainList = parseRoute(buf, cursor);
        String localPart = this.parser.parseValue(buf, cursor, AT_AND_CLOSING_BRACKET);
        if (cursor.atEnd()) {
            return new Mailbox(openingText, domainList, localPart, null);
        }
        pos = cursor.getPos();
        current = (char) (buf.byteAt(pos) & 0xff);
        if (current == AT) {
            cursor.updatePos(pos + 1);
        } else {
            return new Mailbox(openingText, domainList, localPart, null);
        }
        String domain = parseDomain(buf, cursor, CLOSING_BRACKET_ONLY);
        if (cursor.atEnd()) {
            return new Mailbox(openingText, domainList, localPart, domain);
        }
        pos = cursor.getPos();
        current = (char) (buf.byteAt(pos) & 0xff);
        if (current == CLOSING_BRACKET) {
            cursor.updatePos(pos + 1);
        } else {
            return new Mailbox(openingText, domainList, localPart, domain);
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
        return new Mailbox(openingText, domainList, localPart, domain);
    }

    public Mailbox parseMailbox(
            final ByteSequence buf, final ParserCursor cursor, final int[] delimiters) {
        String openingText = this.parser.parseValue(buf, cursor, AT_AND_OPENING_BRACKET);
        if (cursor.atEnd()) {
            return new Mailbox(null, null, openingText, null);
        }
        int pos = cursor.getPos();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current == OPENING_BRACKET) {
            // name <localPart @ domain> form
            return parseMailboxAddress(openingText, buf, cursor, delimiters);
        } else if (current == AT) {
            // localPart @ domain form
            cursor.updatePos(pos + 1);
            String localPart = openingText;
            String domain = parseDomain(buf, cursor, delimiters);
            return new Mailbox(null, null, localPart, domain);
        } else {
            // should never happen
            return new Mailbox(null, null, openingText, null);
        }
    }

    public Mailbox parseMailbox(final String text) {
        ByteSequence raw = ContentUtil.encode(text);
        ParserCursor cursor = new ParserCursor(0, text.length());
        return parseMailbox(raw, cursor, null);
    }

}
