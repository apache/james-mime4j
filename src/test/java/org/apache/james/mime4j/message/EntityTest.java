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

import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.message.Entity;

import junit.framework.TestCase;

public class EntityTest extends TestCase {

    public void testGetDispositionType() throws Exception {
        Entity entity = new BodyPart();

        assertNull(entity.getDispositionType());

        Header header = new Header();
        header.setField(Field.parse("Content-Disposition", "inline"));
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
        header.setField(Field.parse("Content-Disposition",
                "attachment; filename=\"some file.dat\""));
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
