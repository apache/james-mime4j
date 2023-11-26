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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ContentTypeFieldMimeParameterTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<FieldParser<ContentTypeField>> data() {
        return Arrays.asList(ContentTypeFieldImpl.PARSER, ContentTypeFieldLenientImpl.PARSER);
    }

    private final FieldParser<ContentTypeField> parser;

    public ContentTypeFieldMimeParameterTest(FieldParser<ContentTypeField> parser) {
        this.parser = parser;
    }

    @Test
    public void rfc2045_example1() throws MimeException {
        ContentTypeField field = parse("Content-Type: text/plain; charset=us-ascii (Plain text)");

        assertThat(field.getMimeType()).isEqualTo("text/plain");
        assertThat(field.getCharset()).isEqualTo("us-ascii");
    }

    @Test
    public void rfc2045_example2() throws MimeException {
        ContentTypeField field = parse("Content-Type: text/plain; charset=\"us-ascii\"");

        assertThat(field.getMimeType()).isEqualTo("text/plain");
        assertThat(field.getCharset()).isEqualTo("us-ascii");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void rfc2231_example1() throws MimeException {
        ContentTypeField field = parse("Content-Type: message/external-body; access-type=URL;\r\n" +
            " URL*0=\"ftp://\";\r\n" +
            " URL*1=\"cs.utk.edu/pub/moore/bulk-mailer/bulk-mailer.tar\"");

        assertThat(field.getMimeType()).isEqualTo("message/external-body");
        assertThat(field.getParameter("access-type")).isEqualTo("URL");
        assertThat(field.getParameter("url")).isEqualTo("ftp://cs.utk.edu/pub/moore/bulk-mailer/bulk-mailer.tar");
    }

    @Test
    public void rfc2231_example2() throws MimeException {
        ContentTypeField field = parse("Content-Type: message/external-body; access-type=URL;\r\n" +
            " URL=\"ftp://cs.utk.edu/pub/moore/bulk-mailer/bulk-mailer.tar\"");

        assertThat(field.getMimeType()).isEqualTo("message/external-body");
        assertThat(field.getParameter("access-type")).isEqualTo("URL");
        assertThat(field.getParameter("url")).isEqualTo("ftp://cs.utk.edu/pub/moore/bulk-mailer/bulk-mailer.tar");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void rfc2231_example3() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name*=us-ascii'en-us'This%20is%20%2A%2A%2Afun%2A%2A%2A");

        assertThat(field.getMimeType()).isEqualTo("application/x-stuff");
        assertThat(field.getParameter("name")).isEqualTo("This is ***fun***");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void rfc2231_example4() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name*0*=us-ascii'en'This%20is%20even%20more%20;\r\n" +
            " name*1*=%2A%2A%2Afun%2A%2A%2A%20;\r\n" +
            " name*2=\"isn't it!\"");

        assertThat(field.getMimeType()).isEqualTo("application/x-stuff");
        assertThat(field.getParameter("name")).isEqualTo("This is even more ***fun*** isn't it!");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void multipleSectionsOutOfOrder() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name*2=\"[three]\";\r\n" +
            " name*1=\"[two]\";\r\n" +
            " name*0=\"[one]\"");

        assertThat(field.getParameter("name")).isEqualTo("[one][two][three]");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void multipleSectionsDifferentlyCased() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name*0=\"[one]\";\r\n" +
            " NAME*1=\"[two]\";\r\n" +
            " nAmE*2=\"[three]\"");

        assertThat(field.getParameter("name")).isEqualTo("[one][two][three]");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void multipleSectionsSwitchingBetweenExtendedAndRegularValue() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name*0*=utf-8'en'%5Bone%5D;\r\n" +
            " name*1*=%5btwo%5d;\r\n" +
            " name*2=\"[three]\";\r\n" +
            " name*3*=%5Bfour%5D;\r\n" +
            " name*4=\"[five]\";\r\n" +
            " name*5=six");

        assertThat(field.getParameter("name")).isEqualTo("[one][two][three][four][five]six");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void rfc2045AndRfc2231StyleParametersShouldUseRfc2231() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name=\"filename.ext\";\r\n" +
            " name*=utf-8''filen%C3%A4me.ext");

        assertThat(field.getParameter("name")).isEqualTo("filenäme.ext");
    }

    @Ignore("Currently the right-most parameter wins")
    @Test
    public void duplicateParameterNames() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name=one;\r\n" +
            " extra=something;\r\n" +
            " name=two");

        assertThat(field.getParameter("name")).isNull();
        assertThat(field.getParameter("extra")).isEqualTo("something");
    }

    @Ignore("Currently the right-most parameter wins")
    @Test
    public void duplicateParameterNamesDifferingInCase() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name=one;\r\n" +
            " extra=something;\r\n" +
            " NAME=two");

        assertThat(field.getParameter("name")).isNull();
        assertThat(field.getParameter("extra")).isEqualTo("something");
    }

    @Test
    public void commentsEverywhere() throws MimeException {
        ContentTypeField field = parse("Content-Type: (comment)application(comment)/(comment)x-stuff" +
            "(comment);(comment)\r\n" +
            " (comment)name(comment)=(comment)one(comment);(comment)\r\n" +
            "  (comment) extra (comment) = (comment) something (comment)");

        assertThat(field.getParameter("name")).isEqualTo("one");
        assertThat(field.getParameter("extra")).isEqualTo("something");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void iso8859_1_charset() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*=iso-8859-1''filen%E4me.ext");

        assertThat(field.getParameter("name")).isEqualTo("filenäme.ext");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void missingCharset() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*=''filen%AAme.ext");

        assertThat(field.getParameter("name")).isEqualTo("filen%AAme.ext");
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void unknownCharset() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*=foobar''filen%AAme.ext");

        assertThat(field.getParameter("name")).isEqualTo("filen%AAme.ext");
    }

    @Test
    public void sectionIndexMissing() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name**=utf-8''filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name**", "utf-8''filename"));
    }

    @Test
    public void sectionIndexNotANumber() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*x*=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*x*", "filename"));
    }

    @Test
    public void sectionIndexPrefixedWithPlus() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*+0=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*+0", "filename"));
    }

    @Test
    public void sectionIndexPrefixedWithMinus() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*-0=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*-0", "filename"));
    }

    @Test
    public void sectionIndexWithTwoZeros() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*00=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*00", "filename"));
    }

    @Ignore("Missing RFC2231 support")
    @Test
    public void sectionIndexWithLeadingZero() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff;\r\n" +
            " name*0=one;\r\n" +
            " name*01=two");

        assertThat(field.getParameters())
            .containsExactly(new SimpleEntry<>("name", "one"), new SimpleEntry<>("name*01", "two"));
    }

    @Test
    public void sectionIndexWithHugeNumber() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*10000000000000000000=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*10000000000000000000", "filename"));
    }

    @Test
    public void extendedParameterNameWithAdditionalAsterisk() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*0**=utf-8''filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*0**", "utf-8''filename"));
    }

    @Test
    public void extendedParameterNameWithAdditionalText() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*0*x=utf-8''filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*0*x", "utf-8''filename"));
    }

    @Test
    public void extendedParameterValueWithQuotedString() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*0*=\"utf-8''filename\"");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*0*", "utf-8''filename"));
    }

    @Test
    public void extendedInitialParameterValueMissingSingleQuotes() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*0*=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*0*", "filename"));
    }

    @Test
    public void extendedInitialParameterValueMissingSecondSingleQuote() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*0*='");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*0*", "'"));
    }

    @Test
    public void extendedParameterValueWithTrailingPercentSign() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*=utf-8''file%");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*", "utf-8''file%"));
    }

    @Test
    public void extendedParameterValueWithInvalidPercentEncoding() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*=UTF-8''f%oo.html");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*", "UTF-8''f%oo.html"));
    }

    @Test
    public void sectionZeroMissing() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name*1=filename");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name*1", "filename"));
    }

    @Test
    public void semicolonInParameterValue() throws MimeException {
        ContentTypeField field = parse("Content-Type: application/x-stuff; name=\"Here's a semicolon;.txt\"");

        assertThat(field.getParameters()).containsExactly(new SimpleEntry<>("name", "Here's a semicolon;.txt"));
    }

    private ContentTypeField parse(final String input) throws MimeException {
        ByteSequence raw = ContentUtil.encode(input);
        RawField rawField = RawFieldParser.DEFAULT.parseField(raw);
        return parser.parse(rawField, null);
    }
}
