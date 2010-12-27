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

package org.apache.james.mime4j.parser;

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * The basic immutable MIME field.
 */
class RawField implements Field {

    private final ByteSequence raw;
    private int colonIdx;

    private String name;
    private String body;

    public RawField(ByteSequence raw, int colonIdx) {
        this.raw = raw;
        this.colonIdx = colonIdx;
    }

    public String getName() {
        if (name == null) {
            name = parseName();
        }

        return name;
    }

    public String getBody() {
        if (body == null) {
            body = parseBody();
        }

        return body;
    }

    public ByteSequence getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return getName() + ':' + getBody();
    }

    private String parseName() {
        return ContentUtil.decode(raw, 0, colonIdx);
    }

    private String parseBody() {
        int offset = colonIdx + 1;
        int length = raw.length() - offset;
        return ContentUtil.decode(raw, offset, length);
    }

}
