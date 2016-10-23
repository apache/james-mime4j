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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.internet.MimeUtility;

/**
 * Static methods for decoding strings, byte arrays and encoded words.
 */
public class DecoderUtil {

	/**
	 * Decodes a string containing quoted-printable encoded data.
	 *
	 * @param s
	 *            the string to decode.
	 * @return the decoded bytes.
	 */
	private static byte[] decodeQuotedPrintable(String s, DecodeMonitor monitor) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			byte[] bytes = s.getBytes("US-ASCII");

			QuotedPrintableInputStream is = new QuotedPrintableInputStream(new ByteArrayInputStream(bytes), monitor);

			int b = 0;
			while ((b = is.read()) != -1) {
				baos.write(b);
			}
		} catch (IOException e) {
			// This should never happen!
			throw new IllegalStateException(e);
		}

		return baos.toByteArray();
	}

	/**
	 * Decodes a string containing base64 encoded data.
	 *
	 * @param s
	 *            the string to decode.
	 * @param monitor
	 * @return the decoded bytes.
	 */
	private static byte[] decodeBase64(String s, DecodeMonitor monitor) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			byte[] bytes = s.getBytes("US-ASCII");

			Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(bytes), monitor);

			int b = 0;
			while ((b = is.read()) != -1) {
				baos.write(b);
			}
		} catch (IOException e) {
			// This should never happen!
			throw new IllegalStateException(e);
		}

		return baos.toByteArray();
	}

	/**
	 * Decodes an encoded text encoded with the 'B' encoding (described in RFC
	 * 2047) found in a header field body.
	 *
	 * @param encodedText
	 *            the encoded text to decode.
	 * @param charset
	 *            the Java charset to use.
	 * @param monitor
	 * @return the decoded string.
	 * @throws UnsupportedEncodingException
	 *             if the given Java charset isn't supported.
	 */
	static String decodeB(String encodedText, String charset, DecodeMonitor monitor)
			throws UnsupportedEncodingException {
		byte[] decodedBytes = decodeBase64(encodedText, monitor);
		return new String(decodedBytes, charset);
	}

	/**
	 * Decodes an encoded text encoded with the 'Q' encoding (described in RFC
	 * 2047) found in a header field body.
	 *
	 * @param encodedText
	 *            the encoded text to decode.
	 * @param charset
	 *            the Java charset to use.
	 * @return the decoded string.
	 * @throws UnsupportedEncodingException
	 *             if the given Java charset isn't supported.
	 */
	static String decodeQ(String encodedText, String charset, DecodeMonitor monitor)
			throws UnsupportedEncodingException {
		encodedText = replaceUnderscores(encodedText);

		byte[] decodedBytes = decodeQuotedPrintable(encodedText, monitor);
		return new String(decodedBytes, charset);
	}

	static String decodeEncodedWords(String body) {
		return decodeEncodedWords(body, DecodeMonitor.SILENT);
	}

	/**
	 * Decodes a string containing encoded words as defined by RFC 2047. Encoded
	 * words have the form =?charset?enc?encoded-text?= where enc is either 'Q'
	 * or 'q' for quoted-printable and 'B' or 'b' for base64.
	 *
	 * @param body
	 *            the string to decode
	 * @param monitor
	 *            the DecodeMonitor to be used.
	 * @return the decoded string.
	 * @throws IllegalArgumentException
	 *             only if the DecodeMonitor strategy throws it (Strict parsing)
	 */
	public static String decodeEncodedWords(String body, DecodeMonitor monitor) throws IllegalArgumentException {
		try {
			return MimeUtility.decodeText(body);
		} catch (UnsupportedEncodingException e) {
			if (monitor.isListening()) {
				throw new IllegalArgumentException(e);
			} else {
				return body;
			}
		}
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
