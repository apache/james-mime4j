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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentLanguageField;
import org.apache.james.mime4j.field.language.parser.ContentLanguageParser;
import org.apache.james.mime4j.field.language.parser.ParseException;
import org.apache.james.mime4j.stream.Field;

/**
 * Represents a <code>Content-Transfer-Encoding</code> field.
 */
public class ContentLanguageFieldImpl extends AbstractField implements ContentLanguageField {

    private boolean parsed = false;
    private List<String> languages;
    private ParseException parseException;

    ContentLanguageFieldImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        languages = Collections.<String>emptyList();
        String body = getBody();
        if (body != null) {
            ContentLanguageParser parser = new ContentLanguageParser(new StringReader(body));
            try {
                languages = parser.parse();
            } catch (ParseException ex) {
                parseException = ex;
            }
        }
    }

    @Override
    public org.apache.james.mime4j.dom.field.ParseException getParseException() {
        return parseException;
    }

    public List<String> getLanguages() {
        if (!parsed) {
            parse();
        }
        return new ArrayList<String>(languages);
    }

    public static final FieldParser<ContentLanguageField> PARSER = new FieldParser<ContentLanguageField>() {
        
        public ContentLanguageField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentLanguageFieldImpl(rawField, monitor);
        }
        
    };

}

