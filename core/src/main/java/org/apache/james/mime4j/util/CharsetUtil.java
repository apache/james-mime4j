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
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Utility class for working with character sets. 
 */
public class CharsetUtil {
    
    /** carriage return - line feed sequence */
    public static final String CRLF = "\r\n";

    /** US-ASCII CR, carriage return (13) */
    public static final int CR = '\r';

    /** US-ASCII LF, line feed (10) */
    public static final int LF = '\n';

    /** US-ASCII SP, space (32) */
    public static final int SP = ' ';

    /** US-ASCII HT, horizontal-tab (9) */
    public static final int HT = '\t';

    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static final Charset DEFAULT_CHARSET = US_ASCII;

    /**
     * Returns <code>true</code> if the specified character falls into the US
     * ASCII character set (Unicode range 0000 to 007f).
     * 
     * @param ch
     *            character to test.
     * @return <code>true</code> if the specified character falls into the US
     *         ASCII character set, <code>false</code> otherwise.
     */
    public static boolean isASCII(char ch) {
        return (0xFF80 & ch) == 0;
    }

    /**
     * Returns <code>true</code> if the specified string consists entirely of
     * US ASCII characters.
     * 
     * @param s
     *            string to test.
     * @return <code>true</code> if the specified string consists entirely of
     *         US ASCII characters, <code>false</code> otherwise.
     */
    public static boolean isASCII(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("String may not be null");
        }
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isASCII(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the specified character is a whitespace
     * character (CR, LF, SP or HT).
     * 
     * @param ch
     *            character to test.
     * @return <code>true</code> if the specified character is a whitespace
     *         character, <code>false</code> otherwise.
     */
    public static boolean isWhitespace(char ch) {
        return ch == SP || ch == HT || ch == CR || ch == LF;
    }

    /**
     * Returns <code>true</code> if the specified string consists entirely of
     * whitespace characters.
     * 
     * @param s
     *            string to test.
     * @return <code>true</code> if the specified string consists entirely of
     *         whitespace characters, <code>false</code> otherwise.
     */
    public static boolean isWhitespace(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("String may not be null");
        }
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a {@link Charset} instance if character set with the given name 
     * is recognized and supported by Java runtime. Returns <code>null</code> 
     * otherwise.
     * <p/>
     * This method is a wrapper around {@link Charset#forName(String)} method 
     * that catches {@link IllegalCharsetNameException} and 
     *  {@link UnsupportedCharsetException} and returns <code>null</code>.
     */
    public static Charset lookup(final String name) {
        if (name == null) {
            return null;
        }
        try {
            return Charset.forName(name);
        } catch (IllegalCharsetNameException ex) {
            return null;
        } catch (UnsupportedCharsetException ex) {
            return null;
        }
    }
    
 }