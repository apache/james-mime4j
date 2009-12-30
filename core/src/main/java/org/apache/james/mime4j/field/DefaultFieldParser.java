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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.RawField;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

public class DefaultFieldParser extends DelegatingFieldParser {

    private static final DefaultFieldParser PARSER = new DefaultFieldParser();
    

    /**
     * Gets the default parser used to parse fields.
     * 
     * @return the default field parser
     */
    public static DefaultFieldParser getParser() {
        return PARSER;
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
     */
    public static ParsedField parse(final ByteSequence raw) throws MimeException {
    	RawField rawField = new RawField(raw);
        return PARSER.parse(rawField.getName(), rawField.getBody(), raw);
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
     */
    public static ParsedField parse(final String rawStr) throws MimeException {
        ByteSequence raw = ContentUtil.encode(rawStr);
        return parse(raw);
    }


    public DefaultFieldParser() {
        setFieldParser(FieldName.CONTENT_TRANSFER_ENCODING,
                ContentTransferEncodingField.PARSER);
        setFieldParser(FieldName.CONTENT_TYPE, ContentTypeField.PARSER);
        setFieldParser(FieldName.CONTENT_DISPOSITION,
                ContentDispositionField.PARSER);

        final FieldParser<DateTimeField> dateTimeParser = DateTimeField.PARSER;
        setFieldParser(FieldName.DATE, dateTimeParser);
        setFieldParser(FieldName.RESENT_DATE, dateTimeParser);

        final FieldParser<MailboxListField> mailboxListParser = MailboxListField.PARSER;
        setFieldParser(FieldName.FROM, mailboxListParser);
        setFieldParser(FieldName.RESENT_FROM, mailboxListParser);

        final FieldParser<MailboxField> mailboxParser = MailboxField.PARSER;
        setFieldParser(FieldName.SENDER, mailboxParser);
        setFieldParser(FieldName.RESENT_SENDER, mailboxParser);

        final FieldParser<AddressListField> addressListParser = AddressListField.PARSER;
        setFieldParser(FieldName.TO, addressListParser);
        setFieldParser(FieldName.RESENT_TO, addressListParser);
        setFieldParser(FieldName.CC, addressListParser);
        setFieldParser(FieldName.RESENT_CC, addressListParser);
        setFieldParser(FieldName.BCC, addressListParser);
        setFieldParser(FieldName.RESENT_BCC, addressListParser);
        setFieldParser(FieldName.REPLY_TO, addressListParser);
    }

}
