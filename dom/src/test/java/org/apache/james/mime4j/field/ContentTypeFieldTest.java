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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class ContentTypeFieldTest {

    static ContentTypeField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentTypeFieldImpl.PARSER.parse(rawField, null);
    }

    @Test
    public void testMimeTypeWithSemiColonNoParams() throws Exception {
        ContentTypeField f = parse("Content-Type: text/html;");
        Assert.assertEquals("text/html", f.getMimeType());
        Assert.assertNull(f.getParseException());
    }

    @Test
    public void extraSemicolonShouldNotAbortParameterParsing() throws Exception {
        ContentTypeField f = parse("Content-Type: text/html;; charset=utf-8");
        Assert.assertEquals("text/html", f.getMimeType());
        Assert.assertEquals("utf-8", f.getCharset());
        Assert.assertNull(f.getParseException());
    }

    @Test
    public void testGetMimeType() throws Exception {
        ContentTypeField f = parse("Content-Type: text/PLAIN");
        Assert.assertEquals("text/plain", f.getMimeType());

        f = parse("content-type:   TeXt / html   ");
        Assert.assertEquals("text/html", f.getMimeType());

        f = parse("CONTENT-TYPE:   x-app/yada ;"
                + "  param = yada");
        Assert.assertEquals("x-app/yada", f.getMimeType());

        f = parse("CONTENT-TYPE:   yada");
        Assert.assertEquals(null, f.getMimeType());
    }

    @Test
    public void testGetMimeTypeStatic() throws Exception {
        ContentTypeField child = parse("Content-Type: child/type");
        ContentTypeField parent = parse("Content-Type: parent/type");

        Assert.assertEquals("child/type", ContentTypeFieldImpl.getMimeType(child, parent));

        child = null;
        parent = parse("Content-Type: parent/type");
        Assert.assertEquals("text/plain", ContentTypeFieldImpl.getMimeType(child, parent));
        parent = parse("Content-Type: multipart/digest");
        Assert.assertEquals("message/rfc822", ContentTypeFieldImpl.getMimeType(child, parent));

        child = parse("Content-Type:");
        parent = parse("Content-Type: parent/type");
        Assert.assertEquals("text/plain", ContentTypeFieldImpl.getMimeType(child, parent));
        parent = parse("Content-Type: multipart/digest");
        Assert.assertEquals("message/rfc822", ContentTypeFieldImpl.getMimeType(child, parent));
    }

    @Test
    public void testGetCharsetStatic() throws Exception {
        ContentTypeField f = parse("Content-Type: some/type; charset=iso8859-1");
        Assert.assertEquals("iso8859-1", ContentTypeFieldImpl.getCharset(f));

        f = parse("Content-Type: some/type;");
        Assert.assertEquals("us-ascii", ContentTypeFieldImpl.getCharset(f));
    }

    @Test
    public void testGetParameter() throws Exception {
        ContentTypeField f = parse("CONTENT-TYPE:   text / html ;"
                + "  boundary=yada yada");
        Assert.assertEquals("yada", f.getParameter("boundary"));

        f = parse("Content-Type: x-app/yada;"
                + "  boUNdarY= \"ya:\\\"*da\"; "
                + "\tcharset\t =  us-ascii");
        Assert.assertEquals("ya:\"*da", f.getParameter("boundary"));
        Assert.assertEquals("us-ascii", f.getParameter("charset"));

        f = parse("Content-Type: x-app/yada;  "
                + "boUNdarY= \"ya \\\"\\\"\tda \\\"\"; "
                + "\tcharset\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        Assert.assertEquals("ya \"\"\tda \"", f.getParameter("boundary"));
        Assert.assertEquals("\"hepp\"  =us\t-ascii", f.getParameter("charset"));
    }

}
