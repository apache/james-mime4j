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

package org.apache.james.mime4j.descriptor;

import junit.framework.TestCase;

import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.ByteSequence;

public abstract class BaseTestForBodyDescriptors extends TestCase {

    protected abstract MutableBodyDescriptor newBodyDescriptor();

    protected abstract MutableBodyDescriptor newBodyDescriptor(BodyDescriptor parent);
    
    public void testGetParameters() {
        MutableBodyDescriptor bd = null;
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/plain; charset=ISO-8859-1; "
                + "boundary=foo; param1=value1; param2=value2; param3=value3"));
        assertEquals(3, bd.getContentTypeParameters().size());
        assertEquals("value1", bd.getContentTypeParameters().get("param1"));
        assertEquals("value2", bd.getContentTypeParameters().get("param2"));
        assertEquals("value3", bd.getContentTypeParameters().get("param3"));
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/plain; param1=value1; param2=value2;"
                     + " param3=value3"));
        assertEquals(3, bd.getContentTypeParameters().size());
        assertEquals("value1", bd.getContentTypeParameters().get("param1"));
        assertEquals("value2", bd.getContentTypeParameters().get("param2"));
        assertEquals("value3", bd.getContentTypeParameters().get("param3"));
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/plain; "
                + "param1= \" value with\tspaces \" ; "
                + "param2=\"\\\"value4 with escaped \\\" \\\"\";"));
        assertEquals(2, bd.getContentTypeParameters().size());
        assertEquals(" value with\tspaces ", bd.getContentTypeParameters().get("param1"));
        assertEquals("\"value4 with escaped \" \"", bd.getContentTypeParameters().get("param2"));
        
        /*
         * Make sure escaped characters (except ") are still escaped.
         * The parameter value should be \n\"
         */
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/plain; param=\"\\n\\\\\\\"\""));
        assertEquals(1, bd.getContentTypeParameters().size());
        assertEquals("\\n\\\"", bd.getContentTypeParameters().get("param"));
    }
    
    public void testAddField() {
        MutableBodyDescriptor bd = null;
        
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/plain; charset=ISO-8859-1"));
        assertEquals("text/plain", bd.getMimeType());
        assertEquals("iso-8859-1", bd.getCharset());
        bd.addField(new TestField("Content-Type ", "text/html; charset=us-ascii"));
        assertEquals("text/plain", bd.getMimeType());
        assertEquals("iso-8859-1", bd.getCharset());
    }
    
    public void testGetMimeType() {
        MutableBodyDescriptor bd = null;
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/PLAIN"));
        assertEquals("text/plain", bd.getMimeType());
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/PLAIN;"));
        assertEquals("text/plain", bd.getMimeType());
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("content-type", "   TeXt / html   "));
        assertEquals("text/html", bd.getMimeType());
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("CONTENT-TYPE", "   x-app/yada ;  param = yada"));
        assertEquals("x-app/yada", bd.getMimeType());
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("CONTENT-TYPE", "   yada"));
        assertEquals("text/plain", bd.getMimeType());
        
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type ", "text/plain"));
        assertEquals("text/plain", bd.getMimeType());
        bd.addField(new TestField("Content-Type ", "text/html"));
        assertEquals("text/plain", bd.getMimeType());
        
        /*
         * Implicit mime types.
         */
        MutableBodyDescriptor child = null;
        MutableBodyDescriptor parent = null;
        
        parent = newBodyDescriptor();
        parent.addField(new TestField("Content-Type", "mutlipart/alternative; boundary=foo"));
        
        child = newBodyDescriptor(parent);
        assertEquals("text/plain", child.getMimeType());
        child.addField(new TestField("Content-Type", " child/type"));
        assertEquals("child/type", child.getMimeType());
        
        parent = newBodyDescriptor();
        parent.addField(new TestField("Content-Type", "multipart/digest; boundary=foo"));
        
        child = newBodyDescriptor(parent);
        assertEquals("message/rfc822", child.getMimeType());
        child.addField(new TestField("Content-Type", " child/type"));
        assertEquals("child/type", child.getMimeType());
        
    }
    
    public void testParameters() {
        MutableBodyDescriptor bd = null;

        /*
         * Test charset.
         */
        bd = newBodyDescriptor();
        assertEquals("us-ascii", bd.getCharset());
        bd.addField(new TestField("Content-Type ", "text/type; charset=ISO-8859-1"));
        assertEquals("iso-8859-1", bd.getCharset());
        
        bd = newBodyDescriptor();
        assertEquals("us-ascii", bd.getCharset());
        bd.addField(new TestField("Content-Type ", "text/type"));
        assertEquals("us-ascii", bd.getCharset());
        
        /*
         * Test boundary.
         */
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type", "text/html; boundary=yada yada"));
        assertNull(bd.getBoundary());

        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type", "multipart/yada; boundary=yada"));
        assertEquals("yada", bd.getBoundary());

        /*
         * Test some weird parameters.
         */
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type", "multipart/yada; boundary=yada yada"));
        assertEquals("yada", bd.getBoundary());
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type", "multipart/yada; boUNdarY= ya:*da; \tcharset\t =  big5"));
        assertEquals("ya:*da", bd.getBoundary());
        assertEquals("big5", bd.getCharset());
        
        bd = newBodyDescriptor();
        bd.addField(new TestField("Content-Type", "multipart/yada; boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                            + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\""));
        assertEquals("ya \"\"\tda \"", bd.getBoundary());
        assertEquals("\"hepp\"  =us\t-ascii", bd.getCharset());
        
    }
    
    public void testGetContentLength() throws Exception {
        MutableBodyDescriptor bd = null;

        bd = newBodyDescriptor();
        assertEquals(-1, bd.getContentLength());

        bd.addField(new TestField("Content-Length", "9901"));
        assertEquals(9901, bd.getContentLength());

        // only the first content-length counts
        bd.addField(new TestField("Content-Length", "1239901"));
        assertEquals(9901, bd.getContentLength());
    }
    
    public void testDoDefaultToUsAsciiWhenUntyped() throws Exception {
        MutableBodyDescriptor descriptor = newBodyDescriptor();
        descriptor.addField(new TestField("To", "me@example.org"));
        assertEquals("us-ascii", descriptor.getCharset());
    }

    public void testDoNotDefaultToUsAsciiForNonTextTypes() throws Exception {
        MutableBodyDescriptor descriptor = newBodyDescriptor();
        descriptor.addField(new TestField("Content-Type", "image/png; name=blob.png"));
        assertNull(descriptor.getCharset());
    }
    
    private static final class TestField implements Field {

        private final String name;
        private final String body;

        public TestField(String name, String body){
            this.name = name;
            this.body = body;
        }
        
        public String getName() {
            return name;
        }

        public String getBody() {
            return body;
        }

        public ByteSequence getRaw() {
            throw new UnsupportedOperationException();
        }
        
    }
}
