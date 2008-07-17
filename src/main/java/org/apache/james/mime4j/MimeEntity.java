package org.apache.james.mime4j;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.decoder.Base64InputStream;
import org.apache.james.mime4j.decoder.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;

public class MimeEntity extends AbstractEntity {

    /**
     * Internal state, not exposed.
     */
    private static final int T_IN_BODYPART = -2;
    /**
     * Internal state, not exposed.
     */
    private static final int T_IN_MESSAGE = -3;

    private final RootInputStream rootStream;
    private final BufferedLineReaderInputStream inbuffer;
    
    private int recursionMode;
    private MimeBoundaryInputStream mimeStream;
    private LineReaderInputStreamAdaptor dataStream;
    private boolean skipHeader;
    
    private byte[] tmpbuf;
    
    public MimeEntity(
            RootInputStream rootStream,
            BufferedLineReaderInputStream inbuffer,
            BodyDescriptor parent, 
            int startState, 
            int endState,
            boolean maximalBodyDescriptor,
            boolean strictParsing) {
        super(parent, startState, endState, maximalBodyDescriptor, strictParsing);
        this.rootStream = rootStream;
        this.inbuffer = inbuffer;
        this.dataStream = new LineReaderInputStreamAdaptor(inbuffer);
        this.skipHeader = false;
    }

    public MimeEntity(
            RootInputStream rootStream,
            BufferedLineReaderInputStream inbuffer,
            BodyDescriptor parent, 
            int startState, 
            int endState) {
        this(rootStream, inbuffer, parent, startState, endState, false, false);
    }

    public int getRecursionMode() {
        return recursionMode;
    }

    public void setRecursionMode(int recursionMode) {
        this.recursionMode = recursionMode;
    }

    public void skipHeader(String contentType) {
        if (state != EntityStates.T_START_MESSAGE) {
            throw new IllegalStateException("Invalid state: " + stateToString(state));
        }
        skipHeader = true;
        body.addField("Content-Type", contentType);
    }
    
    protected int getLineNumber() {
        return rootStream.getLineNumber();
    }
    
    protected LineReaderInputStream getDataStream() {
        return dataStream;
    }
    
    public EntityStateMachine advance() throws IOException, MimeException {
        switch (state) {
        case EntityStates.T_START_MESSAGE:
            if (skipHeader) {
                state = EntityStates.T_END_HEADER;
            } else {
                state = EntityStates.T_START_HEADER;
            }
            break;
        case EntityStates.T_START_BODYPART:
            state = EntityStates.T_START_HEADER;
            break;
        case EntityStates.T_START_HEADER:
        case EntityStates.T_FIELD:
            state = parseField() ? EntityStates.T_FIELD : EntityStates.T_END_HEADER;
            break;
        case EntityStates.T_END_HEADER:
            String mimeType = body.getMimeType();
            if (recursionMode == RecursionMode.M_FLAT) {
                state = EntityStates.T_BODY;
            } else if (MimeUtil.isMultipart(mimeType)) {
                state = EntityStates.T_START_MULTIPART;
                clearMimeStream();
            } else if (recursionMode != RecursionMode.M_NO_RECURSE 
                    && MimeUtil.isMessage(mimeType)) {
                state = T_IN_MESSAGE;
                return nextMessage();
            } else {
                state = EntityStates.T_BODY;
            }
            break;
        case EntityStates.T_START_MULTIPART:
            if (dataStream.isUsed()) {
                advanceToBoundary();            
                state = EntityStates.T_END_MULTIPART;
            } else {
                createMimeStream();
                state = EntityStates.T_PREAMBLE;
            }
            break;
        case EntityStates.T_PREAMBLE:
            advanceToBoundary();            
            if (mimeStream.isLastPart()) {
                clearMimeStream();
                state = EntityStates.T_END_MULTIPART;
            } else {
                clearMimeStream();
                createMimeStream();
                state = T_IN_BODYPART;
                return nextMimeEntity();
            }
            break;
        case T_IN_BODYPART:
            advanceToBoundary();
            if (mimeStream.eof() && !mimeStream.isLastPart()) {
                monitor(Event.MIME_BODY_PREMATURE_END);
            } else {
                if (!mimeStream.isLastPart()) {
                    clearMimeStream();
                    createMimeStream();
                    state = T_IN_BODYPART;
                    return nextMimeEntity();
                }
            }
            clearMimeStream();
            state = EntityStates.T_EPILOGUE;
            break;
        case EntityStates.T_EPILOGUE:
            state = EntityStates.T_END_MULTIPART;
            break;
        case EntityStates.T_BODY:
        case EntityStates.T_END_MULTIPART:
        case T_IN_MESSAGE:
            state = endState;
            break;
        default:
            if (state == endState) {
                state = EntityStates.T_END_OF_STREAM;
                break;
            }
            throw new IllegalStateException("Invalid state: " + stateToString(state));
        }
        return null;
    }

    private void createMimeStream() throws IOException {
        String boundary = body.getBoundary();
        int bufferSize = 2 * boundary.length();
        if (bufferSize < 4096) {
            bufferSize = 4096;
        }
        if (mimeStream != null) {
            mimeStream = new MimeBoundaryInputStream(
                    new BufferedLineReaderInputStream(mimeStream, bufferSize), boundary);
        } else {
            inbuffer.ensureCapacity(bufferSize);
            mimeStream = new MimeBoundaryInputStream(inbuffer, boundary);
        }
        dataStream = new LineReaderInputStreamAdaptor(mimeStream); 
    }
    
    private void clearMimeStream() {
        mimeStream = null;
        dataStream = new LineReaderInputStreamAdaptor(inbuffer); 
    }
    
    private void advanceToBoundary() throws IOException {
        if (!dataStream.eof()) {
            if (tmpbuf == null) {
                tmpbuf = new byte[2048];
            }
            while (dataStream.read(tmpbuf)!= -1) {
            }
        }
    }
    
    private EntityStateMachine nextMessage() {
        String transferEncoding = body.getTransferEncoding();
        InputStream instream;
        if (MimeUtil.isBase64Encoding(transferEncoding)) {
            log.debug("base64 encoded message/rfc822 detected");
            instream = new Base64InputStream(dataStream);                    
        } else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
            log.debug("quoted-printable encoded message/rfc822 detected");
            instream = new QuotedPrintableInputStream(dataStream);                    
        } else {
            instream = dataStream;
        }
        
        if (recursionMode == RecursionMode.M_RAW) {
            RawEntity message = new RawEntity(instream);
            return message;
        } else {
            MimeEntity message = new MimeEntity(
                    rootStream, 
                    new BufferedLineReaderInputStream(instream, 4 * 1024),
                    body, 
                    EntityStates.T_START_MESSAGE, 
                    EntityStates.T_END_MESSAGE,
                    maximalBodyDescriptor,
                    strictParsing);
            message.setRecursionMode(recursionMode);
            return message;
        }
    }
    
    private EntityStateMachine nextMimeEntity() {
        if (recursionMode == RecursionMode.M_RAW) {
            RawEntity message = new RawEntity(mimeStream);
            return message;
        } else {
            BufferedLineReaderInputStream stream = new BufferedLineReaderInputStream(mimeStream, 4 * 1024);
            MimeEntity mimeentity = new MimeEntity(
                    rootStream, 
                    stream,
                    body, 
                    EntityStates.T_START_BODYPART, 
                    EntityStates.T_END_BODYPART,
                    maximalBodyDescriptor,
                    strictParsing);
            mimeentity.setRecursionMode(recursionMode);
            return mimeentity;
        }
    }
    
    public InputStream getContentStream() {
        switch (state) {
        case EntityStates.T_START_MULTIPART:
        case EntityStates.T_PREAMBLE:
        case EntityStates.T_EPILOGUE:
        case EntityStates.T_BODY:
            return this.dataStream;
        default:
            throw new IllegalStateException("Invalid state: " + stateToString(state));
        }
    }

}
