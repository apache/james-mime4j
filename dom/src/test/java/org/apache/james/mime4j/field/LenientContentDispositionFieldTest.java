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

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Assert;
import org.junit.Test;

public class LenientContentDispositionFieldTest {

    static ContentDispositionField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentDispositionFieldLenientImpl.PARSER.parse(rawField, null);
    }

    static ContentDispositionField parse(final byte[] raw) throws MimeException {
        RawField rawField = RawFieldParser.DEFAULT.parseField(new ByteArrayBuffer(raw, true));
        return ContentDispositionFieldLenientImpl.PARSER.parse(rawField, null);
    }

    @Test
    public void testDispositionTypeWithSemiColonNoParams() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline;");
        Assert.assertEquals("inline", f.getDispositionType());
    }

    @Test
    public void testGetDispositionType() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: attachment");
        Assert.assertEquals("attachment", f.getDispositionType());

        f = parse("content-disposition:   InLiNe   ");
        Assert.assertEquals("inline", f.getDispositionType());

        f = parse("CONTENT-DISPOSITION:   x-yada ;" + "  param = yada");
        Assert.assertEquals("x-yada", f.getDispositionType());

        f = parse("CONTENT-DISPOSITION:   ");
        Assert.assertEquals("", f.getDispositionType());
    }

    @Test
    public void testGetParameter() throws Exception {
        ContentDispositionField f = parse("CONTENT-DISPOSITION:   inline ;"
                + "  filename=yada yada");
        Assert.assertEquals("yada yada", f.getParameter("filename"));

        f = parse("Content-Disposition: x-yada;"
                + "  fileNAme= \"ya:\\\"*da\"; " + "\tSIZE\t =  1234");
        Assert.assertEquals("ya:\"*da", f.getParameter("filename"));
        Assert.assertEquals("1234", f.getParameter("size"));

        f = parse("Content-Disposition: x-yada;  "
                + "fileNAme= \"ya \\\"\\\"\tda \\\"\"; "
                + "\tx-Yada\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        Assert.assertEquals("ya \"\"\tda \"", f.getParameter("filename"));
        Assert.assertEquals("\"hepp\"  =us\t-ascii", f.getParameter("x-yada"));
    }

    @Test
    public void testIsDispositionType() throws Exception {
        ContentDispositionField f = parse("Content-Disposition:INline");
        Assert.assertTrue(f.isDispositionType("InLiNe"));
        Assert.assertFalse(f.isDispositionType("NiLiNe"));
        Assert.assertTrue(f.isInline());
        Assert.assertFalse(f.isAttachment());

        f = parse("Content-Disposition: attachment");
        Assert.assertTrue(f.isDispositionType("ATTACHMENT"));
        Assert.assertFalse(f.isInline());
        Assert.assertTrue(f.isAttachment());

        f = parse("Content-Disposition: x-something");
        Assert.assertTrue(f.isDispositionType("x-SomeThing"));
        Assert.assertFalse(f.isInline());
        Assert.assertFalse(f.isAttachment());
    }

    @Test
    public void testGetFilename() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; filename=yada.txt");
        Assert.assertEquals("yada.txt", f.getFilename());

        f = parse("Content-Disposition: inline; filename=yada yada.txt");
        Assert.assertEquals("yada yada.txt", f.getFilename());

        f = parse("Content-Disposition: inline; filename=\"yada yada.txt\"");
        Assert.assertEquals("yada yada.txt", f.getFilename());

        f = parse("Content-Disposition: inline");
        Assert.assertNull(f.getFilename());
    }

    @Test
    public void testGetFilenameEncoded() throws Exception {
        byte[] data = ("Content-Disposition: attachment;\n" +
            " FileName=\"=?WINDOWS-1251?Q?3244659=5F=C0=EA=F2_=E7=E0_=C8=FE=EB=FC_?=\n" +
            " =?WINDOWS-1251?Q?2020.pdf?=\"")
            .getBytes(StandardCharsets.UTF_8);
        
        ContentDispositionField f = parse(data);

        Assert.assertEquals("WINDOWS-1251 Q encoded filename", "3244659_Акт за Июль 2020.pdf", f.getFilename());
    }

    @Test
    public void testGetFilenameUtf8() throws Exception {
        byte[] data = 
            "Content-Disposition: attachment; filename=\"УПД ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"СТАНЦИЯ ВИРТУАЛЬНАЯ\" 01-05-21.pdf\""
            .getBytes(StandardCharsets.UTF_8);

        ContentDispositionField f = parse(data);

        Assert.assertEquals("UTF8 encoded filename", "УПД ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"СТАНЦИЯ ВИРТУАЛЬНАЯ\" 01-05-21.pdf", f.getFilename());
    }

    @Test
    public void testGetCreationDate() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; "
                + "creation-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        Assert.assertEquals(new Date(0), f.getCreationDate());

        f = parse("Content-Disposition: inline; "
                + "creation-date=Tue, 01 Jan 1970 00:00:00 +0000");
        Assert.assertEquals(new Date(0), f.getCreationDate());

        f = parse("Content-Disposition: attachment");
        Assert.assertNull(f.getCreationDate());
    }

    @Test
    public void testGetModificationDate() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; "
                + "modification-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        Assert.assertEquals(new Date(0), f.getModificationDate());

        f = parse("Content-Disposition: inline; "
                + "modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"");
        Assert.assertEquals(new Date(855782991000l), f.getModificationDate());

        f = parse("Content-Disposition: inline; "
                + "modification-date=yesterday");
        Assert.assertNull(f.getModificationDate());

        f = parse("Content-Disposition: attachment");
        Assert.assertNull(f.getModificationDate());
    }

    @Test
    public void testGetReadDate() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; "
                + "read-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        Assert.assertEquals(new Date(0), f.getReadDate());

        f = parse("Content-Disposition: inline; read-date=");
        Assert.assertNull(f.getReadDate());

        f = parse("Content-Disposition: attachment");
        Assert.assertNull(f.getReadDate());
    }

    @Test
    public void testGetSize() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: attachment; size=0");
        Assert.assertEquals(0, f.getSize());

        f = parse("Content-Disposition: attachment; size=matters");
        Assert.assertEquals(-1, f.getSize());

        f = parse("Content-Disposition: attachment");
        Assert.assertEquals(-1, f.getSize());

        f = parse("Content-Disposition: attachment; size=-12");
        Assert.assertEquals(-1, f.getSize());

        f = parse("Content-Disposition: attachment; size=12");
        Assert.assertEquals(12, f.getSize());
    }

}
