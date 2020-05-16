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

import java.io.StringReader;

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.field.mimeversion.parser.MimeVersionParser;
import org.apache.james.mime4j.field.mimeversion.parser.ParseException;

/**
 * Represents a <code>MIME-Version</code> field.
 */
public class MimeVersionFieldImpl extends AbstractField implements MimeVersionField {

    public static final int DEFAULT_MINOR_VERSION = 0;
    public static final int DEFAULT_MAJOR_VERSION = 1;

    private boolean parsed = false;
    private int major = DEFAULT_MAJOR_VERSION;
    private int minor = DEFAULT_MINOR_VERSION;
    private ParseException parsedException;

    MimeVersionFieldImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        major = DEFAULT_MAJOR_VERSION;
        minor = DEFAULT_MINOR_VERSION;
        String body = getBody();
        if (body != null) {
            StringReader reader = new StringReader(body);
            MimeVersionParser parser = new MimeVersionParser(reader);
            try {
                parser.parse();
                int v = parser.getMajorVersion();
                if (v != MimeVersionParser.INITIAL_VERSION_VALUE) {
                    major = v;
                }
                v = parser.getMinorVersion();
                if (v != MimeVersionParser.INITIAL_VERSION_VALUE) {
                    minor = v;
                }
            } catch (MimeException ex) {
                parsedException = new ParseException(ex);
            }
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

    @Override
    public org.apache.james.mime4j.dom.field.ParseException getParseException() {
        return parsedException;
    }

    public static final FieldParser<MimeVersionField> PARSER = new FieldParser<MimeVersionField>() {

        public MimeVersionField parse(final Field rawField, final DecodeMonitor monitor) {
            return new MimeVersionFieldImpl(rawField, monitor);
        }

    };

}
