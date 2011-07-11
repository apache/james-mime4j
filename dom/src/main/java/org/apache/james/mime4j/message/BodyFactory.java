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

import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.TextBody;

/**
 * Factory for creating message bodies.
 */
public interface BodyFactory {

    /**
     * Creates a {@link BinaryBody} that holds the content of the given input
     * stream.
     *
     * @param is
     *            input stream to create a message body from.
     * @return a binary body.
     * @throws IOException
     *             if an I/O error occurs.
     */
    BinaryBody binaryBody(InputStream is) throws IOException;

    /**
     * Creates a {@link TextBody} that holds the content of the given input
     * stream.
     * <p>
     * The charset corresponding to the given MIME charset name is used to
     * decode the byte content of the input stream into a character stream when
     * calling {@link TextBody#getReader() getReader()} on the returned object.
     * If the MIME charset has no corresponding Java charset or the Java charset
     * cannot be used for decoding then &quot;us-ascii&quot; is used instead.
     *
     * @param is
     *            input stream to create a message body from.
     * @param mimeCharset
     *            name of a MIME charset.
     * @return a text body.
     * @throws IOException
     *             if an I/O error occurs.
     */
    TextBody textBody(InputStream is, String mimeCharset) throws IOException;

}
