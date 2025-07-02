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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MimeParameterMapping {

    private final Map<String, String> parameters = new HashMap<>();
    private final Set<String> hasSection = new HashSet<>();

    /** Charset, taken from the first item added to {@link #parameters}. */
    private String charset;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String get(String name) {
        return parameters.get(name);
    }

    public void addParameter(final String name, String value) {
        String key = name;
        int sectionDelimiter = name.indexOf("*");
        if (sectionDelimiter > -1) {
            key = key.substring(0, sectionDelimiter);
        }
        key = key.toLowerCase(Locale.ROOT);
        //switch on whether there's a section in the key
        if (sectionDelimiter < 0) {
            handleNoSection(key, value);
        } else {
            handleSection(key, value);
        }
    }

    private void handleSection(String key, String value) {
        if (! hasSection.contains(key) && parameters.containsKey(key)) {
            parameters.remove(key);
        }
        if (parameters.containsKey(key)) {
            parameters.put(key, decodeParameterValue(parameters.get(key) + value));
        } else {
            parameters.put(key, decodeParameterValue(value));
        }
        hasSection.add(key);
    }

    private void handleNoSection(String key, String value) {
        if (parameters.containsKey(key)) {
            //if there's already a value here, and this is a no section key
            //ignore this value
            return;
        }
        parameters.put(key, decodeParameterValue(value));
    }

    private String decodeParameterValue(String value) {
        if (value == null) {
            return null;
        }
        int charsetEnd = value.indexOf("'");
        int languageEnd = value.indexOf("'", charsetEnd + 1);
        if (charsetEnd < 0 || languageEnd < 0) {
            if (charset != null) {
                return urlDecode(value);
            } else {
                return MimeUtil.unscrambleHeaderValue(value);
            }
        }
        charset = value.substring(0, charsetEnd);
        String fileName = value.substring(languageEnd + 1);
        return urlDecode(fileName);
    }

    private String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, charset);
        }
        catch (Exception ignore) {
            return value;
        }
    }

    private String removeSectionFromName(String parameterName) {
        int position = parameterName.indexOf('*');
        return parameterName.substring(0, position < 0 ? parameterName.length() : position);
    }
}
