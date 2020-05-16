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

package org.apache.james.mime4j.field;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentLanguageField;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Represents a <code>Content-Transfer-Encoding</code> field.
 */
public class ContentLanguageFieldLenientImpl extends AbstractField implements ContentLanguageField {

    private final static int   COMMA = ',';
    private final static BitSet DELIM = RawFieldParser.INIT_BITSET(COMMA);

    private boolean parsed = false;
    private List<String> languages;

    ContentLanguageFieldLenientImpl(final Field rawField, final DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        languages = new ArrayList<String>();
        RawField f = getRawField();
        ByteSequence buf = f.getRaw();
        int pos = f.getDelimiterIdx() + 1;
        if (buf == null) {
            String body = f.getBody();
            if (body == null) {
                return;
            }
            buf = ContentUtil.encode(body);
            pos = 0;
        }
        RawFieldParser parser = RawFieldParser.DEFAULT;
        ParserCursor cursor = new ParserCursor(pos, buf.length());
        for (;;) {
            String token = parser.parseToken(buf, cursor, DELIM);
            if (token.length() > 0) {
                languages.add(token);
            }
            if (cursor.atEnd()) {
                break;
            } else {
                pos = cursor.getPos();
                if (buf.byteAt(pos) == COMMA) {
                    cursor.updatePos(pos + 1);
                }
            }
        }
    }

    public List<String> getLanguages() {
        if (!parsed) {
            parse();
        }
        return new ArrayList<String>(languages);
    }

    public static final FieldParser<ContentLanguageField> PARSER = new FieldParser<ContentLanguageField>() {

        public ContentLanguageField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentLanguageFieldLenientImpl(rawField, monitor);
        }

    };

}

