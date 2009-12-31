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
import org.apache.james.mime4j.field.impl.ContentTypeFieldImpl;
import org.apache.james.mime4j.field.impl.DefaultFieldParser;
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
        
        f = (ContentTypeField) DefaultFieldParser.parse("Content-Type: text/html;");
        assertEquals("text/html", f.getMimeType());
    }
    
    public void testGetMimeType() throws Exception {
        ContentTypeField f = null;
        
        f = (ContentTypeField) DefaultFieldParser.parse("Content-Type: text/PLAIN");
        assertEquals("text/plain", f.getMimeType());
        
        f = (ContentTypeField) DefaultFieldParser.parse("content-type:   TeXt / html   ");
        assertEquals("text/html", f.getMimeType());
        
        f = (ContentTypeField) DefaultFieldParser.parse("CONTENT-TYPE:   x-app/yada ;"
                                                    + "  param = yada");
        assertEquals("x-app/yada", f.getMimeType());
        
        f = (ContentTypeField) DefaultFieldParser.parse("CONTENT-TYPE:   yada");
        assertEquals("", f.getMimeType());
    }
    
    public void testGetMimeTypeStatic() throws Exception {
        ContentTypeField child = null;
        ContentTypeField parent = null;
        
        child = (ContentTypeField) DefaultFieldParser.parse("Content-Type: child/type");
        parent = (ContentTypeField) DefaultFieldParser.parse("Content-Type: parent/type");
        assertEquals("child/type", ContentTypeFieldImpl.getMimeType(child, parent));
        
        child = null;
        parent = (ContentTypeField) DefaultFieldParser.parse("Content-Type: parent/type");
        assertEquals("text/plain", ContentTypeFieldImpl.getMimeType(child, parent));
        parent = (ContentTypeField) DefaultFieldParser.parse("Content-Type: multipart/digest");
        assertEquals("message/rfc822", ContentTypeFieldImpl.getMimeType(child, parent));
        
        child = (ContentTypeField) DefaultFieldParser.parse("Content-Type:");
        parent = (ContentTypeField) DefaultFieldParser.parse("Content-Type: parent/type");
        assertEquals("text/plain", ContentTypeFieldImpl.getMimeType(child, parent));
        parent = (ContentTypeField) DefaultFieldParser.parse("Content-Type: multipart/digest");
        assertEquals("message/rfc822", ContentTypeFieldImpl.getMimeType(child, parent));
    }
    
    public void testGetCharsetStatic() throws Exception {
        ContentTypeField f = null;
        
        f = (ContentTypeField) DefaultFieldParser.parse("Content-Type: some/type; charset=iso8859-1");
        assertEquals("iso8859-1", ContentTypeFieldImpl.getCharset(f));
        
        f = (ContentTypeField) DefaultFieldParser.parse("Content-Type: some/type;");
        assertEquals("us-ascii", ContentTypeFieldImpl.getCharset(f));
    }
    
    public void testGetParameter() throws Exception {
        ContentTypeField f = null;
        
        f = (ContentTypeField) DefaultFieldParser.parse("CONTENT-TYPE:   text / html ;"
                                                + "  boundary=yada yada");
        assertEquals("yada", f.getParameter("boundary"));
        
        f = (ContentTypeField) DefaultFieldParser.parse("Content-Type: x-app/yada;"
                                                + "  boUNdarY= \"ya:\\\"*da\"; "
                                                + "\tcharset\t =  us-ascii");
        assertEquals("ya:\"*da", f.getParameter("boundary"));
        assertEquals("us-ascii", f.getParameter("charset"));
        
        f = (ContentTypeField) DefaultFieldParser.parse("Content-Type: x-app/yada;  "
                            + "boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                            + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        assertEquals("ya \"\"\tda \"", f.getParameter("boundary"));
        assertEquals("\"hepp\"  =us\t-ascii", f.getParameter("charset"));
    }

}
