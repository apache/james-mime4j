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

package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;

class StringBody extends TextBody {

    private final String content;
    private final Charset charset;

    StringBody(final String content, final Charset charset) {
        super();
        this.content = content;
        this.charset = charset;
    }

    @Override
    public String getMimeCharset() {
        return this.charset.name();
    }

    @Override
    public Reader getReader() throws IOException {
        return new StringReader(this.content);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new StringInputStream(this.content, this.charset, 2048);
    }

    @Override
    public SingleBody copy() {
        return new StringBody(this.content, this.charset);
    }

}
