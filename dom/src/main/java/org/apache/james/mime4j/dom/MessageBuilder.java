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

package org.apache.james.mime4j.dom;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.message.MultipartBuilder;

/**
 * An interface to build instances of {@link Message} and other DOM elements either without
 * any content, by copying content of an existing object or by reading content from
 * an {@link InputStream}.
 */
public interface MessageBuilder {

    Header newHeader();

    Header newHeader(Header source);

    Multipart newMultipart(String subType);

    Multipart newMultipart(Multipart source);

    Multipart newMultipart(MultipartBuilder source);

    Message newMessage();

    Message newMessage(Message source);

    Message newMessage(Message.Builder source);

    Header parseHeader(InputStream source) throws MimeException, IOException;

    Message parseMessage(InputStream source) throws MimeException, IOException;

}
