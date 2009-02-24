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

public class DefaultFieldParser extends DelegatingFieldParser {

    public DefaultFieldParser() {
        setFieldParser(FieldName.CONTENT_TRANSFER_ENCODING,
                ContentTransferEncodingField.PARSER);
        setFieldParser(FieldName.CONTENT_TYPE, ContentTypeField.PARSER);
        setFieldParser(FieldName.CONTENT_DISPOSITION,
                ContentDispositionField.PARSER);

        final FieldParser dateTimeParser = DateTimeField.PARSER;
        setFieldParser(FieldName.DATE, dateTimeParser);
        setFieldParser(FieldName.RESENT_DATE, dateTimeParser);

        final FieldParser mailboxListParser = MailboxListField.PARSER;
        setFieldParser(FieldName.FROM, mailboxListParser);
        setFieldParser(FieldName.RESENT_FROM, mailboxListParser);

        final FieldParser mailboxParser = MailboxField.PARSER;
        setFieldParser(FieldName.SENDER, mailboxParser);
        setFieldParser(FieldName.RESENT_SENDER, mailboxParser);

        final FieldParser addressListParser = AddressListField.PARSER;
        setFieldParser(FieldName.TO, addressListParser);
        setFieldParser(FieldName.RESENT_TO, addressListParser);
        setFieldParser(FieldName.CC, addressListParser);
        setFieldParser(FieldName.RESENT_CC, addressListParser);
        setFieldParser(FieldName.BCC, addressListParser);
        setFieldParser(FieldName.RESENT_BCC, addressListParser);
        setFieldParser(FieldName.REPLY_TO, addressListParser);
    }

}
