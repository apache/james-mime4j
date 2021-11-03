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

package org.apache.james.mime4j.internal;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.james.mime4j.util.MimeUtil;

public class ParameterHelper {
    private static final Pattern URL_ENCODED_PATTERN = Pattern.compile("^([^']*)'([^']*)'(.*)");

    private final Map<String, String> parameters = new HashMap<>();

    public Map<String,String> getParameters() {
        Map<String,String> result = new HashMap<>();
        for (Map.Entry<String, String > entry : parameters.entrySet()) {
            result.put(entry.getKey(), decodeParameterValue(entry.getValue()) );
        }
        return result;
    }

    public void addParameterValue(String name, String value) {
        String key = removeSectionFromName(name).toLowerCase();
        if (parameters.containsKey(key)) {
            parameters.put(key, parameters.get(key) + value);
        } else {
            parameters.put(key, value);
        }
    }

    private static String decodeParameterValue(String value) {
        Matcher matcher = URL_ENCODED_PATTERN.matcher(value);
        if (matcher.matches() && value.contains("%")) {
            try {
                return java.net.URLDecoder.decode(matcher.group(3),
                        matcher.group(1) != null ? matcher.group(1) : "UTF-8");
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        return MimeUtil.unscrambleHeaderValue(value);
    }

    private static String removeSectionFromName(String parameterName) {
        return parameterName.split("\\*")[0];
    }
}
