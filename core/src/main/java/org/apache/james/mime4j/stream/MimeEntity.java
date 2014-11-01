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
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.io.BufferedLineReaderInputStream;
import org.apache.james.mime4j.io.LimitedInputStream;
import org.apache.james.mime4j.io.LineNumberSource;
import org.apache.james.mime4j.io.LineReaderInputStream;
import org.apache.james.mime4j.io.LineReaderInputStreamAdaptor;
import org.apache.james.mime4j.io.MaxHeaderLimitException;
import org.apache.james.mime4j.io.MaxLineLimitException;
import org.apache.james.mime4j.io.MimeBoundaryInputStream;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.MimeUtil;

class MimeEntity implements EntityStateMachine {

    private final EntityState endState;
    private final MimeConfig config;
    private final DecodeMonitor monitor;
    private final FieldBuilder fieldBuilder;
    private final BodyDescriptorBuilder bodyDescBuilder;

    private final ByteArrayBuffer linebuf;
    private final LineNumberSource lineSource;
    private final BufferedLineReaderInputStream inbuffer;

    private EntityState state;
    private int lineCount;
    private boolean endOfHeader;
    private int headerCount;
    private Field field;
    private BodyDescriptor body;

    private RecursionMode recursionMode;
    private MimeBoundaryInputStream currentMimePartStream;
    private LineReaderInputStreamAdaptor dataStream;

    private byte[] tmpbuf;

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            MimeConfig config,
            EntityState startState,
            EntityState endState,
            DecodeMonitor monitor,
            FieldBuilder fieldBuilder,
            BodyDescriptorBuilder bodyDescBuilder) {
        super();
        this.config = config;
        this.state = startState;
        this.endState = endState;
        this.monitor = monitor;
        this.fieldBuilder = fieldBuilder;
        this.bodyDescBuilder = bodyDescBuilder;
        this.linebuf = new ByteArrayBuffer(64);
        this.lineCount = 0;
        this.endOfHeader = false;
        this.headerCount = 0;
        this.lineSource = lineSource;
        this.inbuffer = new BufferedLineReaderInputStream(
                instream,
                4 * 1024,
                config.getMaxLineLen());
        this.dataStream = new LineReaderInputStreamAdaptor(
                inbuffer,
                config.getMaxLineLen());
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            MimeConfig config,
            EntityState startState,
            EntityState endState,
            BodyDescriptorBuilder bodyDescBuilder) {
        this(lineSource, instream, config, startState, endState,
                config.isStrictParsing() ? DecodeMonitor.STRICT : DecodeMonitor.SILENT,
                new DefaultFieldBuilder(config.getMaxHeaderLen()),
                bodyDescBuilder);
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            MimeConfig config,
            BodyDescriptorBuilder bodyDescBuilder) {
        this(lineSource, instream, config,
                EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE,
                config.isStrictParsing() ? DecodeMonitor.STRICT : DecodeMonitor.SILENT,
                new DefaultFieldBuilder(config.getMaxHeaderLen()),
                bodyDescBuilder);
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            FieldBuilder fieldBuilder,
            BodyDescriptorBuilder bodyDescBuilder) {
        this(lineSource, instream, MimeConfig.DEFAULT,
                EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE,
                DecodeMonitor.SILENT,
                fieldBuilder,
                bodyDescBuilder);
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            BodyDescriptorBuilder bodyDescBuilder) {
        this(lineSource, instream, MimeConfig.DEFAULT,
                EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE,
                DecodeMonitor.SILENT,
                new DefaultFieldBuilder(-1),
                bodyDescBuilder);
    }

    public EntityState getState() {
        return state;
    }

    public RecursionMode getRecursionMode() {
        return recursionMode;
    }

    public void setRecursionMode(RecursionMode recursionMode) {
        this.recursionMode = recursionMode;
    }

    public void stop() {
        this.inbuffer.truncate();
    }

    private int getLineNumber() {
        if (lineSource == null)
            return -1;
        else
            return lineSource.getLineNumber();
    }

    private LineReaderInputStream getDataStream() {
        return dataStream;
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

    protected void monitor(Event event) throws MimeException, IOException {
        if (monitor.isListening()) {
            String message = message(event);
            if (monitor.warn(message, "ignoring")) {
                throw new MimeParseEventException(event);
            }
        }
    }

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
                throw new MaxHeaderLimitException("Maximum header limit (" + maxHeaderCount + ") exceeded");
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
                Field parsedField = bodyDescBuilder.addField(rawfield);
                field = parsedField != null ? parsedField : rawfield;
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

    public EntityStateMachine advance() throws IOException, MimeException {
        switch (state) {
        case T_START_MESSAGE:
            state = EntityState.T_START_HEADER;
            break;
        case T_START_BODYPART:
            state = EntityState.T_START_HEADER;
            break;
        case T_START_HEADER:
            bodyDescBuilder.reset();
        case T_FIELD:
            state = nextField() ? EntityState.T_FIELD : EntityState.T_END_HEADER;
            break;
        case T_END_HEADER:
            body = bodyDescBuilder.build();
            String mimeType = body.getMimeType();
            if (recursionMode == RecursionMode.M_FLAT) {
                state = EntityState.T_BODY;
            } else if (MimeUtil.isMultipart(mimeType)) {
                state = EntityState.T_START_MULTIPART;
                clearMimePartStream();
            } else if (recursionMode != RecursionMode.M_NO_RECURSE
                    && MimeUtil.isMessage(mimeType)) {
                state = EntityState.T_BODY;
                return nextMessage();
            } else {
                state = EntityState.T_BODY;
            }
            break;
        case T_START_MULTIPART:
            if (dataStream.isUsed()) {
                advanceToBoundary();
                state = EntityState.T_END_MULTIPART;
                break;
            } else {
                createMimePartStream();
                state = EntityState.T_PREAMBLE;

                boolean empty = currentMimePartStream.isEmptyStream();
                if (!empty) break;
                // continue to next state
            }
        case T_PREAMBLE:
            // removed specific code. Fallback to T_IN_BODYPART that
            // better handle missing parts.
            // Removed the T_IN_BODYPART state (always use T_PREAMBLE)
            advanceToBoundary();
            if (currentMimePartStream.eof() && !currentMimePartStream.isLastPart()) {
                monitor(Event.MIME_BODY_PREMATURE_END);
            } else {
                if (!currentMimePartStream.isLastPart()) {
                    clearMimePartStream();
                    createMimePartStream();
                    return nextMimeEntity();
                }
            }
            boolean empty = currentMimePartStream.isFullyConsumed();
            clearMimePartStream();
            state = EntityState.T_EPILOGUE;
            if (!empty) break;
            // continue to next state
        case T_EPILOGUE:
            state = EntityState.T_END_MULTIPART;
            break;
        case T_BODY:
        case T_END_MULTIPART:
            state = endState;
            break;
        default:
            if (state == endState) {
                state = EntityState.T_END_OF_STREAM;
                break;
            }
            throw new IllegalStateException("Invalid state: " + stateToString(state));
        }
        return null;
    }

    private void createMimePartStream() throws MimeException, IOException {
        String boundary = body.getBoundary();
        if (boundary == null) {
            throw new MimeException("Multipart body does not have a valid boundary");
        }
        try {
            currentMimePartStream = new MimeBoundaryInputStream(inbuffer, boundary,
                    config.isStrictParsing());
        } catch (IllegalArgumentException e) {
            // thrown when boundary is too long
            throw new MimeException(e.getMessage(), e);
        }
        dataStream = new LineReaderInputStreamAdaptor(
                currentMimePartStream,
                config.getMaxLineLen());
    }

    private void clearMimePartStream() {
        currentMimePartStream = null;
        dataStream = new LineReaderInputStreamAdaptor(
                inbuffer,
                config.getMaxLineLen());
    }

    private void advanceToBoundary() throws IOException {
        if (!dataStream.eof()) {
            if (tmpbuf == null) {
                tmpbuf = new byte[2048];
            }
            InputStream instream = getLimitedContentStream();
            while (instream.read(tmpbuf)!= -1) {
            }
        }
    }

    private EntityStateMachine nextMessage() {
        // optimize nesting of streams returning the "lower" stream instead of
        // always return dataStream (that would add a LineReaderInputStreamAdaptor in the chain)
        InputStream instream = currentMimePartStream != null ? currentMimePartStream : inbuffer;
        instream = decodedStream(instream);
        return nextMimeEntity(EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE, instream);
    }

    private InputStream decodedStream(InputStream instream) {
        String transferEncoding = body.getTransferEncoding();
        if (MimeUtil.isBase64Encoding(transferEncoding)) {
            instream = new Base64InputStream(instream, monitor);
        } else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
            instream = new QuotedPrintableInputStream(instream, monitor);
        }
        return instream;
    }

    private EntityStateMachine nextMimeEntity() {
        return nextMimeEntity(EntityState.T_START_BODYPART, EntityState.T_END_BODYPART, currentMimePartStream);
    }

    private EntityStateMachine nextMimeEntity(EntityState startState, EntityState endState, InputStream instream) {
        if (recursionMode == RecursionMode.M_RAW) {
            return new RawEntity(instream);
        } else {
            MimeEntity mimeentity = new MimeEntity(
                    lineSource,
                    instream,
                    config,
                    startState,
                    endState,
                    monitor,
                    fieldBuilder,
                    bodyDescBuilder.newChild());
            mimeentity.setRecursionMode(recursionMode);
            return mimeentity;
        }
    }

    private InputStream getLimitedContentStream() {
        long maxContentLimit = config.getMaxContentLen();
        if (maxContentLimit >= 0) {
            return new LimitedInputStream(dataStream, maxContentLimit);
        } else {
            return dataStream;
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
     * @see org.apache.james.mime4j.stream.EntityStateMachine#getContentStream()
     */
    public InputStream getContentStream() {
        switch (state) {
        case T_START_MULTIPART:
        case T_PREAMBLE:
        case T_EPILOGUE:
        case T_BODY:
            return getLimitedContentStream();
        default:
            throw new IllegalStateException("Invalid state: " + stateToString(state));
        }
    }

    /**
     * @see org.apache.james.mime4j.stream.EntityStateMachine#getDecodedContentStream()
     */
    public InputStream getDecodedContentStream() throws IllegalStateException {
        return decodedStream(getContentStream());
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
    public static String stateToString(EntityState state) {
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
