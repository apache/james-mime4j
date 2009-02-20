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

import java.util.Arrays;
import java.util.List;

import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.parser.Field;

import junit.framework.TestCase;

public class CopyConstructorTest extends TestCase {

    public void testCopyEmptyMessage() throws Exception {
        Message original = new Message();

        Message copy = new Message(original);

        assertNull(copy.getHeader());
        assertNull(copy.getBody());
        assertNull(copy.getParent());
    }

    public void testCopyMessage() throws Exception {
        Message parent = new Message();
        Header header = new Header();
        Body body = new BodyFactory().textBody("test");

        Message original = new Message();
        original.setHeader(header);
        original.setBody(body);
        original.setParent(parent);

        Message copy = new Message(original);

        assertNotNull(copy.getHeader());
        assertNotSame(header, copy.getHeader());

        assertNotNull(copy.getBody());
        assertNotSame(body, copy.getBody());

        assertSame(copy, copy.getBody().getParent());

        assertNull(copy.getParent());
    }

    public void testCopyEmptyBodyPart() throws Exception {
        BodyPart original = new BodyPart();

        BodyPart copy = new BodyPart(original);

        assertNull(copy.getHeader());
        assertNull(copy.getBody());
        assertNull(copy.getParent());
    }

    public void testCopyBodyPart() throws Exception {
        Message parent = new Message();
        Header header = new Header();
        Body body = new BodyFactory().textBody("test");

        BodyPart original = new BodyPart();
        original.setHeader(header);
        original.setBody(body);
        original.setParent(parent);

        BodyPart copy = new BodyPart(original);

        assertNotNull(copy.getHeader());
        assertNotSame(header, copy.getHeader());

        assertNotNull(copy.getBody());
        assertNotSame(body, copy.getBody());

        assertSame(copy, copy.getBody().getParent());

        assertNull(copy.getParent());
    }

    public void testCopyEmptyMultipart() throws Exception {
        Multipart original = new Multipart("mixed");

        Multipart copy = new Multipart(original);

        assertSame(original.getPreamble(), copy.getPreamble());
        assertSame(original.getEpilogue(), copy.getEpilogue());
        assertSame(original.getSubType(), copy.getSubType());
        assertTrue(copy.getBodyParts().isEmpty());
        assertNull(copy.getParent());
    }

    public void testCopyMultipart() throws Exception {
        Message parent = new Message();
        BodyPart bodyPart = new BodyPart();

        Multipart original = new Multipart("mixed");
        original.setPreamble("preamble");
        original.setEpilogue("epilogue");
        original.setParent(parent);
        original.addBodyPart(bodyPart);

        Multipart copy = new Multipart(original);

        assertSame(original.getPreamble(), copy.getPreamble());
        assertSame(original.getEpilogue(), copy.getEpilogue());
        assertSame(original.getSubType(), copy.getSubType());
        assertEquals(1, copy.getBodyParts().size());
        assertNull(copy.getParent());

        BodyPart bodyPartCopy = copy.getBodyParts().iterator().next();
        assertNotSame(bodyPart, bodyPartCopy);

        assertSame(parent, bodyPart.getParent());
        assertNull(bodyPartCopy.getParent());
    }

    public void testCopyMultipartMessage() throws Exception {
        BodyPart bodyPart1 = new BodyPart();
        BodyPart bodyPart2 = new BodyPart();

        Multipart multipart = new Multipart("mixed");
        multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

        Message original = new Message();
        original.setHeader(new Header());
        original.setBody(multipart);

        Message copy = new Message(original);

        Multipart multipartCopy = (Multipart) copy.getBody();
        List<BodyPart> bodyParts = multipartCopy.getBodyParts();
        BodyPart bodyPartCopy1 = bodyParts.get(0);
        BodyPart bodyPartCopy2 = bodyParts.get(1);

        assertNotSame(bodyPart1, bodyPartCopy1);
        assertEquals(original, bodyPart1.getParent());
        assertEquals(copy, bodyPartCopy1.getParent());

        assertNotSame(bodyPart2, bodyPartCopy2);
        assertEquals(original, bodyPart2.getParent());
        assertEquals(copy, bodyPartCopy2.getParent());
    }

    public void testCopyHeader() throws Exception {
        Field f1 = AbstractField.parse("name1: value1");
        Field f2 = AbstractField.parse("name2: value");
        Field f3 = AbstractField.parse("name1: value2");

        Header original = new Header();
        original.addField(f1);
        original.addField(f2);
        original.addField(f3);

        Header copy = new Header(original);

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
