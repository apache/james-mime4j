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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Text body backed by a <code>String</code>.
 */
class StringTextBody extends SingleBody implements TextBody {

    private final String text;
    private final Charset charset;

    public StringTextBody(final String text, Charset charset) {
        this.text = text;
        this.charset = charset;
    }

    /**
     * @see org.apache.james.mime4j.message.TextBody#getReader()
     */
    public Reader getReader() throws IOException {
        return new StringReader(text);
    }

    /**
     * @see org.apache.james.mime4j.message.Body#writeTo(java.io.OutputStream,
     *      Mode)
     */
    public void writeTo(OutputStream out, Mode mode) throws IOException {
        if (out == null)
            throw new IllegalArgumentException();

        Reader reader = new StringReader(text);
        Writer writer = new OutputStreamWriter(out, charset);

        char buffer[] = new char[1024];
        while (true) {
            int nChars = reader.read(buffer);
            if (nChars == -1)
                break;

            writer.write(buffer, 0, nChars);
        }

        reader.close();
        writer.flush();
    }

    @Override
    public StringTextBody copy() {
        return new StringTextBody(text, charset);
    }

}
