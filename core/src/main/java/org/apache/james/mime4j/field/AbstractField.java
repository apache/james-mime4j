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
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * The base class of all field classes.
 */
public abstract class AbstractField implements ParsedField {

    private static final Pattern FIELD_NAME_PATTERN = Pattern
            .compile("^([\\x21-\\x39\\x3b-\\x7e]+):");

    private static final DefaultFieldParser parser = new DefaultFieldParser();
    
    private final String name;
    private final String body;
    private final ByteSequence raw;
    
    protected AbstractField(final String name, final String body, final ByteSequence raw) {
        this.name = name;
        this.body = body;
        this.raw = raw;
    }

    /**
     * Parses the given byte sequence and returns an instance of the
     * <code>Field</code> class. The type of the class returned depends on the
     * field name; see {@link #parse(String)} for a table of field names and
     * their corresponding classes.
     * 
     * @param raw the bytes to parse.
     * @return a <code>ParsedField</code> instance.
     * @throws MimeException if the raw string cannot be split into field name and body.
     * @see #isValidField()
     */
    public static ParsedField parse(final ByteSequence raw) throws MimeException {
        String rawStr = ContentUtil.decode(raw);
        return parse(raw, rawStr);
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
     * @param rawStr the string to parse.
     * @return a <code>ParsedField</code> instance.
     * @throws MimeException if the raw string cannot be split into field name and body.
     * @see #isValidField()
     */
    public static ParsedField parse(final String rawStr) throws MimeException {
        ByteSequence raw = ContentUtil.encode(rawStr);
        return parse(raw, rawStr);
    }

    /**
     * Gets the default parser used to parse fields.
     * 
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
    public ByteSequence getRaw() {
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
     * @see ParsedField#isValidField() 
     */
    public boolean isValidField() {
        return getParseException() == null;
    }

    /**
     * @see ParsedField#getParseException() 
     */
    public ParseException getParseException() {
        return null;
    }

    @Override
    public String toString() {
        return name + ": " + body;
    }

    private static ParsedField parse(final ByteSequence raw, final String rawStr)
            throws MimeException {
        /*
         * Unfold the field.
         */
        final String unfolded = MimeUtil.unfold(rawStr);

        /*
         * Split into name and value.
         */
        final Matcher fieldMatcher = FIELD_NAME_PATTERN.matcher(unfolded);
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

}
