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

package org.apache.james.mime4j.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.field.Field;

import junit.framework.TestCase;

/**
 * 
 *
 * 
 * @version $Id:$
 */
public class MultipartFormTest extends TestCase {

    public void testMultipartFormContent() throws Exception {
        Message message = new Message();
        Header header = new Header();
        header.addField(
                Field.parse("Content-Type: multipart/form-data; boundary=foo"));
        message.setHeader(header);
        
        Multipart multipart = new Multipart("alternative");
        multipart.setParent(message);
        BodyPart p1 = new BodyPart();
        Header h1 = new Header();
        h1.addField(Field.parse("Content-Type: text/plain"));
        p1.setHeader(h1);
        p1.setBody(new StringPart("this stuff"));
        BodyPart p2 = new BodyPart();
        Header h2 = new Header();
        h2.addField(Field.parse("Content-Type: text/plain"));
        p2.setHeader(h2);
        p2.setBody(new StringPart("that stuff"));
        BodyPart p3 = new BodyPart();
        Header h3 = new Header();
        h3.addField(Field.parse("Content-Type: text/plain"));
        p3.setHeader(h3);
        p3.setBody(new StringPart("all kind of stuff"));

        multipart.addBodyPart(p1);
        multipart.addBodyPart(p2);
        multipart.addBodyPart(p3);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        multipart.writeTo(out);
        out.close();
        
        String expected = "\r\n" + 
            "--foo\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "this stuff\r\n" +
            "--foo\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "that stuff\r\n" +
            "--foo\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "all kind of stuff\r\n" +
            "--foo--\r\n" +
            "\r\n";
        String s = out.toString("US-ASCII");
        assertEquals(expected, s);
    }
    
    public class StringPart extends AbstractBody implements TextBody {

        private final String text;
        private final String charset;
        
        public StringPart(final String text, String charset) {
            super();
            if (text == null) {
                throw new IllegalArgumentException("Text may not be null");
            }
            if (charset == null) {
                charset = "UTF-8";
            }
            this.text = text;
            this.charset = charset;
        }
        
        public StringPart(final String text) {
            this(text, null);
        }
        
        public Reader getReader() throws IOException {
            return new StringReader(this.text);
        }

        public void writeTo(final OutputStream out) throws IOException {
            if (out == null) {
                throw new IllegalArgumentException("Output stream may not be null");
            }
            IOUtils.copy(getReader(), out, this.charset);
        }
        
    }
    
}
