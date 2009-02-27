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

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Represents a <code>Content-Transfer-Encoding</code> field.
 */
public class ContentTransferEncodingField extends AbstractField {
    private String encoding;

    ContentTransferEncodingField(String name, String body, ByteSequence raw) {
        super(name, body, raw);
        encoding = body.trim().toLowerCase();
    }

    /**
     * Gets the encoding defined in this field.
     * 
     * @return the encoding or an empty string if not set.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Gets the encoding of the given field if. Returns the default
     * <code>7bit</code> if not set or if <code>f</code> is
     * <code>null</code>.
     * 
     * @return the encoding.
     */
    public static String getEncoding(ContentTransferEncodingField f) {
        if (f != null && f.getEncoding().length() != 0) {
            return f.getEncoding();
        }
        return MimeUtil.ENC_7BIT;
    }

    static final FieldParser PARSER = new FieldParser() {
        public ParsedField parse(final String name, final String body,
                final ByteSequence raw) {
            return new ContentTransferEncodingField(name, body, raw);
        }
    };
}
