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
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.MailboxField;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
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
     * @param monitor a DecodeMonitor object used while parsing/decoding.
     * @return a <code>ParsedField</code> instance.
     * @throws MimeException if the raw string cannot be split into field name and body.
     */
    public static ParsedField parse(
            final ByteSequence raw, 
            final DecodeMonitor monitor) throws MimeException {
        Field rawField = RawFieldParser.DEFAULT.parseField(raw);
        return PARSER.parse(rawField, monitor);
    }

    /**
     * Parses the given string and returns an instance of the 
     * <code>Field</code> class. The type of the class returned depends on
     * the field name:
     * <p>
     * <table>
     *   <tr><th>Class returned</th><th>Field names</th></tr>
     *   <tr><td>{@link ContentTypeFieldImpl}</td><td>Content-Type</td></tr>
     *   <tr><td>{@link ContentTransferEncodingFieldImpl}</td><td>Content-Transfer-Encoding</td></tr>
     *   <tr><td>{@link ContentDispositionFieldImpl}</td><td>Content-Disposition</td></tr>
     *   <tr><td>{@link DateTimeFieldImpl}</td><td>Date, Resent-Date</td></tr>
     *   <tr><td>{@link MailboxFieldImpl}</td><td>Sender, Resent-Sender</td></tr>
     *   <tr><td>{@link MailboxListFieldImpl}</td><td>From, Resent-From</td></tr>
     *   <tr><td>{@link AddressListFieldImpl}</td><td>To, Cc, Bcc, Reply-To, Resent-To, Resent-Cc, Resent-Bcc</td></tr>
     *   <tr><td>{@link UnstructuredFieldImpl}</td><td>Subject and others</td></tr>
     * </table>
     * 
     * @param rawStr the string to parse.
     * @return a <code>ParsedField</code> instance.
     * @throws MimeException if the raw string cannot be split into field name and body.
     */
    public static ParsedField parse(
            final String rawStr,
            final DecodeMonitor monitor) throws MimeException {
        ByteSequence raw = ContentUtil.encode(rawStr);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        // Do not retain the original raw representation as the field
        // may require folding
        return PARSER.parse(rawField, monitor);
    }

    public static ParsedField parse(final String rawStr) throws MimeException {
        return parse(rawStr, DecodeMonitor.SILENT);
    }

    public DefaultFieldParser() {
        super(UnstructuredFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_TYPE, 
                ContentTypeFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_LENGTH,
                ContentLengthFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_TRANSFER_ENCODING,
                ContentTransferEncodingFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_DISPOSITION,
                ContentDispositionFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_ID,
                ContentIdFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_MD5,
                ContentMD5FieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_DESCRIPTION,
                ContentDescriptionFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_LANGUAGE,
                ContentLanguageFieldImpl.PARSER);
        setFieldParser(FieldName.CONTENT_LOCATION,
                ContentLocationFieldImpl.PARSER);
        setFieldParser(FieldName.MIME_VERSION,
                MimeVersionFieldImpl.PARSER);

        FieldParser<DateTimeField> dateTimeParser = DateTimeFieldImpl.PARSER;
        setFieldParser(FieldName.DATE, dateTimeParser);
        setFieldParser(FieldName.RESENT_DATE, dateTimeParser);

        FieldParser<MailboxListField> mailboxListParser = MailboxListFieldImpl.PARSER;
        setFieldParser(FieldName.FROM, mailboxListParser);
        setFieldParser(FieldName.RESENT_FROM, mailboxListParser);

        FieldParser<MailboxField> mailboxParser = MailboxFieldImpl.PARSER;
        setFieldParser(FieldName.SENDER, mailboxParser);
        setFieldParser(FieldName.RESENT_SENDER, mailboxParser);

        FieldParser<AddressListField> addressListParser = AddressListFieldImpl.PARSER;
        setFieldParser(FieldName.TO, addressListParser);
        setFieldParser(FieldName.RESENT_TO, addressListParser);
        setFieldParser(FieldName.CC, addressListParser);
        setFieldParser(FieldName.RESENT_CC, addressListParser);
        setFieldParser(FieldName.BCC, addressListParser);
        setFieldParser(FieldName.RESENT_BCC, addressListParser);
        setFieldParser(FieldName.REPLY_TO, addressListParser);
    }

}
