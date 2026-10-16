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

package org.apache.james.mime4j.io;

import org.apache.james.mime4j.MimeException;

/**
 * Signals a parsing error due to MIME entities (multiparts and embedded
 * messages) being nested more deeply than the maximum limit.
 *
 * @see org.apache.james.mime4j.stream.MimeConfig.Builder#setMaxNestingDepth(int)
 */
public class MaxNestingDepthLimitException extends MimeException {

    private static final long serialVersionUID = 6011628067570972836L;

    public MaxNestingDepthLimitException(final String message) {
        super(message);
    }

}
