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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * The base class of all field classes.
 */
public abstract class AbstractField implements Field {

    private static final String FIELD_NAME_PATTERN = 
        "^([\\x21-\\x39\\x3b-\\x7e]+):";
    private static final Pattern fieldNamePattern = 
        Pattern.compile(FIELD_NAME_PATTERN);
        
    private static final DefaultFieldParser parser = new DefaultFieldParser();
    
    private final String name;
    private final String body;
    private final String raw;
    
    protected AbstractField(final String name, final String body, final String raw) {
        this.name = name;
        this.body = body;
        this.raw = raw;
    }
    
    /**
     * Parses the given string and returns an instance of the 
     * <code>Field</code> class. The type of the class returned depends on
     * the field name:
     * <p>
     * <table>
     *   <tr><th>Class returned</th><th>Field names</th></tr>
     *   <tr><td>{@link ContentTypeField}</td><td>Content-Type</td></tr>
     *   <tr><td>{@link ContentTransferEncodingField}</td><td>Content-Transfer-Encoding</td></tr>
     *   <tr><td>{@link ContentDispositionField}</td><td>Content-Disposition</td></tr>
     *   <tr><td>{@link DateTimeField}</td><td>Date, Resent-Date</td></tr>
     *   <tr><td>{@link MailboxField}</td><td>Sender, Resent-Sender</td></tr>
     *   <tr><td>{@link MailboxListField}</td><td>From, Resent-From</td></tr>
     *   <tr><td>{@link AddressListField}</td><td>To, Cc, Bcc, Reply-To, Resent-To, Resent-Cc, Resent-Bcc</td></tr>
     *   <tr><td>{@link UnstructuredField}</td><td>Subject and others</td></tr>
     * </table>
     * 
     * @param raw the string to parse.
     * @return a <code>Field</code> instance.
     * @throws MimeException if the raw string cannot be split into field name and body.
     * @see #isValidField()
     */
    public static Field parse(final String raw) throws MimeException {
        
        /*
         * Unfold the field.
         */
        final String unfolded = MimeUtil.unfold(raw);
        
        /*
         * Split into name and value.
         */
        final Matcher fieldMatcher = fieldNamePattern.matcher(unfolded);
        if (!fieldMatcher.find()) {
            throw new MimeException("Invalid field in string");
        }
        final String name = fieldMatcher.group(1);
        
        String body = unfolded.substring(fieldMatcher.end());
        if (body.length() > 0 && body.charAt(0) == ' ') {
            body = body.substring(1);
        }
        
        return parser.parse(name, body, raw);
    }

    /**
     * Parses the given field name and field body strings and returns an
     * instance of the <code>Field</code> class. The type of the class
     * returned depends on the field name (see {@link #parse(String)}).
     * <p>
     * This method is convenient for creating or manipulating messages because
     * contrary to {@link #parse(String)} it does not throw a
     * {@link MimeException}.
     * <p>
     * Note that this method does not fold the header field; the specified field
     * body should already have been folded into multiple lines prior to calling
     * this method if folding is desired.
     * 
     * @param name
     *            the field name.
     * @param body
     *            the field body (a.k.a value).
     * @return a <code>Field</code> instance.
     */
    public static Field parse(String name, String body) {
        if (body.length() > 0 && body.charAt(0) == ' ') {
            body = body.substring(1);
        }

        String raw = name + ": " + body;

        // Unfold body
        body = MimeUtil.unfold(body);

        return parser.parse(name, body, raw);
    }

    /**
     * Gets the default parser used to parse fields.
     * @return the default field parser
     */
    public static DefaultFieldParser getParser() {
        return parser;
    }
    
    /**
     * Gets the name of the field (<code>Subject</code>, 
     * <code>From</code>, etc).
     * 
     * @return the field name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the original raw field string.
     * 
     * @return the original raw field string.
     */
    public String getRaw() {
        return raw;
    }
    
    /**
     * Gets the unfolded, unparsed and possibly encoded (see RFC 2047) field 
     * body string.
     * 
     * @return the unfolded unparsed field body string.
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns <code>true</code> if this field is valid, i.e. no errors were
     * encountered while parsing the field value.
     * 
     * @return <code>true</code> if this field is valid, <code>false</code>
     *         otherwise.
     * @see #getParseException()
     */
    public boolean isValidField() {
        return getParseException() == null;
    }

    /**
     * Returns the exception that was thrown by the field parser while parsing
     * the field value. The result is <code>null</code> if the field is valid
     * and no errors were encountered.
     * 
     * @return the exception that was thrown by the field parser or
     *         <code>null</code> if the field is valid.
     */
    public ParseException getParseException() {
        return null;
    }

    /**
     * Determines if this is a <code>Content-Type</code> field.
     * 
     * @return <code>true</code> if this is a <code>Content-Type</code> field,
     *         <code>false</code> otherwise.
     */
    public boolean isContentType() {
        return FieldName.CONTENT_TYPE.equalsIgnoreCase(name);
    }
    
    /**
     * Determines if this is a <code>Subject</code> field.
     * 
     * @return <code>true</code> if this is a <code>Subject</code> field,
     *         <code>false</code> otherwise.
     */
    public boolean isSubject() {
        return FieldName.SUBJECT.equalsIgnoreCase(name);
    }
    
    /**
     * Determines if this is a <code>From</code> field.
     * 
     * @return <code>true</code> if this is a <code>From</code> field,
     *         <code>false</code> otherwise.
     */
    public boolean isFrom() {
        return FieldName.FROM.equalsIgnoreCase(name);
    }
    
    /**
     * Determines if this is a <code>To</code> field.
     * 
     * @return <code>true</code> if this is a <code>To</code> field,
     *         <code>false</code> otherwise.
     */
    public boolean isTo() {
        return FieldName.TO.equalsIgnoreCase(name);
    }
    
    /**
     * @see #getRaw()
     */
    @Override
    public String toString() {
        return raw;
    }
}
