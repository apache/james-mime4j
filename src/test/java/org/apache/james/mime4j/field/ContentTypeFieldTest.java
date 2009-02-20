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

package org.apache.james.mime4j.field;

import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.log4j.BasicConfigurator;

import junit.framework.TestCase;

public class ContentTypeFieldTest extends TestCase {

    @Override
    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }
    
    public void testMimeTypeWithSemiColonNoParams() throws Exception  {
        ContentTypeField f = null;
        
        f = (ContentTypeField) AbstractField.parse("Content-Type: text/html;");
        assertEquals("text/html", f.getMimeType());
    }
    
    public void testGetMimeType() throws Exception {
        ContentTypeField f = null;
        
        f = (ContentTypeField) AbstractField.parse("Content-Type: text/PLAIN");
        assertEquals("text/plain", f.getMimeType());
        
        f = (ContentTypeField) AbstractField.parse("content-type:   TeXt / html   ");
        assertEquals("text/html", f.getMimeType());
        
        f = (ContentTypeField) AbstractField.parse("CONTENT-TYPE:   x-app/yada ;"
                                                    + "  param = yada");
        assertEquals("x-app/yada", f.getMimeType());
        
        f = (ContentTypeField) AbstractField.parse("CONTENT-TYPE:   yada");
        assertEquals("", f.getMimeType());
    }
    
    public void testGetMimeTypeStatic() throws Exception {
        ContentTypeField child = null;
        ContentTypeField parent = null;
        
        child = (ContentTypeField) AbstractField.parse("Content-Type: child/type");
        parent = (ContentTypeField) AbstractField.parse("Content-Type: parent/type");
        assertEquals("child/type", ContentTypeField.getMimeType(child, parent));
        
        child = null;
        parent = (ContentTypeField) AbstractField.parse("Content-Type: parent/type");
        assertEquals("text/plain", ContentTypeField.getMimeType(child, parent));
        parent = (ContentTypeField) AbstractField.parse("Content-Type: multipart/digest");
        assertEquals("message/rfc822", ContentTypeField.getMimeType(child, parent));
        
        child = (ContentTypeField) AbstractField.parse("Content-Type:");
        parent = (ContentTypeField) AbstractField.parse("Content-Type: parent/type");
        assertEquals("text/plain", ContentTypeField.getMimeType(child, parent));
        parent = (ContentTypeField) AbstractField.parse("Content-Type: multipart/digest");
        assertEquals("message/rfc822", ContentTypeField.getMimeType(child, parent));
    }
    
    public void testGetCharsetStatic() throws Exception {
        ContentTypeField f = null;
        
        f = (ContentTypeField) AbstractField.parse("Content-Type: some/type; charset=iso8859-1");
        assertEquals("iso8859-1", ContentTypeField.getCharset(f));
        
        f = (ContentTypeField) AbstractField.parse("Content-Type: some/type;");
        assertEquals("us-ascii", ContentTypeField.getCharset(f));
    }
    
    public void testGetParameter() throws Exception {
        ContentTypeField f = null;
        
        f = (ContentTypeField) AbstractField.parse("CONTENT-TYPE:   text / html ;"
                                                + "  boundary=yada yada");
        assertEquals("yada", f.getParameter("boundary"));
        
        f = (ContentTypeField) AbstractField.parse("Content-Type: x-app/yada;"
                                                + "  boUNdarY= \"ya:\\\"*da\"; "
                                                + "\tcharset\t =  us-ascii");
        assertEquals("ya:\"*da", f.getParameter("boundary"));
        assertEquals("us-ascii", f.getParameter("charset"));
        
        f = (ContentTypeField) AbstractField.parse("Content-Type: x-app/yada;  "
                            + "boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                            + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        assertEquals("ya \"\"\tda \"", f.getParameter("boundary"));
        assertEquals("\"hepp\"  =us\t-ascii", f.getParameter("charset"));
    }

}
