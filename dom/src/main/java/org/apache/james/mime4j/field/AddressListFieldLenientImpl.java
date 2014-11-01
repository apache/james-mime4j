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
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.field.address.LenientAddressParser;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.ParserCursor;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Address list field such as <code>To</code> or <code>Reply-To</code>.
 */
public class AddressListFieldLenientImpl extends AbstractField implements AddressListField {

    private boolean parsed = false;

    private AddressList addressList;

    AddressListFieldLenientImpl(final Field rawField, final DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    public AddressList getAddressList() {
        if (!parsed)
            parse();

        return addressList;
    }

    private void parse() {
        parsed = true;
        RawField f = getRawField();
        ByteSequence buf = f.getRaw();
        int pos = f.getDelimiterIdx() + 1;
        if (buf == null) {
            String body = f.getBody();
            if (body == null) {
                addressList = new AddressList(Collections.<Mailbox>emptyList(), true);
                return;
            }
            buf = ContentUtil.encode(body);
            pos = 0;
        }
        ParserCursor cursor = new ParserCursor(pos, buf.length());
        addressList = LenientAddressParser.DEFAULT.parseAddressList(buf, cursor);
    }

    public static final FieldParser<AddressListField> PARSER = new FieldParser<AddressListField>() {

        public AddressListField parse(final Field rawField, final DecodeMonitor monitor) {
            return new AddressListFieldLenientImpl(rawField, monitor);
        }

    };

}
