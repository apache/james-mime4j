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

import java.util.Map;

import org.apache.james.mime4j.util.MimeUtil;

public class Fields {
    private Fields() {
    }

    public static ContentTypeField contentType(String contentType) {
        String body = MimeUtil.fold(contentType,
                Field.CONTENT_TYPE.length() + 2);

        return (ContentTypeField) Field.parse(Field.CONTENT_TYPE, body);
    }

    public static ContentTypeField contentType(String mimeType,
            Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return (ContentTypeField) Field.parse(Field.CONTENT_TYPE, mimeType);
        } else {
            StringBuilder sb = new StringBuilder(mimeType);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append("; ");
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(quote(entry.getValue()));
            }
            String contentType = sb.toString();
            return contentType(contentType);
        }
    }

    public static ContentTransferEncodingField contentTransferEncoding(
            String contentTransferEncoding) {
        return (ContentTransferEncodingField) Field.parse(
                Field.CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
    }

    private static String quote(String value) {
        for (int idx = 0; idx < value.length(); idx++) {
            if (isSpecial(value.charAt(idx))) {
                value = value.replaceAll("[\\\"]", "\\\\$0");
                return "\"" + value + "\"";
            }
        }

        return value;
    }

    private static boolean isSpecial(char ch) {
        final String tspecials = "()<>@,;:\\\"/[]?="; // rfc 2045
        return ch < 32 || ch >= 127 || tspecials.indexOf(ch) != -1;
    }
}
