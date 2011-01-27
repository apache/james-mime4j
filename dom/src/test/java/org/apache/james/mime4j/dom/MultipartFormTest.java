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

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MimeWriter;
import org.apache.james.mime4j.message.MultipartImpl;

public class MultipartFormTest extends TestCase {

    public void testMultipartFormContent() throws Exception {
        BasicBodyFactory bodyFactory = new BasicBodyFactory();
        
        MessageImpl message = new MessageImpl();
        Header header = new HeaderImpl();
        header.addField(
                DefaultFieldParser.parse("Content-Type: multipart/form-data; boundary=foo"));
        message.setHeader(header);
        
        Multipart multipart = new MultipartImpl("alternative");
        multipart.setParent(message);
        BodyPart p1 = new BodyPart();
        Header h1 = new HeaderImpl();
        h1.addField(DefaultFieldParser.parse("Content-Type: text/plain"));
        p1.setHeader(h1);
        p1.setBody(bodyFactory.textBody("this stuff"));
        BodyPart p2 = new BodyPart();
        Header h2 = new HeaderImpl();
        h2.addField(DefaultFieldParser.parse("Content-Type: text/plain"));
        p2.setHeader(h2);
        p2.setBody(bodyFactory.textBody("that stuff"));
        BodyPart p3 = new BodyPart();
        Header h3 = new HeaderImpl();
        h3.addField(DefaultFieldParser.parse("Content-Type: text/plain"));
        p3.setHeader(h3);
        p3.setBody(bodyFactory.textBody("all kind of stuff"));

        multipart.addBodyPart(p1);
        multipart.addBodyPart(p2);
        multipart.addBodyPart(p3);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MimeWriter.DEFAULT.writeMultipart(multipart, out);
        out.close();
        
        String expected = "--foo\r\n" +
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
            "--foo--";
        String s = out.toString("US-ASCII");
        assertEquals(expected, s);
    }
    
}
