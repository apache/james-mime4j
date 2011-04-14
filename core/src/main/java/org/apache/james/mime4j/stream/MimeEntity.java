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
import org.apache.james.mime4j.io.MimeBoundaryInputStream;
import org.apache.james.mime4j.util.MimeUtil;

class MimeEntity extends AbstractEntity {

    private final LineNumberSource lineSource;
    private final BufferedLineReaderInputStream inbuffer;

    private RecursionMode recursionMode;
    private MimeBoundaryInputStream currentMimePartStream;
    private LineReaderInputStreamAdaptor dataStream;

    private byte[] tmpbuf;

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            MimeEntityConfig config,
            EntityState startState,
            EntityState endState,
            DecodeMonitor monitor,
            FieldBuilder fieldBuilder,
            MutableBodyDescriptor body) {
        super(config, startState, endState, monitor, fieldBuilder, body);
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
            MimeEntityConfig config,
            EntityState startState,
            EntityState endState,
            MutableBodyDescriptor body) {
        this(lineSource, instream, config, startState, endState,
                config.isStrictParsing() ? DecodeMonitor.STRICT : DecodeMonitor.SILENT,
                new DefaultFieldBuilder(config.getMaxHeaderLen()), body);
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            MimeEntityConfig config,
            MutableBodyDescriptor body) {
        this(lineSource, instream, config,
                EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE,
                config.isStrictParsing() ? DecodeMonitor.STRICT : DecodeMonitor.SILENT,
                new DefaultFieldBuilder(config.getMaxHeaderLen()), body);
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            FieldBuilder fieldBuilder,
            MutableBodyDescriptor body) {
        this(lineSource, instream, new MimeEntityConfig(),
                EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE,
                DecodeMonitor.SILENT,
                fieldBuilder, body);
    }

    MimeEntity(
            LineNumberSource lineSource,
            InputStream instream,
            MutableBodyDescriptor body) {
        this(lineSource, instream, new MimeEntityConfig(),
                EntityState.T_START_MESSAGE, EntityState.T_END_MESSAGE,
                DecodeMonitor.SILENT,
                new DefaultFieldBuilder(-1), body);
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

    @Override
    protected int getLineNumber() {
        if (lineSource == null)
            return -1;
        else
            return lineSource.getLineNumber();
    }

    @Override
    protected LineReaderInputStream getDataStream() {
        return dataStream;
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
        case T_FIELD:
            state = nextField() ? EntityState.T_FIELD : EntityState.T_END_HEADER;
            break;
        case T_END_HEADER:
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
        // TODO move the following lines inside the MimeBoundaryInputStream constructor
        int bufferSize = 2 * boundary.length();
        if (bufferSize < 4096) {
            bufferSize = 4096;
        }
        try {
            inbuffer.ensureCapacity(bufferSize);
            currentMimePartStream = new MimeBoundaryInputStream(inbuffer, boundary);
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
            RawEntity message = new RawEntity(instream);
            return message;
        } else {
            MimeEntity mimeentity = new MimeEntity(
                    lineSource,
                    instream,
                    config,
                    startState,
                    endState,
                    monitor,
                    fieldBuilder,
                    body.newChild());
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

}
