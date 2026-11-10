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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Static methods for encoding header field values. This includes encoded-words
 * as defined in <a href='http://www.faqs.org/rfcs/rfc2047.html'>RFC 2047</a>
 * or display-names of an e-mail address, for example.
 */
public class EncoderUtil {
    private static final byte[] BASE64_TABLE = Base64OutputStream.BASE64_TABLE;
    private static final char BASE64_PAD = '=';

    private static final BitSet Q_REGULAR_CHARS = initChars("=_?");

    private static final BitSet Q_RESTRICTED_CHARS = initChars("=_?\"#$%&'(),.:;<>@[\\]^`{|}~");

    private static final int MAX_USED_CHARACTERS = 50;

    private static final String ENC_WORD_PREFIX = "=?";
    private static final String ENC_WORD_SUFFIX = "?=";

    private static final int ENCODED_WORD_MAX_LENGTH = 75; // RFC 2047

    private static final BitSet TOKEN_CHARS = initChars("()<>@,;:\\\"/[]?=");

    // RFC 2231 attribute-char: a token char other than "*", "'" and "%"
    private static final BitSet ATTRIBUTE_CHARS = initChars("()<>@,;:\\\"/[]?=*'%");

    private static final Pattern WHITESPACE = Pattern.compile("[ \t]");

    /** Longest run without whitespace written as is: it fits, with the folding space and a ";", on a 998 octet line */
    private static final int MAX_PARAMETER_LENGTH = 998 - 2;

    /** Length of the value of an RFC 2231 section written for a parameter that is too long */
    private static final int PARAMETER_SECTION_LENGTH = 60;

    private static final BitSet ATEXT_CHARS = initChars("()<>@.,;:\\\"[]");

    private static final Pattern QUOTE = Pattern.compile("[\\\\\"]");

    /** Characters that end or restructure an address in a header: never written raw in a domain */
    private static final String DOMAIN_STRUCTURE_CHARS = ",;:<>()\"@\\";
    // Inside a domain literal: ":" (IPv6 literals) and "(" ")" are read back as dtext
    private static final String DOMAIN_LITERAL_STRUCTURE_CHARS = ",;<>\"@";

    private static BitSet initChars(String specials) {
        BitSet bs = new BitSet(128);
        for (char ch = 33; ch < 127; ch++) {
            if (specials.indexOf(ch) == -1) {
                bs.set(ch);
            }
        }
        return bs;
    }

    /**
     * Selects one of the two encodings specified in RFC 2047.
     */
    public enum Encoding {
        /** The B encoding (identical to base64 defined in RFC 2045). */
        B,
        /** The Q encoding (similar to quoted-printable defined in RFC 2045). */
        Q
    }

    /**
     * Indicates the intended usage of an encoded word.
     */
    public enum Usage {
        /**
         * Encoded word is used to replace a 'text' token in any Subject or
         * Comments header field.
         */
        TEXT_TOKEN,
        /**
         * Encoded word is used to replace a 'word' entity within a 'phrase',
         * for example, one that precedes an address in a From, To, or Cc
         * header.
         */
        WORD_ENTITY
    }

    private EncoderUtil() {
    }

    /**
     * Encodes the display-name portion of an address. See <a
     * href='http://www.faqs.org/rfcs/rfc5322.html'>RFC 5322</a> section 3.4
     * and <a href='http://www.faqs.org/rfcs/rfc2047.html'>RFC 2047</a> section
     * 5.3. The specified string should not be folded.
     *
     * @param displayName
     *            display-name to encode.
     * @return encoded display-name.
     */
    public static String encodeAddressDisplayName(String displayName) {
        // display-name = phrase
        // phrase = 1*( encoded-word / word )
        // word = atom / quoted-string
        // atom = [CFWS] 1*atext [CFWS]
        // CFWS = comment or folding white space

        // CR and LF (control characters) make hasToBeEncoded true: never written raw
        if (hasToBeEncoded(displayName, 0)) {
            return encodeEncodedWord(displayName, Usage.WORD_ENTITY);
        } else if (isAtomPhrase(displayName)) {
            return displayName;
        } else {
            return quote(displayName);
        }
    }

    /**
     * Encodes the local part of an address specification as described in RFC
     * 5322 section 3.4.1. Leading and trailing CFWS should have been removed
     * before calling this method. The specified string should not contain any
     * illegal (control or non-ASCII) characters.
     *
     * @param localPart
     *            the local part to encode
     * @return the encoded local part.
     */
    public static String encodeAddressLocalPart(String localPart) {
        // local-part = dot-atom / quoted-string
        // dot-atom = [CFWS] dot-atom-text [CFWS]
        // CFWS = comment or folding white space

        if (isDotAtomText(localPart)) {
            return localPart;
        } else {
            // CR and LF can be neither qtext nor quoted-pair (RFC 5322 section 3.2.4): readers drop them
            return quote(localPart.replace("\r", "").replace("\n", ""));
        }
    }

    /**
     * Encodes the domain of an address specification as described in RFC 5322 section 3.4.1. A
     * dot-atom (non ASCII characters allowed, as in internationalized domains) is returned
     * unchanged. From a domain literal, the dtext characters a lenient reader takes for the end
     * of the address (",", ";", "&lt;", "&gt;", "\"", "@") are dropped. From any other domain, such as the ones a lenient parser reads from
     * malformed addresses, the characters that would change the structure of the header (e.g. a
     * "," adding recipients) are dropped.
     *
     * @param domain
     *            domain to encode.
     * @return the encoded domain.
     */
    public static String encodeAddressDomain(String domain) {
        // domain = dot-atom / domain-literal
        // domain-literal = [CFWS] "[" *([FWS] dtext) [FWS] "]" [CFWS]
        // dtext = %d33-90 / %d94-126
        if (isDotAtomText(domain, true)) {
            return domain;
        }
        if (isDomainLiteral(domain)) {
            return removeChars(domain, DOMAIN_LITERAL_STRUCTURE_CHARS);
        }
        StringBuilder sb = new StringBuilder(domain.length());
        for (int idx = 0; idx < domain.length(); idx++) {
            char ch = domain.charAt(idx);
            if (ch >= 128 || (ch > 32 && ch < 127 && DOMAIN_STRUCTURE_CHARS.indexOf(ch) == -1)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String removeChars(String str, String chars) {
        StringBuilder sb = null;
        for (int idx = 0; idx < str.length(); idx++) {
            char ch = str.charAt(idx);
            if (chars.indexOf(ch) != -1) {
                if (sb == null) {
                    sb = new StringBuilder(str.length());
                    sb.append(str, 0, idx);
                }
            } else if (sb != null) {
                sb.append(ch);
            }
        }
        return sb == null ? str : sb.toString();
    }

    private static boolean isDomainLiteral(String str) {
        final int length = str.length();
        if (length < 2 || str.charAt(0) != '[' || str.charAt(length - 1) != ']') {
            return false;
        }
        for (int idx = 1; idx < length - 1; idx++) {
            char ch = str.charAt(idx);
            boolean dtext = (ch >= 33 && ch <= 90) || (ch >= 94 && ch <= 126) || ch >= 128;
            if (!dtext && ch != ' ' && ch != '\t') {
                return false;
            }
        }
        return true;
    }

    /**
     * Encodes the specified strings into a header parameter as described in RFC
     * 2045 section 5.1 and RFC 2183 section 2. The specified strings should not
     * contain any illegal (control or non-ASCII) characters.
     *
     * @param name
     *            parameter name.
     * @param value
     *            parameter value.
     * @return encoded result.
     * @throws IllegalArgumentException if the name is not a token, or holds a "*" outside of the
     *             RFC 2231 forms (<code>name*</code>, <code>name*N</code>, <code>name*N*</code>).
     */
    public static String encodeHeaderParameter(String name, String value) {
        // A name that is not a token (";", "=") would add parameters, a stray "*" would be read
        // as an RFC 2231 section
        if (!isToken(name) || !isPlainOrRfc2231Name(name)) {
            throw new IllegalArgumentException("Invalid parameter name: " + name);
        }
        name = name.toLowerCase(Locale.US);

        return encodeHeaderParameterSection(name, value);
    }

    /**
     * Encodes the specified strings into a header parameter like
     * {@link #encodeHeaderParameter(String, String)}, for a header that is
     * written on lines of at most 998 octets (RFC 5322 section 2.1.1). Folding
     * only breaks a line at a whitespace: a parameter that holds a run without
     * whitespace too long for one line is split into RFC 2231 sections, percent encoded in UTF-8 when the value
     * is not printable ASCII, if the header parser of the field decodes them.
     *
     * @param name
     *            parameter name.
     * @param value
     *            parameter value.
     * <p>The readers of these fields decode the RFC 2047 encoded words of a parameter value, as
     * many mailers write <code>filename="=?utf-8?B?...?="</code>, so a value holding a literal
     * <code>=?</code> cannot be written as a plain quoted string. It is written as an RFC 2231
     * extended parameter (<code>name*=utf-8''%3D%3F...</code>) when the parser of the field
     * decodes them, and otherwise, when the value starts with <code>=?</code> (the only case the
     * lenient readers decode), as an encoded word of the whole value.</p>
     *
     * @param name
     *            parameter name.
     * @param value
     *            parameter value.
     * @param rfc2231Sections
     *            whether the parser of the field decodes RFC 2231 parameters, so that a
     *            parameter too long for one line may be split into RFC 2231 sections.
     * @return encoded result.
     * @throws IllegalArgumentException if the name is invalid, or if the
     *             parameter is too long for one line and cannot be split.
     */
    public static String encodeHeaderParameter(String name, String value, boolean rfc2231Sections) {
        String parameter = encodeHeaderParameter(name, value);
        if (name.indexOf('*') < 0 && value.contains("=?")) {
            String lowerName = name.toLowerCase(Locale.US);
            if (rfc2231Sections) {
                return encodeExtended(lowerName, value);
            } else if (value.startsWith("=?")) {
                parameter = lowerName + "=\"" + ENC_WORD_PREFIX + "utf-8?B?"
                        + encodeB(value.getBytes(Charsets.UTF_8)) + ENC_WORD_SUFFIX + "\"";
            }
        }
        if (fitsOnFoldedLines(parameter)) {
            return parameter;
        }
        name = name.toLowerCase(Locale.US);
        if (!rfc2231Sections || name.indexOf('*') >= 0) {
            throw new IllegalArgumentException("Parameter too long for a header line: " + name);
        }
        return isPrintableAscii(value) ? encodeSections(name, value) : encodeEncodedSections(name, value);
    }

    private static String encodeHeaderParameterSection(String name, String value) {
        // value := token / quoted-string
        if (isToken(value)) {
            return name + "=" + value;
        } else {
            return name + "=" + quote(value);
        }
    }

    private static boolean fitsOnFoldedLines(String parameter) {
        for (String run : WHITESPACE.split(parameter)) {
            if (run.getBytes(Charsets.UTF_8).length > MAX_PARAMETER_LENGTH) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPrintableAscii(String value) {
        for (int idx = 0; idx < value.length(); idx++) {
            char ch = value.charAt(idx);
            if (ch < 32 || ch >= 127) {
                return false;
            }
        }
        return true;
    }

    // RFC 2231 section 3: name*0="..."; name*1="..."
    private static String encodeSections(String name, String value) {
        StringBuilder sb = new StringBuilder();
        int section = 0;
        for (int start = 0; start < value.length(); start += PARAMETER_SECTION_LENGTH) {
            if (section > 0) {
                sb.append("; ");
            }
            String part = value.substring(start, Math.min(value.length(), start + PARAMETER_SECTION_LENGTH));
            sb.append(encodeHeaderParameterSection(name + "*" + section, part));
            section++;
        }
        return sb.toString();
    }

    // RFC 2231 sections 3 and 4: name*0*=utf-8''%XX...; name*1*=%XX...
    private static String encodeEncodedSections(String name, String value) {
        StringBuilder sb = new StringBuilder();
        StringBuilder current = new StringBuilder("utf-8''");
        int section = 0;
        for (byte b : value.getBytes(Charsets.UTF_8)) {
            int v = b & 0xff;
            String encoded = ATTRIBUTE_CHARS.get(v)
                    ? String.valueOf((char) v)
                    : "%" + hexDigit(v >>> 4) + hexDigit(v & 0xf);
            if (current.length() + encoded.length() > PARAMETER_SECTION_LENGTH) {
                appendEncodedSection(sb, name, section++, current);
                current.setLength(0);
            }
            current.append(encoded);
        }
        appendEncodedSection(sb, name, section, current);
        return sb.toString();
    }

    // RFC 2231 section 4: name*=utf-8''%XX..., in sections when too long for one
    private static String encodeExtended(String name, String value) {
        String sections = encodeEncodedSections(name, value);
        String single = name + "*0*=";
        if (sections.indexOf(';') < 0 && sections.startsWith(single)) {
            return name + "*=" + sections.substring(single.length());
        }
        return sections;
    }

    private static void appendEncodedSection(StringBuilder sb, String name, int section, CharSequence value) {
        if (section > 0) {
            sb.append("; ");
        }
        sb.append(name).append('*').append(section).append("*=").append(value);
    }

    /**
     * Shortcut method that encodes the specified text into an encoded-word if
     * the text has to be encoded.
     *
     * @param text
     *            text to encode.
     * @param usage
     *            whether the encoded-word is to be used to replace a text token
     *            or a word entity (see RFC 822).
     * @param usedCharacters
     *            number of characters already used up (<code>0 &lt;= usedCharacters &lt;= 50</code>).
     * @return the specified text if encoding is not necessary or an encoded
     *         word or a sequence of encoded words otherwise.
     */
    public static String encodeIfNecessary(String text, Usage usage,
            int usedCharacters) {
        if (hasToBeEncoded(text, usedCharacters))
            return encodeEncodedWord(text, usage, usedCharacters);
        else
            return text;
    }

    /**
     * Determines if the specified string has to encoded into an encoded-word.
     * Returns <code>true</code> if the text contains characters that don't
     * fall into the printable ASCII character set, if the text contains a
     * 'word' (sequence of non-whitespace characters) longer than 77 characters
     * (including characters already used up in the line) or if the text contains
     * <code>=?</code>, which readers would take for the start of an encoded word
     * (RFC 2047 section 5).
     *
     * @param text
     *            text to analyze.
     * @param usedCharacters
     *            number of characters already used up (<code>0 &lt;= usedCharacters &lt;= 50</code>).
     * @return <code>true</code> if the specified text has to be encoded into
     *         an encoded-word, <code>false</code> otherwise.
     */
    public static boolean hasToBeEncoded(String text, int usedCharacters) {
        if (text == null)
            throw new IllegalArgumentException();
        if (usedCharacters < 0 || usedCharacters > MAX_USED_CHARACTERS)
            throw new IllegalArgumentException();

        int nonWhiteSpaceCount = usedCharacters;

        for (int idx = 0; idx < text.length(); idx++) {
            char ch = text.charAt(idx);
            if (ch == '\t' || ch == ' ') {
                nonWhiteSpaceCount = 0;
            } else {
                nonWhiteSpaceCount++;
                if (nonWhiteSpaceCount > 77) {
                    // Line cannot be folded into multiple lines with no more
                    // than 78 characters each. Encoding as encoded-words makes
                    // that possible. One character has to be reserved for
                    // folding white space; that leaves 77 characters.
                    return true;
                }

                if (ch < 32 || ch >= 127) {
                    // non-printable ascii character has to be encoded
                    return true;
                }

                if (ch == '=' && idx + 1 < text.length() && text.charAt(idx + 1) == '?') {
                    // would be decoded as an encoded word when read back
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Encodes the specified text into an encoded word or a sequence of encoded
     * words separated by space. The text is separated into a sequence of
     * encoded words if it does not fit in a single one.
     * <p>
     * The charset to encode the specified text into a byte array and the
     * encoding to use for the encoded-word are detected automatically.
     * <p>
     * This method assumes that zero characters have already been used up in the
     * current line.
     *
     * @param text
     *            text to encode.
     * @param usage
     *            whether the encoded-word is to be used to replace a text token
     *            or a word entity (see RFC 822).
     * @return the encoded word (or sequence of encoded words if the given text
     *         does not fit in a single encoded word).
     * @see #hasToBeEncoded(String, int)
     */
    public static String encodeEncodedWord(String text, Usage usage) {
        return encodeEncodedWord(text, usage, 0, null, null);
    }

    /**
     * Encodes the specified text into an encoded word or a sequence of encoded
     * words separated by space. The text is separated into a sequence of
     * encoded words if it does not fit in a single one.
     * <p>
     * The charset to encode the specified text into a byte array and the
     * encoding to use for the encoded-word are detected automatically.
     *
     * @param text
     *            text to encode.
     * @param usage
     *            whether the encoded-word is to be used to replace a text token
     *            or a word entity (see RFC 822).
     * @param usedCharacters
     *            number of characters already used up (<code>0 &lt;= usedCharacters &lt;= 50</code>).
     * @return the encoded word (or sequence of encoded words if the given text
     *         does not fit in a single encoded word).
     * @see #hasToBeEncoded(String, int)
     */
    public static String encodeEncodedWord(String text, Usage usage,
            int usedCharacters) {
        return encodeEncodedWord(text, usage, usedCharacters, null, null);
    }

    /**
     * Encodes the specified text into an encoded word or a sequence of encoded
     * words separated by space. The text is separated into a sequence of
     * encoded words if it does not fit in a single one.
     *
     * @param text
     *            text to encode.
     * @param usage
     *            whether the encoded-word is to be used to replace a text token
     *            or a word entity (see RFC 822).
     * @param usedCharacters
     *            number of characters already used up (<code>0 &lt;= usedCharacters &lt;= 50</code>).
     * @param charset
     *            the Java charset that should be used to encode the specified
     *            string into a byte array. A suitable charset is detected
     *            automatically if this parameter is <code>null</code> or names
     *            a charset that cannot encode (e.g. ISO-2022-CN).
     * @param encoding
     *            the encoding to use for the encoded-word (either B or Q). A
     *            suitable encoding is automatically chosen if this parameter is
     *            <code>null</code>.
     * @return the encoded word (or sequence of encoded words if the given text
     *         does not fit in a single encoded word).
     * @see #hasToBeEncoded(String, int)
     */
    public static String encodeEncodedWord(String text, Usage usage,
            int usedCharacters, Charset charset, Encoding encoding) {
        if (text == null)
            throw new IllegalArgumentException();
        if (usedCharacters < 0 || usedCharacters > MAX_USED_CHARACTERS)
            throw new IllegalArgumentException();

        if (charset == null || !charset.canEncode())
            charset = determineCharset(text);

        byte[] bytes = encode(text, charset);

        if (encoding == null)
            encoding = determineEncoding(bytes, usage);

        if (encoding == Encoding.B) {
            String prefix = ENC_WORD_PREFIX + charset.name() + "?B?";
            return encodeB(prefix, text, usedCharacters, charset, bytes);
        } else {
            String prefix = ENC_WORD_PREFIX + charset.name() + "?Q?";
            return encodeQ(prefix, text, usage, usedCharacters, charset, bytes);
        }
    }

    /**
     * Encodes the specified byte array using the B encoding defined in RFC
     * 2047.
     *
     * @param bytes
     *            byte array to encode.
     * @return encoded string.
     */
    public static String encodeB(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        int idx = 0;
        final int end = bytes.length;
        for (; idx < end - 2; idx += 3) {
            int data = (bytes[idx] & 0xff) << 16 | (bytes[idx + 1] & 0xff) << 8
                    | bytes[idx + 2] & 0xff;
            sb.append((char) BASE64_TABLE[data >> 18 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 12 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 6 & 0x3f]);
            sb.append((char) BASE64_TABLE[data & 0x3f]);
        }

        if (idx == end - 2) {
            int data = (bytes[idx] & 0xff) << 16 | (bytes[idx + 1] & 0xff) << 8;
            sb.append((char) BASE64_TABLE[data >> 18 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 12 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 6 & 0x3f]);
            sb.append(BASE64_PAD);

        } else if (idx == end - 1) {
            int data = (bytes[idx] & 0xff) << 16;
            sb.append((char) BASE64_TABLE[data >> 18 & 0x3f]);
            sb.append((char) BASE64_TABLE[data >> 12 & 0x3f]);
            sb.append(BASE64_PAD);
            sb.append(BASE64_PAD);
        }

        return sb.toString();
    }

    /**
     * Encodes the specified byte array using the Q encoding defined in RFC
     * 2047.
     *
     * @param bytes
     *            byte array to encode.
     * @param usage
     *            whether the encoded-word is to be used to replace a text token
     *            or a word entity (see RFC 822).
     * @return encoded string.
     */
    public static String encodeQ(byte[] bytes, Usage usage) {
        BitSet qChars = usage == Usage.TEXT_TOKEN ? Q_REGULAR_CHARS
                : Q_RESTRICTED_CHARS;

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            int v = aByte & 0xff;
            if (v == 32) {
                sb.append('_');
            } else if (!qChars.get(v)) {
                sb.append('=');
                sb.append(hexDigit(v >>> 4));
                sb.append(hexDigit(v & 0xf));
            } else {
                sb.append((char) v);
            }
        }

        return sb.toString();
    }

    /**
     * Encodes the given stream using Quoted-Printable.
     * This assumes that stream is binary and therefore escapes
     * all line endings.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void encodeQBinary(final InputStream in, final OutputStream out) throws IOException {
        QuotedPrintableOutputStream qpOut = new QuotedPrintableOutputStream(out, true);
        ContentUtil.copy(in, qpOut);
        qpOut.close();
    }

    /**
     * Encodes the given stream using Quoted-Printable.
     * This assumes that stream is text and therefore does not escape
     * all line endings.
     * @param in not null
     * @param out not null
     * @throws IOException
     */
    public static void encodeQ(final InputStream in, final OutputStream out) throws IOException {
        QuotedPrintableOutputStream qpOut = new QuotedPrintableOutputStream(out, false);
        ContentUtil.copy(in, qpOut);
        qpOut.close();
    }

    /**
     * Encodes the given stream using base64.
     *
     * @param in not null
     * @param out not null
     * @throws IOException if an I/O error occurs
     */
    public static void encodeB(final InputStream in, final OutputStream out) throws IOException {
        Base64OutputStream b64Out = new Base64OutputStream(out);
        ContentUtil.copy(in, b64Out);
        b64Out.close();
    }

    /**
     * Tests whether the specified string is a token as defined in RFC 2045
     * section 5.1.
     *
     * @param str
     *            string to test.
     * @return <code>true</code> if the specified string is a RFC 2045 token,
     *         <code>false</code> otherwise.
     */
    public static boolean isToken(String str) {
        // token := 1*<any (US-ASCII) CHAR except SPACE, CTLs, or tspecials>
        // tspecials := "(" / ")" / "<" / ">" / "@" / "," / ";" / ":" / "\" /
        // <"> / "/" / "[" / "]" / "?" / "="
        // CTL := 0.- 31., 127.

        final int length = str.length();
        if (length == 0)
            return false;

        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            if (!TOKEN_CHARS.get(ch))
                return false;
        }

        return true;
    }

    private static boolean isAtomPhrase(String str) {
        // atom = [CFWS] 1*atext [CFWS]
        // Readers drop leading and trailing whitespace and collapse runs into one SP:
        // only words separated by a single SP read back unchanged

        boolean containsAText = false;
        boolean previousIsSpace = true;

        final int length = str.length();
        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            if (ATEXT_CHARS.get(ch)) {
                containsAText = true;
                previousIsSpace = false;
            } else if (ch == ' ' && !previousIsSpace) {
                previousIsSpace = true;
            } else {
                return false;
            }
        }

        return containsAText && !previousIsSpace;
    }

    /**
     * Whether the name holds no "*", or holds it in one of the RFC 2231 forms:
     * <code>name*</code>, <code>name*N</code> or <code>name*N*</code>.
     */
    private static boolean isPlainOrRfc2231Name(String name) {
        int star = name.indexOf('*');
        if (star < 0) {
            return true;
        }
        if (star == 0) {
            return false;
        }
        int idx = star + 1;
        final int length = name.length();
        while (idx < length && name.charAt(idx) >= '0' && name.charAt(idx) <= '9') {
            idx++;
        }
        if (idx == length) {
            // name* or name*N
            return true;
        }
        // name*N*: digits required before the last star
        return idx > star + 1 && idx == length - 1 && name.charAt(idx) == '*';
    }

    // RFC 5322 section 3.2.3
    private static boolean isDotAtomText(String str) {
        return isDotAtomText(str, false);
    }

    private static boolean isDotAtomText(String str, boolean allowNonAscii) {
        // dot-atom-text = 1*atext *("." 1*atext)
        // atext = ALPHA / DIGIT / "!" / "#" / "$" / "%" / "&" / "'" / "*" /
        // "+" / "-" / "/" / "=" / "?" / "^" / "_" / "`" / "{" / "|" / "}" / "~"

        char prev = '.';

        final int length = str.length();
        if (length == 0)
            return false;

        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);

            if (ch == '.') {
                if (prev == '.' || idx == length - 1)
                    return false;
            } else {
                if (!ATEXT_CHARS.get(ch) && !(allowNonAscii && ch >= 128))
                    return false;
            }

            prev = ch;
        }

        return true;
    }

    // RFC 5322 section 3.2.4
    private static String quote(String str) {
        // quoted-string = [CFWS] DQUOTE *([FWS] qcontent) [FWS] DQUOTE [CFWS]
        // qcontent = qtext / quoted-pair
        // qtext = %d33 / %d35-91 / %d93-126
        // quoted-pair = ("\" (VCHAR / WSP))
        // VCHAR = %x21-7E
        // DQUOTE = %x22

        return '"' + QUOTE.matcher(str).replaceAll("\\\\$0") + '"';
    }

    private static String encodeB(String prefix, String text,
            int usedCharacters, Charset charset, byte[] bytes) {
        int encodedLength = bEncodedLength(bytes);

        int totalLength = prefix.length() + encodedLength
                + ENC_WORD_SUFFIX.length();
        if (totalLength <= ENCODED_WORD_MAX_LENGTH - usedCharacters) {
            return prefix + encodeB(bytes) + ENC_WORD_SUFFIX;
        } else {
            int splitOffset = text.offsetByCodePoints(text.length() / 2, -1);
            String part1 = text.substring(0, splitOffset);
            byte[] bytes1 = encode(part1, charset);
            String word1 = encodeB(prefix, part1, usedCharacters, charset,
                    bytes1);

            String part2 = text.substring(splitOffset);
            byte[] bytes2 = encode(part2, charset);
            String word2 = encodeB(prefix, part2, 0, charset, bytes2);

            return word1 + " " + word2;
        }
    }

    private static int bEncodedLength(byte[] bytes) {
        return (bytes.length + 2) / 3 * 4;
    }

    private static String encodeQ(String prefix, String text, Usage usage,
            int usedCharacters, Charset charset, byte[] bytes) {
        int encodedLength = qEncodedLength(bytes, usage);

        int totalLength = prefix.length() + encodedLength
                + ENC_WORD_SUFFIX.length();
        if (totalLength <= ENCODED_WORD_MAX_LENGTH - usedCharacters) {
            return prefix + encodeQ(bytes, usage) + ENC_WORD_SUFFIX;
        } else {
            int splitOffset = text.offsetByCodePoints(text.length() / 2, -1);
            String part1 = text.substring(0, splitOffset);
            byte[] bytes1 = encode(part1, charset);
            String word1 = encodeQ(prefix, part1, usage, usedCharacters,
                    charset, bytes1);

            String part2 = text.substring(splitOffset);
            byte[] bytes2 = encode(part2, charset);
            String word2 = encodeQ(prefix, part2, usage, 0, charset, bytes2);

            return word1 + " " + word2;
        }
    }

    private static int qEncodedLength(byte[] bytes, Usage usage) {
        BitSet qChars = usage == Usage.TEXT_TOKEN ? Q_REGULAR_CHARS
                : Q_RESTRICTED_CHARS;

        int count = 0;

        for (byte aByte : bytes) {
            int v = aByte & 0xff;
            if (v == 32) {
                count++;
            } else if (!qChars.get(v)) {
                count += 3;
            } else {
                count++;
            }
        }

        return count;
    }

    private static byte[] encode(String text, Charset charset) {
        ByteBuffer buffer = charset.encode(text);
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return bytes;
    }

    private static Charset determineCharset(String text) {
        // it is an important property of iso-8859-1 that it directly maps
        // unicode code points 0000 to 00ff to byte values 00 to ff.
        boolean ascii = true;
        final int len = text.length();
        for (int index = 0; index < len; index++) {
            char ch = text.charAt(index);
            if (ch > 0xff) {
                return Charsets.UTF_8;
            }
            if (ch > 0x7f) {
                ascii = false;
            }
        }
        return ascii ? Charsets.US_ASCII : Charsets.ISO_8859_1;
    }

    private static Encoding determineEncoding(byte[] bytes, Usage usage) {
        if (bytes.length == 0)
            return Encoding.Q;

        BitSet qChars = usage == Usage.TEXT_TOKEN ? Q_REGULAR_CHARS
                : Q_RESTRICTED_CHARS;

        int qEncoded = 0;
        for (byte aByte : bytes) {
            int v = aByte & 0xff;
            if (v != 32 && !qChars.get(v)) {
                qEncoded++;
            }
        }

        int percentage = qEncoded * 100 / bytes.length;
        return percentage > 30 ? Encoding.B : Encoding.Q;
    }

    private static char hexDigit(int i) {
        return i < 10 ? (char) (i + '0') : (char) (i - 10 + 'A');
    }
}
