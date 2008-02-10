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

package org.apache.james.mime4j.util;

import java.nio.charset.Charset;


/**
 * Frequently used RFC 882 constants and utility methods.
 * 
 * @version $Id:$
 */
public final class MessageUtils {
 
    public static final int STRICT_IGNORE     = 1;
    public static final int STRICT_ERROR      = 2;
    public static final int LENIENT           = 3;
    
    public static final Charset ASCII = CharsetUtil.getCharset("US-ASCII");

    public static final Charset ISO_8859_1 = CharsetUtil.getCharset("ISO-8859-1");
    
    public static final Charset DEFAULT_CHARSET = ASCII;

    public static final String CRLF = "\r\n";
    
    public static boolean isASCII(char ch) {
        return ((int)ch & 0xFF80) == 0;
    }
    
    public static boolean isASCII(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("String may not be null");
        }
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isASCII(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
}
