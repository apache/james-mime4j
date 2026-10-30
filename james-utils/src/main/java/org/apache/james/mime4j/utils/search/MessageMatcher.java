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
package org.apache.james.mime4j.utils.search;

import com.google.common.collect.ImmutableList;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Searches an email for content.
 */
public class MessageMatcher {

    public static class MessageMatcherBuilder {

        private List<CharSequence> searchContents;
        private List<String> contentTypes;
        private boolean isCaseInsensitive;
        private boolean includeHeaders;
        private boolean ignoringMime;
        private Logger logger;

        public MessageMatcherBuilder() {
            this.searchContents = ImmutableList.of();
            this.contentTypes = ImmutableList.of();
            this.isCaseInsensitive = false;
            this.includeHeaders = false;
            this.ignoringMime = false;
            this.logger = LoggerFactory.getLogger(MessageMatcher.class);
        }

        public MessageMatcherBuilder searchContents(List<CharSequence> searchContents) {
            this.searchContents = searchContents;
            return this;
        }

        public MessageMatcherBuilder contentTypes(List<String> contentTypes) {
            this.contentTypes = contentTypes;
            return this;
        }

        public MessageMatcherBuilder caseInsensitive(boolean isCaseInsensitive) {
            this.isCaseInsensitive = isCaseInsensitive;
            return this;
        }

        public MessageMatcherBuilder includeHeaders(boolean includeHeaders) {
            this.includeHeaders = includeHeaders;
            return this;
        }

        public MessageMatcherBuilder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public MessageMatcherBuilder ignoringMime(boolean ignoringMime) {
            this.ignoringMime = ignoringMime;
            return this;
        }

        public MessageMatcher build() {
            return new MessageMatcher(searchContents, isCaseInsensitive, includeHeaders, ignoringMime, contentTypes, logger);
        }

    }

    public static MessageMatcherBuilder builder() {
        return new MessageMatcherBuilder();
    }

    private final Logger logger;
    private final List<CharSequence> searchContents;
    private final List<String> contentTypes;
    private final boolean isCaseInsensitive;
    private final boolean includeHeaders;
    private final boolean ignoringMime;

    private MessageMatcher(List<CharSequence> searchContents, boolean isCaseInsensitive, boolean includeHeaders,
                           boolean ignoringMime, List<String> contentTypes, Logger logger) {
        this.contentTypes = ImmutableList.copyOf(contentTypes);
        this.searchContents = ImmutableList.copyOf(searchContents);
        this.isCaseInsensitive = isCaseInsensitive;
        this.includeHeaders = includeHeaders;
        this.ignoringMime = ignoringMime;
        this.logger = logger;
    }

    /**
     * Is searchContents found in the given input?
     *
     * @param input
     *            <code>InputStream</code> containing an email
     * @return true if the content exists and the stream contains the content,
     *         false otherwise. It takes the mime structure into account.
     * @throws IOException
     * @throws MimeException
     */
    public boolean messageMatches(final InputStream input) throws IOException, MimeException {
        // All the searched texts are looked for in a single pass: the input can only be read once
        final List<NeedleMatcher> matchers = new ArrayList<>();
        for (CharSequence charSequence : searchContents) {
            if (charSequence != null) {
                matchers.add(new NeedleMatcher(createBuffer(charSequence)));
            }
        }
        if (allFound(matchers)) {
            return true;
        }
        if (ignoringMime) {
            return searchIn(new InputStreamReader(input), matchers);
        }
        return matchBufferInMailBeingMimeAware(input, matchers);
    }

    private boolean matchBufferInMailBeingMimeAware(final InputStream input, final List<NeedleMatcher> matchers) throws IOException, MimeException {
        try {
            MimeConfig config = MimeConfig.custom().setMaxLineLen(-1).setMaxHeaderLen(-1).build();

            MimeTokenStream parser = new MimeTokenStream(config);
            parser.parse(input);
            while (parser.next() != EntityState.T_END_OF_STREAM) {
                final EntityState state = parser.getState();
                switch (state) {
                    case T_PREAMBLE:
                    case T_EPILOGUE:
                    case T_BODY:
                        if (contentTypes.isEmpty() || contentTypes.contains(parser.getBodyDescriptor().getMimeType())) {
                            if (checkBody(matchers, parser)) {
                                return true;
                            }
                        }
                        break;
                    case T_FIELD:
                        if (includeHeaders) {
                            if (checkHeader(matchers, parser)) {
                                return true;
                            }
                        }
                        break;
                case T_END_BODYPART:
                case T_END_HEADER:
                case T_END_MESSAGE:
                case T_END_MULTIPART:
                case T_END_OF_STREAM:
                case T_RAW_ENTITY:
                case T_START_BODYPART:
                case T_START_HEADER:
                case T_START_MESSAGE:
                case T_START_MULTIPART:
                    break;
                }
            }
        } catch (IllegalCharsetNameException e) {
            handle(e);
        } catch (UnsupportedCharsetException e) {
            handle(e);
        } catch (IllegalStateException e) {
            handle(e);
        }
        return false;
    }

    private boolean checkHeader(final List<NeedleMatcher> matchers, MimeTokenStream parser) throws IOException {
        final String value = parser.getField().getBody();
        final StringReader reader = new StringReader(value);
        return searchIn(reader, matchers);
    }

    private boolean checkBody(final List<NeedleMatcher> matchers, MimeTokenStream parser) throws IOException {
        try (Reader reader = parser.getReader()) {
            return searchIn(reader, matchers);
        }
    }

    private CharBuffer createBuffer(final CharSequence searchContent) {
        final CharBuffer buffer;
        if (isCaseInsensitive) {
            final int length = searchContent.length();
            buffer = CharBuffer.allocate(length);
            for (int i = 0; i < length; i++) {
                final char next = searchContent.charAt(i);
                final char upperCase = Character.toUpperCase(next);
                buffer.put(upperCase);
            }
            buffer.flip();
        } else {
            buffer = CharBuffer.wrap(searchContent);
        }
        return buffer;
    }

    protected void handle(Exception e) throws IOException, MimeException {
        logger.warn("Cannot read MIME body.");
        logger.debug("Failed to read body.", e);
    }

    public boolean isFoundIn(final Reader reader, final CharBuffer buffer) throws IOException {
        return searchIn(reader, Collections.singletonList(new NeedleMatcher(buffer)));
    }

    /**
     * Feeds the reader to the matchers not found yet. A match never spans two readers.
     *
     * @return true once every matcher found its text
     */
    private boolean searchIn(final Reader reader, final List<NeedleMatcher> matchers) throws IOException {
        for (NeedleMatcher matcher : matchers) {
            matcher.reset();
        }
        int read;
        while ((read = reader.read()) != -1) {
            final char next = computeNextChar(isCaseInsensitive, (char) read);
            boolean allFound = true;
            for (NeedleMatcher matcher : matchers) {
                if (!matcher.found) {
                    matcher.accept(next);
                    allFound &= matcher.found;
                }
            }
            if (allFound) {
                return true;
            }
        }
        return false;
    }

    private static boolean allFound(final List<NeedleMatcher> matchers) {
        for (NeedleMatcher matcher : matchers) {
            if (!matcher.found) {
                return false;
            }
        }
        return true;
    }

    private char computeNextChar(boolean isCaseInsensitive, char read) {
        if (isCaseInsensitive) {
            return Character.toUpperCase(read);
        } else {
            return read;
        }
    }

    /**
     * Knuth-Morris-Pratt matcher of one searched text: matches overlapping prefixes and texts
     * ending the input.
     */
    private static final class NeedleMatcher {
        private final CharBuffer needle;
        private final int length;
        private final int[] fallback;
        private int matched;
        private boolean found;

        NeedleMatcher(final CharBuffer needle) {
            this.needle = needle;
            this.length = needle.limit();
            this.fallback = fallbackTable(needle, length);
            this.found = length == 0;
        }

        void reset() {
            matched = 0;
        }

        void accept(final char next) {
            while (matched > 0 && needle.get(matched) != next) {
                matched = fallback[matched - 1];
            }
            if (needle.get(matched) == next) {
                matched++;
                if (matched == length) {
                    found = true;
                }
            }
        }

        /**
         * For each prefix of the searched text, the length of its longest proper prefix that is also a suffix.
         */
        private static int[] fallbackTable(final CharBuffer buffer, final int length) {
            final int[] fallback = new int[length];
            int k = 0;
            for (int i = 1; i < length; i++) {
                while (k > 0 && buffer.get(i) != buffer.get(k)) {
                    k = fallback[k - 1];
                }
                if (buffer.get(i) == buffer.get(k)) {
                    k++;
                }
                fallback[i] = k;
            }
            return fallback;
        }
    }

}
