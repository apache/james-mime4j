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

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.MailboxField;
import org.apache.james.mime4j.field.address.LenientAddressParser;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Mailbox field such as <code>Sender</code> or <code>Resent-Sender</code>.
 */
public class MailboxFieldLenientImpl extends AbstractField implements MailboxField {

    private boolean parsed = false;

    private Mailbox mailbox;

    MailboxFieldLenientImpl(final Field rawField, final DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    public Mailbox getMailbox() {
        if (!parsed) {
            parse();
        }
        return mailbox;
    }

    private void parse() {
        parsed = true;
        RawField f = getRawField();
        ByteSequence buf = f.getRaw();
        int pos = f.getDelimiterIdx() + 1;
        if (buf == null) {
            String body = f.getBody();
            if (body == null) {
                return;
            }
            buf = ContentUtil.encode(body);
            pos = 0;
        }
        ParserCursor cursor = new ParserCursor(pos, buf.length());
        mailbox = LenientAddressParser.DEFAULT.parseMailbox(buf, cursor, null);
    }

    public static final FieldParser<MailboxField> PARSER = new FieldParser<MailboxField>() {

        public MailboxField parse(final Field rawField, final DecodeMonitor monitor) {
            return new MailboxFieldLenientImpl(rawField, monitor);
        }

    };
}
