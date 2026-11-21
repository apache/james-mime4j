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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.stream.NameValuePair;
import org.junit.Test;

public class FieldsDefaultMaxHeaderLenTest {

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static Message writeAndRead(ContentDispositionField disposition) throws Exception {
        Message message = Message.Builder.of()
            .setField(disposition)
            .setField(Fields.subject("sentinel"))
            .setBody("SENTINEL-BODY", StandardCharsets.US_ASCII)
            .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DefaultMessageWriter().writeMessage(message, out);
        return new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(out.toByteArray()));
    }

    @Test
    public void longNonAsciiFilenameShouldBeRejectedRatherThanWrittenUnreadable() {
        String name = repeat('é', 1500);

        assertThatThrownBy(() -> Fields.contentDisposition("attachment", name))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Fields.contentDisposition("attachment",
                Collections.singletonList(new NameValuePair("filename", name))))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void longestAcceptedFilenameShouldBeReadBackWithTheDefaultConfig() throws Exception {
        int longest = 0;
        for (int length = 1000; length < 20000; length++) {
            try {
                Fields.contentDisposition("attachment", repeat('é', length));
                longest = length;
            } catch (IllegalArgumentException e) {
                break;
            }
        }
        assertThat(longest).isGreaterThan(1000);
        String name = repeat('é', longest);

        Message parsed = writeAndRead(Fields.contentDisposition("attachment", name));

        assertThat(((ContentDispositionField) parsed.getHeader().getField("Content-Disposition")).getFilename()).isEqualTo(name);
        assertThat(parsed.getSubject()).isEqualTo("sentinel");
    }

    @Test
    public void longContentTypeParameterShouldBeRejected() {
        String value = repeat('a', 900) + " " + repeat('b', 900);
        StringBuilder many = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            many.append(value).append(' ');
        }
        assertThatThrownBy(() -> Fields.contentType("text/plain", Collections.singletonMap("x", many.toString())))
            .isInstanceOf(IllegalArgumentException.class);
        ContentTypeField fine = Fields.contentType("text/plain", Collections.singletonMap("x", value));
        assertThat(fine.getParameter("x")).isEqualTo(value);
    }
}
