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
import java.util.Date;

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.field.datetime.parser.ParseException;
import org.apache.james.mime4j.field.datetime.parser.TokenMgrError;

/**
 * Date-time field such as <code>Date</code> or <code>Resent-Date</code>.
 */
public class DateTimeFieldImpl extends AbstractField implements DateTimeField {
    private boolean parsed = false;

    private Date date;
    private ParseException parseException;

    DateTimeFieldImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    /**
     * @see org.apache.james.mime4j.dom.field.DateTimeField#getDate()
     */
    public Date getDate() {
        if (!parsed)
            parse();

        return date;
    }

    /**
     * @see org.apache.james.mime4j.dom.field.DateTimeField#getParseException()
     */
    @Override
    public ParseException getParseException() {
        if (!parsed)
            parse();

        return parseException;
    }

    private void parse() {
        String body = getBody();

        try {
            date = new DateTimeParser(new StringReader(body)).parseAll()
                    .getDate();
        } catch (ParseException e) {
            parseException = e;
        } catch (TokenMgrError e) {
            parseException = new ParseException(e);
        }

        parsed = true;
    }

    public static final FieldParser<DateTimeField> PARSER = new FieldParser<DateTimeField>() {

        public DateTimeField parse(final Field rawField, final DecodeMonitor monitor) {
            return new DateTimeFieldImpl(rawField, monitor);
        }

    };
}
