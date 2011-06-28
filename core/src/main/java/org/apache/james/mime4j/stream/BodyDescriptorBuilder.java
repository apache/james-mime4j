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

public interface BodyDescriptorBuilder {

    /**
     * Resets the internal state.
     */
    void reset();

    /**
     * Adds a field to the builder updating its internal state. The field
     * can be transformed as a result of this operation.
     * @param field the MIME field.
     *
     * @return null or an elaborated field representing the same data.
     */
    Field addField(RawField field) throws MimeException;

    /**
     * Builds an instance of {@link BodyDescriptor} based on the internal
     * state.
     *
     * @return body descriptor
     */
    BodyDescriptor build();

    BodyDescriptorBuilder newChild();

}
