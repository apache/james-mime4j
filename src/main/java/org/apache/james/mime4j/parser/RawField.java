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

/**
 * The basic immutable MIME field.
 */
public class RawField implements Field {
    
    private final String name;
    private final String body;
    private final String raw;
    
    public RawField(String name, String body, String raw) {
        super();
        this.name = name;
        this.body = body;
        this.raw = raw;
    }
    
    public String getName() {
        return this.name;
    }

    public String getBody() {
        return this.body;
    }

    public String getRaw() {
        return this.raw;
    }
    
    public String toString() {
        return this.raw;
    }
    
}
