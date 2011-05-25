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

import java.util.Locale;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.FieldParser;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Represents a <code>Content-Transfer-Encoding</code> field.
 */
public class ContentTransferEncodingFieldImpl extends AbstractField implements ContentTransferEncodingField {

    private boolean parsed = false;
    private String encoding;

    ContentTransferEncodingFieldImpl(Field rawField, DecodeMonitor monitor) {
        super(rawField, monitor);
    }

    private void parse() {
        parsed = true;
        String body = getBody();
        if (body != null) {
            encoding = body.trim().toLowerCase(Locale.US);
        } else {
            encoding = null;
        }
    }
    
    /**
     * @see org.apache.james.mime4j.dom.field.ContentTransferEncodingField#getEncoding()
     */
    public String getEncoding() {
        if (!parsed) {
            parse();
        }
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

    public static final FieldParser<ContentTransferEncodingField> PARSER = new FieldParser<ContentTransferEncodingField>() {
        
        public ContentTransferEncodingField parse(final Field rawField, final DecodeMonitor monitor) {
            return new ContentTransferEncodingFieldImpl(rawField, monitor);
        }
        
    };
}
