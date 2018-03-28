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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.internal.AbstractEntityBuilder;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

public class AbstractEntityBuilderTest {

    static class AbstractEntityBuilderImpl extends AbstractEntityBuilder {
    }

    @Test
    public void testFieldOperations() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();
        ParsedField field1 = DefaultFieldParser.parse("Content-Type: text/plain");
        builder.addField(field1);

        Assert.assertTrue(builder.containsField("Content-Type"));
        Assert.assertTrue(builder.containsField("CONTENT-TYPE"));

        Assert.assertSame(field1, builder.getField("Content-Type"));
        Assert.assertSame(field1, builder.getField("CONTENT-TYPE"));

        List<Field> fields1 = builder.getFields();
        Assert.assertNotNull(fields1);
        Assert.assertEquals(1, fields1.size());
        Assert.assertSame(field1, fields1.get(0));

        ParsedField field2 = DefaultFieldParser.parse("Content-Transfer-Encoding: 8bit");
        builder.addField(field2);

        List<Field> fields2 = builder.getFields("Stuff");
        Assert.assertNotNull(fields2);
        Assert.assertEquals(0, fields2.size());

        List<Field> fields3 = builder.getFields("Content-Transfer-Encoding");
        Assert.assertNotNull(fields3);
        Assert.assertEquals(1, fields3.size());
        Assert.assertSame(field2, fields3.get(0));

        builder.removeFields("CONTENT-TYPE");

        Assert.assertFalse(builder.containsField("Content-Type"));
        Assert.assertFalse(builder.containsField("CONTENT-TYPE"));

        List<Field> fields4 = builder.getFields();
        Assert.assertNotNull(fields4);
        Assert.assertEquals(1, fields4.size());
        Assert.assertSame(field2, fields4.get(0));

        ParsedField field3 = DefaultFieldParser.parse("Content-Transfer-Encoding: 7bit");
        builder.addField(field3);

        Assert.assertSame(field2, builder.getField("Content-Transfer-Encoding"));
        Assert.assertSame(field2, builder.getField("Content-Transfer-Encoding", ContentTransferEncodingField.class));
        List<Field> fields5 = builder.getFields("Content-Transfer-Encoding");
        Assert.assertNotNull(fields5);
        Assert.assertEquals(2, fields5.size());
        Assert.assertSame(field2, fields5.get(0));
        Assert.assertSame(field3, fields5.get(1));

        List<ContentTransferEncodingField> fields6 = builder.getFields("Content-Transfer-Encoding", ContentTransferEncodingField.class);
        Assert.assertNotNull(fields6);
        Assert.assertEquals(2, fields6.size());
        Assert.assertSame(field2, fields6.get(0));
        Assert.assertSame(field3, fields6.get(1));

        builder.clearFields();

        Assert.assertFalse(builder.containsField("Content-Type"));
        Assert.assertFalse(builder.containsField("Content-Transfer-Encoding"));

        List<Field> fields7 = builder.getFields();
        Assert.assertNotNull(fields7);
        Assert.assertEquals(0, fields7.size());
    }

    @Test
    public void testContentType() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        builder.setContentType("text/plain");

        Assert.assertEquals("text/plain", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        builder.setContentType("text/plain", new NameValuePair("charset", "ASCII"), new NameValuePair("stuff", null));

        Assert.assertEquals("text/plain", builder.getMimeType());
        Assert.assertEquals("ASCII", builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        builder.setContentType(null);

        Assert.assertEquals(null, builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());
        Assert.assertFalse(builder.containsField("Content-Type"));
    }

    @Test
    public void testContentTransferEncoding() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        builder.setContentTransferEncoding("7bit");

        Assert.assertEquals("7bit", builder.getContentTransferEncoding());
        Assert.assertTrue(builder.containsField("Content-Transfer-Encoding"));

        builder.setContentTransferEncoding(null);

        Assert.assertEquals(null, builder.getContentTransferEncoding());
        Assert.assertFalse(builder.containsField("Content-Transfer-Encoding"));
    }

    @Test
    public void testContentDisposition() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        builder.setContentDisposition("attachment");

        Assert.assertEquals("attachment", builder.getDispositionType());
        Assert.assertEquals(null, builder.getFilename());
        Assert.assertEquals(-1L, builder.getSize());
        Assert.assertEquals(null, builder.getCreationDate());
        Assert.assertEquals(null, builder.getModificationDate());
        Assert.assertEquals(null, builder.getReadDate());
        Assert.assertTrue(builder.containsField("Content-Disposition"));

        builder.setContentDisposition("attachment", "some-file.txt");

        Assert.assertEquals("attachment", builder.getDispositionType());
        Assert.assertEquals("some-file.txt", builder.getFilename());
        Assert.assertEquals(-1L, builder.getSize());
        Assert.assertEquals(null, builder.getCreationDate());
        Assert.assertEquals(null, builder.getModificationDate());
        Assert.assertEquals(null, builder.getReadDate());
        Assert.assertTrue(builder.containsField("Content-Disposition"));

        builder.setContentDisposition("attachment", "some-other-file.txt", 1234L);

        Assert.assertEquals("attachment", builder.getDispositionType());
        Assert.assertEquals("some-other-file.txt", builder.getFilename());
        Assert.assertEquals(1234L, builder.getSize());
        Assert.assertEquals(null, builder.getCreationDate());
        Assert.assertEquals(null, builder.getModificationDate());
        Assert.assertEquals(null, builder.getReadDate());
        Assert.assertTrue(builder.containsField("Content-Disposition"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date t1 = dateFormat.parse("2014-08-01 12:00");
        Date t2 = dateFormat.parse("2014-08-01 12:30");
        Date t3 = dateFormat.parse("2014-08-01 14:00");
        builder.setContentDisposition("attachment", "some-other-file.txt", 1234L, t1, t2, t3);

        Assert.assertEquals("attachment", builder.getDispositionType());
        Assert.assertEquals("some-other-file.txt", builder.getFilename());
        Assert.assertEquals(1234L, builder.getSize());
        Assert.assertEquals(t1, builder.getCreationDate());
        Assert.assertEquals(t2, builder.getModificationDate());
        Assert.assertEquals(t3, builder.getReadDate());
        Assert.assertTrue(builder.containsField("Content-Disposition"));

        builder.setContentDisposition(null);

        Assert.assertEquals(null, builder.getDispositionType());
        Assert.assertFalse(builder.containsField("Content-Disposition"));
    }

    @Test
    public void testSetTextBody() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        TextBody body1 = BasicBodyFactory.INSTANCE.textBody("test", Charsets.ISO_8859_1);
        builder.setBody(body1);

        Assert.assertSame(body1, builder.getBody());
        Assert.assertEquals("text/plain", builder.getMimeType());
        Assert.assertEquals("ISO-8859-1", builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        TextBody body2 = BasicBodyFactory.INSTANCE.textBody("test");
        builder.setBody(body2);

        Assert.assertSame(body2, builder.getBody());
        Assert.assertEquals("text/plain", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        builder.setBody((TextBody) null);
        Assert.assertFalse(builder.containsField("Content-Type"));
    }

    @Test
    public void testSetBinaryBody() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        BinaryBody body = BasicBodyFactory.INSTANCE.binaryBody(new byte[]{1, 2, 3});
        builder.setBody(body);

        Assert.assertSame(body, builder.getBody());
        Assert.assertEquals("application/octet-stream", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        builder.setBody((TextBody) null);
        Assert.assertFalse(builder.containsField("Content-Type"));
    }

    @Test
    public void testSetMessageBody() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        Message message = new MessageImpl();
        builder.setBody(message);

        Assert.assertSame(message, builder.getBody());
        Assert.assertEquals("message/rfc822", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        builder.setBody((Message) null);
        Assert.assertFalse(builder.containsField("Content-Type"));
    }

    @Test
    public void testSetMultipartBody() throws Exception {
        AbstractEntityBuilder builder = new AbstractEntityBuilderImpl();

        Multipart multipart = new MultipartImpl("stuff",
            Collections.singletonList(new NameValuePair("report-type", "disposition-notification")));
        builder.setBody(multipart);

        Assert.assertSame(multipart, builder.getBody());
        Assert.assertEquals("multipart/stuff", builder.getMimeType());
        Assert.assertEquals(null, builder.getCharset());
        Assert.assertTrue(builder.containsField("Content-Type"));

        final ContentTypeField field = (ContentTypeField) builder.getField("Content-Type");
        Assert.assertNotNull(field.getBoundary());
        Assert.assertEquals("disposition-notification", field.getParameter("report-type"));

        builder.setBody((Message) null);
        Assert.assertFalse(builder.containsField("Content-Type"));
    }

}
