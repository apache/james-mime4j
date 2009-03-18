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

import org.apache.log4j.BasicConfigurator;

public class ContentDispositionFieldTest extends TestCase {

    @Override
    public void setUp() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

    public void testDispositionTypeWithSemiColonNoParams() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline;");
        assertEquals("inline", f.getDispositionType());
    }

    public void testGetDispositionType() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment");
        assertEquals("attachment", f.getDispositionType());

        f = (ContentDispositionField) AbstractField
                .parse("content-disposition:   InLiNe   ");
        assertEquals("inline", f.getDispositionType());

        f = (ContentDispositionField) AbstractField
                .parse("CONTENT-DISPOSITION:   x-yada ;" + "  param = yada");
        assertEquals("x-yada", f.getDispositionType());

        f = (ContentDispositionField) AbstractField.parse("CONTENT-DISPOSITION:   ");
        assertEquals("", f.getDispositionType());
    }

    public void testGetParameter() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("CONTENT-DISPOSITION:   inline ;"
                        + "  filename=yada yada");
        assertEquals("yada", f.getParameter("filename"));

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: x-yada;"
                        + "  fileNAme= \"ya:\\\"*da\"; " + "\tSIZE\t =  1234");
        assertEquals("ya:\"*da", f.getParameter("filename"));
        assertEquals("1234", f.getParameter("size"));

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: x-yada;  "
                        + "fileNAme= \"ya \\\"\\\"\tda \\\"\"; "
                        + "\tx-Yada\t =  \"\\\"hepp\\\"  =us\t-ascii\"");
        assertEquals("ya \"\"\tda \"", f.getParameter("filename"));
        assertEquals("\"hepp\"  =us\t-ascii", f.getParameter("x-yada"));
    }

    public void testIsDispositionType() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField.parse("Content-Disposition:INline");
        assertTrue(f.isDispositionType("InLiNe"));
        assertFalse(f.isDispositionType("NiLiNe"));
        assertTrue(f.isInline());
        assertFalse(f.isAttachment());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment");
        assertTrue(f.isDispositionType("ATTACHMENT"));
        assertFalse(f.isInline());
        assertTrue(f.isAttachment());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: x-something");
        assertTrue(f.isDispositionType("x-SomeThing"));
        assertFalse(f.isInline());
        assertFalse(f.isAttachment());
    }

    public void testGetFilename() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; filename=yada.txt");
        assertEquals("yada.txt", f.getFilename());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; filename=yada yada.txt");
        assertEquals("yada", f.getFilename());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; filename=\"yada yada.txt\"");
        assertEquals("yada yada.txt", f.getFilename());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline");
        assertNull(f.getFilename());
    }

    public void testGetCreationDate() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; "
                        + "creation-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        assertEquals(new Date(0), f.getCreationDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; "
                        + "creation-date=Tue, 01 Jan 1970 00:00:00 +0000");
        assertNull(f.getCreationDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment");
        assertNull(f.getCreationDate());
    }

    public void testGetModificationDate() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; "
                        + "modification-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        assertEquals(new Date(0), f.getModificationDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; "
                        + "modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"");
        assertEquals(new Date(855782991000l), f.getModificationDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; "
                        + "modification-date=yesterday");
        assertNull(f.getModificationDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment");
        assertNull(f.getModificationDate());
    }

    public void testGetReadDate() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; "
                        + "read-date=\"Tue, 01 Jan 1970 00:00:00 +0000\"");
        assertEquals(new Date(0), f.getReadDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: inline; read-date=");
        assertNull(f.getReadDate());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment");
        assertNull(f.getReadDate());
    }

    public void testGetSize() throws Exception {
        ContentDispositionField f = null;

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment; size=0");
        assertEquals(0, f.getSize());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment; size=matters");
        assertEquals(-1, f.getSize());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment");
        assertEquals(-1, f.getSize());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment; size=-12");
        assertEquals(-1, f.getSize());

        f = (ContentDispositionField) AbstractField
                .parse("Content-Disposition: attachment; size=12");
        assertEquals(12, f.getSize());
    }

}
