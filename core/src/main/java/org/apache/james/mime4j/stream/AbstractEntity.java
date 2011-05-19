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

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.io.LineReaderInputStream;
import org.apache.james.mime4j.io.MaxHeaderLimitException;
import org.apache.james.mime4j.io.MaxLineLimitException;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.CharsetUtil;

/**
 * Abstract MIME entity.
 */
abstract class AbstractEntity implements EntityStateMachine {

    protected final EntityState startState;
    protected final EntityState endState;
    protected final MimeEntityConfig config;
    protected final DecodeMonitor monitor;
    protected final FieldBuilder fieldBuilder;
    protected final FieldParser<?> fieldParser;
    protected final MutableBodyDescriptor body;

    private final ByteArrayBuffer linebuf;

    protected EntityState state;
    private int lineCount;
    private boolean endOfHeader;
    private int headerCount;
    private Field field;

    AbstractEntity(
            MimeEntityConfig config,
            EntityState startState,
            EntityState endState,
            DecodeMonitor monitor,
            FieldBuilder fieldBuilder,
            FieldParser<?> fieldParser,
            MutableBodyDescriptor body) {
        this.config = config;
        this.state = startState;
        this.startState = startState;
        this.endState = endState;
        this.monitor = monitor;
        this.fieldBuilder = fieldBuilder;
        this.fieldParser = fieldParser;
        this.body = body;
        this.linebuf = new ByteArrayBuffer(64);
        this.lineCount = 0;
        this.endOfHeader = false;
        this.headerCount = 0;
    }

    public EntityState getState() {
        return state;
    }

    /**
     * Returns the current line number or <code>-1</code> if line number
     * information is not available.
     */
    protected abstract int getLineNumber();

    protected abstract LineReaderInputStream getDataStream();

    private void readRawField() throws IOException, MimeException {
        if (endOfHeader)
            throw new IllegalStateException();
        LineReaderInputStream instream = getDataStream();
        try {
            for (;;) {
                // If there's still data stuck in the line buffer
                // copy it to the field buffer
                int len = linebuf.length();
                if (len > 0) {
                    fieldBuilder.append(linebuf);
                }
                linebuf.clear();
                if (instream.readLine(linebuf) == -1) {
                    monitor(Event.HEADERS_PREMATURE_END);
                    endOfHeader = true;
                    break;
                }
                len = linebuf.length();
                if (len > 0 && linebuf.byteAt(len - 1) == '\n') {
                    len--;
                }
                if (len > 0 && linebuf.byteAt(len - 1) == '\r') {
                    len--;
                }
                if (len == 0) {
                    // empty line detected
                    endOfHeader = true;
                    break;
                }
                lineCount++;
                if (lineCount > 1) {
                    int ch = linebuf.byteAt(0);
                    if (ch != CharsetUtil.SP && ch != CharsetUtil.HT) {
                        // new header detected
                        break;
                    }
                }
            }
        } catch (MaxLineLimitException e) {
            throw new MimeException(e);
        }
    }

    protected boolean nextField() throws MimeException, IOException {
        int maxHeaderCount = config.getMaxHeaderCount();
        // the loop is here to transparently skip invalid headers
        for (;;) {
            if (endOfHeader) {
                return false;
            }
            if (maxHeaderCount > 0 && headerCount >= maxHeaderCount) {
                throw new MaxHeaderLimitException("Maximum header limit exceeded");
            }
            headerCount++;
            fieldBuilder.reset();
            readRawField();
            try {
                RawField rawfield = fieldBuilder.build();
                if (rawfield == null) {
                    continue;
                }
                if (rawfield.getDelimiterIdx() != rawfield.getName().length()) {
                    monitor(Event.OBSOLETE_HEADER);
                }
                if (fieldParser != null) {
                    field = fieldParser.parse(
                            rawfield.getName(), rawfield.getBody(), rawfield.getRaw(), monitor);
                } else {
                    field = rawfield;
                }
                body.addField(field);
                return true;
            } catch (MimeException e) {
                monitor(Event.INVALID_HEADER);
                if (config.isMalformedHeaderStartsBody()) {
                    LineReaderInputStream instream = getDataStream();
                    ByteArrayBuffer buf = fieldBuilder.getRaw();
                    // Complain, if raw data is not available or cannot be 'unread'
                    if (buf == null || !instream.unread(buf)) {
                        throw new MimeParseEventException(Event.INVALID_HEADER);
                    }
                    return false;
                }
            }
        }
    }

    /**
     * <p>Gets a descriptor for the current entity.
     * This method is valid if {@link #getState()} returns:</p>
     * <ul>
     * <li>{@link EntityState#T_BODY}</li>
     * <li>{@link EntityState#T_START_MULTIPART}</li>
     * <li>{@link EntityState#T_EPILOGUE}</li>
     * <li>{@link EntityState#T_PREAMBLE}</li>
     * </ul>
     * @return <code>BodyDescriptor</code>, not nulls
     */
    public BodyDescriptor getBodyDescriptor() {
        switch (getState()) {
        case T_BODY:
        case T_START_MULTIPART:
        case T_PREAMBLE:
        case T_EPILOGUE:
        case T_END_OF_STREAM:
            return body;
        default:
            throw new IllegalStateException("Invalid state :" + stateToString(state));
        }
    }

    /**
     * This method is valid, if {@link #getState()} returns {@link EntityState#T_FIELD}.
     * @return String with the fields raw contents.
     * @throws IllegalStateException {@link #getState()} returns another
     *   value than {@link EntityState#T_FIELD}.
     */
    public Field getField() {
        switch (getState()) {
        case T_FIELD:
            return field;
        default:
            throw new IllegalStateException("Invalid state :" + stateToString(state));
        }
    }

    /**
     * Monitors the given event.
     * Subclasses may override to perform actions upon events.
     * Base implementation logs at warn.
     * @param event <code>Event</code>, not null
     * @throws MimeException subclasses may elect to throw this exception upon
     * invalid content
     * @throws IOException subclasses may elect to throw this exception
     */
    protected void monitor(Event event) throws MimeException, IOException {
        if (monitor.isListening()) {
            String message = message(event);
            if (monitor.warn(message, "ignoring")) {
                throw new MimeParseEventException(event);
            }
        }
    }

    /**
     * Creates an indicative message suitable for display
     * based on the given event and the current state of the system.
     * @param event <code>Event</code>, not null
     * @return message suitable for use as a message in an exception
     * or for logging
     */
    protected String message(Event event) {
        final String message;
        if (event == null) {
            message = "Event is unexpectedly null.";
        } else {
            message = event.toString();
        }

        int lineNumber = getLineNumber();
        if (lineNumber <= 0)
            return message;
        else
            return "Line " + lineNumber + ": " + message;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + stateToString(state)
        + "][" + body.getMimeType() + "][" + body.getBoundary() + "]";
    }

    /**
     * Renders a state as a string suitable for logging.
     * @param state
     * @return rendered as string, not null
     */
    public static final String stateToString(EntityState state) {
        final String result;
        switch (state) {
            case T_END_OF_STREAM:
                result = "End of stream";
                break;
            case T_START_MESSAGE:
                result = "Start message";
                break;
            case T_END_MESSAGE:
                result = "End message";
                break;
            case T_RAW_ENTITY:
                result = "Raw entity";
                break;
            case T_START_HEADER:
                result = "Start header";
                break;
            case T_FIELD:
                result = "Field";
                break;
            case T_END_HEADER:
                result = "End header";
                break;
            case T_START_MULTIPART:
                result = "Start multipart";
                break;
            case T_END_MULTIPART:
                result = "End multipart";
                break;
            case T_PREAMBLE:
                result = "Preamble";
                break;
            case T_EPILOGUE:
                result = "Epilogue";
                break;
            case T_START_BODYPART:
                result = "Start bodypart";
                break;
            case T_END_BODYPART:
                result = "End bodypart";
                break;
            case T_BODY:
                result = "Body";
                break;
            default:
                result = "Unknown";
                break;
        }
        return result;
    }

}
