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

package org.apache.james.mime4j.stream;

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * The basic immutable MIME field.
 */
public final class RawField {

    private final ByteSequence raw;
    private final int delimiterIdx;
    private final String name;
    private final String body;

    RawField(ByteSequence raw, int delimiterIdx, String name, String body) {
        if (name == null) {
            throw new IllegalArgumentException("Field may not be null");
        }
    	this.raw = raw;
    	this.delimiterIdx = delimiterIdx;
        this.name = name.trim();
        this.body = body;
    }

    public RawField(String name, String body) {
        this(null, -1, name, body);
    }

    public ByteSequence getRaw() {
        return raw;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        if (body != null) {
            return body;
        }
        if (raw != null) {
            int len = raw.length();
            int off = delimiterIdx + 1;
            if (len > off + 1 && (raw.byteAt(off) & 0xff) == 0x20) off++;
            return MimeUtil.unfold(ContentUtil.decode(raw, off, len - off));
        }
        return null;
    }

    int getDelimiterIdx() {
        return delimiterIdx;
    }

    @Override
    public String toString() {
        if (raw != null) {
            return ContentUtil.decode(raw);
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(name);
            buf.append(": ");
            if (body != null) {
                buf.append(body);
            }
            return buf.toString();
        }
    }
    
}
