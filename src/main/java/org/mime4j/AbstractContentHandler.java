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

package org.mime4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract <code>ContentHandler</code> with default implementations of all
 * the methods of the <code>ContentHandler</code> interface.
 *
 * 
 * @version $Id: AbstractContentHandler.java,v 1.3 2004/10/02 12:41:10 ntherning Exp $
 */
public abstract class AbstractContentHandler implements ContentHandler {
    
    public void endMultipart() {
    }
    public void startMultipart(BodyDescriptor bd) {
    }
    public void body(BodyDescriptor bd, InputStream is) throws IOException {
    }
    public void endBodyPart() {
    }
    public void endHeader() {
    }
    public void endMessage() {
    }
    public void epilogue(InputStream is) throws IOException {
    }
    public void field(String fieldData) {
    }
    public void preamble(InputStream is) throws IOException {
    }
    public void startBodyPart() {
    }
    public void startHeader() {
    }
    public void startMessage() {
    }
    public void raw(InputStream is) throws IOException {
    }
}
