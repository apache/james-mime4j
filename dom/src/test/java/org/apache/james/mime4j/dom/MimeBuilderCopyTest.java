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

import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.stream.Field;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MimeBuilderCopyTest {

    @Test
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

        Assert.assertNotNull(copy.getHeader());
        Assert.assertNotSame(header, copy.getHeader());

        Assert.assertNotNull(copy.getBody());
        Assert.assertNotSame(body, copy.getBody());

        Assert.assertSame(copy, copy.getBody().getParent());

        Assert.assertNull(copy.getParent());
    }

    @Test
    public void testCopyEmptyBodyPart() throws Exception {
        BodyPart original = new BodyPart();

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        BodyPart copy = builder.copy(original);

        Assert.assertNull(copy.getHeader());
        Assert.assertNull(copy.getBody());
        Assert.assertNull(copy.getParent());
    }

    @Test
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

        Assert.assertNotNull(copy.getHeader());
        Assert.assertNotSame(header, copy.getHeader());

        Assert.assertNotNull(copy.getBody());
        Assert.assertNotSame(body, copy.getBody());

        Assert.assertSame(copy, copy.getBody().getParent());

        Assert.assertNull(copy.getParent());
    }

    @Test
    public void testCopyEmptyMultipart() throws Exception {
        Multipart original = new MultipartImpl("mixed");

        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        Multipart copy = builder.copy(original);

        Assert.assertSame(original.getPreamble(), copy.getPreamble());
        Assert.assertSame(original.getEpilogue(), copy.getEpilogue());
        Assert.assertSame(original.getSubType(), copy.getSubType());
        Assert.assertTrue(copy.getBodyParts().isEmpty());
        Assert.assertNull(copy.getParent());
    }

    @Test
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

        Assert.assertSame(original.getPreamble(), copy.getPreamble());
        Assert.assertSame(original.getEpilogue(), copy.getEpilogue());
        Assert.assertSame(original.getSubType(), copy.getSubType());
        Assert.assertEquals(1, copy.getBodyParts().size());
        Assert.assertNull(copy.getParent());

        Entity bodyPartCopy = copy.getBodyParts().iterator().next();
        Assert.assertNotSame(bodyPart, bodyPartCopy);

        Assert.assertSame(parent, bodyPart.getParent());
        Assert.assertNull(bodyPartCopy.getParent());
    }

    @Test
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

        Assert.assertNotSame(bodyPart1, bodyPartCopy1);
        Assert.assertEquals(original, bodyPart1.getParent());
        Assert.assertEquals(copy, bodyPartCopy1.getParent());

        Assert.assertNotSame(bodyPart2, bodyPartCopy2);
        Assert.assertEquals(original, bodyPart2.getParent());
        Assert.assertEquals(copy, bodyPartCopy2.getParent());
    }

    @Test
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
        Assert.assertEquals(Arrays.asList(f1, f2, f3), copy.getFields());
        Assert.assertEquals(Arrays.asList(f1, f3), copy.getFields("name1"));

        // modify original
        original.removeFields("name1");
        Assert.assertEquals(Arrays.asList(f2), original.getFields());

        // copy may not be affected
        Assert.assertEquals(Arrays.asList(f1, f2, f3), copy.getFields());
        Assert.assertEquals(Arrays.asList(f1, f3), copy.getFields("name1"));
    }

}
