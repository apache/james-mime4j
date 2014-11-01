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

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.field.address.LenientAddressParser;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Mailbox-list field such as <code>From</code> or <code>Resent-From</code>.
 */
public class MailboxListFieldLenientImpl extends AbstractField implements MailboxListField {

    private boolean parsed = false;

    private MailboxList mailboxList;

    MailboxListFieldLenientImpl(final Field rawField, final DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    public MailboxList getMailboxList() {
        if (!parsed) {
            parse();
        }
        return mailboxList;
    }

    private void parse() {
        parsed = true;
        RawField f = getRawField();
        ByteSequence buf = f.getRaw();
        int pos = f.getDelimiterIdx() + 1;
        if (buf == null) {
            String body = f.getBody();
            if (body == null) {
                mailboxList = new MailboxList(Collections.<Mailbox>emptyList(), true);
                return;
            }
            buf = ContentUtil.encode(body);
            pos = 0;
        }
        ParserCursor cursor = new ParserCursor(pos, buf.length());
        mailboxList = LenientAddressParser.DEFAULT.parseAddressList(buf, cursor).flatten();
    }

    public static final FieldParser<MailboxListField> PARSER = new FieldParser<MailboxListField>() {

        public MailboxListField parse(final Field rawField, final DecodeMonitor monitor) {
            return new MailboxListFieldLenientImpl(rawField, monitor);
        }

    };

}
