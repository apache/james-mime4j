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
import java.io.OutputStream;

import org.apache.james.mime4j.Field;

/**
 * An interface to write out content of {@link Message} and other DOM elements to
 * an {@link OutputStream}.
 */
public interface MessageWriter {

    void writeMessage(Message message, OutputStream out) throws IOException;

    void writeBody(Body body, OutputStream out) throws IOException;

    void writeEntity(Entity entity, OutputStream out) throws IOException;

    void writeMultipart(Multipart multipart, OutputStream out) throws IOException;

    void writeField(Field field, OutputStream out) throws IOException;

    void writeHeader(Header header, OutputStream out) throws IOException;

}
