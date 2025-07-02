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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
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
        String data = "Content-Disposition: attachment;\n" +
            " FileName=\"=?WINDOWS-1251?Q?3244659=5F=C0=EA=F2_=E7=E0_=C8=FE=EB=FC_?=\n" +
            " =?WINDOWS-1251?Q?2020.pdf?=\"";

        ContentDispositionField f = parse(data);

        Assert.assertEquals("WINDOWS-1251 Q encoded filename", "3244659_Акт за Июль 2020.pdf", f.getFilename());
    }

    @Test
    public void testGetFilenameUtf8() throws Exception {
        String data =
            "Content-Disposition: attachment; filename=\"УПД ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \\\"СТАНЦИЯ ВИРТУАЛЬНАЯ\\\" 01-05-21.pdf\"";
        ContentDispositionField f = parse(data);

        Assert.assertEquals("UTF8 encoded filename", "УПД ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"СТАНЦИЯ ВИРТУАЛЬНАЯ\" 01-05-21.pdf", f.getFilename());
    }

    @Test
    public void testGetFilenameMultipartUtf8() throws Exception {
        String data = "Content-Disposition: attachment;\n" +
            "	filename*0*=\"UTF-8''%D0%A0%D0%BE%D1%81%D1%82%D0%B5%D0%BB%D0%B5%D0%BA%D0%BE\";\n" +
            "	filename*1*=\"%D0%BC%2E%78%6C%73%78\"\n";

        ContentDispositionField f = parse(data);
        Assert.assertEquals("Ростелеком.xlsx", f.getFilename());
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

    @Test
    public void testMultipartFileName() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: attachment;\n" +
                " filename*0=\"looooooooooooooooooooooooooooooooooooooooooooooooooooooooooo\";\n" +
                " filename*1=\"oooooooooooooooooooooooooooooooooooooong_fiiiiiiiiiiiiiiiiii\";\n" +
                " filename*2=\"iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiileeeeeeeeeeeeeeeeeee\";\n" +
                " filename*3=\"eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.txt\"");
        Assert.assertEquals("loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooong_fiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiileeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.txt", f.getFilename());
    }

    @Test
    public void testNonAsciiFilename() throws MimeException {
         ContentDispositionField f = parse("Content-Disposition: attachment;"
                        + "\nfilename*0=\"=?UTF-8?Q?3-2_FORPROSJEKT_2-Sheet_-_XXX_A_2_40_?="
                        + "\n=?UTF-8?Q?\";"
                        + "\nfilename*1=\"201_-_Fasader_nord=C3=B8st_og_nordvest.dwg?=\"");

        Assert.assertEquals("3-2 FORPROSJEKT 2-Sheet - XXX A 2 40 201 - Fasader nordøst og nordvest.dwg",
                f.getFilename());
    }

    @Test
    public void testBadEncodingFilename() throws MimeException {
         ContentDispositionField f = parse("Content-Disposition: attachment; \n" +
                 "        filename*=utf-8''4%P001!.DOC;\n" +
                 "        filename=\"4%P002!.DOC\"");

         Assert.assertEquals("attachment", f.getDispositionType());
         Assert.assertEquals("4%P001!.DOC", f.getFilename());
    }

    @Test
    public void testDuplicateFields() throws MimeException {
        //test that the first is taken and that concatenation is not applied
        ContentDispositionField f = parse("Content-Disposition: attachment; \n" +
                "filename=\"foo\";\n" +
                "filename=\"bar2.rtf\";\n" +
                "filename=\"bar3.rtf\"");
        assertEquals("foo", f.getFilename());


        //test that field names with * are preferred to those without
        //and test that those with * are properly concatenated
        f = parse("Content-Disposition: attachment; \n" +
                "filename*=\"foo\";\n" +
                "filename=\"bar2.rtf\";\n" +
                "filename*=\"bar3.rtf\"");
        assertEquals("foobar3.rtf", f.getFilename());

        f = parse("Content-Disposition: attachment; \n" +
                "filename=\"bar1.rtf\";\n" +
                "filename*=\"foo\";\n" +
                "filename*=\"bar3.rtf\"");
        assertEquals("foobar3.rtf", f.getFilename());

        f = parse("Content-Disposition: attachment; \n" +
                "filename*=\"foo\";\n" +
                "filename*=\"bar2.rtf\";\n" +
                "filename=\"bar3.rtf\"");
        assertEquals("foobar2.rtf", f.getFilename());
    }
}
