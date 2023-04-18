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

import java.util.Date;

import junit.framework.TestCase;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

public class ContentDispositionFieldTest extends TestCase {

    static ContentDispositionField parse(final String s) throws MimeException {
        ByteSequence raw = ContentUtil.encode(s);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return ContentDispositionFieldImpl.PARSER.parse(rawField, null);
    }

    public void testDispositionTypeWithSemiColonNoParams() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline;");
        assertEquals("inline", f.getDispositionType());
    }

    public void testGetDispositionType() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: attachment");
        assertEquals("attachment", f.getDispositionType());

        f = parse("content-disposition:   InLiNe   ");
        assertEquals("inline", f.getDispositionType());

        f = parse("CONTENT-DISPOSITION:   x-yada ;" + "  param = yada");
        assertEquals("x-yada", f.getDispositionType());

        f = parse("CONTENT-DISPOSITION:   ");
        assertEquals("", f.getDispositionType());
    }

    public void testGetParameter() throws Exception {
        ContentDispositionField f = parse("CONTENT-DISPOSITION:   inline ;"
                        + "  filename=yada yada");
        assertEquals("yada", f.getParameter("filename"));

        f = parse("Content-Disposition: x-yada;"
                        + "  fileNAme= \"ya:\\\"*da\"; " + "\tSIZE\t =  1234");
        assertEquals("ya:\"*da", f.getParameter("filename"));
        assertEquals("1234", f.getParameter("size"));

        f = parse("Content-Disposition: x-yada;  "
                        + "fileNAme= \"ya \\\"\\\"\tda \\\"\"; "
                        + "\tx-Yada\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        assertEquals("ya \"\"\tda \"", f.getParameter("filename"));
        assertEquals("\"hepp\"  =us\t-ascii", f.getParameter("x-yada"));
    }

    public void testIsDispositionType() throws Exception {
        ContentDispositionField f = parse("Content-Disposition:INline");
        assertTrue(f.isDispositionType("InLiNe"));
        assertFalse(f.isDispositionType("NiLiNe"));
        assertTrue(f.isInline());
        assertFalse(f.isAttachment());

        f = parse("Content-Disposition: attachment");
        assertTrue(f.isDispositionType("ATTACHMENT"));
        assertFalse(f.isInline());
        assertTrue(f.isAttachment());

        f = parse("Content-Disposition: x-something");
        assertTrue(f.isDispositionType("x-SomeThing"));
        assertFalse(f.isInline());
        assertFalse(f.isAttachment());
    }

    private static String getMessage(ContentDispositionField field) {
        if( field.getParseException() == null) {
            return "";
        }
        return field.getParseException().getMessage() + " when parsing " + field.getRaw();
    }

    public void testGetFilename() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; filename=yada.txt");
        assertEquals("yada.txt", f.getFilename());

        f = parse("Content-Disposition: inline; filename=yada yada.txt");
        assertEquals("yada", f.getFilename());

        f = parse("Content-Disposition: inline; filename=\"yada yada.txt\"");
        assertEquals("yada yada.txt", f.getFilename());

        f = parse("Content-Disposition: inline");
        assertNull(f.getFilename());
    }

    public void testNonAsciiFilename() throws MimeException {
        ContentDispositionField f =
                parse("Content-Disposition: attachment;"
                        + "\nfilename*0=\"=?UTF-8?Q?3-2_FORPROSJEKT_2-Sheet_-_XXX_A_2_40_?="
                        + "\n=?UTF-8?Q?\";"
                        + "\nfilename*1=\"201_-_Fasader_nord=C3=B8st_og_nordvest.dwg?=\"");

        assertEquals(getMessage(f),
                "3-2 FORPROSJEKT 2-Sheet - XXX A 2 40 201 - Fasader nordøst og nordvest.dwg",
                f.getFilename());
    }

    public void testExtendedNotation() throws MimeException {
        ContentDispositionField f = parse("Content-Disposition: attachment;\n" +
                " filename*=utf-8''%D8%AF%D9%8A%D9%86%D8%A7%D8%B5%D9%88%D8%B1%2E%6F%64%74");
        String name = f.getFilename();
        assertEquals(getMessage(f), "ديناصور.odt", name);
    }

    public void testExtendedNotationWithEmptyCharsetShouldNotCrash() throws MimeException {
        String fileName = "''%D8%AF%D9%8A%D9%86%D8%A7%D8%B5%D9%88%D8%B1%2E%6F%64%74";
        String fileNameString = String.format("\"Content-Disposition: attachment;\n" +
                " \nfilename*=%s\"", fileName);
        ContentDispositionField f = parse(fileNameString);
        String name = f.getFilename();
        assertEquals(getMessage(f), "%D8%AF%D9%8A%D9%86%D8%A7%D8%B5%D9%88%D8%B1%2E%6F%64%74", name);
    }

    public void testFileNameWithInitialSection() throws MimeException {
        ContentDispositionField f = parse("Content-Disposition: attachment;"
                + "\nfilename*0*=filename.txt");
        assertEquals(getMessage(f),"filename.txt", f.getFilename());
    }

    public void testFileNameWithMultipleSections() throws MimeException {
        ContentDispositionField f = parse("Content-Disposition: attachment;"
                + "\nfilename*0=\"first part \"; filename*1=\"of long filename.txt\"");
        f.getFilename();
        assertEquals(getMessage(f),"first part of long filename.txt", f.getFilename());
    }


    public void testGetCreationDate() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; "
                        + "creation-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        assertEquals(new Date(0), f.getCreationDate());

        f = parse("Content-Disposition: inline; "
                        + "creation-date=Tue, 01 Jan 1970 00:00:00 +0000");
        assertNull(f.getCreationDate());

        f = parse("Content-Disposition: attachment");
        assertNull(f.getCreationDate());
    }

    public void testGetModificationDate() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; "
                        + "modification-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        assertEquals(new Date(0), f.getModificationDate());

        f = parse("Content-Disposition: inline; "
                        + "modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"");
        assertEquals(new Date(855782991000l), f.getModificationDate());

        f = parse("Content-Disposition: inline; "
                        + "modification-date=yesterday");
        assertNull(f.getModificationDate());

        f = parse("Content-Disposition: attachment");
        assertNull(f.getModificationDate());
    }

    public void testGetReadDate() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: inline; "
                        + "read-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        assertEquals(new Date(0), f.getReadDate());

        f = parse("Content-Disposition: inline; read-date=");
        assertNull(f.getReadDate());

        f = parse("Content-Disposition: attachment");
        assertNull(f.getReadDate());
    }

    public void testGetSize() throws Exception {
        ContentDispositionField f = parse("Content-Disposition: attachment; size=0");
        assertEquals(0, f.getSize());

        f = parse("Content-Disposition: attachment; size=matters");
        assertEquals(-1, f.getSize());

        f = parse("Content-Disposition: attachment");
        assertEquals(-1, f.getSize());

        f = parse("Content-Disposition: attachment; size=-12");
        assertEquals(-1, f.getSize());

        f = parse("Content-Disposition: attachment; size=12");
        assertEquals(12, f.getSize());
    }

}
