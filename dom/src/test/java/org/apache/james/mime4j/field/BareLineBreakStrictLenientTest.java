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

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentLanguageField;
import org.apache.james.mime4j.dom.field.ContentLocationField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * A CR or LF that is not part of a fold is deleted by MimeUtil#unfold, which the strict parsers
 * read: the lenient parsers must not take it as a word separator either.
 */
public class BareLineBreakStrictLenientTest {

    @Test
    public void bareCarriageReturnInsideTheTypeShouldBeReadTheSameWayByBothParsers() throws Exception {
        String raw = "Content-Type: multi\rpart/mixed; boundary=x";

        ContentTypeField strict = (ContentTypeField) DefaultFieldParser.parse(raw);
        ContentTypeField lenient = (ContentTypeField) LenientFieldParser.parse(raw);

        Assert.assertEquals("multipart/mixed", strict.getMimeType());
        Assert.assertEquals(strict.getMimeType(), lenient.getMimeType());
        Assert.assertTrue(lenient.isMultipart());
        Assert.assertEquals("x", lenient.getBoundary());
    }

    @Test
    public void bareLineBreaksShouldBeReadTheSameWayByBothParsers() throws Exception {
        assertSameMimeType("Content-Type: text/pl\nain; charset=us-ascii");
        assertSameMimeType("Content-Type: text/plain\r");
        assertSameDisposition("Content-Disposition: +;e='\r4", "e");
        assertSameDisposition("Content-Disposition: e;filename=\"\\\n\\\"\"", "filename");

        ContentLanguageField strictLanguage = (ContentLanguageField) DefaultFieldParser.parse("Content-Language: e\rs-ie2-ze4-1l");
        ContentLanguageField lenientLanguage = (ContentLanguageField) LenientFieldParser.parse("Content-Language: e\rs-ie2-ze4-1l");
        Assert.assertEquals(strictLanguage.getLanguages(), lenientLanguage.getLanguages());

        MimeVersionField strictVersion = (MimeVersionField) DefaultFieldParser.parse("MIME-Version: 2\r2.0");
        MimeVersionField lenientVersion = (MimeVersionField) LenientFieldParser.parse("MIME-Version: 2\r2.0");
        Assert.assertEquals(22, strictVersion.getMajorVersion());
        Assert.assertEquals(strictVersion.getMajorVersion(), lenientVersion.getMajorVersion());
        Assert.assertEquals(strictVersion.getMinorVersion(), lenientVersion.getMinorVersion());

        String location = "Content-Location: \"\\\n\\\r\n \"";
        ContentLocationField strictLocation = (ContentLocationField) DefaultFieldParser.parse(location);
        ContentLocationField lenientLocation = (ContentLocationField) LenientFieldParser.parse(location);
        Assert.assertEquals(strictLocation.getLocation(), lenientLocation.getLocation());
    }

    @Test
    public void aFoldShouldStillSeparateWords() throws Exception {
        ContentDispositionField lenient = (ContentDispositionField) LenientFieldParser.parse(
            "Content-Disposition: attachment; filename=\"a\r\n b\"; size=1\r\n 2");
        Assert.assertEquals("a b", lenient.getFilename());
    }

    private static void assertSameMimeType(String raw) throws Exception {
        ContentTypeField strict = (ContentTypeField) DefaultFieldParser.parse(raw);
        ContentTypeField lenient = (ContentTypeField) LenientFieldParser.parse(raw);
        Assert.assertEquals(raw, strict.getMimeType(), lenient.getMimeType());
        Assert.assertEquals(raw, strict.getParameters(), lenient.getParameters());
    }

    private static void assertSameDisposition(String raw, String parameter) throws Exception {
        ContentDispositionField strict = (ContentDispositionField) DefaultFieldParser.parse(raw);
        ContentDispositionField lenient = (ContentDispositionField) LenientFieldParser.parse(raw);
        Assert.assertEquals(raw, strict.getDispositionType(), lenient.getDispositionType());
        Assert.assertEquals(raw, strict.getParameter(parameter), lenient.getParameter(parameter));
    }

    @Test
    public void bareCarriageReturnInsideAUtf8SequenceShouldBeReadTheSameWayByBothParsers() throws Exception {
        assertFilename("caf\u00e9.txt", "Content-Disposition: a; filename=\"caf\u00c3\r\u00a9.txt\"");
        assertFilename("caf\u00e9.txt", "Content-Disposition: a; filename=caf\u00c3\r\u00a9.txt");
    }

    @Test
    public void bareLineFeedInsideAUtf8SequenceShouldBeReadTheSameWayByBothParsers() throws Exception {
        assertFilename("caf\u00e9.txt", "Content-Disposition: a; filename=\"caf\u00c3\n\u00a9.txt\"");
    }

    private static void assertFilename(String expected, String latin1) throws Exception {
        RawField field = RawFieldParser.DEFAULT.parseField(
            new ByteArrayBuffer(latin1.getBytes(StandardCharsets.ISO_8859_1), true));

        ContentDispositionField strict = (ContentDispositionField) DefaultFieldParser.getParser().parse(field, DecodeMonitor.SILENT);
        ContentDispositionField lenient = (ContentDispositionField) LenientFieldParser.getParser().parse(field, DecodeMonitor.SILENT);

        Assert.assertEquals(expected, lenient.getFilename());
        Assert.assertEquals(expected, strict.getFilename());
    }

    @Test
    public void bareCarriageReturnAfterABackslashInACommentShouldBeReadTheSameWayByBothParsers() throws Exception {
        for (String lineBreak : new String[] {"\r", "\n", "\r\n"}) {
            String header = "Content-Disposition: attachment (\\" + lineBreak + "(x); filename=a.exe";
            ContentDispositionField strict = (ContentDispositionField) DefaultFieldParser.parse(header);
            ContentDispositionField lenient = (ContentDispositionField) LenientFieldParser.parse(header);

            Assert.assertEquals("a.exe", strict.getFilename());
            Assert.assertEquals(strict.getFilename(), lenient.getFilename());

            String language = "Content-Language: (\\" + lineBreak + "(x) en";
            Assert.assertEquals(
                    ((ContentLanguageField) DefaultFieldParser.parse(language)).getLanguages(),
                    ((ContentLanguageField) LenientFieldParser.parse(language)).getLanguages());
        }
    }
}
