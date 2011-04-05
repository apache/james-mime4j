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
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * The basic immutable MIME field.
 */
public class RawFieldParser {

    static final int COLON   = ':';
    static final int SPACE   = 0x20;
    static final int TAB     = 0x09;
    static final int CR      = 0x0d;
    static final int LF      = 0x0a;
    
    private static final BitSet FIELD_CHARS = new BitSet();
    
    public static final RawFieldParser DEFAULT = new RawFieldParser(); 

    static {
        for (int i = 0x21; i <= 0x39; i++) {
            FIELD_CHARS.set(i);
        }
        for (int i = 0x3b; i <= 0x7e; i++) {
            FIELD_CHARS.set(i);
        }
    }

    public RawField parseField(final ByteSequence raw) throws MimeException {
        int len = raw.length();
        int colonIdx = -1;
        int headerNameEndIdx = -1;
        boolean obsolete = false;
        for (int i = 0; i < len; i++) {
            if (!FIELD_CHARS.get(raw.byteAt(i) & 0xff)) {
                headerNameEndIdx = i;
                for (; i < len; i++) {
                    int j = raw.byteAt(i) & 0xff;
                    if (j == COLON) {
                        colonIdx = i;
                        break;
                    } else if (j != SPACE && j != TAB) {
                        throw new MimeException("Invalid header: unexpected char " + j + " after colon");
                    } else {
                        obsolete = true;
                    }
                }
                break;
            }
        }
        if (colonIdx == -1) {
            throw new MimeException("Invalid header: no colon found");
        }
        // make sure we ignore ending WSP (obsolete rfc822 syntax)
        String name = ContentUtil.decode(raw, 0, headerNameEndIdx);
        return new RawField(raw, colonIdx, obsolete, name, null);
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
    
    static final int[] DELIMS = { ';' };

    RawBody parseRawBody(final ByteSequence buf, final ParserCursor cursor) {
        int pos = cursor.getPos();
        int indexFrom = pos;
        int indexTo = cursor.getUpperBound();
        while (pos < indexTo) {
            int ch = buf.byteAt(pos);
            if (isOneOf(ch, DELIMS)) {
                break;
            }
            pos++;
        }
        String value = copyTrimmed(buf, indexFrom, pos);
        if (pos == indexTo) {
            cursor.updatePos(pos);
            return new RawBody(value, null);
        }
        cursor.updatePos(pos + 1);
        List<NameValuePair> params = parseParameters(buf, cursor);
        return new RawBody(value, params);
    }
    
    List<NameValuePair> parseParameters(final ByteSequence buf, final ParserCursor cursor) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        int pos = cursor.getPos();
        int indexTo = cursor.getUpperBound();

        while (pos < indexTo) {
            int ch = buf.byteAt(pos);
            if (isWhitespace(ch)) {
                pos++;
            } else {
                break;
            }
        }
        cursor.updatePos(pos);
        if (cursor.atEnd()) {
            return params;
        }

        while (!cursor.atEnd()) {
            NameValuePair param = parseParameter(buf, cursor, DELIMS);
            params.add(param);
        }
        return params;
    }

    NameValuePair parseParameter(final ByteSequence buf, final ParserCursor cursor) {
        return parseParameter(buf, cursor, DELIMS);
    }
    
    NameValuePair parseParameter(final ByteSequence buf, final ParserCursor cursor, final int[] delimiters) {
        boolean terminated = false;

        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();

        // Find name
        String name = null;
        while (pos < indexTo) {
            int ch = buf.byteAt(pos);
            if (ch == '=') {
                break;
            }
            if (isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            pos++;
        }

        if (pos == indexTo) {
            terminated = true;
            name = copyTrimmed(buf, indexFrom, indexTo);
        } else {
            name = copyTrimmed(buf, indexFrom, pos);
            pos++;
        }

        if (terminated) {
            cursor.updatePos(pos);
            return new NameValuePair(name, null, false);
        }

        // Find value
        String value = null;
        int i1 = pos;

        boolean qouted = false;
        boolean escaped = false;
        while (pos < indexTo) {
            int ch = buf.byteAt(pos);
            if (ch == '"' && !escaped) {
                qouted = !qouted;
            }
            if (!qouted && !escaped && isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            if (escaped) {
                escaped = false;
            } else {
                escaped = qouted && ch == '\\';
            }
            pos++;
        }

        int i2 = pos;
        // Trim leading white spaces
        while (i1 < i2 && (isWhitespace(buf.byteAt(i1)))) {
            i1++;
        }
        // Trim trailing white spaces
        while ((i2 > i1) && (isWhitespace(buf.byteAt(i2 - 1)))) {
            i2--;
        }
        boolean quoted = false;
        // Strip away quotes if necessary
        if (((i2 - i1) >= 2)
            && (buf.byteAt(i1) == '"')
            && (buf.byteAt(i2 - 1) == '"')) {
            quoted = true;
            i1++;
            i2--;
        }
        if (quoted) {
            value = copyEscaped(buf, i1, i2);
        } else {
            value = copy(buf, i1, i2);
        }
        if (terminated) {
            pos++;
        }
        cursor.updatePos(pos);
        return new NameValuePair(name, value, quoted);
    }
    
    private static boolean isOneOf(final int ch, final int[] chs) {
        if (chs != null) {
            for (int i = 0; i < chs.length; i++) {
                if (ch == chs[i]) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isWhitespace(int i) {
        return i == SPACE || i == TAB || i == CR || i == LF;
    }
    
    private static String copy(final ByteSequence buf, int beginIndex, int endIndex) {
        return ContentUtil.decode(buf, beginIndex, endIndex - beginIndex);
    }

    private static String copyTrimmed(final ByteSequence buf, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && isWhitespace(buf.byteAt(beginIndex))) {
            beginIndex++;
        }
        while (endIndex > beginIndex && isWhitespace(buf.byteAt(endIndex - 1))) {
            endIndex--;
        }
        return ContentUtil.decode(buf, beginIndex, endIndex - beginIndex);
    }

    private static String copyEscaped(final ByteSequence buf, int beginIndex, int endIndex) {
        ByteArrayBuffer copy = new ByteArrayBuffer(buf.length());
        boolean escaped = false;
        for (int i = beginIndex; i < endIndex; i++) {
            int b = buf.byteAt(i);
            if (escaped) {
                if (b != '\"' && b != '\\') {
                    copy.append('\\');
                }
                copy.append(b);
                escaped = false;
            } else {
                if (b == '\\') {
                    escaped = true;
                } else {
                    copy.append(b);
                }
            }
        }
        return ContentUtil.decode(copy, 0, copy.length());
    }

}
