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
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * The basic immutable MIME field.
 */
public class RawFieldParser {

    static final int COLON   = ':';
    static final int SPACE   = 0x20;
    static final int TAB     = 0x09;
    
    private static final BitSet FIELD_CHARS = new BitSet();
    
    public static final RawFieldParser DEFAULT = new RawFieldParser(); 

    static {
        for (int i = 0x21; i <= 0x39; i++) {
            FIELD_CHARS.set(i);
        }
        for (int i = 0x3b; i <= 0x7e; i++) {
            FIELD_CHARS.set(i);
        }
    }

    public RawField parseField(final ByteSequence raw) throws MimeException {
        int len = raw.length();
        int colonIdx = -1;
        int headerNameEndIdx = -1;
        boolean obsolete = false;
        for (int i = 0; i < len; i++) {
            if (!FIELD_CHARS.get(raw.byteAt(i) & 0xff)) {
                headerNameEndIdx = i;
                for (; i < len; i++) {
                    int j = raw.byteAt(i) & 0xff;
                    if (j == COLON) {
                        colonIdx = i;
                        break;
                    } else if (j != SPACE && j != TAB) {
                        throw new MimeException("Invalid header: unexpected char " + j + " after colon");
                    } else {
                        obsolete = true;
                    }
                }
                break;
            }
        }
        if (colonIdx == -1) {
            throw new MimeException("Invalid header: no colon found");
        }
        // make sure we ignore ending WSP (obsolete rfc822 syntax)
        String name = ContentUtil.decode(raw, 0, headerNameEndIdx);
        return new RawField(raw, colonIdx, obsolete, name, null);
    }

}
