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

import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.junit.Assert;
import org.junit.Test;

public class EntityImplTest {

    @Test
    public void testSetBody() throws Exception {
        Entity entity = new BodyPart();
        Assert.assertNull(entity.getBody());

        Body body = new BasicBodyFactory().textBody("test");
        Assert.assertNull(body.getParent());

        entity.setBody(body);
        Assert.assertSame(body, entity.getBody());
        Assert.assertSame(entity, body.getParent());
    }

    @Test
    public void testSetBodyTwice() throws Exception {
        Entity entity = new BodyPart();

        Body b1 = new BasicBodyFactory().textBody("foo");
        Body b2 = new BasicBodyFactory().textBody("bar");

        entity.setBody(b1);
        try {
            entity.setBody(b2);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testRemoveBody() throws Exception {
        Entity entity = new BodyPart();
        Body body = new BasicBodyFactory().textBody("test");
        entity.setBody(body);

        Body removed = entity.removeBody();
        Assert.assertSame(body, removed);

        Assert.assertNull(entity.getBody());
        Assert.assertNull(removed.getParent());
    }

    @Test
    public void testGetDispositionType() throws Exception {
        BodyPart entity = new BodyPart();

        Assert.assertNull(entity.getDispositionType());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Content-Disposition: inline"));
        entity.setHeader(header);

        Assert.assertEquals("inline", entity.getDispositionType());
    }

    @Test
    public void testGetFilename() throws Exception {
        BodyPart entity = new BodyPart();

        Assert.assertNull(entity.getFilename());

        Header header = new HeaderImpl();
        header.setField(DefaultFieldParser.parse("Content-Disposition: attachment; "
                + "filename=\"some file.dat\""));
        entity.setHeader(header);

        Assert.assertEquals("some file.dat", entity.getFilename());
    }

}
