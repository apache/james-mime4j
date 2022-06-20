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

package org.apache.james.mime4j.stream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.RecycledByteArrayBuffer;

/**
 * <p>
 * Field builders are intended to construct {@link RawField} instances from multiple lines
 * contained in {@link ByteArrayBuffer}s.
 * </p>
 * <p>
 * Field builders are stateful and modal as they have to store intermediate results between
 * method invocations and also rely on a particular sequence of method invocations
 * (the mode of operation).
 * </p>
 * <p>
 * Consumers are expected to interact with field builder in the following way:
 * </p>
 * <ul>
 * <li>Invoke {@link #reset()} method in order to reset builder's internal state and make it
 *   ready to start the process of building a new {@link RawField}.</li>
 * <li>Invoke {@link #append(ByteArrayBuffer)} method one or multiple times in order to build
 *   an internal representation of a MIME field from individual lines of text.</li>
 * <li>Optionally {@link #getRaw()} method can be invoked in order to get combined content
 *   of all lines processed so far. Please note builder implementations can return
 *   <code>null</code> if they do not retain original raw content.</li>
 * <li>Invoke {@link #build()} method in order to generate a {@link RawField} instance
 *   based on the internal state of the builder.</li>
 * </ul>
 */
public interface FieldBuilder {

    /**
     * Resets the internal state of the builder making it ready to process new input.
     */
    void reset();

    /**
     * Updates builder's internal state by adding a new line of text.
     */
    void append(ByteArrayBuffer line) throws MimeException;

    /**
     * Builds an instance of {@link RawField} based on the internal state.
     */
    RawField build() throws MimeException;

    /**
     * Returns combined content of all lines processed so far or <code>null</code>
     * if the builder does not retain original raw content.
     */
    RecycledByteArrayBuffer getRaw();

    void release();

}
