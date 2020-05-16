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

package org.apache.james.mime4j.message;

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.field.LenientFieldParser;
import org.apache.james.mime4j.parser.AbstractContentHandler;

/**
 * Abstract implementation of ContentHandler that automates common
 * tasks. Currently performs header parsing.
 *
 * Older versions of this class performed decoding of content streams.
 * This can be now easily achieved by calling setContentDecoding(true) on the MimeStreamParser.
 */
public abstract class SimpleContentHandler extends AbstractContentHandler {

    private final FieldParser<? extends ParsedField> fieldParser;
    private final DecodeMonitor monitor;

    public SimpleContentHandler(
            final FieldParser<? extends ParsedField> fieldParser,
            final DecodeMonitor monitor) {
        super();
        this.fieldParser = fieldParser != null ? fieldParser : LenientFieldParser.getParser();
        this.monitor = monitor != null ? monitor : DecodeMonitor.SILENT;
    }

    public SimpleContentHandler() {
        this(null, null);
    }

    /**
     * Called after headers are parsed.
     */
    public abstract void headers(Header header);

    /* Implement introduced callbacks. */

    private Header currHeader;

    /**
     * @see org.apache.james.mime4j.parser.AbstractContentHandler#startHeader()
     */
    @Override
    public final void startHeader() {
        currHeader = new HeaderImpl();
    }

    /**
     * @see org.apache.james.mime4j.parser.AbstractContentHandler#field(Field)
     */
    @Override
    public final void field(Field field) throws MimeException {
        ParsedField parsedField;
        if (field instanceof ParsedField) {
            parsedField = (ParsedField) field;
        } else {
            parsedField = fieldParser.parse(field, monitor);
        }
        currHeader.addField(parsedField);
    }

    /**
     * @see org.apache.james.mime4j.parser.AbstractContentHandler#endHeader()
     */
    @Override
    public final void endHeader() {
        Header tmp = currHeader;
        currHeader = null;
        headers(tmp);
    }

}
