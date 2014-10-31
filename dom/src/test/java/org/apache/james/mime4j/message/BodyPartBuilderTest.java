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

import java.io.InputStream;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class BodyPartBuilderTest {

    @Test
    public void testSetBody() throws Exception {
        BodyPartBuilder builder = BodyPartBuilder.create();

        Body body = BasicBodyFactory.INSTANCE.textBody("test");
        Assert.assertNull(body.getParent());

        builder.setBody(body);
        final BodyPart bodyPart = builder.build();
        Assert.assertSame(body, bodyPart.getBody());
        Assert.assertSame(bodyPart, body.getParent());
    }

    @Test
    public void testSetTextBody() throws Exception {
        BodyPartBuilder builder = BodyPartBuilder.create();

        BodyFactory bodyFactory = Mockito.spy(new BasicBodyFactory());
        builder.use(bodyFactory);

        builder.setBody("blah", Charsets.ISO_8859_1);
        Assert.assertEquals("text/plain", builder.getMimeType());
        Assert.assertEquals("ISO-8859-1", builder.getCharset());

        builder.setBody("blah", "stuff", Charsets.ISO_8859_1);
        Assert.assertEquals("text/stuff", builder.getMimeType());
        Assert.assertEquals("ISO-8859-1", builder.getCharset());

        builder.setBody("blah", "stuff", null);
        Assert.assertEquals("text/stuff", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());

        BodyPart bodyPart = builder.build();
        Assert.assertNotNull(bodyPart);
        Body body = bodyPart.getBody();
        Assert.assertSame(bodyPart, body.getParent());
        ContentTypeField field = bodyPart.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/stuff", field.getMimeType());
        Assert.assertEquals(null, field.getCharset());

        Mockito.verify(bodyFactory, Mockito.times(2)).textBody(
                Mockito.<InputStream>any(), Mockito.eq("ISO-8859-1"));
        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(
                Mockito.<InputStream>any(),  Mockito.isNull(String.class));

    }

    @Test
    public void testSetBinaryBody() throws Exception {
        BodyPartBuilder builder = BodyPartBuilder.create();

        BodyFactory bodyFactory = Mockito.spy(new BasicBodyFactory());
        builder.use(bodyFactory);

        builder.setBody(new byte[] {1,2,3}, "some/stuff");
        Assert.assertEquals("some/stuff", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());

        builder.setBody(new byte[] {1,2,3,4}, null);
        Assert.assertEquals("application/octet-stream", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());

        BodyPart bodyPart = builder.build();
        Assert.assertNotNull(bodyPart);
        Body body = bodyPart.getBody();
        Assert.assertSame(bodyPart, body.getParent());
        ContentTypeField field = bodyPart.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("application/octet-stream", field.getMimeType());
        Assert.assertEquals(null, field.getCharset());

        Mockito.verify(bodyFactory, Mockito.times(2)).binaryBody(Mockito.<InputStream>any());
    }

}
