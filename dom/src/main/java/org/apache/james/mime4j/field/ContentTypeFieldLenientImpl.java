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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;
import org.apache.james.mime4j.stream.RawBody;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;

/**
 * Represents a <code>Content-Type</code> field.
 */
public class ContentTypeFieldLenientImpl extends AbstractField implements ContentTypeField {

    private boolean parsed = false;

    private String mimeType = null;
    private String mediaType = null;
    private String subType = null;
    private Map<String, String> parameters = new HashMap<String, String>();

    ContentTypeFieldLenientImpl(final Field rawField, final DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    public String getMimeType() {
        if (!parsed) {
            parse();
        }
        return mimeType;
    }

    public String getMediaType() {
        if (!parsed) {
            parse();
        }
        return mediaType;
    }

    public String getSubType() {
        if (!parsed) {
            parse();
        }
        return subType;
    }

    public String getParameter(String name) {
        if (!parsed) {
            parse();
        }
        return parameters.get(name.toLowerCase());
    }

    public Map<String, String> getParameters() {
        if (!parsed) {
            parse();
        }
        return Collections.unmodifiableMap(parameters);
    }

    public boolean isMimeType(String mimeType) {
        if (!parsed) {
            parse();
        }
        return this.mimeType != null && this.mimeType.equalsIgnoreCase(mimeType);
    }

    public boolean isMultipart() {
        if (!parsed) {
            parse();
        }
        return this.mimeType != null && mimeType.startsWith(TYPE_MULTIPART_PREFIX);
    }

    public String getBoundary() {
        return getParameter(PARAM_BOUNDARY);
    }

    public String getCharset() {
        return getParameter(PARAM_CHARSET);
    }

    private void parse() {
        parsed = true;
        RawField f = getRawField();
        RawBody body = RawFieldParser.DEFAULT.parseRawBody(f);
        String main = body.getValue();
        String type = null;
        String subtype = null;
        if (main != null) {
            main = main.toLowerCase().trim();
            int index = main.indexOf('/');
            boolean valid = false;
            if (index != -1) {
                type = main.substring(0, index).trim();
                subtype = main.substring(index + 1).trim();
                if (type.length() > 0 && subtype.length() > 0) {
                    main = type + "/" + subtype;
                    valid = true;
                }
            }
            if (!valid) {
                if (monitor.isListening()) {
                    monitor.warn("Invalid Content-Type: " + body, "Content-Type value ignored");
                }
                main = null;
                type = null;
                subtype = null;
            }
        }
        mimeType = main;
        mediaType = type;
        subType = subtype;
        parameters.clear();
        for (NameValuePair nmp: body.getParams()) {
            String name = nmp.getName().toLowerCase(Locale.US);
            parameters.put(name, nmp.getValue());
        }
    }

    public static final FieldParser<ContentTypeField> PARSER = new FieldParser<ContentTypeField>() {

        public ContentTypeField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentTypeFieldLenientImpl(rawField, monitor);
        }

    };

}
