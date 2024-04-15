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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.util.BufferRecycler;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.RecycledByteArrayBuffer;

/**
 * Static methods for decoding strings, byte arrays and encoded words.
 */
public class DecoderUtil {
    protected static final ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef = new ThreadLocal<>();

    public static BufferRecycler getBufferRecycler() {
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        BufferRecycler br = (ref == null) ? null : ref.get();

        if (br == null) {
            br = new BufferRecycler();
            ref = new SoftReference<>(br);
            _recyclerRef.set(ref);
        }
        return br;
    }

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
            RecycledByteArrayBuffer buf = new RecycledByteArrayBuffer(getBufferRecycler(), s.length());
            try {
                int b;
                while ((b = is.read()) != -1) {
                    buf.append(b);
                }
                return buf.toByteArray();
            } finally {
                is.close();
                buf.release();
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
            RecycledByteArrayBuffer buf = new RecycledByteArrayBuffer(getBufferRecycler(), s.length());
            try {
                int b;
                while ((b = is.read()) != -1) {
                    buf.append(b);
                }
                return buf.toByteArray();
            } finally {
                is.close();
                buf.release();
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

    static byte[] decodeByteAryB(String encodedText, DecodeMonitor monitor) throws UnsupportedEncodingException {
        return decodeBase64(encodedText, monitor);
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

    static byte[] decodeByteAryQ(String encodedText, DecodeMonitor monitor) throws UnsupportedEncodingException {
        return decodeQuotedPrintable(replaceUnderscores(encodedText), monitor);
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
        return decodeEncodedWords(body, monitor, null, Collections.emptyMap());
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
        return decodeEncodedWords(body, null, fallback, Collections.emptyMap());
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
        return decodeEncodedWords(body, monitor, fallback, Collections.emptyMap());
    }

    /**
     * Decodes a string containing encoded words as defined by RFC 2047. Encoded
     * words have the form =?charset?enc?encoded-text?= where enc is either 'Q'
     * or 'q' for quoted-printable and 'B' or 'b' for base64. Using fallback
     * charset if charset in encoded words is invalid. Additionally, the found charset
     * will be overridden if a corresponding mapping is found.
     *
     * @param body the string to decode
     * @param monitor the DecodeMonitor to be used.
     * @param fallback the fallback Charset to be used.
     * @param charsetOverrides the Charsets to override and their replacements. Must not be null.
     * @return the decoded string.
     * @throws IllegalArgumentException only if the DecodeMonitor strategy throws it (Strict parsing)
     */
    public static String decodeEncodedWords(String body, DecodeMonitor monitor, Charset fallback,
            Map<Charset, Charset> charsetOverrides)
            throws IllegalArgumentException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();
        int position = 0;
        String mimeCharset ="";
        String encoding ="";

        while (position < body.length()) {
            int startPattern = body.indexOf("=?", position);
            if (startPattern < 0) {
                if (position == 0) {
                    return body;
                }
                appendStringBuffer(fallback, charsetOverrides, out, sb, mimeCharset);
                sb.append(body, position, body.length());
                break;
            }

            int charsetEnd = body.indexOf('?', startPattern + 2);
            int encodingEnd = body.indexOf('?', charsetEnd + 1);
            int encodedTextEnd = body.indexOf("?=", encodingEnd + 1);

            if (charsetEnd < 0 || encodingEnd < 0 || encodedTextEnd < 0) {
                // Invalid pattern
                appendStringBuffer(fallback, charsetOverrides, out, sb, mimeCharset);
                sb.append(body, position, startPattern + 2);
                position = startPattern + 2;
            } else if (encodingEnd == encodedTextEnd) {
                appendStringBuffer(fallback, charsetOverrides, out, sb, mimeCharset);
                sb.append(body, position, Math.min(encodedTextEnd + 2, body.length()));
                position = encodedTextEnd +2;
            } else {
                String separator = body.substring(position, startPattern);
                if ((!CharsetUtil.isWhitespace(separator) || position == 0) && !separator.isEmpty()) {
                    appendStringBuffer(fallback, charsetOverrides, out, sb, mimeCharset);
                    sb.append(separator);
                }
                String mimeCurCharset = body.substring(startPattern + 2, charsetEnd);
                String curEncoding = body.substring(charsetEnd + 1, encodingEnd);
                String encodedText = body.substring(encodingEnd + 1, encodedTextEnd);

                if (!mimeCharset.isEmpty() && !mimeCurCharset.equals(mimeCharset)){
                    appendStringBuffer(fallback, charsetOverrides, out, sb, mimeCharset);
                }

                if (!encoding.isEmpty() && !curEncoding.equals(encoding)){
                    appendStringBuffer(fallback, charsetOverrides, out, sb, mimeCharset);
                }

                mimeCharset=mimeCurCharset;
                encoding=curEncoding;

                if (encodedText.isEmpty()) {
                    position = encodedTextEnd + 2;
                    continue;
                }
                byte []decoded;

                decoded = tryDecodeEncodedWord(mimeCurCharset, curEncoding, encodedText, monitor, fallback, charsetOverrides);
                if (decoded != null) {
                    if (0 < decoded.length) {
                        try {
                            out.write(decoded);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    sb.append(body, startPattern, encodedTextEnd + 2);
                }
                position = encodedTextEnd + 2;
            }
        }
        appendStringBuffer(fallback, charsetOverrides, out, sb,mimeCharset);

        return sb.toString();
    }

    private static void appendStringBuffer(Charset fallback, Map<Charset, Charset> charsetOverrides, ByteArrayOutputStream out, StringBuilder sb, String mimeCharset) {
        if (0 < out.size()) {
            byte[] byTemp = out.toByteArray();
            Charset charset = lookupCharset(mimeCharset, fallback, charsetOverrides);
            if (null == charset) {
                int aaa = 0;
                System.out.println(aaa);
            }
            sb.append(new String(byTemp, charset));
            out.reset();
        }
    }

    // return null on error
    private static byte[] tryDecodeEncodedWord(
            final String mimeCharset,
            final String encoding,
            final String encodedText,
            final DecodeMonitor monitor,
            final Charset fallback,
            final Map<Charset, Charset> charsetOverrides) {
        Charset charset = lookupCharset(mimeCharset, fallback, charsetOverrides);
        if (charset == null) {
            monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                    "Mime charser '", mimeCharset, "' doesn't have a corresponding Java charset");
            return null;
        }

        if (encodedText.length() == 0) {
            monitor(monitor, mimeCharset, encoding, encodedText, "leaving word encoded",
                    "Missing encoded text in encoded word");
            return null;
        }

        try {
            if (encoding.equalsIgnoreCase("Q")) {
                return decodeByteAryQ(encodedText, monitor);
            } else if (encoding.equalsIgnoreCase("B")) {
                return decodeByteAryB(encodedText, monitor);
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

    private static Charset lookupCharset(
            final String mimeCharset,
            final Charset fallback,
            final Map<Charset, Charset> charsetOverrides) {
        Charset charset = CharsetUtil.lookup(mimeCharset);
        if (charset == null) {
            return fallback;
        }
        Charset override = charsetOverrides.get(charset);
        return override != null ? override : charset;
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
