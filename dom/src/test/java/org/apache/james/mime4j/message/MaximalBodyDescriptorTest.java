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

import org.apache.james.mime4j.BodyDescriptor;
import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.MimeConfig;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RawField;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class MaximalBodyDescriptorTest {

    MimeTokenStream parser;

    @Before
    public void setUp() throws Exception {
        parser = new MimeTokenStream(MimeConfig.STRICT, new DefaultBodyDescriptorBuilder(null));
    }

    @Test
    public void testAddField() throws Exception {
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type ", "text/plain; charset=ISO-8859-1"));
        BodyDescriptor bd = builder.build();
        Assert.assertEquals("text/plain", bd.getMimeType());
        Assert.assertEquals("ISO-8859-1", bd.getCharset());
        builder.addField(new RawField("Content-Type ", "text/html; charset=us-ascii"));
        bd = builder.build();
        Assert.assertEquals("text/plain", bd.getMimeType());
        Assert.assertEquals("ISO-8859-1", bd.getCharset());
    }

    @Test
    public void testGetMimeType() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type ", "text/PLAIN"));
        BodyDescriptor bd = builder.build();
        Assert.assertEquals("text/plain", bd.getMimeType());

        builder.reset();
        builder.addField(new RawField("content-type", "   TeXt / html   "));
        bd = builder.build();
        Assert.assertEquals("text/html", bd.getMimeType());

        builder.reset();
        builder.addField(new RawField("CONTENT-TYPE", "   x-app/yada ;  param = yada"));
        bd = builder.build();
        Assert.assertEquals("x-app/yada", bd.getMimeType());

        builder.reset();
        builder.addField(new RawField("CONTENT-TYPE", "   yada"));
        bd = builder.build();
        Assert.assertEquals("text/plain", bd.getMimeType());

        /*
         * Make sure that only the first Content-Type header added is used.
         */
        builder.reset();
        builder.addField(new RawField("Content-Type ", "text/plain"));
        bd = builder.build();
        Assert.assertEquals("text/plain", bd.getMimeType());
        builder.addField(new RawField("Content-Type ", "text/html"));
        bd = builder.build();
        Assert.assertEquals("text/plain", bd.getMimeType());

        /*
         * Implicit mime types.
         */
        BodyDescriptorBuilder parent = new DefaultBodyDescriptorBuilder();
        parent.addField(new RawField("Content-Type", "mutlipart/alternative; boundary=foo"));
        BodyDescriptorBuilder child = parent.newChild();
        bd = child.build();
        Assert.assertEquals("text/plain", bd.getMimeType());
        child.addField(new RawField("Content-Type", " child/type"));
        bd = child.build();
        Assert.assertEquals("child/type", bd.getMimeType());

        parent.reset();
        parent.addField(new RawField("Content-Type", "multipart/digest; boundary=foo"));

        child = parent.newChild();
        bd = child.build();
        Assert.assertEquals("message/rfc822", bd.getMimeType());
        child.addField(new RawField("Content-Type", " child/type"));
        bd = child.build();
        Assert.assertEquals("child/type", bd.getMimeType());

    }

    @Test
    public void testParameters() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        /*
         * Test charset.
         */
        BodyDescriptor bd = builder.build();
        Assert.assertEquals("us-ascii", bd.getCharset());
        builder.addField(new RawField("Content-Type ", "text/type; charset=ISO-8859-1"));
        bd = builder.build();
        Assert.assertEquals("ISO-8859-1", bd.getCharset());

        builder.reset();
        bd = builder.build();
        Assert.assertEquals("us-ascii", bd.getCharset());
        builder.addField(new RawField("Content-Type ", "text/type"));
        bd = builder.build();
        Assert.assertEquals("us-ascii", bd.getCharset());

        /*
         * Test boundary.
         */
        builder.reset();
        builder.addField(new RawField("Content-Type", "text/html; boundary=yada yada"));
        bd = builder.build();
        Assert.assertNull(bd.getBoundary());

        builder.reset();
        builder.addField(new RawField("Content-Type", "multipart/yada; boundary=yada"));
        bd = builder.build();
        Assert.assertEquals("yada", bd.getBoundary());

        builder.reset();
        builder.addField(new RawField("Content-Type", "multipart/yada; boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\""));
        bd = builder.build();
        Assert.assertEquals("ya \"\"\tda \"", bd.getBoundary());
        Assert.assertEquals("\"hepp\"  =us\t-ascii", bd.getCharset());

    }

    @Test
    public void testGetContentLength() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        BodyDescriptor bd = builder.build();
        Assert.assertEquals(-1, bd.getContentLength());

        builder.addField(new RawField("Content-Length", "9901"));
        bd = builder.build();
        Assert.assertEquals(9901, bd.getContentLength());

        // only the first content-length counts
        builder.addField(new RawField("Content-Length", "1239901"));
        bd = builder.build();
        Assert.assertEquals(9901, bd.getContentLength());
    }

    @Test
    public void testMultipartNoBoundary() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type", "multipart/yada; "));
        BodyDescriptor bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());
    }

    @Test
    public void testDoDefaultToUsAsciiWhenUntyped() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("To", "me@example.org"));
        BodyDescriptor bd = builder.build();
        Assert.assertEquals("us-ascii", bd.getCharset());
    }

    @Test
    public void testDoNotDefaultToUsAsciiForNonTextTypes() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type", "image/png; name=blob.png"));
        BodyDescriptor bd = builder.build();
        Assert.assertNull(bd.getCharset());
    }

    @Test
    public void testMimeVersionDefault() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.RFC822_SIMPLE_BYTES);
        Assert.assertEquals(1, descriptor.getMimeMajorVersion());
        Assert.assertEquals(0, descriptor.getMimeMinorVersion());
    }

    @Test
    public void testMimeVersion() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_ASCII_COMMENT_IN_MIME_VERSION_BYTES);
        Assert.assertEquals(2, descriptor.getMimeMajorVersion());
        Assert.assertEquals(4, descriptor.getMimeMinorVersion());
    }

    @Test
    public void testContentId() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_8859_BYTES);
        Assert.assertEquals(1, descriptor.getMimeMajorVersion());
        Assert.assertEquals(0, descriptor.getMimeMinorVersion());
        Assert.assertEquals(ExampleMail.CONTENT_ID, descriptor.getContentId());
    }

    @Test
    public void testContentDescription() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_8859_BYTES);
        Assert.assertEquals(1, descriptor.getMimeMajorVersion());
        Assert.assertEquals(0, descriptor.getMimeMinorVersion());
        Assert.assertEquals(ExampleMail.CONTENT_DESCRIPTION, descriptor.getContentDescription());
    }

    @Test
    public void testMimeVersionHeaderBreak() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_ASCII_MIME_VERSION_SPANS_TWO_LINES_BYTES);
        Assert.assertEquals(4, descriptor.getMimeMajorVersion());
        Assert.assertEquals(1, descriptor.getMimeMinorVersion());
    }

    @Test
    public void testContentDispositionType() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BYTES);
        Assert.assertEquals("inline", descriptor.getContentDispositionType());
    }

    @Test
    public void testContentDispositionTypeCaseConversion() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BYTES);
        Assert.assertEquals("Should be converted to lower case", "inline", descriptor.getContentDispositionType());
        Assert.assertNotNull(descriptor.getContentDispositionParameters());
        Assert.assertEquals(0, descriptor.getContentDispositionParameters().size());
    }

    @Test
    public void testContentDispositionParameters() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS_BYTES);
        Assert.assertEquals("inline", descriptor.getContentDispositionType());
        Assert.assertNotNull(descriptor.getContentDispositionParameters());
        Assert.assertEquals(3, descriptor.getContentDispositionParameters().size());
        Assert.assertEquals("value", descriptor.getContentDispositionParameters().get("param"));
        Assert.assertEquals("1", descriptor.getContentDispositionParameters().get("one"));
        Assert.assertEquals("bar", descriptor.getContentDispositionParameters().get("foo"));
    }

    @Test
    public void testContentDispositionStandardParameters() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_BINARY_ATTACHMENTS_BYTES, 1);
        Assert.assertEquals("attachment", descriptor.getContentDispositionType());
        Assert.assertNotNull(descriptor.getContentDispositionParameters());
        Assert.assertEquals(5, descriptor.getContentDispositionParameters().size());
        Assert.assertEquals("blob.png", descriptor.getContentDispositionFilename());

        SimpleDateFormat dateparser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateparser.setTimeZone(TimeZone.getTimeZone("GMT"));

        Assert.assertEquals(dateparser.parse("2008-06-21 15:32:18"), descriptor.getContentDispositionModificationDate());
        Assert.assertEquals(dateparser.parse("2008-06-20 10:15:09"), descriptor.getContentDispositionCreationDate());
        Assert.assertEquals(dateparser.parse("2008-06-22 12:08:56"), descriptor.getContentDispositionReadDate());
        Assert.assertEquals(10234, descriptor.getContentDispositionSize());
    }

    @Test
    public void testLanguageParameters() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_BINARY_ATTACHMENTS_BYTES, 3);
        Assert.assertNotNull(descriptor.getContentLanguage());
        Assert.assertEquals(3, descriptor.getContentLanguage().size());
        Assert.assertEquals("en", descriptor.getContentLanguage().get(0));
        Assert.assertEquals("en-US", descriptor.getContentLanguage().get(1));
        Assert.assertEquals("en-CA", descriptor.getContentLanguage().get(2));
    }

    @Test
    public void testContentLocationRelativeUrl() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 0);
        Assert.assertEquals("relative/url", descriptor.getContentLocation());
    }

    @Test
    public void testContentLocationAbsoluteUrl() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 1);
        Assert.assertEquals("http://www.example.org/absolute/rhubard.txt", descriptor.getContentLocation());
    }

    @Test
    public void testContentLocationWithComment() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 3);
        Assert.assertEquals("http://www.example.org/absolute/comments/rhubard.txt", descriptor.getContentLocation());
    }

    @Test
    public void testContentLocationFoldedUrl() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 4);
        Assert.assertEquals("http://www.example.org/this/is/a/very/long/url/split/over/two/lines/", descriptor.getContentLocation());
    }

    @Test
    public void testContentMD5Url() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS_BYTES);
        Assert.assertEquals(ExampleMail.MD5_CONTENT, descriptor.getContentMD5Raw());
    }

    private MaximalBodyDescriptor describe(byte[] mail, int zeroBasedPart) throws Exception {
        ByteArrayInputStream bias = new ByteArrayInputStream(mail);
        parser.parse(bias);
        EntityState state = parser.next();
        while (state != EntityState.T_END_OF_STREAM && zeroBasedPart >= 0) {
            state = parser.next();
            if (state == EntityState.T_BODY) {
                --zeroBasedPart;
            }
        }
        assertEquals(EntityState.T_BODY, state);
        BodyDescriptor descriptor = parser.getBodyDescriptor();
        assertNotNull(descriptor);
        assertTrue("Parser is maximal so body descriptor should be maximal", descriptor instanceof MaximalBodyDescriptor);
        return (MaximalBodyDescriptor) descriptor;
    }

    private MaximalBodyDescriptor describe(byte[] mail) throws Exception {
        ByteArrayInputStream bias = new ByteArrayInputStream(mail);
        parser.parse(bias);
        EntityState state = parser.next();
        while (state != EntityState.T_BODY && state != EntityState.T_END_OF_STREAM) {
            state = parser.next();
        }
        assertEquals(EntityState.T_BODY, state);
        BodyDescriptor descriptor = parser.getBodyDescriptor();
        assertNotNull(descriptor);
        assertTrue("Parser is maximal so body descriptor should be maximal", descriptor instanceof MaximalBodyDescriptor);
        return (MaximalBodyDescriptor) descriptor;
    }

}
