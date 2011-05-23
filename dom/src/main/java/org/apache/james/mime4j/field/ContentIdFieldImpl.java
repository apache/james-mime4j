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
import org.apache.james.mime4j.dom.field.ContentIdField;
import org.apache.james.mime4j.stream.FieldParser;
import org.apache.james.mime4j.util.ByteSequence;

/**
 * Represents a <code>Content-Transfer-Encoding</code> field.
 */
public class ContentIdFieldImpl extends AbstractField implements ContentIdField {

    private boolean parsed = false;
    private String id;

    ContentIdFieldImpl(String name, String body, ByteSequence raw, DecodeMonitor monitor) {
        super(name, body, raw, monitor);
    }

    private void parse() {
        parsed = true;
        String body = getBody();
        if (body != null) {
            id = body.trim();
        } else {
            id = null;
        }
    }
    
    public String getId() {
        if (!parsed) {
            parse();
        }
        return id;
    }

    public static final FieldParser<ContentIdField> PARSER = new FieldParser<ContentIdField>() {
        public ContentIdField parse(final String name, final String body,
                final ByteSequence raw, DecodeMonitor monitor) {
            return new ContentIdFieldImpl(name, body, raw, monitor);
        }
    };

}

