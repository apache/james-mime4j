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

import java.util.BitSet;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.io.MaxHeaderLengthLimitException;
import org.apache.james.mime4j.util.ByteArrayBuffer;

/**
 * Default implementation of {@link FieldBuilder}.
 *
 */
public class DefaultFieldBuilder implements FieldBuilder {

    private static final BitSet FIELD_CHARS = new BitSet();

    static {
        for (int i = 0x21; i <= 0x39; i++) {
            FIELD_CHARS.set(i);
        }
        for (int i = 0x3b; i <= 0x7e; i++) {
            FIELD_CHARS.set(i);
        }
    }

    private final ByteArrayBuffer buf;
    private final int maxlen;

    public DefaultFieldBuilder(int maxlen) {
        this.buf = new ByteArrayBuffer(1024);
        this.maxlen = maxlen;
    }

    public void reset() {
        this.buf.clear();
    }

    public void append(final ByteArrayBuffer line) throws MaxHeaderLengthLimitException {
        if (line == null) {
            return;
        }
        int len = line.length();
        if (this.maxlen > 0 && this.buf.length() + len >= this.maxlen) {
            throw new MaxHeaderLengthLimitException("Maximum header length limit (" + this.maxlen + ") exceeded");
        }
        this.buf.append(line.buffer(), 0, line.length());
    }

    public RawField build() throws MimeException {
        int len = this.buf.length();
        if (len > 0) {
            if (this.buf.byteAt(len - 1) == '\n') {
                len --;
            }
            if (this.buf.byteAt(len - 1) == '\r') {
                len --;
            }
        }
        ByteArrayBuffer copy = new ByteArrayBuffer(this.buf.buffer(), len, false);
        RawField field = RawFieldParser.DEFAULT.parseField(copy);
        String name = field.getName();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (!FIELD_CHARS.get(ch)) {
                throw new MimeException("MIME field name contains illegal characters: "
                        + field.getName());
            }
        }
        return field;
    }

    public ByteArrayBuffer getRaw() {
        return this.buf;
    }

}
