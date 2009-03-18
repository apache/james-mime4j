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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class, which provides some MIME related application logic.
 */
public final class MimeUtil {
    private static final Log log = LogFactory.getLog(MimeUtil.class);
    
    /**
     * The <code>quoted-printable</code> encoding.
     */
    public static final String ENC_QUOTED_PRINTABLE = "quoted-printable";
    /**
     * The <code>binary</code> encoding.
     */
    public static final String ENC_BINARY = "binary";
    /**
     * The <code>base64</code> encoding.
     */
    public static final String ENC_BASE64 = "base64";
    /**
     * The <code>8bit</code> encoding.
     */
    public static final String ENC_8BIT = "8bit";
    /**
     * The <code>7bit</code> encoding.
     */
    public static final String ENC_7BIT = "7bit";

    /** <code>MIME-Version</code> header name (lowercase) */
    public static final String MIME_HEADER_MIME_VERSION = "mime-version";
    /** <code>Content-ID</code> header name (lowercase) */
    public static final String MIME_HEADER_CONTENT_ID = "content-id";
    /** <code>Content-Description</code> header name (lowercase) */
    public static final String MIME_HEADER_CONTENT_DESCRIPTION = "content-description";
    /** 
     * <code>Content-Disposition</code> header name (lowercase). 
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>. 
     */
    public static final String MIME_HEADER_CONTENT_DISPOSITION = "content-disposition";
    /** 
     * <code>Content-Disposition</code> filename parameter (lowercase). 
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>. 
     */
    public static final String PARAM_FILENAME = "filename";
    /** 
     * <code>Content-Disposition</code> modification-date parameter (lowercase). 
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>. 
     */
    public static final String PARAM_MODIFICATION_DATE = "modification-date";
    /** 
     * <code>Content-Disposition</code> creation-date parameter (lowercase). 
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>. 
     */
    public static final String PARAM_CREATION_DATE = "creation-date";
    /** 
     * <code>Content-Disposition</code> read-date parameter (lowercase). 
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>. 
     */
    public static final String PARAM_READ_DATE = "read-date";
    /** 
     * <code>Content-Disposition</code> size parameter (lowercase). 
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>. 
     */
    public static final String PARAM_SIZE = "size";
    /**
     * <code>Content-Langauge</code> header (lower case).
     * See <a href='http://www.faqs.org/rfcs/rfc4646.html'>RFC4646</a>.
     */
    public static final String MIME_HEADER_LANGAUGE = "content-language";
    /**
     * <code>Content-Location</code> header (lower case).
     * See <a href='http://www.faqs.org/rfcs/rfc2557.html'>RFC2557</a>.
     */
    public static final String MIME_HEADER_LOCATION = "content-location";
    /**
     * <code>Content-MD5</code> header (lower case).
     * See <a href='http://www.faqs.org/rfcs/rfc1864.html'>RFC1864</a>.
     */
    public static final String MIME_HEADER_MD5 = "content-md5";

    // used to create unique ids
    private static final Random random = new Random();
    
    // used to create unique ids
    private static int counter = 0;

    private MimeUtil() {
        // this is an utility class to be used statically.
        // this constructor protect from instantiation.
    }
    
    /**
     * Returns, whether the given two MIME types are identical.
     */
    public static boolean isSameMimeType(String pType1, String pType2) {
        return pType1 != null  &&  pType2 != null  &&  pType1.equalsIgnoreCase(pType2);
    }

    /**
     * Returns true, if the given MIME type is that of a message. 
     */
    public static boolean isMessage(String pMimeType) {
        return pMimeType != null  &&  pMimeType.equalsIgnoreCase("message/rfc822");
    }

    /**
     * Return true, if the given MIME type indicates a multipart entity.
     */
    public static boolean isMultipart(String pMimeType) {
        return pMimeType != null  &&  pMimeType.toLowerCase().startsWith("multipart/");
    }

    /**
     * Returns, whether the given transfer-encoding is "base64".
     */
    public static boolean isBase64Encoding(String pTransferEncoding) {
        return ENC_BASE64.equalsIgnoreCase(pTransferEncoding);
    }

    /**
     * Returns, whether the given transfer-encoding is "quoted-printable".
     */
    public static boolean isQuotedPrintableEncoded(String pTransferEncoding) {
        return ENC_QUOTED_PRINTABLE.equalsIgnoreCase(pTransferEncoding);
    }

    /**
     * <p>Parses a complex field value into a map of key/value pairs. You may
     * use this, for example, to parse a definition like
     * <pre>
     *   text/plain; charset=UTF-8; boundary=foobar
     * </pre>
     * The above example would return a map with the keys "", "charset",
     * and "boundary", and the values "text/plain", "UTF-8", and "foobar".
     * </p><p>
     * Header value will be unfolded and excess white space trimmed.
     * </p>
     * @param pValue The field value to parse.
     * @return The result map; use the key "" to retrieve the first value.
     */
    @SuppressWarnings("fallthrough")
    public static Map<String, String> getHeaderParams(String pValue) {
        pValue = pValue.trim();

        pValue = unfold(pValue);
        
        Map<String, String> result = new HashMap<String, String>();

        // split main value and parameters
        String main;
        String rest;
        if (pValue.indexOf(";") == -1) {
            main = pValue;
            rest = null;
        } else {
            main = pValue.substring(0, pValue.indexOf(";"));
            rest = pValue.substring(main.length() + 1);
        }

        result.put("", main);
        if (rest != null) {
            char[] chars = rest.toCharArray();
            StringBuilder paramName = new StringBuilder(64);
            StringBuilder paramValue = new StringBuilder(64);

            final byte READY_FOR_NAME = 0;
            final byte IN_NAME = 1;
            final byte READY_FOR_VALUE = 2;
            final byte IN_VALUE = 3;
            final byte IN_QUOTED_VALUE = 4;
            final byte VALUE_DONE = 5;
            final byte ERROR = 99;

            byte state = READY_FOR_NAME;
            boolean escaped = false;
            for (char c : chars) {
                switch (state) {
                    case ERROR:
                        if (c == ';')
                            state = READY_FOR_NAME;
                        break;

                    case READY_FOR_NAME:
                        if (c == '=') {
                            log.error("Expected header param name, got '='");
                            state = ERROR;
                            break;
                        }

                        paramName.setLength(0);
                        paramValue.setLength(0);

                        state = IN_NAME;
                        // fall-through

                    case IN_NAME:
                        if (c == '=') {
                            if (paramName.length() == 0)
                                state = ERROR;
                            else
                                state = READY_FOR_VALUE;
                            break;
                        }

                        // not '='... just add to name
                        paramName.append(c);
                        break;

                    case READY_FOR_VALUE:
                        boolean fallThrough = false;
                        switch (c) {
                            case ' ':
                            case '\t':
                                break;  // ignore spaces, especially before '"'

                            case '"':
                                state = IN_QUOTED_VALUE;
                                break;

                            default:
                                state = IN_VALUE;
                                fallThrough = true;
                                break;
                        }
                        if (!fallThrough)
                            break;

                        // fall-through

                    case IN_VALUE:
                        fallThrough = false;
                        switch (c) {
                            case ';':
                            case ' ':
                            case '\t':
                                result.put(
                                   paramName.toString().trim().toLowerCase(),
                                   paramValue.toString().trim());
                                state = VALUE_DONE;
                                fallThrough = true;
                                break;
                            default:
                                paramValue.append(c);
                                break;
                        }
                        if (!fallThrough)
                            break;

                    case VALUE_DONE:
                        switch (c) {
                            case ';':
                                state = READY_FOR_NAME;
                                break;

                            case ' ':
                            case '\t':
                                break;

                            default:
                                state = ERROR;
                                break;
                        }
                        break;
                        
                    case IN_QUOTED_VALUE:
                        switch (c) {
                            case '"':
                                if (!escaped) {
                                    // don't trim quoted strings; the spaces could be intentional.
                                    result.put(
                                            paramName.toString().trim().toLowerCase(),
                                            paramValue.toString());
                                    state = VALUE_DONE;
                                } else {
                                    escaped = false;
                                    paramValue.append(c);                                    
                                }
                                break;
                                
                            case '\\':
                                if (escaped) {
                                    paramValue.append('\\');
                                }
                                escaped = !escaped;
                                break;

                            default:
                                if (escaped) {
                                    paramValue.append('\\');
                                }
                                escaped = false;
                                paramValue.append(c);
                                break;
                        }
                        break;

                }
            }

            // done looping.  check if anything is left over.
            if (state == IN_VALUE) {
                result.put(
                        paramName.toString().trim().toLowerCase(),
                        paramValue.toString().trim());
            }
        }

        return result;
    }

    /**
     * Creates a new unique message boundary string that can be used as boundary
     * parameter for the Content-Type header field of a message.
     * 
     * @return a new unique message boundary string.
     */
    public static String createUniqueBoundary() {
        StringBuilder sb = new StringBuilder();
        sb.append("-=Part.");
        sb.append(Integer.toHexString(nextCounterValue()));
        sb.append('.');
        sb.append(Long.toHexString(random.nextLong()));
        sb.append('.');
        sb.append(Long.toHexString(System.currentTimeMillis()));
        sb.append('.');
        sb.append(Long.toHexString(random.nextLong()));
        sb.append("=-");
        return sb.toString();
    }

    /**
     * Creates a new unique message identifier that can be used in message
     * header field such as Message-ID or In-Reply-To. If the given host name is
     * not <code>null</code> it will be used as suffix for the message ID
     * (following an at sign).
     * 
     * The resulting string is enclosed in angle brackets (&lt; and &gt;);
     * 
     * @param hostName host name to be included in the message ID or
     *            <code>null</code> if no host name should be included.
     * @return a new unique message identifier.
     */
    public static String createUniqueMessageId(String hostName) {
        StringBuilder sb = new StringBuilder("<Mime4j.");
        sb.append(Integer.toHexString(nextCounterValue()));
        sb.append('.');
        sb.append(Long.toHexString(random.nextLong()));
        sb.append('.');
        sb.append(Long.toHexString(System.currentTimeMillis()));
        if (hostName != null) {
            sb.append('@');
            sb.append(hostName);
        }
        sb.append('>');
        return sb.toString();
    }

    /**
     * Formats the specified date into a RFC 822 date-time string.
     * 
     * @param date
     *            date to be formatted into a string.
     * @param zone
     *            the time zone to use or <code>null</code> to use the default
     *            time zone.
     * @return the formatted time string.
     */
    public static String formatDate(Date date, TimeZone zone) {
        DateFormat df = RFC822_DATE_FORMAT.get();

        if (zone == null) {
            df.setTimeZone(TimeZone.getDefault());
        } else {
            df.setTimeZone(zone);
        }

        return df.format(date);
    }

    /**
     * Splits the specified string into a multiple-line representation with
     * lines no longer than 76 characters (because the line might contain
     * encoded words; see <a href='http://www.faqs.org/rfcs/rfc2047.html'>RFC
     * 2047</a> section 2). If the string contains non-whitespace sequences
     * longer than 76 characters a line break is inserted at the whitespace
     * character following the sequence resulting in a line longer than 76
     * characters.
     * 
     * @param s
     *            string to split.
     * @param usedCharacters
     *            number of characters already used up. Usually the number of
     *            characters for header field name plus colon and one space.
     * @return a multiple-line representation of the given string.
     */
    public static String fold(String s, int usedCharacters) {
        final int maxCharacters = 76;

        final int length = s.length();
        if (usedCharacters + length <= maxCharacters)
            return s;

        StringBuilder sb = new StringBuilder();

        int lastLineBreak = -usedCharacters;
        int wspIdx = indexOfWsp(s, 0);
        while (true) {
            if (wspIdx == length) {
                sb.append(s.substring(Math.max(0, lastLineBreak)));
                return sb.toString();
            }

            int nextWspIdx = indexOfWsp(s, wspIdx + 1);

            if (nextWspIdx - lastLineBreak > maxCharacters) {
                sb.append(s.substring(Math.max(0, lastLineBreak), wspIdx));
                sb.append("\r\n");
                lastLineBreak = wspIdx;
            }

            wspIdx = nextWspIdx;
        }
    }

    /**
     * Unfold a multiple-line representation into a single line.
     * 
     * @param s
     *            string to unfold.
     * @return unfolded string.
     */
    public static String unfold(String s) {
        final int length = s.length();
        for (int idx = 0; idx < length; idx++) {
            char c = s.charAt(idx);
            if (c == '\r' || c == '\n') {
                return unfold0(s, idx);
            }
        }

        return s;
    }

    private static String unfold0(String s, int crlfIdx) {
        final int length = s.length();
        StringBuilder sb = new StringBuilder(length);

        if (crlfIdx > 0) {
            sb.append(s.substring(0, crlfIdx));
        }

        for (int idx = crlfIdx + 1; idx < length; idx++) {
            char c = s.charAt(idx);
            if (c != '\r' && c != '\n') {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static int indexOfWsp(String s, int fromIndex) {
        final int len = s.length();
        for (int index = fromIndex; index < len; index++) {
            char c = s.charAt(index);
            if (c == ' ' || c == '\t')
                return index;
        }
        return len;
    }

    private static synchronized int nextCounterValue() {
        return counter++;
    }

    private static final ThreadLocal<DateFormat> RFC822_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new Rfc822DateFormat();
        }
    };

    private static final class Rfc822DateFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;

        public Rfc822DateFormat() {
            super("EEE, d MMM yyyy HH:mm:ss ", Locale.US);
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo,
                FieldPosition pos) {
            StringBuffer sb = super.format(date, toAppendTo, pos);

            int zoneMillis = calendar.get(GregorianCalendar.ZONE_OFFSET);
            int dstMillis = calendar.get(GregorianCalendar.DST_OFFSET);
            int minutes = (zoneMillis + dstMillis) / 1000 / 60;

            if (minutes < 0) {
                sb.append('-');
                minutes = -minutes;
            } else {
                sb.append('+');
            }

            sb.append(String.format("%02d%02d", minutes / 60, minutes % 60));

            return sb;
        }
    }
}
