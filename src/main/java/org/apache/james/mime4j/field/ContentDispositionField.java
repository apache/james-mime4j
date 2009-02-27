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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;
import org.apache.james.mime4j.field.contentdisposition.parser.TokenMgrError;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.util.ByteSequence;

/**
 * Represents a <code>Content-Disposition</code> field.
 */
public class ContentDispositionField extends AbstractField {
    private static Log log = LogFactory.getLog(ContentDispositionField.class);

    /** The <code>inline</code> disposition type. */
    public static final String DISPOSITION_TYPE_INLINE = "inline";

    /** The <code>attachment</code> disposition type. */
    public static final String DISPOSITION_TYPE_ATTACHMENT = "attachment";

    /** The name of the <code>filename</code> parameter. */
    public static final String PARAM_FILENAME = "filename";

    /** The name of the <code>creation-date</code> parameter. */
    public static final String PARAM_CREATION_DATE = "creation-date";

    /** The name of the <code>modification-date</code> parameter. */
    public static final String PARAM_MODIFICATION_DATE = "modification-date";

    /** The name of the <code>read-date</code> parameter. */
    public static final String PARAM_READ_DATE = "read-date";

    /** The name of the <code>size</code> parameter. */
    public static final String PARAM_SIZE = "size";

    private boolean parsed = false;

    private String dispositionType = "";
    private Map<String, String> parameters = new HashMap<String, String>();
    private ParseException parseException;

    private boolean creationDateParsed;
    private Date creationDate;

    private boolean modificationDateParsed;
    private Date modificationDate;

    private boolean readDateParsed;
    private Date readDate;

    ContentDispositionField(String name, String body, ByteSequence raw) {
        super(name, body, raw);
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
     * Gets the disposition type defined in this Content-Disposition field.
     * 
     * @return the disposition type or an empty string if not set.
     */
    public String getDispositionType() {
        if (!parsed)
            parse();

        return dispositionType;
    }

    /**
     * Gets the value of a parameter. Parameter names are case-insensitive.
     * 
     * @param name
     *            the name of the parameter to get.
     * @return the parameter value or <code>null</code> if not set.
     */
    public String getParameter(String name) {
        if (!parsed)
            parse();

        return parameters.get(name.toLowerCase());
    }

    /**
     * Gets all parameters.
     * 
     * @return the parameters.
     */
    public Map<String, String> getParameters() {
        if (!parsed)
            parse();

        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Determines if the disposition type of this field matches the given one.
     * 
     * @param dispositionType
     *            the disposition type to match against.
     * @return <code>true</code> if the disposition type of this field
     *         matches, <code>false</code> otherwise.
     */
    public boolean isDispositionType(String dispositionType) {
        if (!parsed)
            parse();

        return this.dispositionType.equalsIgnoreCase(dispositionType);
    }

    /**
     * Return <code>true</code> if the disposition type of this field is
     * <i>inline</i>, <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the disposition type of this field is
     *         <i>inline</i>, <code>false</code> otherwise.
     */
    public boolean isInline() {
        if (!parsed)
            parse();

        return dispositionType.equals(DISPOSITION_TYPE_INLINE);
    }

    /**
     * Return <code>true</code> if the disposition type of this field is
     * <i>attachment</i>, <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the disposition type of this field is
     *         <i>attachment</i>, <code>false</code> otherwise.
     */
    public boolean isAttachment() {
        if (!parsed)
            parse();

        return dispositionType.equals(DISPOSITION_TYPE_ATTACHMENT);
    }

    /**
     * Gets the value of the <code>filename</code> parameter if set.
     * 
     * @return the <code>filename</code> parameter value or <code>null</code>
     *         if not set.
     */
    public String getFilename() {
        return getParameter(PARAM_FILENAME);
    }

    /**
     * Gets the value of the <code>creation-date</code> parameter if set and
     * valid.
     * 
     * @return the <code>creation-date</code> parameter value or
     *         <code>null</code> if not set or invalid.
     */
    public Date getCreationDate() {
        if (!creationDateParsed) {
            creationDate = parseDate(PARAM_CREATION_DATE);
            creationDateParsed = true;
        }

        return creationDate;
    }

    /**
     * Gets the value of the <code>modification-date</code> parameter if set
     * and valid.
     * 
     * @return the <code>modification-date</code> parameter value or
     *         <code>null</code> if not set or invalid.
     */
    public Date getModificationDate() {
        if (!modificationDateParsed) {
            modificationDate = parseDate(PARAM_MODIFICATION_DATE);
            modificationDateParsed = true;
        }

        return modificationDate;
    }

    /**
     * Gets the value of the <code>read-date</code> parameter if set and
     * valid.
     * 
     * @return the <code>read-date</code> parameter value or <code>null</code>
     *         if not set or invalid.
     */
    public Date getReadDate() {
        if (!readDateParsed) {
            readDate = parseDate(PARAM_READ_DATE);
            readDateParsed = true;
        }

        return readDate;
    }

    /**
     * Gets the value of the <code>size</code> parameter if set and valid.
     * 
     * @return the <code>size</code> parameter value or <code>-1</code> if
     *         not set or invalid.
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
            if (log.isDebugEnabled()) {
                log.debug("Parsing " + paramName + " null");
            }
            return null;
        }

        try {
            return new DateTimeParser(new StringReader(value)).parseAll()
                    .getDate();
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing " + paramName + " '" + value + "': "
                        + e.getMessage());
            }
            return null;
        } catch (org.apache.james.mime4j.field.datetime.parser.TokenMgrError e) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing " + paramName + " '" + value + "': "
                        + e.getMessage());
            }
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
            if (log.isDebugEnabled()) {
                log.debug("Parsing value '" + body + "': " + e.getMessage());
            }
            parseException = e;
        } catch (TokenMgrError e) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing value '" + body + "': " + e.getMessage());
            }
            parseException = new ParseException(e.getMessage());
        }

        final String dispositionType = parser.getDispositionType();

        if (dispositionType != null) {
            this.dispositionType = dispositionType.toLowerCase(Locale.US);

            List<String> paramNames = parser.getParamNames();
            List<String> paramValues = parser.getParamValues();

            if (paramNames != null && paramValues != null) {
                final int len = Math.min(paramNames.size(), paramValues.size());
                for (int i = 0; i < len; i++) {
                    String paramName = paramNames.get(i).toLowerCase(Locale.US);
                    String paramValue = paramValues.get(i);
                    parameters.put(paramName, paramValue);
                }
            }
        }

        parsed = true;
    }

    static final FieldParser PARSER = new FieldParser() {
        public ParsedField parse(final String name, final String body,
                final ByteSequence raw) {
            return new ContentDispositionField(name, body, raw);
        }
    };
}
