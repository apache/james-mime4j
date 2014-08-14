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

import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.field.ContentTransferEncodingFieldImpl;
import org.apache.james.mime4j.field.ContentTypeFieldImpl;

/**
 * A MIME body part (as defined in RFC 2045).
 */
public class BodyPart extends AbstractEntity {

    /**
     * Creates a new empty <code>BodyPart</code>.
     */
    public BodyPart() {
    }

    @Override
    protected String calcTransferEncoding(ContentTransferEncodingField f) {
        return ContentTransferEncodingFieldImpl.getEncoding(f);
    }

    @Override
    protected String calcMimeType(ContentTypeField child, ContentTypeField parent) {
        return ContentTypeFieldImpl.getMimeType(child, parent);
    }

    @Override
    protected String calcCharset(ContentTypeField contentType) {
        return ContentTypeFieldImpl.getCharset(contentType);
    }

}
