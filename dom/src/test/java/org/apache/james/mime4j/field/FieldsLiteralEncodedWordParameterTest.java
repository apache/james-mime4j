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

import java.util.Collections;

import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;
import org.junit.Test;

/**
 * A parameter value holding a literal "=?...?=" is read back unchanged, although the readers
 * decode the encoded words of the quoted parameter values they receive.
 */
public class FieldsLiteralEncodedWordParameterTest {

    private static String raw(Field field) {
        return field.getName() + ": " + field.getBody();
    }

    private static void assertFilenameRoundTrips(String filename) throws Exception {
        ContentDispositionField field = Fields.contentDisposition("attachment", filename, -1, null, null, null);
        assertEquals(raw(field), filename, field.getFilename());
        String raw = raw(field);
        assertEquals(raw, filename, ((ContentDispositionField) DefaultFieldParser.parse(raw)).getFilename());
        assertEquals(raw, filename, ((ContentDispositionField) LenientFieldParser.parse(raw)).getFilename());
    }

    @Test
    public void filenameThatLooksLikeAnEncodedWordShouldBeReadBackUnchanged() throws Exception {
        assertFilenameRoundTrips("=?utf-8?q?x?=");
        assertFilenameRoundTrips("a =?utf-8?q?x?= b");
        assertFilenameRoundTrips("=?utf-8?q?x?=aaaaaaaaa\"\"\"\"\"\"\"");
        // long enough for RFC 2231 sections
        StringBuilder sb = new StringBuilder("=?utf-8?q?x?=");
        for (int i = 0; i < 100; i++) {
            sb.append("y");
        }
        assertFilenameRoundTrips(sb.toString());
    }

    @Test
    public void contentDispositionParameterMapShouldKeepALiteralEncodedWord() throws Exception {
        ContentDispositionField field = Fields.contentDisposition("6",
                Collections.singletonMap("report-type", "=?utf-8?q?x?=aaaaaaaaa\"\"\"\"\"\"\""));
        assertEquals("=?utf-8?q?x?=aaaaaaaaa\"\"\"\"\"\"\"", field.getParameter("report-type"));
    }

    @Test
    public void contentTypeParameterThatHoldsAnEncodedWordShouldBeReadBackUnchanged() throws Exception {
        String[] values = {"=?utf-8?q?x?=\"", "=?utf-8?q?x?=", "a =?utf-8?q?x?="};
        for (String value : values) {
            ContentTypeField field = Fields.contentType("text/plain", new NameValuePair("name", value));
            String raw = raw(field);
            assertEquals(raw, value, field.getParameter("name"));
            assertEquals(raw, value, ((ContentTypeField) LenientFieldParser.parse(raw)).getParameter("name"));
        }
    }

    @Test
    public void receivedEncodedFilenamesShouldStillBeDecoded() throws Exception {
        String raw = "Content-Disposition: attachment; filename=\"=?utf-8?q?caf=C3=A9.txt?=\"";
        assertEquals("café.txt", ((ContentDispositionField) LenientFieldParser.parse(raw)).getFilename());
        assertEquals("café.txt", ((ContentDispositionField) DefaultFieldParser.parse(raw)).getFilename());
    }

    @Test
    public void plainFilenamesShouldStillBeQuoted() {
        ContentDispositionField field = Fields.contentDisposition("attachment", "a b.txt", -1, null, null, null);
        assertEquals("Content-Disposition: attachment; filename=\"a b.txt\"", raw(field));
    }
}
