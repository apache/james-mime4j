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

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.james.mime4j.MimeException;

/**
 * This class tracks parameter mappings and tries to respect <a href="https://www.rfc-editor.org/rfc/rfc2231">rfc2231</a>
 * and conform roughly to Thunderbird's behavior as described on <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=588781>bug 588781</a>.
 * <p>
 * See <a href="https://issues.apache.org/jira/browse/MIME4J-332">MIME4J-332</a>
 * <p>
 *     These are the behaviors:
 * <ul>
 *    <li>If there are multiple keys for standard parameters, use the first and ignore the rest</li>
 *    <li>If there are multiple keys for extended parameters, use the first and ignore the rest</li>
 *    <li>If there's an extended and standard key for a given field, prefer the extended</li>
 *    <li>If there's an extended and a continuation, prefer the extended, recognizing that a continuation field may also include charset information</li>
 * </ul>
 */
public class MimeParameterMapping {

    /**
     * What type of parameter name is it
     */
    enum PARAMETER_TYPE {
        /**
         * standard parameter name, e.g. "filename"
         */
        STANDARD,
        /**
         * extended parameter without continuation numbers, e.g. "filename*", typically a sign that charset is included
         */
        EXTENDED,
        /**
         * continuation parameter, e.g. "filename*0", used to extend field lengths beyond the initial length limit, may also
         * include charset information like {@link PARAMETER_TYPE#EXTENDED}
         */
        CONTINUATION
    }

    /** RFC 2231 section number, optionally followed by the '*' of an encoded section */
    private static final Pattern STAR_AND_NUMBER = Pattern.compile("\\*(\\d{1,9})(\\*?)");

    private final Set<String> parameterNames = new HashSet<>();
    private final Map<String, String> standard = new HashMap<>();
    private final Map<String, String> extended = new HashMap<>();
    private final Map<String, TreeMap<Integer, Section>> continuation = new HashMap<>();

    private final Map<String, String> parameters = new HashMap<>();
    private boolean needToUpdate = true;

    /**
     *
     * @return an Unmodifiable map of the parameters as calculated
     * by the algorithm described in the class javadoc via {@link #get(String)}
     */
    public Map<String, String> getParameters() {
        if (needToUpdate) {
            updateParameters();
            needToUpdate = false;
        }
        return Collections.unmodifiableMap(parameters);
    }

    private void updateParameters() {
        parameters.clear();
        for (String param : parameterNames) {
            parameters.put(param, get(param));
        }
    }

    /**
     * Applies the algorithm described in the class javadoc and returns
     * the decoded value from the best option.
     *
     * @param name field name (must be lowercased)
     * @return field value or <code>null</code> if it doesn't exist
     */
    public String get(String name) {
        if (! parameterNames.contains(name)) {
            return null;
        }

        if (extended.containsKey(name)) {
            try {
                return decodeParameterValue(extended.get(name));
            } catch (DecodeException e) {
                //ignore and try next
            }
        }
        if (continuation.containsKey(name)) {
            try {
                return decodeContinuation(continuation.get(name));
            } catch (DecodeException e) {
                //ignore and try standard
            }
        }
        if (standard.containsKey(name)) {
            // RFC 2231 encoding only applies to extended parameters
            String value = standard.get(name);
            return value != null ? MimeUtil.unscrambleHeaderValue(value) : null;
        }
        //couldn't decode anything. not clear what the "spec" says is the right behavior
        //For now, back off in reverse order

        if (continuation.containsKey(name)) {
            return joinSections(continuation.get(name));
        }

        if (extended.containsKey(name)) {
            return extended.get(name);
        }
        //this should be unreachable
        return null;
    }

    public void addParameter(final String name, String value) {
        ParameterTypePair parameterTypePair = getParameterTypePair(name);
        parameterNames.add(parameterTypePair.fieldName);
        needToUpdate = true;
        switch (parameterTypePair.fieldType) {
            case EXTENDED:
                extended.putIfAbsent(parameterTypePair.fieldName, value);
                break;
            case CONTINUATION:
                if (value == null) {
                    // a valueless section carries nothing: the joining stops at the gap it leaves
                    break;
                }
                continuation.computeIfAbsent(parameterTypePair.fieldName, k -> new TreeMap<>())
                        .putIfAbsent(parameterTypePair.section, new Section(value, parameterTypePair.encoded));
                break;
            case STANDARD:
                standard.putIfAbsent(parameterTypePair.fieldName, value);
        }
    }

    private String decodeParameterValue(String value) throws DecodeException {
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
        //check that the charset is valid
        try {
            Charset.forName(charset);
        } catch (IllegalArgumentException e) {
            return fileName;
        }
        return urlDecode(fileName, charset);
    }

    /**
     * Joins the sections in the order of their numbers, up to the first missing one (RFC 2231 section 3).
     * Encoded sections are percent decoded with the charset of the first section (RFC 2231 section 4.1).
     */
    private String decodeContinuation(TreeMap<Integer, Section> sections) throws DecodeException {
        Section first = sections.get(0);
        if (first == null) {
            return joinSections(sections);
        }
        String value = first.value;
        int charsetEnd = value.indexOf('\'');
        int languageEnd = value.indexOf('\'', charsetEnd + 1);
        if (!first.encoded || charsetEnd < 0 || languageEnd < 0) {
            return MimeUtil.unscrambleHeaderValue(joinSections(sections));
        }
        String charset = value.substring(0, charsetEnd);
        try {
            Charset.forName(charset);
        } catch (IllegalArgumentException e) {
            return joinSections(sections).substring(languageEnd + 1);
        }
        StringBuilder encoded = new StringBuilder(value.substring(languageEnd + 1));
        for (int i = 1; sections.containsKey(i); i++) {
            Section section = sections.get(i);
            if (section.encoded) {
                encoded.append(section.value);
            } else {
                encoded.append(section.value.replace("%", "%25"));
            }
        }
        return urlDecode(encoded.toString(), charset);
    }

    private static String joinSections(TreeMap<Integer, Section> sections) {
        StringBuilder result = new StringBuilder();
        // Leniently start from the lowest section when *0 is missing
        for (int i = sections.firstKey(); sections.containsKey(i); i++) {
            result.append(sections.get(i).value);
        }
        return result.toString();
    }

    private String urlDecode(String value, String charset) throws DecodeException {
        try {
            // RFC 2231 only uses %XX escapes: '+' is not a space
            return java.net.URLDecoder.decode(value.replace("+", "%2B"), charset);
        } catch (Exception e) {
            throw new DecodeException(e);
        }
    }

    private ParameterTypePair getParameterTypePair(String parameterName) {
        int position = parameterName.indexOf('*');
        if (position < 0) {
            return new ParameterTypePair(parameterName.toLowerCase(Locale.ROOT), PARAMETER_TYPE.STANDARD);
        }
        String fieldName = parameterName.substring(0, position);
        fieldName = fieldName.toLowerCase(Locale.ROOT);

        String starAndAfter = parameterName.substring(position);
        if ("*".equals(starAndAfter)) {
            return new ParameterTypePair(fieldName, PARAMETER_TYPE.EXTENDED);
        }
        Matcher m = STAR_AND_NUMBER.matcher(starAndAfter);
        if (m.matches()) {
            return new ParameterTypePair(fieldName, PARAMETER_TYPE.CONTINUATION,
                Integer.parseInt(m.group(1)), !m.group(2).isEmpty());
        }
        //something is weird, treat this as a STAR?
        return new ParameterTypePair(fieldName, PARAMETER_TYPE.EXTENDED);
    }

    private static class ParameterTypePair {
        private final String fieldName;
        private final PARAMETER_TYPE fieldType;
        private final int section;
        private final boolean encoded;

        public ParameterTypePair(String fieldName, PARAMETER_TYPE fieldType) {
            this(fieldName, fieldType, -1, false);
        }

        public ParameterTypePair(String fieldName, PARAMETER_TYPE fieldType, int section, boolean encoded) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.section = section;
            this.encoded = encoded;
        }
    }

    private static class Section {
        private final String value;
        private final boolean encoded;

        Section(String value, boolean encoded) {
            this.value = value;
            this.encoded = encoded;
        }
    }

    private static class DecodeException extends MimeException {

        public DecodeException(String message) {
            super(message);
        }

        public DecodeException(Throwable cause) {
            super(cause);
        }

        public DecodeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
