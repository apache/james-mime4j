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
import java.util.BitSet;
import java.util.List;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * <p>
 * Low level parser for header field elements. The parsing routines of this class are designed
 * to produce near zero intermediate garbage and make no intermediate copies of input data.
 * </p>
 * <p>
 * This class is immutable and thread safe.
 * </p>
 */
public class RawFieldParser {

    public static BitSet INIT_BITSET(int ... b) {
        BitSet bitset = new BitSet(b.length);
        for (int aB : b) {
            bitset.set(aB);
        }
        return bitset;
    }

    static final BitSet COLON                   = INIT_BITSET(':');
    static final BitSet EQUAL_OR_SEMICOLON      = INIT_BITSET('=', ';');
    static final BitSet SEMICOLON               = INIT_BITSET(';');

    public static final RawFieldParser DEFAULT = new RawFieldParser();

    /**
     * Parses the sequence of bytes into {@link RawField}.
     *
     * @throws MimeException if the input data does not contain a valid MIME field.
     */
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

    /**
     * Parses the field body containing a value with parameters into {@link RawBody}.
     *
     * @param field unstructured (raw) field
     */
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

    /**
     * Parses the sequence of bytes containing a value with parameters into {@link RawBody}.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    public RawBody parseRawBody(final ByteSequence buf, final ParserCursor cursor) {
        String value = parseToken(buf, cursor, SEMICOLON);
        if (cursor.atEnd()) {
            return new RawBody(value, new ArrayList<NameValuePair>());
        }
        cursor.updatePos(cursor.getPos() + 1);
        List<NameValuePair> params = parseParameters(buf, cursor);
        return new RawBody(value, params);
    }

    /**
     * Parses the sequence of bytes containing field parameters delimited with semicolon into
     * a list of {@link NameValuePair}s.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    public List<NameValuePair> parseParameters(final ByteSequence buf, final ParserCursor cursor) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        skipWhiteSpace(buf, cursor);
        while (!cursor.atEnd()) {
            NameValuePair param = parseParameter(buf, cursor);
            params.add(param);
        }
        return params;
    }

    /**
     * Parses the sequence of bytes containing a field parameter delimited with semicolon into
     * {@link NameValuePair}.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    public NameValuePair parseParameter(final ByteSequence buf, final ParserCursor cursor) {
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

    /**
     * Extracts from the sequence of bytes a token terminated with any of the given delimiters
     * discarding semantically insignificant whitespace characters and comments.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be <code>null</code> if the token
     *  is not delimited by any character.
     */
    public String parseToken(final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (delimiters != null && delimiters.get(current)) {
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

    /**
     * Extracts from the sequence of bytes a value which can be enclosed in quote marks and
     * terminated with any of the given delimiters discarding semantically insignificant
     * whitespace characters and comments.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be <code>null</code> if the value
     *  is not delimited by any character.
     */
    public String parseValue(final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters) {
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (delimiters != null && delimiters.get(current)) {
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
                copyUnquotedContent(buf, cursor, delimiters, dst);
                whitespace = false;
            }
        }
        return dst.toString();
    }

    /**
     * Skips semantically insignificant whitespace characters and moves the cursor to the closest
     * non-whitespace character.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    public void skipWhiteSpace(final ByteSequence buf, final ParserCursor cursor) {
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

    /**
     * Skips semantically insignificant content if the current position is positioned at the
     * beginning of a comment and moves the cursor past the end of the comment.
     * Nested comments and escaped characters are recognized and handled appropriately.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    public void skipComment(final ByteSequence buf, final ParserCursor cursor) {
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

    /**
     * Skips semantically insignificant whitespace characters and comments and moves the cursor
     * to the closest semantically significant non-whitespace character.
     * Nested comments and escaped characters are recognized and handled appropriately.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    public void skipAllWhiteSpace(final ByteSequence buf, final ParserCursor cursor) {
        while (!cursor.atEnd()) {
            char current = (char) (buf.byteAt(cursor.getPos()) & 0xff);
            if (CharsetUtil.isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
            } else if (current == '(') {
                skipComment(buf, cursor);
            } else {
                break;
            }
        }
    }

    /**
     * Transfers content into the destination buffer until a whitespace character, a comment,
     * or any of the given delimiters is encountered.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be <code>null</code> if the value
     *  is delimited by a whitespace or a comment only.
     * @param dst destination buffer
     */
    public void copyContent(final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters,
            final StringBuilder dst) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; i++) {
            char current = (char) (buf.byteAt(i) & 0xff);
            if ((delimiters != null && delimiters.get(current))
                    || CharsetUtil.isWhitespace(current) || current == '(') {
                break;
            } else {
                pos++;
                dst.append(current);
            }
        }
        cursor.updatePos(pos);
    }

    /**
     * Transfers content into the destination buffer until a whitespace character, a comment,
     * a quote, or any of the given delimiters is encountered.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be <code>null</code> if the value
     *  is delimited by a whitespace, a quote or a comment only.
     * @param dst destination buffer
     */
    public void copyUnquotedContent(final ByteSequence buf, final ParserCursor cursor, final BitSet delimiters,
                            final StringBuilder dst) {
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; i++) {
            char current = (char) (buf.byteAt(i) & 0xff);
            if ((delimiters != null && delimiters.get(current))
                    || CharsetUtil.isWhitespace(current) || current == '(' || current == '\"') {
                break;
            } else {
                pos++;
                dst.append(current);
            }
        }
        cursor.updatePos(pos);
    }

    /**
     * Transfers content enclosed with quote marks into the destination buffer.
     *
     * @param buf buffer with the sequence of bytes to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param dst destination buffer
     */
    public void copyQuotedContent(final ByteSequence buf, final ParserCursor cursor,
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
                } else if (current != '\r' && current != '\n') {
                    dst.append(current);
                }
            }
        }
        cursor.updatePos(pos);
    }

}
