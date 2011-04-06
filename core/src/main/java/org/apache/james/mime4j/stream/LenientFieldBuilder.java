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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.io.MaxHeaderLengthLimitException;
import org.apache.james.mime4j.util.ByteArrayBuffer;

public class LenientFieldBuilder implements FieldBuilder {

    private final ByteArrayBuffer buf;
    private final int maxlen;
    

    public LenientFieldBuilder(int maxlen) {
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
            // buffer is over the max limit: quietly ignore all further input
            return;
        }
        if (this.buf == null) {
        }
        int beginIndex = 0;
        int endIndex = len;
        while (beginIndex < endIndex && RawFieldParser.isWhitespace(line.byteAt(beginIndex))) {
            beginIndex++;
        }
        while (endIndex > beginIndex && RawFieldParser.isWhitespace(line.byteAt(endIndex - 1))) {
            endIndex--;
        }
        if (this.buf.length() > 0) {
            this.buf.append(' ');
        }
        this.buf.append(line.buffer(), beginIndex, endIndex - beginIndex);
    }
    
    public RawField build() throws MimeException {
        if (this.buf == null) {
            return null;
        }
        int idx = RawFieldParser.indexOf(this.buf, RawFieldParser.COLON);
        if (idx == -1) {
            throw new MimeException("Invalid MIME field: no name/value separator found: " +
                    this.buf.toString());
        }
        String name = RawFieldParser.copyTrimmed(this.buf, 0, idx);
        String value = RawFieldParser.copyTrimmed(this.buf, idx + 1, this.buf.length());
        return new RawField(name, value);
    }
    
    public ByteArrayBuffer getRaw() {
        return null;
    }
    
}
