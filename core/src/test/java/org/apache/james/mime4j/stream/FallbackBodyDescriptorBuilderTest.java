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

package org.apache.james.mime4j.stream;

import org.apache.james.mime4j.BodyDescriptor;

import junit.framework.TestCase;

public class FallbackBodyDescriptorBuilderTest extends TestCase {

    public void testAddField() throws Exception {
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
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
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
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
        BodyDescriptorBuilder parent = new FallbackBodyDescriptorBuilder();
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
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
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

    public void testMultipartNoBoundary() throws Exception {
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type", "multipart/yada; "));
        BodyDescriptor bd = builder.build();
        assertEquals("text/plain", bd.getMimeType());
    }

    public void testGetContentLength() throws Exception {
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
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

    public void testDoDefaultToUsAsciiWhenUntyped() throws Exception {
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
        builder.addField(new RawField("To", "me@example.org"));
        BodyDescriptor bd = builder.build();
        assertEquals("us-ascii", bd.getCharset());
    }

    public void testDoNotDefaultToUsAsciiForNonTextTypes() throws Exception {
        BodyDescriptorBuilder builder = new FallbackBodyDescriptorBuilder();
        builder.addField(new RawField("Content-Type", "image/png; name=blob.png"));
        BodyDescriptor bd = builder.build();
        assertNull(bd.getCharset());
    }

}
