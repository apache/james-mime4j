/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mime4j;

import org.mime4j.BodyDescriptor;

import junit.framework.TestCase;

/**
 * 
 *
 * @author Niklas Therning
 * @version $Id: BodyDescriptorTest.java,v 1.2 2004/10/02 12:41:11 ntherning Exp $
 */
public class BodyDescriptorTest extends TestCase {

    public void testGetParameters() {
        BodyDescriptor bd = null;
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/plain; charset=ISO-8859-1; "
                + "boundary=foo; param1=value1; param2=value2; param3=value3");
        assertEquals(3, bd.getParameters().size());
        assertEquals("value1", (String) bd.getParameters().get("param1"));
        assertEquals("value2", (String) bd.getParameters().get("param2"));
        assertEquals("value3", (String) bd.getParameters().get("param3"));
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/plain; param1=value1; param2=value2;"
                     + " param3=value3");
        assertEquals(3, bd.getParameters().size());
        assertEquals("value1", (String) bd.getParameters().get("param1"));
        assertEquals("value2", (String) bd.getParameters().get("param2"));
        assertEquals("value3", (String) bd.getParameters().get("param3"));
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/plain; "
                + "param1= \" value with\tspaces \" ; "
                + "param2=\"\\\"value4 with escaped \\\" \\\"\";");
        assertEquals(2, bd.getParameters().size());
        assertEquals(" value with\tspaces ", (String) bd.getParameters().get("param1"));
        assertEquals("\"value4 with escaped \" \"", (String) bd.getParameters().get("param2"));
        
        /*
         * Make sure escaped characters (except ") are still escaped.
         * The parameter value should be \n\"
         */
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/plain; param=\"\\n\\\\\\\"\"");
        assertEquals(1, bd.getParameters().size());
        assertEquals("\\n\\\"", (String) bd.getParameters().get("param"));
    }
    
    public void testAddField() {
        BodyDescriptor bd = null;
        
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/plain; charset=ISO-8859-1");
        assertEquals("text/plain", bd.getMimeType());
        assertEquals("iso-8859-1", bd.getCharset());
        bd.addField("Content-Type ", "text/html; charset=us-ascii");
        assertEquals("text/plain", bd.getMimeType());
        assertEquals("iso-8859-1", bd.getCharset());
    }
    
    public void testGetMimeType() {
        BodyDescriptor bd = null;
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/PLAIN");
        assertEquals("text/plain", bd.getMimeType());
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/PLAIN;");
        assertEquals("text/plain", bd.getMimeType());
        
        bd = new BodyDescriptor();
        bd.addField("content-type", "   TeXt / html   ");
        assertEquals("text/html", bd.getMimeType());
        
        bd = new BodyDescriptor();
        bd.addField("CONTENT-TYPE", "   x-app/yada ;  param = yada");
        assertEquals("x-app/yada", bd.getMimeType());
        
        bd = new BodyDescriptor();
        bd.addField("CONTENT-TYPE", "   yada");
        assertEquals("text/plain", bd.getMimeType());
        
        /*
         * Make sure that only the first Content-Type header added is used.
         */
        bd = new BodyDescriptor();
        bd.addField("Content-Type ", "text/plain");
        assertEquals("text/plain", bd.getMimeType());
        bd.addField("Content-Type ", "text/html");
        assertEquals("text/plain", bd.getMimeType());
        
        /*
         * Implicit mime types.
         */
        BodyDescriptor child = null;
        BodyDescriptor parent = null;
        
        parent = new BodyDescriptor();
        parent.addField("Content-Type", "mutlipart/alternative; boundary=foo");
        
        child = new BodyDescriptor(parent);
        assertEquals("text/plain", child.getMimeType());
        child.addField("Content-Type", " child/type");
        assertEquals("child/type", child.getMimeType());
        
        parent = new BodyDescriptor();
        parent.addField("Content-Type", "multipart/digest; boundary=foo");
        
        child = new BodyDescriptor(parent);
        assertEquals("message/rfc822", child.getMimeType());
        child.addField("Content-Type", " child/type");
        assertEquals("child/type", child.getMimeType());
        
    }
    
    public void testParameters() {
        BodyDescriptor bd = null;

        /*
         * Test charset.
         */
        bd = new BodyDescriptor();
        assertEquals("us-ascii", bd.getCharset());
        bd.addField("Content-Type ", "some/type; charset=ISO-8859-1");
        assertEquals("iso-8859-1", bd.getCharset());
        
        bd = new BodyDescriptor();
        assertEquals("us-ascii", bd.getCharset());
        bd.addField("Content-Type ", "some/type");
        assertEquals("us-ascii", bd.getCharset());
        
        /*
         * Test boundary.
         */
        bd = new BodyDescriptor();
        bd.addField("Content-Type", "text/html; boundary=yada yada");
        assertNull(bd.getBoundary());

        bd = new BodyDescriptor();
        bd.addField("Content-Type", "multipart/yada; boundary=yada");
        assertEquals("yada", bd.getBoundary());

        /*
         * Test some weird parameters.
         */
        bd = new BodyDescriptor();
        bd.addField("Content-Type", "multipart/yada; boundary=yada yada");
        assertEquals("yada", bd.getBoundary());
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type", "multipart/yada; boUNdarY= ya:*da; \tcharset\t =  big5");
        assertEquals("ya:*da", bd.getBoundary());
        assertEquals("big5", bd.getCharset());
        
        bd = new BodyDescriptor();
        bd.addField("Content-Type", "multipart/yada; boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                            + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        assertEquals("ya \"\"\tda \"", bd.getBoundary());
        assertEquals("\"hepp\"  =us\t-ascii", bd.getCharset());
        
    }

}
