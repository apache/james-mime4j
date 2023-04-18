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

package org.apache.james.mime4j.util;

import java.util.HashMap;
import java.util.Map;

public class MimeParameterMapping {

    private final Map<String, String> parameters = new HashMap<>();

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String get(String name) {
        return parameters.get(name);
    }

    public void addParameter(String name, String value) {
        String key = removeSectionFromName(name).toLowerCase();
        if (parameters.containsKey(key)) {
            parameters.put(key, decodeParameterValue(parameters.get(key) + value));
        } else {
            parameters.put(key, decodeParameterValue(value));
        }
    }

    private String decodeParameterValue(String value) {
        if (value == null) {
            return null;
        }
        int charsetEnd = value.indexOf("'");
        int languageEnd = value.indexOf("'", charsetEnd + 1);
        if (charsetEnd < 0 || languageEnd < 0) {
            return MimeUtil.unscrambleHeaderValue(value);
        }
        String charset = value.substring(0, charsetEnd);
        String fileName = value.substring(languageEnd + 1);
        try {
            return java.net.URLDecoder.decode(fileName, charset);
        }
        catch (Exception ignore) {
            return fileName;
        }
    }

    private String removeSectionFromName(String parameterName) {
        int position = parameterName.indexOf('*');
        return parameterName.substring(0, position < 0 ? parameterName.length() : position);
    }
}
