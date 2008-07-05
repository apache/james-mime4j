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

    private final InputBuffer inbuffer;
    private final InputStream rawStream;
    
    private int recursionMode;
    private MimeBoundaryInputStream mimeStream;
    private EOFSensitiveInputStream dataStream;
    private boolean skipHeader;
    
    public MimeEntity(
            RootInputStream rootStream,
            InputStream rawStream,
            InputBuffer inbuffer,
            BodyDescriptor parent, 
            int startState, 
            int endState,
            boolean maximalBodyDescriptor,
            boolean strictParsing) {
        super(rootStream, parent, startState, endState, maximalBodyDescriptor, strictParsing);
        this.inbuffer = inbuffer;
        this.rawStream = rawStream;
        this.dataStream = new EOFSensitiveInputStream(rawStream);
        this.skipHeader = false;
    }

    public MimeEntity(
            RootInputStream rootStream,
            InputStream rawStream,
            InputBuffer inbuffer,
            BodyDescriptor parent, 
            int startState, 
            int endState) {
        this(rootStream, rawStream, inbuffer, parent, startState, endState, false, false);
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
    
    protected InputStream getDataStream() {
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
            initHeaderParsing();
            state = parseField() ? EntityStates.T_FIELD : EntityStates.T_END_HEADER;
            break;
        case EntityStates.T_FIELD:
            state = parseField() ? EntityStates.T_FIELD : EntityStates.T_END_HEADER;
            break;
        case EntityStates.T_END_HEADER:
            String mimeType = body.getMimeType();
            if (MimeUtil.isMultipart(mimeType)) {
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
        mimeStream = new MimeBoundaryInputStream(inbuffer, body.getBoundary());
        dataStream = new EOFSensitiveInputStream(mimeStream); 
    }
    
    private void clearMimeStream() {
        mimeStream = null;
        dataStream = new EOFSensitiveInputStream(rawStream); 
    }
    
    private void advanceToBoundary() throws IOException {
        if (!dataStream.eof()) {
            byte[] tmp = new byte[2048];
            while (dataStream.read(tmp)!= -1) {
            }
        }
    }
    
    private EntityStateMachine nextMessage() {
        String transferEncoding = body.getTransferEncoding();
        InputStream instream;
        if (MimeUtil.isBase64Encoding(transferEncoding)) {
            log.debug("base64 encoded message/rfc822 detected");
            instream = new EOLConvertingInputStream(
                    new Base64InputStream(mimeStream));                    
        } else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
            log.debug("quoted-printable encoded message/rfc822 detected");
            instream = new EOLConvertingInputStream(
                    new QuotedPrintableInputStream(mimeStream));                    
        } else {
            instream = dataStream;
        }
        
        if (recursionMode == RecursionMode.M_RAW) {
            RawEntity message = new RawEntity(instream);
            return message;
        } else {
            MimeEntity message = new MimeEntity(
                    rootStream, 
                    instream,
                    inbuffer, 
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
            MimeEntity mimeentity = new MimeEntity(
                    rootStream, 
                    mimeStream,
                    inbuffer, 
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
