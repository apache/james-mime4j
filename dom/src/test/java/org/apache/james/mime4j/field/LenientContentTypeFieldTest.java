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

public class LenientContentTypeFieldTest {

    static ContentTypeField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentTypeFieldLenientImpl.PARSER.parse(rawField, null);
    }

    @Test
    public void testMimeTypeWithSemiColonNoParams() throws Exception {
        ContentTypeField f = parse("Content-Type: text/html;");
        Assert.assertEquals("text/html", f.getMimeType());
    }

    @Test
    public void testMimeTypeWithMultipleSemiColon() throws Exception {
        ContentTypeField f = parse("Content-Type: text/html;;;");
        Assert.assertEquals("text/html", f.getMimeType());
        Assert.assertEquals(1, f.getParameters().size());
    }

    @Test
    public void testMimeTypeWithNonameParam() throws Exception {
        ContentTypeField f = parse("Content-Type: text/html;=stuff");
        Assert.assertEquals("text/html", f.getMimeType());
        Assert.assertEquals(1, f.getParameters().size());
        Assert.assertEquals("stuff", f.getParameter(""));
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
    public void testGetParameter() throws Exception {
        ContentTypeField f = parse("CONTENT-TYPE:   text / html ;"
                + "  boundary=yada yada");
        Assert.assertEquals("yada yada", f.getParameter("boundary"));

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
