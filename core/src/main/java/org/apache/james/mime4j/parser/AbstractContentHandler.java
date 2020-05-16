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

package org.apache.james.mime4j.parser;

import org.apache.james.mime4j.BodyDescriptor;
import org.apache.james.mime4j.ContentHandler;
import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.MimeException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract base class for custom {@link ContentHandler} implementations. Methods of this class
 * take no action and are expected to be selectively overridden by super-classes.
 */
public abstract class AbstractContentHandler implements ContentHandler {

    public void endMultipart() throws MimeException {
    }

    public void startMultipart(BodyDescriptor bd) throws MimeException {
    }

    public void body(BodyDescriptor bd, InputStream is)
            throws MimeException, IOException {
    }

    public void endBodyPart() throws MimeException {
    }

    public void endHeader() throws MimeException {
    }

    public void endMessage() throws MimeException {
    }

    public void epilogue(InputStream is) throws MimeException, IOException {
    }

    public void field(Field field) throws MimeException {
    }

    public void preamble(InputStream is) throws MimeException, IOException {
    }

    public void startBodyPart() throws MimeException {
    }

    public void startHeader() throws MimeException {
    }

    public void startMessage() throws MimeException {
    }

    public void raw(InputStream is) throws MimeException, IOException {
    }

}
