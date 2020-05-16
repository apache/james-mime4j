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

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.ParseException;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.ByteSequence;

/**
 * The base class of all field classes.
 */
public abstract class AbstractField implements ParsedField {

    protected final Field rawField;
    protected final DecodeMonitor monitor;

    protected AbstractField(final Field rawField, final DecodeMonitor monitor) {
        this.rawField = rawField;
        this.monitor = monitor != null ? monitor : DecodeMonitor.SILENT;
    }

    /**
     * Gets the name of the field (<code>Subject</code>,
     * <code>From</code>, etc).
     *
     * @return the field name.
     */
    public String getName() {
        return rawField.getName();
    }

    /**
     * Gets the unfolded, unparsed and possibly encoded (see RFC 2047) field
     * body string.
     *
     * @return the unfolded unparsed field body string.
     */
    public String getBody() {
        return rawField.getBody();
    }

    /**
     * Gets original (raw) representation of the field, if available,
     * <code>null</code> otherwise.
     */
    public ByteSequence getRaw() {
        return rawField.getRaw();
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

    protected RawField getRawField() {
        if (rawField instanceof RawField) {
            return ((RawField) rawField);
        } else {
            return new RawField(rawField.getName(), rawField.getBody());
        }
    }

    @Override
    public String toString() {
        return rawField.toString();
    }

}
