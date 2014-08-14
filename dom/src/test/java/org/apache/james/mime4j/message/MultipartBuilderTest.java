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
import java.util.Arrays;
import java.util.List;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.stream.Field;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MultipartBuilderTest {

    @Test
    public void testBasis() throws Exception {
        MultipartBuilder builder = MultipartBuilder.create("stuff");
        builder.setPreamble("Hello");
        builder.setEpilogue("Bye");

        Assert.assertEquals("stuff", builder.getSubType());
        Assert.assertEquals("Hello", builder.getPreamble());
        Assert.assertEquals("Bye", builder.getEpilogue());

        TextBody body1 = BasicBodyFactory.INSTANCE.textBody("test");
        final BodyPart bodyPart1 = new BodyPart();
        bodyPart1.setBody(body1);
        BinaryBody body2 = BasicBodyFactory.INSTANCE.binaryBody(new byte[]{1, 2, 3});
        final BodyPart bodyPart2 = new BodyPart();
        bodyPart2.setBody(body2);

        builder.addBodyPart(bodyPart1);
        builder.addBodyPart(bodyPart2, 0);

        Assert.assertEquals(2, builder.getCount());
        Assert.assertEquals(Arrays.asList(bodyPart2, bodyPart1), builder.getBodyParts());

        TextBody body3 = BasicBodyFactory.INSTANCE.textBody("test");
        final BodyPart bodyPart3 = new BodyPart();
        bodyPart3.setBody(body3);

        try {
            builder.addBodyPart(bodyPart3, 5);
            Assert.fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            builder.replaceBodyPart(bodyPart3, 5);
            Assert.fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException expected) {
        }
        builder.replaceBodyPart(bodyPart3, 1);
        builder.removeBodyPart(0);

        Assert.assertEquals(1, builder.getCount());
        Assert.assertEquals(Arrays.asList(bodyPart3), builder.getBodyParts());
    }

    @Test
    public void testAddCustomPart() throws Exception {
        MultipartBuilder builder = MultipartBuilder.create();

        BodyFactory bodyFactory = Mockito.spy(new BasicBodyFactory());
        builder.use(bodyFactory);

        builder.addTextPart("stuff1", Charsets.UTF_8);
        builder.addTextPart("stuff2", null);
        builder.addBinaryPart(new byte[]{1, 2, 3}, "some/stuff");
        builder.addBinaryPart(new byte[] {1,2,3}, null);
        builder.addTextPart("stuff3", Charsets.US_ASCII);

        Assert.assertEquals(5, builder.getCount());
        List<Entity> bodyParts = builder.getBodyParts();
        Assert.assertNotNull(bodyParts);
        Assert.assertEquals(5, bodyParts.size());

        Entity entity1 = bodyParts.get(0);
        ContentTypeField ct1 = entity1.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/plain", ct1.getMimeType());
        Assert.assertEquals("UTF-8", ct1.getCharset());
        ContentTransferEncodingField te1 = entity1.getHeader().getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("quoted-printable", te1.getEncoding());

        Entity entity2 = bodyParts.get(1);
        ContentTypeField ct2 = entity2.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/plain", ct2.getMimeType());
        Assert.assertEquals("ISO-8859-1", ct2.getCharset());
        ContentTransferEncodingField te2 = entity2.getHeader().getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("quoted-printable", te2.getEncoding());

        Entity entity3 = bodyParts.get(2);
        ContentTypeField ct3 = entity3.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("some/stuff", ct3.getMimeType());
        Assert.assertEquals(null, ct3.getCharset());
        ContentTransferEncodingField te3 = entity3.getHeader().getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("base64", te3.getEncoding());

        Entity entity4 = bodyParts.get(3);
        ContentTypeField ct4 = entity4.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("application/octet-stream", ct4.getMimeType());
        Assert.assertEquals(null, ct4.getCharset());
        ContentTransferEncodingField te4 = entity4.getHeader().getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("base64", te4.getEncoding());

        Entity entity5 = bodyParts.get(4);
        ContentTypeField ct5 = entity5.getHeader().getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/plain", ct5.getMimeType());
        Assert.assertEquals("US-ASCII", ct5.getCharset());
        ContentTransferEncodingField te5 = entity5.getHeader().getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("7bit", te5.getEncoding());

        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(
                Mockito.<InputStream>any(), Mockito.eq("UTF-8"));
        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(
                Mockito.<InputStream>any(), Mockito.eq("ISO-8859-1"));
        Mockito.verify(bodyFactory, Mockito.times(1)).textBody(
                Mockito.<InputStream>any(), Mockito.eq("US-ASCII"));
        Mockito.verify(bodyFactory, Mockito.times(2)).binaryBody(Mockito.<InputStream>any());
    }

    @Test
    public void testCopy() throws Exception {
        MultipartBuilder builder1 = MultipartBuilder.create("stuff");
        builder1.setPreamble("Hello");
        builder1.setEpilogue("Bye");
        builder1.addTextPart("stuff1", Charsets.UTF_8);
        builder1.addBinaryPart(new byte[]{1, 2, 3}, "some/stuff");

        Multipart multipart = builder1.build();

        MultipartBuilder builder2 = MultipartBuilder.createCopy(multipart);
        Assert.assertEquals("stuff", builder2.getSubType());
        Assert.assertEquals("Hello", builder2.getPreamble());
        Assert.assertEquals("Bye", builder2.getEpilogue());
        Assert.assertEquals(2, builder2.getCount());

        List<Entity> bodyParts = builder2.getBodyParts();

        Assert.assertNotNull(bodyParts);
        Assert.assertEquals(2, bodyParts.size());

        Entity entity1 = bodyParts.get(0);
        Assert.assertNotNull(entity1);
        Header header1 = entity1.getHeader();
        Assert.assertNotNull(header1);
        List<Field> fields1 = header1.getFields();
        Assert.assertNotNull(fields1);
        Assert.assertEquals(2, fields1.size());
        ContentTypeField ct1 = header1.getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("text/plain", ct1.getMimeType());
        Assert.assertEquals("UTF-8", ct1.getCharset());
        ContentTransferEncodingField te1 = header1.getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("quoted-printable", te1.getEncoding());

        Entity entity2 = bodyParts.get(1);
        Assert.assertNotNull(entity2);
        Header header2 = entity2.getHeader();
        Assert.assertNotNull(header2);
        List<Field> fields2 = header2.getFields();
        Assert.assertNotNull(fields2);
        Assert.assertEquals(2, fields2.size());
        ContentTypeField ct2 = header2.getField("Content-Type", ContentTypeField.class);
        Assert.assertEquals("some/stuff", ct2.getMimeType());
        Assert.assertEquals(null, ct2.getCharset());
        ContentTransferEncodingField te2 = entity2.getHeader().getField("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertEquals("base64", te2.getEncoding());
    }

}
