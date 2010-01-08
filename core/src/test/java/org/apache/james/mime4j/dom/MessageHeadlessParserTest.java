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

package org.apache.james.mime4j.dom;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.stream.MimeEntityConfig;

public class MessageHeadlessParserTest extends TestCase {


	public void testMalformedHeaderShouldEndHeader() throws Exception {
		String headlessContent = "Subject: my subject\r\n"
			    + "Hi, how are you?\r\n"
				+ "This is a simple message with no CRLFCELF between headers and body.\r\n"
				+ "ThisIsNotAnHeader: because this should be already in the body\r\n"
				+ "\r\n"
				+ "Instead this should be better parsed as a text/plain body\r\n";

		MimeEntityConfig mimeEntityConfig = new MimeEntityConfig();
		mimeEntityConfig.setMalformedHeaderStartsBody(true);
		MessageImpl message = new MessageImpl(new ByteArrayInputStream(headlessContent
				.getBytes("UTF-8")), mimeEntityConfig);
		assertEquals("text/plain", message.getMimeType());
		assertEquals(1, message.getHeader().getFields().size());
		BufferedReader reader = new BufferedReader(((TextBody) message.getBody()).getReader());
		String firstLine = reader.readLine();
		assertEquals("Hi, how are you?", firstLine);
	}

	public void testSimpleNonMimeTextHeadless() throws Exception {
		String headlessContent = "Hi, how are you?\r\n"
				+ "This is a simple message with no headers. While mime messages should start with\r\n"
				+ "header: headervalue\r\n"
				+ "\r\n"
				+ "Instead this should be better parsed as a text/plain body\r\n";

		MimeEntityConfig mimeEntityConfig = new MimeEntityConfig();
		mimeEntityConfig.setMalformedHeaderStartsBody(true);
		MessageImpl message = new MessageImpl(new ByteArrayInputStream(headlessContent
				.getBytes("UTF-8")), mimeEntityConfig);
		assertEquals("text/plain", message.getMimeType());
		assertEquals(0, message.getHeader().getFields().size());
		BufferedReader reader = new BufferedReader(((TextBody) message.getBody()).getReader());
		String firstLine = reader.readLine();
		assertEquals("Hi, how are you?", firstLine);
	}

	public void testMultipartFormContent() throws Exception {
		String contentType = "multipart/form-data; boundary=foo";
		String headlessContent = "\r\n"
				+ "--foo\r\nContent-Disposition: form-data; name=\"field01\""
				+ "\r\n"
				+ "\r\n"
				+ "this stuff\r\n"
				+ "--foo\r\n"
				+ "Content-Disposition: form-data; name=\"field02\"\r\n"
				+ "\r\n"
				+ "that stuff\r\n"
				+ "--foo\r\n"
				+ "Content-Disposition: form-data; name=\"field03\"; filename=\"mypic.jpg\"\r\n"
				+ "Content-Type: image/jpeg\r\n" + "\r\n"
				+ "all kind of stuff\r\n" + "--foo--\r\n";

		MimeEntityConfig mimeEntityConfig = new MimeEntityConfig();
		mimeEntityConfig.setDefaultContentType(contentType);
		MessageImpl message = new MessageImpl(new ByteArrayInputStream(headlessContent
				.getBytes("UTF-8")), mimeEntityConfig);
		assertEquals("multipart/form-data", message.getMimeType());
		assertEquals(1, message.getHeader().getFields().size());
		ContentTypeField contentTypeField = ((ContentTypeField) message
				.getHeader().getField(FieldName.CONTENT_TYPE));
		assertEquals("foo", contentTypeField.getBoundary());
		Multipart multipart = (Multipart) message.getBody();
		assertEquals(3, multipart.getCount());
	}
}
