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

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

public class MessageHeadlessParserTest {

    @Test
    public void testMalformedHeaderShouldEndHeader() throws Exception {
        String headlessContent = "Subject: my subject\r\n"
                + "Hi, how are you?\r\n"
                + "This is a simple message with no CRLFCELF between headers and body.\r\n"
                + "ThisIsNotAnHeader: because this should be already in the body\r\n"
                + "\r\n"
                + "Instead this should be better parsed as a text/plain body\r\n";

        MimeConfig config = MimeConfig.custom()
                .setMalformedHeaderStartsBody(true)
                .build();
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(config);
        Message message = builder.parseMessage(
                new ByteArrayInputStream(headlessContent.getBytes("UTF-8")));
        Assert.assertEquals("text/plain", message.getMimeType());
        Assert.assertEquals(1, message.getHeader().getFields().size());
        BufferedReader reader = new BufferedReader(((TextBody) message.getBody()).getReader());
        String firstLine = reader.readLine();
        Assert.assertEquals("Hi, how are you?", firstLine);
    }

    @Test
    public void testSimpleNonMimeTextHeadless() throws Exception {
        String headlessContent = "Hi, how are you?\r\n"
                + "This is a simple message with no headers. While mime messages should start with\r\n"
                + "header: headervalue\r\n"
                + "\r\n"
                + "Instead this should be better parsed as a text/plain body\r\n";

        MimeConfig config = MimeConfig.custom()
                .setMalformedHeaderStartsBody(true)
                .build();
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(config);
        Message message = builder.parseMessage(
                new ByteArrayInputStream(headlessContent.getBytes("UTF-8")));
        Assert.assertEquals("text/plain", message.getMimeType());
        Assert.assertEquals(0, message.getHeader().getFields().size());
        BufferedReader reader = new BufferedReader(((TextBody) message.getBody()).getReader());
        String firstLine = reader.readLine();
        Assert.assertEquals("Hi, how are you?", firstLine);
    }

    @Test
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

        MimeConfig config = MimeConfig.custom()
                .setHeadlessParsing(contentType)
                .build();
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        builder.setMimeEntityConfig(config);

        Message message = builder.parseMessage(InputStreams.create(headlessContent, Charsets.UTF_8));
        Assert.assertEquals("multipart/form-data", message.getMimeType());
        Assert.assertEquals(1, message.getHeader().getFields().size());
        ContentTypeField contentTypeField = ((ContentTypeField) message
                .getHeader().getField(FieldName.CONTENT_TYPE));
        Assert.assertEquals("foo", contentTypeField.getBoundary());
        Multipart multipart = (Multipart) message.getBody();
        Assert.assertEquals(3, multipart.getCount());
    }
}
