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
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentLocationField;
import org.apache.james.mime4j.field.structured.parser.ParseException;
import org.apache.james.mime4j.field.structured.parser.StructuredFieldParser;

/**
 * Represents a <code>Content-Location</code> field.
 */
public class ContentLocationFieldImpl extends AbstractField implements ContentLocationField {

    private boolean parsed = false;
    private String location;
    private ParseException parseException;

    ContentLocationFieldImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        String body = getBody();
        location = null;
        if (body != null) {
            StringReader stringReader = new StringReader(body);
            StructuredFieldParser parser = new StructuredFieldParser(stringReader);
            try {
                // From RFC2017 3.1
                /*
                 * Extraction of the URL string from the URL-parameter is even simpler:
                 * The enclosing quotes and any linear whitespace are removed and the
                 * remaining material is the URL string.
                 * Read more: http://www.faqs.org/rfcs/rfc2017.html#ixzz0aufO9nRL
                 */
                location = parser.parse().replaceAll("\\s", "");
            } catch (ParseException ex) {
                parseException = ex;
            }
        }
    }

    public String getLocation() {
        if (!parsed) {
            parse();
        }
        return location;
    }

    @Override
    public org.apache.james.mime4j.dom.field.ParseException getParseException() {
        return parseException;
    }

    public static final FieldParser<ContentLocationField> PARSER = new FieldParser<ContentLocationField>() {

        public ContentLocationField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentLocationFieldImpl(rawField, monitor);
        }

    };

}

