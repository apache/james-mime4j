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

import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.message.Entity;

import junit.framework.TestCase;

public class EntityTest extends TestCase {

    public void testSetBody() throws Exception {
        Entity entity = new BodyPart();
        assertNull(entity.getBody());

        Body body = new BodyFactory().textBody("test");
        assertNull(body.getParent());

        entity.setBody(body);
        assertSame(body, entity.getBody());
        assertSame(entity, body.getParent());
    }

    public void testSetBodyTwice() throws Exception {
        Entity entity = new BodyPart();

        Body b1 = new BodyFactory().textBody("foo");
        Body b2 = new BodyFactory().textBody("bar");

        entity.setBody(b1);
        try {
            entity.setBody(b2);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testRemoveBody() throws Exception {
        Entity entity = new BodyPart();
        Body body = new BodyFactory().textBody("test");
        entity.setBody(body);

        Body removed = entity.removeBody();
        assertSame(body, removed);

        assertNull(entity.getBody());
        assertNull(removed.getParent());
    }

    public void testGetDispositionType() throws Exception {
        Entity entity = new BodyPart();

        assertNull(entity.getDispositionType());

        Header header = new Header();
        header.setField(AbstractField.parse("Content-Disposition: inline"));
        entity.setHeader(header);

        assertEquals("inline", entity.getDispositionType());
    }

    public void testSetContentDispositionType() throws Exception {
        Entity entity = new BodyPart();

        entity.setContentDisposition("attachment");

        assertEquals("attachment", entity.getHeader().getField(
                "Content-Disposition").getBody());
    }

    public void testSetContentDispositionTypeFilename() throws Exception {
        Entity entity = new BodyPart();

        entity.setContentDisposition("attachment", "some file.dat");

        assertEquals("attachment; filename=\"some file.dat\"", entity
                .getHeader().getField("Content-Disposition").getBody());
    }

    public void testGetFilename() throws Exception {
        Entity entity = new BodyPart();

        assertNull(entity.getFilename());

        Header header = new Header();
        header.setField(AbstractField.parse("Content-Disposition: attachment; "
                + "filename=\"some file.dat\""));
        entity.setHeader(header);

        assertEquals("some file.dat", entity.getFilename());
    }

    public void testSetFilename() throws Exception {
        Entity entity = new BodyPart();

        entity.setFilename("file name.ext");

        assertEquals("attachment; filename=\"file name.ext\"", entity
                .getHeader().getField("Content-Disposition").getBody());

        entity.setFilename(null);

        assertEquals("attachment", entity.getHeader().getField(
                "Content-Disposition").getBody());
    }

}
