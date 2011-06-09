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

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.ContentLocationField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.FieldParser;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Represents a <code>Content-Location</code> field.
 */
public class ContentLocationFieldLenientImpl extends AbstractField implements ContentLocationField {

    private final static int[] DELIM = new int[] {};
    
    private boolean parsed = false;
    private String location;

    ContentLocationFieldLenientImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        location = null;
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
        String token = parser.parseValue(buf, cursor, DELIM);
        StringBuilder sb = new StringBuilder(token.length());
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (!CharsetUtil.isWhitespace(ch)) {
                sb.append(ch);
            }
        }
        this.location = sb.toString();
    }
    
    public String getLocation() {
        if (!parsed) {
            parse();
        }
        return location;
    }

    public static final FieldParser<ContentLocationField> PARSER = new FieldParser<ContentLocationField>() {
        
        public ContentLocationField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentLocationFieldLenientImpl(rawField, monitor);
        }
        
    };

}

