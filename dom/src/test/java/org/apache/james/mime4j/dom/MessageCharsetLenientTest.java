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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * check that the Charset handling of BasicBodyFactory can be influenced with
 * the boolean lenient flag
 * 
 * @author wf
 *
 */
public class MessageCharsetLenientTest {

	/**
	 * set up a message with an invalid charset
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLenientCharsetHandling() throws Exception {
		// this list of invalidCharsets is taken from parsing a sample of some 1/4 million e-mails
		// so all of the showed up in real world e-mails
		String invalidCharsets[] = {
				"%CHARSET",
				"'iso-8859-1'",
				"'utf-8'",
				"0",
				"238",
				"DEFAULT_CHARSET",
				"DIN_66003",
				"ISO 8859-1",
				"None",
				"Standard",
				"UTF-7",
				"X-CTEXT",
				"X-UNKNOWN",
				"\\iso-8859-1\"",
				"\\us-ascii\"",
				"ansi_x3.110-1983",
				"charset=us-ascii",
				"en",
				"iso-0-250-250-250-25-0-25",
				"iso-10646",
				"iso-1149-1",
				"iso-2191-1",
				"iso-3817-4",
				"iso-4736-8",
				"iso-5266-7",
				"iso-5666-3",
				"iso-5978-6",
				"iso-6558-5",
				"iso-7708-8",
				"iso-8085-5",
				"iso-8589-0",
				"iso-8814-4",
				"iso-8859-1 name=FAQ.htm",
				"iso-8859-16",
				"iso-8859-1?",
				"iso-8859-8-i",
				"iso-9284-4",
				"latin-iso8859-1",
				"unicode-1-1-utf-7",
				"unknown-8bit",
				"utf-7",
				"windows-1250 reply-type=original",
				"windows-1252 <!DOCTYPE HTML PUBLIC -//W3C//DTD HTML 4.01 Transitional//EN>",
				"x-user-defined", " {$RND_CHARSET$}" };
		
		// check with lenient charset handling on and off
		boolean[] lenientstates = { true, false };
		// create the message builder
		DefaultMessageBuilder builder = new DefaultMessageBuilder();
		// count how many Exception hits we got
		int invalidCount=0;
		// test in bosh states
		for (boolean lenient : lenientstates) {
			// set how lenient we are
			BasicBodyFactory basicBodyFactory = new BasicBodyFactory(lenient);
            builder.setBodyFactory(basicBodyFactory);
			// check the list of invalid Charsets
			for (String invalidCharset : invalidCharsets) {
				// create a message with the charset 
				String charsetContent = "Subject: my subject\r\n"
						+ "Content-Type: text/plain; charset=" + invalidCharset + "\r\n"
						+ "Strange charset isn't it?\r" + "\r\n";
        // try parsing it
				try {
					Message message = builder.parseMessage(new ByteArrayInputStream(
							charsetContent.getBytes("UTF-8")));
					// check some message attribute
					Assert.assertEquals("text/plain", message.getMimeType());
					// if we get here we had a lenient mode - in non lenient an exception would have been thrown
					Assert.assertTrue("Charset:"+invalidCharset+" should not be allowed when lenient is "+lenient,lenient);
				} catch (UnsupportedEncodingException ex) {
					Assert.assertFalse("Charset:"+invalidCharset+" should not throw an exception when lenient is "+lenient,lenient);
					invalidCount++;
				}
			}
		} // for
		Assert.assertEquals(invalidCharsets.length,invalidCount);
	}

}
