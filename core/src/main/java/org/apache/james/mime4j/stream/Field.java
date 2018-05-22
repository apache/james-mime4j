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

import org.apache.james.mime4j.util.ByteSequence;

/**
 * <p>
 * This interface represents an abstract MIME field. A MIME field must have a non <code>null</code>
 * name and a content body (unfolded but unparsed and possibly encoded). Optionally implementing
 * classes may also retain the original (raw) representation in a form of {@link ByteSequence}.
 * </p>
 * <p>
 * Specific implementations of this interface may also use a richer model to represent the field
 * if its body can be parsed into a set of constituent elements.
 * </p>
 */
public interface Field {

    /**
     * Returns the name of the field.
     */
    String getName();

    /**
     * Gets the unparsed and possibly encoded (see RFC 2047) field body string.
     *
     * @return the unparsed field body string.
     */
    String getBody();

    /**
     * Gets original (raw) representation of the field, if available,
     * <code>null</code> otherwise.
     */
    ByteSequence getRaw();

}
