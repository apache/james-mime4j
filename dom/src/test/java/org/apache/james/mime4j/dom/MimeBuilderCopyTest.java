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

import java.util.Arrays;
import java.util.List;

import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.stream.Field;

import junit.framework.TestCase;

public class MimeBuilderCopyTest extends TestCase {

    public void testCopyMessage() throws Exception {
        MessageImpl parent = new MessageImpl();
        Header header = new HeaderImpl();
        Body body = new BasicBodyFactory().textBody("test");

        MessageImpl original = new MessageImpl();
        original.setHeader(header);
        original.setBody(body);
        original.setParent(parent);

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Message copy = builder.copy(original);

        assertNotNull(copy.getHeader());
        assertNotSame(header, copy.getHeader());

        assertNotNull(copy.getBody());
        assertNotSame(body, copy.getBody());

        assertSame(copy, copy.getBody().getParent());

        assertNull(copy.getParent());
    }

    public void testCopyEmptyBodyPart() throws Exception {
        BodyPart original = new BodyPart();

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        BodyPart copy = builder.copy(original);

        assertNull(copy.getHeader());
        assertNull(copy.getBody());
        assertNull(copy.getParent());
    }

    public void testCopyBodyPart() throws Exception {
        MessageImpl parent = new MessageImpl();
        Header header = new HeaderImpl();
        Body body = new BasicBodyFactory().textBody("test");

        BodyPart original = new BodyPart();
        original.setHeader(header);
        original.setBody(body);
        original.setParent(parent);

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        BodyPart copy = builder.copy(original);

        assertNotNull(copy.getHeader());
        assertNotSame(header, copy.getHeader());

        assertNotNull(copy.getBody());
        assertNotSame(body, copy.getBody());

        assertSame(copy, copy.getBody().getParent());

        assertNull(copy.getParent());
    }

    public void testCopyEmptyMultipart() throws Exception {
        Multipart original = new MultipartImpl("mixed");

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Multipart copy = builder.copy(original);

        assertSame(original.getPreamble(), copy.getPreamble());
        assertSame(original.getEpilogue(), copy.getEpilogue());
        assertSame(original.getSubType(), copy.getSubType());
        assertTrue(copy.getBodyParts().isEmpty());
        assertNull(copy.getParent());
    }

    public void testCopyMultipart() throws Exception {
        MessageImpl parent = new MessageImpl();
        BodyPart bodyPart = new BodyPart();

        MultipartImpl original = new MultipartImpl("mixed");
        original.setPreamble("preamble");
        original.setEpilogue("epilogue");
        original.setParent(parent);
        original.addBodyPart(bodyPart);

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Multipart copy = builder.copy(original);

        assertSame(original.getPreamble(), copy.getPreamble());
        assertSame(original.getEpilogue(), copy.getEpilogue());
        assertSame(original.getSubType(), copy.getSubType());
        assertEquals(1, copy.getBodyParts().size());
        assertNull(copy.getParent());

        Entity bodyPartCopy = copy.getBodyParts().iterator().next();
        assertNotSame(bodyPart, bodyPartCopy);

        assertSame(parent, bodyPart.getParent());
        assertNull(bodyPartCopy.getParent());
    }

    public void testCopyMultipartMessage() throws Exception {
        BodyPart bodyPart1 = new BodyPart();
        BodyPart bodyPart2 = new BodyPart();

        Multipart multipart = new MultipartImpl("mixed");
        multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

        MessageImpl original = new MessageImpl();
        original.setHeader(new HeaderImpl());
        original.setBody(multipart);

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Message copy = builder.copy(original);

        Multipart multipartCopy = (Multipart) copy.getBody();
        List<Entity> bodyParts = multipartCopy.getBodyParts();
        Entity bodyPartCopy1 = bodyParts.get(0);
        Entity bodyPartCopy2 = bodyParts.get(1);

        assertNotSame(bodyPart1, bodyPartCopy1);
        assertEquals(original, bodyPart1.getParent());
        assertEquals(copy, bodyPartCopy1.getParent());

        assertNotSame(bodyPart2, bodyPartCopy2);
        assertEquals(original, bodyPart2.getParent());
        assertEquals(copy, bodyPartCopy2.getParent());
    }

    public void testCopyHeader() throws Exception {
        Field f1 = DefaultFieldParser.parse("name1: value1");
        Field f2 = DefaultFieldParser.parse("name2: value");
        Field f3 = DefaultFieldParser.parse("name1: value2");

        Header original = new HeaderImpl();
        original.addField(f1);
        original.addField(f2);
        original.addField(f3);

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Header copy = builder.copy(original);

        // copy must have same fields as original
        assertEquals(Arrays.asList(f1, f2, f3), copy.getFields());
        assertEquals(Arrays.asList(f1, f3), copy.getFields("name1"));

        // modify original
        original.removeFields("name1");
        assertEquals(Arrays.asList(f2), original.getFields());

        // copy may not be affected
        assertEquals(Arrays.asList(f1, f2, f3), copy.getFields());
        assertEquals(Arrays.asList(f1, f3), copy.getFields("name1"));
    }

}
