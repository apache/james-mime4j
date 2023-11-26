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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;
import org.apache.james.mime4j.field.contentdisposition.parser.ParseException;
import org.apache.james.mime4j.field.contentdisposition.parser.TokenMgrError;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.stream.Field;

/**
 * Represents a <code>Content-Disposition</code> field.
 */
public class ContentDispositionFieldImpl extends AbstractField implements ContentDispositionField {

    private boolean parsed = false;

    private String dispositionType = "";
    private final Map<String, String> parameters = new HashMap<String, String>();
    private ParseException parseException;

    private boolean creationDateParsed;
    private Date creationDate;

    private boolean modificationDateParsed;
    private Date modificationDate;

    private boolean readDateParsed;
    private Date readDate;

    ContentDispositionFieldImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    /**
     * Gets the exception that was raised during parsing of the field value, if
     * any; otherwise, null.
     */
    @Override
    public ParseException getParseException() {
        if (!parsed)
            parse();

        return parseException;
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getDispositionType()
     */
    public String getDispositionType() {
        if (!parsed)
            parse();

        return dispositionType;
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        if (!parsed)
            parse();

        return parameters.get(name.toLowerCase());
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getParameters()
     */
    public Map<String, String> getParameters() {
        if (!parsed)
            parse();

        return Collections.unmodifiableMap(parameters);
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#isDispositionType(java.lang.String)
     */
    public boolean isDispositionType(String dispositionType) {
        if (!parsed)
            parse();

        return this.dispositionType.equalsIgnoreCase(dispositionType);
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#isInline()
     */
    public boolean isInline() {
        if (!parsed)
            parse();

        return dispositionType.equals(DISPOSITION_TYPE_INLINE);
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#isAttachment()
     */
    public boolean isAttachment() {
        if (!parsed)
            parse();

        return dispositionType.equals(DISPOSITION_TYPE_ATTACHMENT);
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getFilename()
     */
    public String getFilename() {
        return getParameter(PARAM_FILENAME);
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getCreationDate()
     */
    public Date getCreationDate() {
        if (!creationDateParsed) {
            creationDate = parseDate(PARAM_CREATION_DATE);
            creationDateParsed = true;
        }

        return creationDate;
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getModificationDate()
     */
    public Date getModificationDate() {
        if (!modificationDateParsed) {
            modificationDate = parseDate(PARAM_MODIFICATION_DATE);
            modificationDateParsed = true;
        }

        return modificationDate;
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getReadDate()
     */
    public Date getReadDate() {
        if (!readDateParsed) {
            readDate = parseDate(PARAM_READ_DATE);
            readDateParsed = true;
        }

        return readDate;
    }

    /**
     * @see org.apache.james.mime4j.dom.field.ContentDispositionField#getSize()
     */
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

    private Date parseDate(String paramName) {
        String value = getParameter(paramName);
        if (value == null) {
            monitor.warn("Parsing " + paramName + " null", "returning null");
            return null;
        }

        try {
            return new DateTimeParser(new StringReader(value)).parseAll()
                    .getDate();
        } catch (org.apache.james.mime4j.field.datetime.parser.ParseException e) {
            if (monitor.isListening()) {
                monitor.warn(paramName + " parameter is invalid: " + value,
                        paramName + " parameter is ignored");
            }
            return null;
        } catch (TokenMgrError e) {
            monitor.warn(paramName + " parameter is invalid: " + value,
                    paramName + "parameter is ignored");
            return null;
        }
    }

    private void parse() {
        String body = getBody();

        ContentDispositionParser parser = new ContentDispositionParser(
                new StringReader(body));
        try {
            parser.parseAll();
        } catch (ParseException e) {
            parseException = e;
        } catch (TokenMgrError e) {
            parseException = new ParseException(e);
        }

        final String dispositionType = parser.getDispositionType();

        if (dispositionType != null) {
            this.dispositionType = dispositionType.toLowerCase(Locale.US);
            this.parameters.putAll(parser.getParameters());
        }
        parsed = true;
    }

    public static final FieldParser<ContentDispositionField> PARSER = new FieldParser<ContentDispositionField>() {

        public ContentDispositionField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentDispositionFieldImpl(rawField, monitor);
        }

    };
}
