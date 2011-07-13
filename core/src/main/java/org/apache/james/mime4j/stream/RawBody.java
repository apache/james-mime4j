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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a field's body consisting of a textual value and a number of optional
 * name / value parameters separated with semicolon.
 * <pre>
 * value; param1 = value1; param2 = "value2";
 * </pre>
 */
public final class RawBody {

    private final String value;
    private final List<NameValuePair> params;

    RawBody(final String value, final List<NameValuePair> params) {
        if (value == null) {
            throw new IllegalArgumentException("Field value not be null");
        }
        this.value = value;
        this.params = params != null ? params : new ArrayList<NameValuePair>();
    }

    public String getValue() {
        return this.value;
    }

    public List<NameValuePair> getParams() {
        return new ArrayList<NameValuePair>(this.params);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.value);
        buf.append("; ");
        for (NameValuePair param: this.params) {
            buf.append("; ");
            buf.append(param);
        }
        return buf.toString();
    }

}
