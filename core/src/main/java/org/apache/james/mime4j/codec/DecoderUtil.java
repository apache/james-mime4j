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

package org.apache.james.mime4j.codec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.CharsetUtil;

/**
 * Static methods for decoding strings, byte arrays and encoded words.
 */
public class DecoderUtil {

    private static final Pattern PATTERN_ENCODED_WORD = Pattern.compile(
            "=\\?(.+?)\\?(\\w)\\?(.*?)\\?=", Pattern.DOTALL);

    /**
     * Decodes a string containing quoted-printable encoded data.
     *
     * @param s the string to decode.
     * @return the decoded bytes.
     */
    private static byte[] decodeQuotedPrintable(String s, DecodeMonitor monitor) {
        try {
            QuotedPrintableInputStream is = new QuotedPrintableInputStream(
                    InputStreams.createAscii(s), monitor);
            try {
                ByteArrayBuffer buf = new ByteArrayBuffer(s.length());
                int b;
                while ((b = is.read()) != -1) {
                    buf.append(b);
                }
                return buf.toByteArray();
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            // This should never happen!
            throw new Error(ex);
        }
    }

    /**
     * Decodes a string containing base64 encoded data.
     *
     * @param s the string to decode.
     * @param monitor
     * @return the decoded bytes.
     */
    private static byte[] decodeBase64(String s, DecodeMonitor monitor) {
        try {
            Base64InputStream is = new Base64InputStream(
                    InputStreams.createAscii(s), monitor);
            try {
                ByteArrayBuffer buf = new ByteArrayBuffer(s.length());
                int b;
                while ((b = is.read()) != -1) {
                    buf.append(b);
                }
                return buf.toByteArray();
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            // This should never happen!
            throw new Error(ex);
        }
    }

    /**
     * Decodes an encoded text encoded with the 'B' encoding (described in
     * RFC 2047) found in a header field body.
     *
     * @param encodedText the encoded text to decode.
     * @param charset the Java charset to use.
     * @param monitor
     * @return the decoded string.
     * @throws UnsupportedEncodingException if the given Java charset isn't
     *         supported.
     */
    static String decodeB(String encodedText, String charset, DecodeMonitor monitor)
            throws UnsupportedEncodingException {
        byte[] decodedBytes = decodeBase64(encodedText, monitor);
        return new String(decodedBytes, charset);
    }

    /**
     * Decodes an encoded text encoded with the 'Q' encoding (described in
     * RFC 2047) found in a header field body.
     *
     * @param encodedText the encoded text to decode.
     * @param charset the Java charset to use.
     * @return the decoded string.
     * @throws UnsupportedEncodingException if the given Java charset isn't
     *         supported.
     */
    static String decodeQ(String encodedText, String charset, DecodeMonitor monitor)
            throws UnsupportedEncodingException {
        encodedText = replaceUnderscores(encodedText);

        byte[] decodedBytes = decodeQuotedPrintable(encodedText, monitor);
        return new String(decodedBytes, charset);
    }

    static String decodeEncodedWords(String body)  {
        return decodeEncodedWords(body, DecodeMonitor.SILENT);
    }

    /**
     * Decodes a string containing encoded words as defined by RFC 2047. Encoded
     * words have the form =?charset?enc?encoded-text?= where enc is either 'Q'
     * or 'q' for quoted-printable and 'B' or 'b' for base64.
     *
     * @param body the string to decode
     * @param monitor the DecodeMonitor to be used.
     * @return the decoded string.
     * @throws IllegalArgumentException only if the DecodeMonitor strategy throws it (Strict parsing)
     */
    public static String decodeEncodedWords(String body, DecodeMonitor monitor) throws IllegalArgumentException {
        return decodeEncodedWords(body, monitor, null);
    }

    /**
     * Decodes a string containing encoded words as defined by RFC 2047. Encoded
     * words have the form =?charset?enc?encoded-text?= where enc is either 'Q'
     * or 'q' for quoted-printable and 'B' or 'b' for base64. Using fallback
     * charset if charset in encoded words is invalid.
     *
     * @param body the string to decode
     * @param fallback the fallback Charset to be used.
     * @return the decoded string.
     * @throws IllegalArgumentException only if the DecodeMonitor strategy throws it (Strict parsing)
     */
    public static String decodeEncodedWords(String body, Charset fallback) throws IllegalArgumentException {
        return decodeEncodedWords(body, null, fallback);
    }

    /**
     * Decodes a string containing encoded words as defined by RFC 2047. Encoded
     * words have the form =?charset?enc?encoded-text?= where enc is either 'Q'
     * or 'q' for quoted-printable and 'B' or 'b' for base64. Using fallback
     * charset if charset in encoded words is invalid.
     *
     * @param body the string to decode
     * @param monitor the DecodeMonitor to be used.
     * @param fallback the fallback Charset to be used.
     * @return the decoded string.
     * @throws IllegalArgumentException only if the DecodeMonitor strategy throws it (Strict parsing)
     */
    public static String decodeEncodedWords(String body, DecodeMonitor monitor, Charset fallback)
            throws IllegalArgumentException {
        int tailIndex = 0;
        boolean lastMatchValid = false;

        StringBuilder sb = new StringBuilder();

        for (Matcher matcher = PATTERN_ENCODED_WORD.matcher(body); matcher.find();) {
            String separator = body.substring(tailIndex, matcher.start());
            String mimeCharset = matcher.group(1);
            String encoding = matcher.group(2);
            String encodedText = matcher.group(3);

            if (encodedText.isEmpty())
                return "";

            String decoded;
            decoded = tryDecodeEncodedWord(mimeCharset, encoding, encodedText, monitor, fallback);
            if (decoded == null) {
                sb.append(separator);
                sb.append(matcher.group(0));
            } else {
                if (!lastMatchValid || !CharsetUtil.isWhitespace(separator)) {
                    sb.append(separator);
                }
                sb.append(decoded);
            }

            tailIndex = matcher.end();
            lastMatchValid = decoded != null;
        }

        if (tailIndex == 0) {
            return body;
        } else {
            sb.append(body.substring(tailIndex));
            return sb.toString();
        }
    }

    // return null on error
    private static String tryDecodeEncodedWord(final String mimeCharset,
            final String encoding, final String encodedText, final DecodeMonitor monitor, final Charset fallback) {
        Charset charset = CharsetUtil.lookup(mimeCharset);
        if (charset == null) {
            if(fallback == null) {
                monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                        "Mime charser '", mimeCharset, "' doesn't have a corresponding Java charset");
                return null;
            } else {
                charset = fallback;
            }
        }

        if (encodedText.length() == 0) {
            monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                    "Missing encoded text in encoded word");
            return null;
        }

        try {
            if (encoding.equalsIgnoreCase("Q")) {
                return DecoderUtil.decodeQ(encodedText, charset.name(), monitor);
            } else if (encoding.equalsIgnoreCase("B")) {
                return DecoderUtil.decodeB(encodedText, charset.name(), monitor);
            } else {
                monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                        "Warning: Unknown encoding in encoded word");
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            // should not happen because of isDecodingSupported check above
            monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                    "Unsupported encoding (", e.getMessage(), ") in encoded word");
            return null;
        } catch (RuntimeException e) {
            monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                    "Could not decode (", e.getMessage(), ") encoded word");
            return null;
        }
    }

    private static void monitor(DecodeMonitor monitor, String mimeCharset, String encoding,
            String encodedText, String dropDesc, String... strings) throws IllegalArgumentException {
        if (monitor.isListening()) {
            String encodedWord = recombine(mimeCharset, encoding, encodedText);
            StringBuilder text = new StringBuilder();
            for (String str : strings) {
                text.append(str);
            }
            text.append(" (");
            text.append(encodedWord);
            text.append(")");
            String exceptionDesc = text.toString();
            if (monitor.warn(exceptionDesc, dropDesc))
                throw new IllegalArgumentException(text.toString());
        }
    }

    private static String recombine(final String mimeCharset,
            final String encoding, final String encodedText) {
        return "=?" + mimeCharset + "?" + encoding + "?" + encodedText + "?=";
    }

    // Replace _ with =20
    private static String replaceUnderscores(String str) {
        // probably faster than String#replace(CharSequence, CharSequence)

        StringBuilder sb = new StringBuilder(128);

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                sb.append("=20");
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
