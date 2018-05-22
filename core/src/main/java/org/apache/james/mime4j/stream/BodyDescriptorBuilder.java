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

/**
 * <p>
 * Body descriptor builders are intended to construct {@link BodyDescriptor} instances from
 * multiple unstructured {@link RawField}s.
 * </p>
 * <p>
 * Body descriptor builders are stateful and modal as they have to store intermediate results
 * between method invocations and also rely on a particular sequence of method invocations
 * (the mode of operation).
 * </p>
 * <p>
 * Consumers are expected to interact with body descriptor builders in the following way:
 * </p>
 * <ul>
 * <li>Invoke {@link #reset()} method in order to reset builder's internal state and make it
 *   ready to start the process of building a new {@link BodyDescriptor}.</li>
 * <li>Invoke {@link #addField(RawField)} multiple times method in order to collect
 *   necessary details for building a body descriptor. The builder can optionally
 *   transform the unstructured field given an an input into a structured one and return
 *   an instance {@link Field} that also implements a richer interface for a particular type
 *   of fields such as <code>Content-Type</code>. The builder can also return <code>null</code>
 *   if the field is to be ignored</li>
 * <li>Optionally invoke {@link #newChild()} for each embedded body of content. Please note that
 *   the resultant {@link BodyDescriptorBuilder}} child instance can inherit some its parent
 *   properties such as MIME type.</li>
 * <li>Invoke {@link #build()} method in order to generate a {@link BodyDescriptor}} instance
 *   based on the internal state of the builder.</li>
 * </ul>
 */
public interface BodyDescriptorBuilder {

    /**
     * Resets the internal state of the builder making it ready to process new input.
     */
    void reset();

    /**
     * Updates builder's internal state by adding a new field. The builder can optionally
     * transform the unstructured field given an an input into a structured one and return
     * an instance {@link Field} that also implements a richer interface for a particular type
     * of fields such as <code>Content-Type</code>. The builder can also return <code>null</code>
     * if the field is to be ignored.
     */
    Field addField(RawField field) throws MimeException;

    /**
     * Builds an instance of {@link BodyDescriptor} based on the internal state.
     */
    BodyDescriptor build();

    /**
     * Creates an instance of {@link BodyDescriptorBuilder} to be used for processing of an
     * embedded content body. Please the child instance can inherit some of its parent properties
     * such as MIME type.
     */
    BodyDescriptorBuilder newChild();

}
