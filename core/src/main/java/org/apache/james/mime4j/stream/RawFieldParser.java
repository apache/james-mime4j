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

package org.apache.james.mime4j.stream;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * The basic immutable MIME field.
 */
public class RawFieldParser {

    static final int[] COLON                   = { ':' };
    static final int[] EQUAL_OR_SEMICOLON      = { '=', ';' };
    static final int[] SEMICOLON               = { ';' };

    public static final RawFieldParser DEFAULT = new RawFieldParser();

    public RawField parseField(final ByteSequence raw) throws MimeException {
        if (raw == null) {
            return null;
        }
        ParserCursor cursor = new ParserCursor(0, raw.length());
        String name = parseToken(raw, cursor, COLON);
        if (cursor.atEnd()) {
            throw new MimeException("Invalid MIME field: no name/value separator found: " +
                    raw.toString());
        }
        return new RawField(raw, cursor.getPos(), name, null);
    }

    public RawBody parseRawBody(final RawField field) {
        ByteSequence buf = field.getRaw();
        int pos = field.getDelimiterIdx() + 1;
        if (buf == null) {
            String body = field.getBody();
            if (body == null) {
                return new RawBody("", null);
            }
            buf = ContentUtil.encode(body);
            pos = 0;
        }
        ParserCursor cursor = new ParserCursor(pos, buf.length());
        return parseRawBody(buf, cursor);
    }

    RawBody parseRawBody(final ByteSequence buf, final ParserCursor cursor) {
        String value = parseToken(buf, cursor, SEMICOLON);
        if (cursor.atEnd()) {
            return new RawBody(value, new ArrayList<NameValuePair>());
        }
        cursor.updatePos(cursor.getPos() + 1);
        List<NameValuePair> params = parseParameters(buf, cursor);
        return new RawBody(value, params);
    }

    List<NameValuePair> parseParameters(final ByteSequence buf, final ParserCursor cursor) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        skipWhiteSpace(buf, cursor);
        while (!cursor.atEnd()) {
            NameValuePair param = parseParameter(buf, cursor);
            params.add(param);
        }
        return params;
    }

    NameValuePair parseParameter(final ByteSequence buf, final ParserCursor cursor) {
        String name = parseToken(buf, cursor, EQUAL_OR_SEMICOLON);
        if (cursor.atEnd()) {
            return new NameValuePair(name, null);
        }
        int delim = buf.byteAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim == ';') {
            return new NameValuePair(name, null);
        }
        String value = parseValue(buf, cursor, SEMICOLON);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        return new NameValuePair(name, value);
    }

    static boolean isOneOf(final int ch, final int[] chs) {
        if (chs != null) {
            for (int i = 0; i < chs.length; i++) {
                if (ch == chs[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    static String parseToken(final ByteSequence buf, final ParserCursor cursor, final int[] delimiters) {
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (isOneOf(current, delimiters)) {
                break;
            } else if (CharsetUtil.isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
                whitespace = true;
            } else if (current == '(') {
                skipComment(buf, cursor);
            } else {
                if (dst.length() > 0 && whitespace) {
                    dst.append(' ');
                }
                copyContent(buf, cursor, delimiters, dst);
                whitespace = false;
            }
        }
        return dst.toString();
    }

    static String parseValue(final ByteSequence buf, final ParserCursor cursor, final int[] delimiters) {
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (isOneOf(current, delimiters)) {
                break;
            } else if (CharsetUtil.isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
                whitespace = true;
            } else if (current == '(') {
                skipComment(buf, cursor);
            } else if (current == '\"') {
                if (dst.length() > 0 && whitespace) {
                    dst.append(' ');
                }
                copyQuotedContent(buf, cursor, dst);
                whitespace = false;
            } else {
                if (dst.length() > 0 && whitespace) {
                    dst.append(' ');
                }
                copyContent(buf, cursor, delimiters, dst);
                whitespace = false;
            }
        }
        return dst.toString();
    }

    static void skipWhiteSpace(final ByteSequence buf, final ParserCursor cursor) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; i++) {
            char current = (char) (buf.byteAt(i) & 0xff);
            if (!CharsetUtil.isWhitespace(current)) {
                break;
            } else {
                pos++;
            }
        }
        cursor.updatePos(pos);
    }

    static void skipComment(final ByteSequence buf, final ParserCursor cursor) {
        if (cursor.atEnd()) {
            return;
        }
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current != '(') {
            return;
        }
        pos++;
        indexFrom++;

        int level = 1;
        boolean escaped = false;
        for (int i = indexFrom; i < indexTo; i++, pos++) {
            current = (char) (buf.byteAt(i) & 0xff);
            if (escaped) {
                escaped = false;
            } else {
                if (current == '\\') {
                    escaped = true;
                } else if (current == '(') {
                    level++;
                } else if (current == ')') {
                    level--;
                }
            }
            if (level <= 0) {
                pos++;
                break;
            }
        }
        cursor.updatePos(pos);
    }

    static void copyContent(final ByteSequence buf, final ParserCursor cursor, final int[] delimiters,
            final StringBuilder dst) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; i++) {
            char current = (char) (buf.byteAt(i) & 0xff);
            if (isOneOf(current, delimiters) || CharsetUtil.isWhitespace(current) || current == '(') {
                break;
            } else {
                pos++;
                dst.append(current);
            }
        }
        cursor.updatePos(pos);
    }

    static void copyQuotedContent(final ByteSequence buf, final ParserCursor cursor,
            final StringBuilder dst) {
        if (cursor.atEnd()) {
            return;
        }
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        char current = (char) (buf.byteAt(pos) & 0xff);
        if (current != '\"') {
            return;
        }
        pos++;
        indexFrom++;
        boolean escaped = false;
        for (int i = indexFrom; i < indexTo; i++, pos++) {
            current = (char) (buf.byteAt(i) & 0xff);
            if (escaped) {
                if (current != '\"' && current != '\\') {
                    dst.append('\\');
                }
                dst.append(current);
                escaped = false;
            } else {
                if (current == '\"') {
                    pos++;
                    break;
                }
                if (current == '\\') {
                    escaped = true;
                } else {
                    dst.append(current);
                }
            }
        }
        cursor.updatePos(pos);
    }

}
