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
        for (CharSequence charSequence : searchContents) {
            if (charSequence != null) {
                final CharBuffer buffer = createBuffer(charSequence);
                if (ignoringMime) {
                    if (! isFoundIn(new InputStreamReader(input), buffer)) {
                        return false;
                    }
                } else {
                    if (!matchBufferInMailBeingMimeAware(input, buffer)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean matchBufferInMailBeingMimeAware(final InputStream input, final CharBuffer buffer) throws IOException, MimeException {
        try {
            MimeConfig config = MimeConfig.custom().setMaxLineLen(-1).setMaxHeaderLen(-1).build();

            MimeTokenStream parser = new MimeTokenStream(config);
            parser.parse(input);
            while (parser.next() != EntityState.T_END_OF_STREAM) {
                final EntityState state = parser.getState();
                switch (state) {
                    case T_BODY:
                        if (contentTypes.isEmpty() || contentTypes.contains(parser.getBodyDescriptor().getMimeType())) {
                            if (checkBody(buffer, parser)) {
                                return true;
                            }
                        }
                    case T_PREAMBLE:
                    case T_EPILOGUE:
                        if (includeHeaders) {
                            if (checkBody(buffer, parser)) {
                                return true;
                            }
                        }
                        break;
                    case T_FIELD:
                        if (includeHeaders) {
                            if (checkHeader(buffer, parser)) {
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

    private boolean checkHeader(final CharBuffer buffer, MimeTokenStream parser) throws IOException {
        final String value = parser.getField().getBody();
        final StringReader reader = new StringReader(value);
        return isFoundIn(reader, buffer);
    }

    private boolean checkBody(final CharBuffer buffer, MimeTokenStream parser) throws IOException {
        final Reader reader = parser.getReader();
        return isFoundIn(reader, buffer);
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
        int read;
        while ((read = reader.read()) != -1) {
            if (matches(buffer, computeNextChar(isCaseInsensitive, (char) read))) {
                return true;
            }
        }
        return false;
    }

    private char computeNextChar(boolean isCaseInsensitive, char read) {
        if (isCaseInsensitive) {
            return Character.toUpperCase(read);
        } else {
            return read;
        }
    }

    private boolean matches(final CharBuffer buffer, final char next) {
        if (buffer.hasRemaining()) {
            final boolean partialMatch = (buffer.position() > 0);
            final char matching = buffer.get();
            if (next != matching) {
                buffer.rewind();
                if (partialMatch) {
                    return matches(buffer, next);
                }
            }
        } else {
            return true;
        }
        return false;
    }

}
