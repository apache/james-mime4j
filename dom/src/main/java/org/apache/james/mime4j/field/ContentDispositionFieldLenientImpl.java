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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;
import org.apache.james.mime4j.stream.RawBody;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;

/**
 * Represents a <code>Content-Disposition</code> field.
 */
public class ContentDispositionFieldLenientImpl extends AbstractField implements ContentDispositionField {

    private static final String DEFAULT_DATE_FORMAT = "EEE, dd MMM yyyy hh:mm:ss ZZZZ";
    
    private final List<String> datePatterns;
    
    private boolean parsed = false;

    private String dispositionType = "";
    private Map<String, String> parameters = new HashMap<String, String>();

    private boolean creationDateParsed;
    private Date creationDate;

    private boolean modificationDateParsed;
    private Date modificationDate;

    private boolean readDateParsed;
    private Date readDate;

    ContentDispositionFieldLenientImpl(final Field rawField, 
            final Collection<String> dateParsers, final DecodeMonitor monitor) {
        super(rawField, monitor);
        this.datePatterns = new ArrayList<String>();
        if (dateParsers != null) {
            this.datePatterns.addAll(dateParsers);
        } else {
            this.datePatterns.add(DEFAULT_DATE_FORMAT);
        }
    }

    public String getDispositionType() {
        if (!parsed) {
            parse();
        }
        return dispositionType;
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

    public boolean isDispositionType(String dispositionType) {
        if (!parsed) {
            parse();
        }
        return this.dispositionType.equalsIgnoreCase(dispositionType);
    }

    public boolean isInline() {
        if (!parsed) {
            parse();
        }
        return dispositionType.equals(DISPOSITION_TYPE_INLINE);
    }

    public boolean isAttachment() {
        if (!parsed) {
            parse();
        }
        return dispositionType.equals(DISPOSITION_TYPE_ATTACHMENT);
    }

    public String getFilename() {
        return getParameter(PARAM_FILENAME);
    }

    public Date getCreationDate() {
        if (!creationDateParsed) {
            creationDate = parseDate(PARAM_CREATION_DATE);
            creationDateParsed = true;
        }
        return creationDate;
    }

    public Date getModificationDate() {
        if (!modificationDateParsed) {
            modificationDate = parseDate(PARAM_MODIFICATION_DATE);
            modificationDateParsed = true;
        }
        return modificationDate;
    }

    public Date getReadDate() {
        if (!readDateParsed) {
            readDate = parseDate(PARAM_READ_DATE);
            readDateParsed = true;
        }
        return readDate;
    }

    public long getSize() {
        String value = getParameter(PARAM_SIZE);
        if (value == null)
            return -1;

        try {
            long size = Long.parseLong(value);
            return size < 0 ? -1 : size;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void parse() {
        parsed = true;
        RawField f = getRawField();
        RawBody body = RawFieldParser.DEFAULT.parseRawBody(f);
        String main = body.getValue();
        if (main != null) {
            dispositionType = main.toLowerCase(Locale.US);
        } else {
            dispositionType = null;
        }
        parameters.clear();
        for (NameValuePair nmp: body.getParams()) {
            String name = nmp.getName().toLowerCase(Locale.US);
            parameters.put(name, nmp.getValue());
        }
    }

    private Date parseDate(final String paramName) {
        String value = getParameter(paramName);
        if (value == null) {
            return null;
        }
        for (String datePattern: datePatterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(datePattern, Locale.US);
                parser.setTimeZone(TimeZone.getTimeZone("GMT"));
                parser.setLenient(true);
                return parser.parse(value);
            } catch (ParseException ignore) {
            }
        }
        if (monitor.isListening()) {
            monitor.warn(paramName + " parameter is invalid: " + value, 
                    paramName + " parameter is ignored");
        }
        return null;
    }

    public static final FieldParser<ContentDispositionField> PARSER = new FieldParser<ContentDispositionField>() {
        
        public ContentDispositionField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentDispositionFieldLenientImpl(rawField, null, monitor);
        }
        
    };
}
