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

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.james.mime4j.ExampleMail;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RawField;

public class MaximalBodyDescriptorTest extends TestCase {

    MimeTokenStream parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MimeConfig config = new MimeConfig();
        config.setStrictParsing(true);
        parser = new MimeTokenStream(config, new DefaultBodyDescriptorBuilder(null));
    }

    public void testAddField() throws Exception {
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type ", "text/plain; charset=ISO-8859-1"));
        BodyDescriptor bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());
        assertEquals("ISO-8859-1", bd.getCharset());
        builder.addField(new RawField("Content-Type ", "text/html; charset=us-ascii"));
        bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());
        assertEquals("ISO-8859-1", bd.getCharset());
    }

    public void testGetMimeType() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type ", "text/PLAIN"));
        BodyDescriptor bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());

        builder.reset();
        builder.addField(new RawField("content-type", "   TeXt / html   "));
        bd = builder.build();
        assertEquals("text/html", bd.getMimeType());

        builder.reset();
        builder.addField(new RawField("CONTENT-TYPE", "   x-app/yada ;  param = yada"));
        bd = builder.build();
        assertEquals("x-app/yada", bd.getMimeType());

        builder.reset();
        builder.addField(new RawField("CONTENT-TYPE", "   yada"));
        bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());

        /*
         * Make sure that only the first Content-Type header added is used.
         */
        builder.reset();
        builder.addField(new RawField("Content-Type ", "text/plain"));
        bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());
        builder.addField(new RawField("Content-Type ", "text/html"));
        bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());

        /*
         * Implicit mime types.
         */
        BodyDescriptorBuilder parent = new DefaultBodyDescriptorBuilder();
        parent.addField(new RawField("Content-Type", "mutlipart/alternative; boundary=foo"));
        BodyDescriptorBuilder child = parent.newChild();
        bd = child.build();
        assertEquals("text/plain", bd.getMimeType());
        child.addField(new RawField("Content-Type", " child/type"));
        bd = child.build();
        assertEquals("child/type", bd.getMimeType());

        parent.reset();
        parent.addField(new RawField("Content-Type", "multipart/digest; boundary=foo"));

        child = parent.newChild();
        bd = child.build();
        assertEquals("message/rfc822", bd.getMimeType());
        child.addField(new RawField("Content-Type", " child/type"));
        bd = child.build();
        assertEquals("child/type", bd.getMimeType());

    }

    public void testParameters() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        /*
         * Test charset.
         */
        BodyDescriptor bd = builder.build();
        assertEquals("us-ascii", bd.getCharset());
        builder.addField(new RawField("Content-Type ", "text/type; charset=ISO-8859-1"));
        bd = builder.build();
        assertEquals("ISO-8859-1", bd.getCharset());

        builder.reset();
        bd = builder.build();
        assertEquals("us-ascii", bd.getCharset());
        builder.addField(new RawField("Content-Type ", "text/type"));
        bd = builder.build();
        assertEquals("us-ascii", bd.getCharset());

        /*
         * Test boundary.
         */
        builder.reset();
        builder.addField(new RawField("Content-Type", "text/html; boundary=yada yada"));
        bd = builder.build();
        assertNull(bd.getBoundary());

        builder.reset();
        builder.addField(new RawField("Content-Type", "multipart/yada; boundary=yada"));
        bd = builder.build();
        assertEquals("yada", bd.getBoundary());

        builder.reset();
        builder.addField(new RawField("Content-Type", "multipart/yada; boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                            + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\""));
        bd = builder.build();
        assertEquals("ya \"\"\tda \"", bd.getBoundary());
        assertEquals("\"hepp\"  =us\t-ascii", bd.getCharset());

    }

    public void testGetContentLength() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        BodyDescriptor bd = builder.build();
        assertEquals(-1, bd.getContentLength());

        builder.addField(new RawField("Content-Length", "9901"));
        bd = builder.build();
        assertEquals(9901, bd.getContentLength());

        // only the first content-length counts
        builder.addField(new RawField("Content-Length", "1239901"));
        bd = builder.build();
        assertEquals(9901, bd.getContentLength());
    }

    public void testMultipartNoBoundary() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type", "multipart/yada; "));
        BodyDescriptor bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());
    }

    public void testDoDefaultToUsAsciiWhenUntyped() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("To", "me@example.org"));
        BodyDescriptor bd = builder.build();
        assertEquals("us-ascii", bd.getCharset());
    }

    public void testDoNotDefaultToUsAsciiForNonTextTypes() throws Exception {
        BodyDescriptorBuilder builder = new DefaultBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type", "image/png; name=blob.png"));
        BodyDescriptor bd = builder.build();
        assertNull(bd.getCharset());
    }

    public void testMimeVersionDefault() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.RFC822_SIMPLE_BYTES);
        assertEquals(1, descriptor.getMimeMajorVersion());
        assertEquals(0, descriptor.getMimeMinorVersion());
    }

    public void testMimeVersion() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_ASCII_COMMENT_IN_MIME_VERSION_BYTES);
        assertEquals(2, descriptor.getMimeMajorVersion());
        assertEquals(4, descriptor.getMimeMinorVersion());
    }

    public void testContentId() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_8859_BYTES);
        assertEquals(1, descriptor.getMimeMajorVersion());
        assertEquals(0, descriptor.getMimeMinorVersion());
        assertEquals(ExampleMail.CONTENT_ID, descriptor.getContentId());
    }

    public void testContentDescription() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_8859_BYTES);
        assertEquals(1, descriptor.getMimeMajorVersion());
        assertEquals(0, descriptor.getMimeMinorVersion());
        assertEquals(ExampleMail.CONTENT_DESCRIPTION, descriptor.getContentDescription());
    }

    public void testMimeVersionHeaderBreak() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_ASCII_MIME_VERSION_SPANS_TWO_LINES_BYTES);
        assertEquals(4, descriptor.getMimeMajorVersion());
        assertEquals(1, descriptor.getMimeMinorVersion());
    }

    public void testContentDispositionType() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BYTES);
        assertEquals("inline", descriptor.getContentDispositionType());
    }

    public void testContentDispositionTypeCaseConversion() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_BASE64_LATIN1_BYTES);
        assertEquals("Should be converted to lower case", "inline", descriptor.getContentDispositionType());
        assertNotNull(descriptor.getContentDispositionParameters());
        assertEquals(0, descriptor.getContentDispositionParameters().size());
    }

    public void testContentDispositionParameters() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS_BYTES);
        assertEquals("inline", descriptor.getContentDispositionType());
        assertNotNull(descriptor.getContentDispositionParameters());
        assertEquals(3, descriptor.getContentDispositionParameters().size());
        assertEquals("value", descriptor.getContentDispositionParameters().get("param"));
        assertEquals("1", descriptor.getContentDispositionParameters().get("one"));
        assertEquals("bar", descriptor.getContentDispositionParameters().get("foo"));
    }

    public void testContentDispositionStandardParameters() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_BINARY_ATTACHMENTS_BYTES, 1);
        assertEquals("attachment", descriptor.getContentDispositionType());
        assertNotNull(descriptor.getContentDispositionParameters());
        assertEquals(5, descriptor.getContentDispositionParameters().size());
        assertEquals("blob.png", descriptor.getContentDispositionFilename());

        SimpleDateFormat dateparser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateparser.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals(dateparser.parse("2008-06-21 15:32:18"), descriptor.getContentDispositionModificationDate());
        assertEquals(dateparser.parse("2008-06-20 10:15:09"), descriptor.getContentDispositionCreationDate());
        assertEquals(dateparser.parse("2008-06-22 12:08:56"), descriptor.getContentDispositionReadDate());
        assertEquals(10234, descriptor.getContentDispositionSize());
    }

    public void testLanguageParameters() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_BINARY_ATTACHMENTS_BYTES, 3);
        assertNotNull(descriptor.getContentLanguage());
        assertEquals(3, descriptor.getContentLanguage().size());
        assertEquals("en", descriptor.getContentLanguage().get(0));
        assertEquals("en-US", descriptor.getContentLanguage().get(1));
        assertEquals("en-CA", descriptor.getContentLanguage().get(2));
    }

    public void testContentLocationRelativeUrl() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 0);
        assertEquals("relative/url", descriptor.getContentLocation());
    }

    public void testContentLocationAbsoluteUrl() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 1);
        assertEquals("http://www.example.org/absolute/rhubard.txt", descriptor.getContentLocation());
    }

    public void testContentLocationWithComment() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 3);
        assertEquals("http://www.example.org/absolute/comments/rhubard.txt", descriptor.getContentLocation());
    }

    public void testContentLocationFoldedUrl() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.MULTIPART_WITH_CONTENT_LOCATION_BYTES, 4);
        assertEquals("http://www.example.org/this/is/a/very/long/url/split/over/two/lines/", descriptor.getContentLocation());
    }

    public void testContentMD5Url() throws Exception {
        MaximalBodyDescriptor descriptor = describe(ExampleMail.ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS_BYTES);
        assertEquals(ExampleMail.MD5_CONTENT, descriptor.getContentMD5Raw());
    }

    private MaximalBodyDescriptor describe(byte[] mail, int zeroBasedPart) throws Exception {
        ByteArrayInputStream bias = new ByteArrayInputStream(mail);
        parser.parse(bias);
        EntityState state = parser.next();
        while (state != EntityState.T_END_OF_STREAM && zeroBasedPart>=0) {
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
        while (state != EntityState.T_BODY && state != EntityState.T_END_OF_STREAM)
        {
            state = parser.next();
        }
        assertEquals(EntityState.T_BODY, state);
        BodyDescriptor descriptor = parser.getBodyDescriptor();
        assertNotNull(descriptor);
        assertTrue("Parser is maximal so body descriptor should be maximal", descriptor instanceof MaximalBodyDescriptor);
        return (MaximalBodyDescriptor) descriptor;
    }

}
