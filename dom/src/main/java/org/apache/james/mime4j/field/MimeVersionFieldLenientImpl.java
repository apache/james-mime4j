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

import java.util.BitSet;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.FieldParser;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Represents a <code>MIME-Version</code> field.
 */
public class MimeVersionFieldLenientImpl extends AbstractField implements MimeVersionField {

    private final static int FULL_STOP  = '.';
    private final static BitSet DELIM = RawFieldParser.INIT_BITSET(FULL_STOP);
    
    public static final int DEFAULT_MINOR_VERSION = 0;
    public static final int DEFAULT_MAJOR_VERSION = 1;
    
    private boolean parsed = false;
    private int major = DEFAULT_MAJOR_VERSION;
    private int minor = DEFAULT_MINOR_VERSION;

    MimeVersionFieldLenientImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        major = DEFAULT_MAJOR_VERSION;
        minor = DEFAULT_MINOR_VERSION;
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
        String token1 = parser.parseValue(buf, cursor, DELIM);
        try {
            major = Integer.parseInt(token1);
            if (major < 0) {
                major = 0;
            }
        } catch (NumberFormatException ex) {
        }
        if (!cursor.atEnd() && buf.byteAt(cursor.getPos()) == FULL_STOP) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        String token2 = parser.parseValue(buf, cursor, null);
        try {
            minor = Integer.parseInt(token2);
            if (minor < 0) {
                minor = 0;
            }
        } catch (NumberFormatException ex) {
        }
    }

    public int getMinorVersion() {
        if (!parsed) {
            parse();
        }
        return minor;
    }

    public int getMajorVersion() {
        if (!parsed) {
            parse();
        }
        return major;
    }

    public static final FieldParser<MimeVersionField> PARSER = new FieldParser<MimeVersionField>() {
        
        public MimeVersionField parse(final Field rawField, final DecodeMonitor monitor) {
            return new MimeVersionFieldLenientImpl(rawField, monitor);
        }
        
    };
    
}
