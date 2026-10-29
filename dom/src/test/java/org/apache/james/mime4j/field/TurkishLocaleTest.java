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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.MimeUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TurkishLocaleTest {
    private static final String MESSAGE = "Content-Type: MULTIPART/MIXED; BOUNDARY=foo\r\n\r\n"
        + "--foo\r\n\r\nfirst\r\n--foo--\r\n";

    private Locale defaultLocale;

    @Before
    public void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
    }

    @After
    public void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void mimeTypesShouldBeLowerCasedIndependentlyOfTheLocale() {
        ContentTypeField strict = ContentTypeFieldImpl.PARSER.parse(new RawField("Content-Type", "MULTIPART/MIXED; FILENAME=x"), null);
        ContentTypeField lenient = ContentTypeFieldLenientImpl.PARSER.parse(new RawField("Content-Type", "MULTIPART/MIXED; FILENAME=x"), null);

        Assert.assertEquals("multipart/mixed", strict.getMimeType());
        Assert.assertEquals("x", strict.getParameter("filename"));
        Assert.assertEquals("x", strict.getParameter("FILENAME"));
        Assert.assertEquals("multipart/mixed", lenient.getMimeType());
        Assert.assertEquals("x", lenient.getParameter("FILENAME"));
        Assert.assertTrue(MimeUtil.isMultipart("MULTIPART/MIXED"));
    }

    @Test
    public void contentDispositionParametersShouldBeFoundIndependentlyOfTheLocale() {
        ContentDispositionField field = ContentDispositionFieldImpl.PARSER.parse(new RawField("Content-Disposition", "attachment; FILENAME=x"), null);

        Assert.assertEquals("x", field.getParameter("FILENAME"));
    }

    @Test
    public void tokenStreamShouldRecogniseMultipartsIndependentlyOfTheLocale() throws Exception {
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(new ByteArrayInputStream(MESSAGE.getBytes(StandardCharsets.US_ASCII)));

        EntityState state = stream.getState();
        while (state != EntityState.T_START_MULTIPART && state != EntityState.T_END_OF_STREAM) {
            state = stream.next();
        }
        Assert.assertEquals(EntityState.T_START_MULTIPART, state);
    }

    @Test
    public void messageBuilderShouldRecogniseMultipartsIndependentlyOfTheLocale() throws Exception {
        Message message = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(MESSAGE.getBytes(StandardCharsets.US_ASCII)));

        Assert.assertTrue(message.getBody() instanceof Multipart);
    }
}
