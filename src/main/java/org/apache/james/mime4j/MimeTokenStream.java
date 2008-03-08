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

package org.apache.james.mime4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.decoder.Base64InputStream;
import org.apache.james.mime4j.decoder.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;


/**
 * <p>
 * Parses MIME (or RFC822) message streams of bytes or characters.
 * The stream is converted into an event stream.
 * <p>
 * <p>
 * Typical usage:
 * </p>
 * <pre>
 *      MimeTokenStream stream = new MimeTokenStream();
 *      stream.parse(new BufferedInputStream(new FileInputStream("mime.msg")));
 *      for (int state = stream.getState();
 *           state != MimeTokenStream.T_END_OF_STREAM;
 *           state = stream.next()) {
 *          switch (state) {
 *            case MimeTokenStream.T_BODY:
 *              System.out.println("Body detected, contents = "
 *                + stream.getInputStream() + ", header data = "
 *                + stream.getBodyDescriptor());
 *              break;
 *            case MimeTokenStream.T_FIELD:
 *              System.out.println("Header field detected: "
 *                + stream.getField());
 *              break;
 *            case MimeTokenStream.T_START_MULTIPART:
 *              System.out.println("Multipart message detexted,"
 *                + " header data = "
 *                + stream.getBodyDescriptor());
 *            ...
 *          }
 *      }
 * </pre>
 * <p>
 * <strong>NOTE:</strong> All lines must end with CRLF 
 * (<code>\r\n</code>). If you are unsure of the line endings in your stream 
 * you should wrap it in a {@link org.apache.james.mime4j.EOLConvertingInputStream}
 * instance.</p>
 * <p>Instances of {@link MimeTokenStream} are reusable: Invoking the
 * method {@link #parse(InputStream)} resets the token streams internal
 * state. However, they are definitely <em>not</em> thread safe. If you
 * have a multi threaded application, then the suggested use is to have
 * one instance per thread.</p>
 * 
 * @version $Id: MimeStreamParser.java,v 1.8 2005/02/11 10:12:02 ntherning Exp $
 */
public class MimeTokenStream {
    private static final Log log = LogFactory.getLog(MimeTokenStream.class);

    /**
     * This token indicates, that the MIME stream has been completely
     * and successfully parsed, and no more data is available.
     */
    public static final int T_END_OF_STREAM = -1;
    /**
     * This token indicates, that the MIME stream is currently
     * at the beginning of a message.
     */
    public static final int T_START_MESSAGE = 0;
    /**
     * This token indicates, that the MIME stream is currently
     * at the end of a message.
     */
    public static final int T_END_MESSAGE = 1;
    /**
     * This token indicates, that a raw entity is currently being processed.
     * You may call {@link #getInputStream()} to obtain the raw entity
     * data.
     */
    public static final int T_RAW_ENTITY = 2;
    /**
     * This token indicates, that a message parts headers are now
     * being parsed.
     */
    public static final int T_START_HEADER = 3;
    /**
     * This token indicates, that a message parts field has now
     * been parsed. You may call {@link #getField()} to obtain the
     * raw field contents.
     */
    public static final int T_FIELD = 4;
    /**
     * This token indicates, that part headers have now been
     * parsed.
     */
    public static final int T_END_HEADER = 5;
    /**
     * This token indicates, that a multipart body is being parsed.
     */
    public static final int T_START_MULTIPART = 6;
    /**
     * This token indicates, that a multipart body has been parsed.
     */
    public static final int T_END_MULTIPART = 7;
    /**
     * This token indicates, that a multiparts preamble is being
     * parsed. You may call {@link #getInputStream()} to access the
     * preamble contents.
     */
    public static final int T_PREAMBLE = 8;
    /**
     * This token indicates, that a multiparts epilogue is being
     * parsed. You may call {@link #getInputStream()} to access the
     * epilogue contents.
     */
    public static final int T_EPILOGUE = 9;
    /**
     * This token indicates, that the MIME stream is currently
     * at the beginning of a body part.
     */
    public static final int T_START_BODYPART = 10;
    /**
     * This token indicates, that the MIME stream is currently
     * at the end of a body part.
     */
    public static final int T_END_BODYPART = 11;
    /**
     * This token indicates, that an atomic entity is being parsed.
     * Use {@link #getInputStream()} to access the entity contents.
     */
    public static final int T_BODY = 12;
    /**
     * Internal state, not exposed.
     */
    private static final int T_IN_BODYPART = -2;
    /**
     * Internal state, not exposed.
     */
    private static final int T_IN_MESSAGE = -3;

    private static final BitSet fieldChars = new BitSet();
    static {
        for (int i = 0x21; i <= 0x39; i++) {
            fieldChars.set(i);
        }
        for (int i = 0x3b; i <= 0x7e; i++) {
            fieldChars.set(i);
        }
    }

    /** 
     * Recursively parse every <code>message/rfc822</code> part 
     * @see #getRecursionMode() 
     */
    public static final int M_RECURSE = 0;
    /**
     * Do not recurse <code>message/rfc822</code> parts 
     * @see #getRecursionMode()
     */
    public static final int M_NO_RECURSE = 1;
    /** 
     * Parse into raw entities
     * @see #getRecursionMode() 
     */
    public static final int M_RAW = 2;
    
    /**
     * Renders a state as a string suitable for logging.
     * @param state 
     * @return rendered as string, not null
     */
    public static final String stateToString(int state) {
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
                result = "Premable";
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
            case T_IN_BODYPART:
                result = "Bodypart";
                break;
            case T_IN_MESSAGE:
                result = "In message";
                break;
            default:
                result = "Unknown";
                break;
        }
        return result;
    }
    
    /**
     * Creates a stream that strictly validates the input.
     * @return <code>MimeTokenStream</code> which throws a 
     * <code>MimeException</code> whenever possible issues 
     * are dedicated in the input
     */
    public static final MimeTokenStream createStrictValidationStream() {
        return new MimeTokenStream(true);
    }
    
    /**
     * Enumerates events which can be monitored.
     */
    public final static class Event { 

        /** Indicates that a body part ended prematurely. */
        public static final Event MIME_BODY_PREMATURE_END 
            = new Event("Body part ended prematurely. " +
                    "Boundary detected in header or EOF reached."); 
        /** Indicates that unexpected end of headers detected.*/
        public static final Event HEADERS_PREMATURE_END 
            = new Event("Unexpected end of headers detected. " +
                    "Higher level boundary detected or EOF reached.");
        
        private final String code;
        
        private Event(final String code) {
            super();
            this.code = code;
        }
        
        public int hashCode() {
            return code.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Event other = (Event) obj;
            return code.equals(other.code);
        }
        
        public String toString() {
            return code;
        }
    }

    abstract static class StateMachine {
        int state;
        abstract int next() throws IOException, MimeException;
        abstract InputStream read();
    }

    private static class RawEntity extends StateMachine {
        private InputStream stream;
        RawEntity(InputStream stream) {
            this.stream = stream;
            state = T_RAW_ENTITY;
        }
        int next() {
            state = T_END_OF_STREAM;
            return state;
        }
        InputStream read() {
            return stream;
        }
    }

    private abstract class Entity extends StateMachine {
        private final BodyDescriptor parent;
        private final Cursor cursor;
        private final StringBuffer sb = new StringBuffer();
        private BodyDescriptor body;
        private int pos, start;
        private int lineNumber, startLineNumber;
        private final int endState;
        
        String field, fieldName, fieldValue;

        Entity(Cursor cursor, BodyDescriptor parent, int startState, int endState) {
            this.parent = parent;
            this.cursor = cursor;
            state = startState;
            this.endState = endState;
        }

        private void setParsingFieldState() {
            state = parseField() ? T_FIELD : T_END_HEADER;
        }

        private int setParseBodyPartState() throws IOException, MimeException {
            cursor.advanceToBoundary();
            if (cursor.isEnded()) {
                monitor(Event.MIME_BODY_PREMATURE_END);
            } else {
                if (cursor.moreMimeParts()) {
                    final String boundary = body.getBoundary();
                    cursor.boundary(boundary);
                   
                    if (isRaw()) {
                        currentStateMachine = new RawEntity(cursor.nextSection());
                    } else {
                        currentStateMachine = new BodyPart(cursor.nextMimePartCursor(), body);
                    }
                    entities.add(currentStateMachine);
                    state = T_IN_BODYPART;
                    return currentStateMachine.state;
                }
            }
            state = T_EPILOGUE;
            return T_EPILOGUE;
        }

        int next() throws IOException, MimeException {
            switch (state) {
                case T_START_MESSAGE:
                case T_START_BODYPART:
                    state = T_START_HEADER;
                    break;
                case T_START_HEADER:
                    initHeaderParsing();
                    setParsingFieldState();
                    break;
                case T_FIELD:
                    setParsingFieldState();
                    break;
                case T_END_HEADER:
                    final String mimeType = body.getMimeType();
                    if (MimeUtil.isMultipart(mimeType)) {
                        state = T_START_MULTIPART;
                    } else if (recursionMode != M_NO_RECURSE && MimeUtil.isMessage(mimeType)) {
                        Cursor nextCursor = cursor;
                        final String transferEncoding = body.getTransferEncoding();
                        if (MimeUtil.isBase64Encoding(transferEncoding)) {
                            log.debug("base64 encoded message/rfc822 detected");
                            nextCursor = cursor.decodeBase64();
                        } else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
                            log.debug("quoted-printable encoded message/rfc822 detected");
                            nextCursor = cursor.decodeQuotedPrintable();
                        }
                        state = T_IN_MESSAGE;
                        return parseMessage(nextCursor, body);
                    } else {
                        state = T_BODY;
                        break;
                    }
                    break;
                case T_START_MULTIPART:
                    cursor.boundary(body.getBoundary());
                    if (cursor.isEnded()) {
                        state = T_END_MULTIPART;
                    } else {
                        state = T_PREAMBLE;
                    }
                    break;
                case T_PREAMBLE:
                    return setParseBodyPartState();
                case T_IN_BODYPART:
                    return setParseBodyPartState();
                case T_EPILOGUE:
                    state = T_END_MULTIPART;
                    break;
                case T_BODY:
                case T_END_MULTIPART:
                case T_IN_MESSAGE:
                    state = endState;
                    break;
                default:
                    if (state == endState) {
                        state = T_END_OF_STREAM;
                        break;
                    }
                    throw new IllegalStateException("Invalid state: " + state);
            }
            return state;
        }

        InputStream read() {
            switch (getState()) {
                case T_PREAMBLE:
                case T_EPILOGUE:
                case T_BODY:
                    return cursor.nextSection();
                case T_START_MULTIPART:
                    return cursor.rest();
                default:
                    throw new IllegalStateException("Expected state to be either of T_RAW_ENTITY, T_PREAMBLE, or T_EPILOGUE.");
            }
        }
        
        private void initHeaderParsing() throws IOException, MimeException {
            body = newBodyDescriptor(parent);
            startLineNumber = lineNumber = cursor.getLineNumber();

            int curr = 0;
            int prev = 0;
            while ((curr = cursor.advance()) != -1) {
                if (curr == '\n' && (prev == '\n' || prev == 0)) {
                    /*
                     * [\r]\n[\r]\n or an immediate \r\n have been seen.
                     */
                    sb.deleteCharAt(sb.length() - 1);
                    break;
                }
                sb.append((char) curr);
                prev = curr == '\r' ? prev : curr;
            }
            
            if (curr == -1) {
                monitor(Event.HEADERS_PREMATURE_END);
            }
        }

        private boolean parseField() {
            while (pos < sb.length()) {
                while (pos < sb.length() && sb.charAt(pos) != '\r') {
                    pos++;
                }
                if (pos < sb.length() - 1 && sb.charAt(pos + 1) != '\n') {
                    pos++;
                    continue;
                }
                if (pos >= sb.length() - 2 || fieldChars.get(sb.charAt(pos + 2))) {
                    /*
                     * field should be the complete field data excluding the 
                     * trailing \r\n.
                     */
                    field = sb.substring(start, pos);
                    start = pos + 2;
                    
                    /*
                     * Check for a valid field.
                     */
                    int index = field.indexOf(':');
                    boolean valid = false;
                    if (index != -1 && fieldChars.get(field.charAt(0))) {
                        valid = true;
                        fieldName = field.substring(0, index).trim();
                        for (int i = 0; i < fieldName.length(); i++) {
                            if (!fieldChars.get(fieldName.charAt(i))) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            fieldValue = field.substring(index + 1);
                            body.addField(fieldName, fieldValue);
                            startLineNumber = lineNumber;
                            pos += 2;
                            lineNumber++;
                            return true;
                        }
                    }
                    if (log.isWarnEnabled()) {
                        log.warn("Line " + startLineNumber 
                                + ": Ignoring invalid field: '" + field.trim() + "'");
                    }
                    startLineNumber = lineNumber;
                }
                pos += 2;
                lineNumber++;
            }
            return false;
        }
    }

    private class Message extends Entity {
        Message(Cursor cursor, BodyDescriptor parent) {
            super(cursor, parent, T_START_MESSAGE, T_END_MESSAGE);
        }
    }

    private class BodyPart extends Entity {
        BodyPart(Cursor cursor, BodyDescriptor parent) {
            super(cursor, parent, T_START_BODYPART, T_END_BODYPART);
        }
    }
    
    private final boolean strictParsing;
    private int state = T_END_OF_STREAM;
    private Cursor cursor;
    private StateMachine currentStateMachine;
    private final List entities = new ArrayList();
    
    private int recursionMode = M_RECURSE;
    
    /**
     * Constructs a standard (lax) stream.
     * Optional validation events will be logged only.
     * Use {@link #createStrictValidationStream()} to create
     * a stream that strictly validates the input.
     */
    public MimeTokenStream() {
        this(false);
    }
    
    private MimeTokenStream(final boolean strictParsing) {
        this.strictParsing = strictParsing;
    }
    
    /** Instructs the {@code MimeTokenStream} to parse the given streams contents.
     * If the {@code MimeTokenStream} has already been in use, resets the streams
     * internal state.
     */
    public void parse(InputStream stream) {
        entities.clear();
        cursor = new StreamCursor(stream);
        state = parseMessage(cursor, null);
    }

    private int parseMessage(Cursor cursor, BodyDescriptor parent) {
        switch (recursionMode) {
            case M_RAW:
                currentStateMachine = new RawEntity(cursor.nextSection());
                break;
            case M_NO_RECURSE:
                // expected to be called only at start of paring
            case M_RECURSE:
                currentStateMachine = new Message(cursor, parent);
                break;
        }
        entities.add(currentStateMachine);
        return currentStateMachine.state;
    }
    
    /**
     * Determines if this parser is currently in raw mode.
     * 
     * @return <code>true</code> if in raw mode, <code>false</code>
     *         otherwise.
     * @see #setRaw(boolean)
     */
    public boolean isRaw() {
        return recursionMode == M_RAW;
    }
    
    /**
     * Enables or disables raw mode. In raw mode all future entities 
     * (messages or body parts) in the stream will be reported to the
     * {@link ContentHandler#raw(InputStream)} handler method only.
     * The stream will contain the entire unparsed entity contents 
     * including header fields and whatever is in the body.
     * 
     * @param raw <code>true</code> enables raw mode, <code>false</code>
     *        disables it.
     * @deprecated pass {@link #M_RAW} to {@link #setRecursionMode(int)} 
     */
    public void setRaw(boolean raw) {
        if (raw) {
            recursionMode = M_RAW;
        } else {
            recursionMode = M_RECURSE;
        }
    }
    
    /**
     * Gets the current recursion mode.
     * The recursion mode specifies the approach taken to parsing parts.
     * {@link #M_RAW}  mode does not parse the part at all.
     * {@link #M_RECURSE} mode recursively parses each mail
     * when an <code>message/rfc822</code> part is encounted;
     * {@link #M_NO_RECURSE} does not.
     * @return {@link #M_RECURSE}, {@link #M_RAW} or {@link #M_NO_RECURSE}
     */
    public int getRecursionMode() {
        return recursionMode;
    }
    
    /**
     * Sets the current recursion.
     * The recursion mode specifies the approach taken to parsing parts.
     * {@link #M_RAW}  mode does not parse the part at all.
     * {@link #M_RECURSE} mode recursively parses each mail
     * when an <code>message/rfc822</code> part is encounted;
     * {@link #M_NO_RECURSE} does not.
     * @param mode {@link #M_RECURSE}, {@link #M_RAW} or {@link #M_NO_RECURSE}
     */
    public void setRecursionMode(int mode) {
        this.recursionMode = mode;
    }

    /**
     * Finishes the parsing and stops reading lines.
     * NOTE: No more lines will be parsed but the parser
     * will still call 
     * {@link ContentHandler#endMultipart()},
     * {@link ContentHandler#endBodyPart()},
     * {@link ContentHandler#endMessage()}, etc to match previous calls
     * to 
     * {@link ContentHandler#startMultipart(BodyDescriptor)},
     * {@link ContentHandler#startBodyPart()},
     * {@link ContentHandler#startMessage()}, etc.
     */
    public void stop() {
        cursor.stop();
    }

    /**
     * Returns the current state.
     */
    public int getState() {
        return state;
    }

    /**
     * This method is valid, if {@link #getState()} returns {@link #T_FIELD}.
     * @return String with the fields raw contents.
     * @throws IllegalStateException {@link #getState()} returns another
     *   value than {@link #T_FIELD}.
     */
    public String getField() {
        switch (getState()) {
            case T_FIELD:
                return ((Entity) currentStateMachine).field;
            default:
                throw new IllegalStateException("Expected state to be T_FIELD.");
        }
    }

    /**
     * This method is valid, if {@link #getState()} returns {@link #T_FIELD}.
     * @return String with the fields name.
     * @throws IllegalStateException {@link #getState()} returns another
     *   value than {@link #T_FIELD}.
     */
    public String getFieldName() {
        switch (getState()) {
            case T_FIELD:
                return ((Entity) currentStateMachine).fieldName;
            default:
                throw new IllegalStateException("Expected state to be T_FIELD.");
        }
    }

    /**
     * This method is valid, if {@link #getState()} returns {@link #T_FIELD}.
     * @return String with the fields value.
     * @throws IllegalStateException {@link #getState()} returns another
     *   value than {@link #T_FIELD}.
     */
    public String getFieldValue() {
        switch (getState()) {
            case T_FIELD:
                return ((Entity) currentStateMachine).fieldValue;
            default:
                throw new IllegalStateException("Expected state to be T_FIELD.");
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
        if (strictParsing) {
            throw new MimeParseEventException(event);
        } else {
            warn(event);
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
        String preamble = "";
        try {
            preamble = "Line " + cursor.getLineNumber() + ": ";
        } catch (IOException e) {
            log.debug("Cannot get event line number.", e);
        }

        final String message;
        if (event == null) {
            message = "Event is unexpectedly null.";
        } else {
            message = event.toString();
        }
        final String result = preamble + message;
        return result;
    }
    
    /**
     * Logs (at warn) an indicative message based on the given event 
     * and the current state of the system.
     * @param event <code>Event</code>, not null
     */
    protected void warn(Event event) {
        if (log.isWarnEnabled()) {
            log.warn(message(event));
        }
    }
    
    /**
     * Logs (at debug) an indicative message based on the given event
     * and the current state of the system.
     * @param event <code>Event</code>, not null
     */
    protected void debug(Event event) {
        if (log.isDebugEnabled()) {
            log.debug(message(event));
        }
    }

    /**
     * This method is valid, if {@link #getState()} returns either of
     * {@link #T_RAW_ENTITY}, {@link #T_PREAMBLE}, or {@link #T_EPILOGUE}.
     * It returns the raw entity, preamble, or epilogue contents.
     * @return Data stream, depending on the current state.
     * @throws IllegalStateException {@link #getState()} returns an
     *   invalid value.
     */
    public InputStream getInputStream() {
        return currentStateMachine.read();
    }

    /**
     * Gets a reader configured for the current body or body part.
     * The reader will return a transfer and charset decoded 
     * stream of characters based on the MIME fields with the standard
     * defaults.
     * This is a conveniance method and relies on {@link #getInputStream()}.
     * Consult the javadoc for that method for known limitations.
     * 
     * @return <code>Reader</code>, not null
     * @see #getInputStream 
     * @throws IllegalStateException {@link #getState()} returns an
     *   invalid value 
     * @throws UnsupportedCharsetException if there is no JVM support 
     * for decoding the charset
     * @throws IllegalCharsetNameException if the charset name specified
     * in the mime type is illegal
     */
    public Reader getReader() {
        final BodyDescriptor bodyDescriptor = getBodyDescriptor();
        final String mimeCharset = bodyDescriptor.getCharset();
        final String transferEncoding = bodyDescriptor.getTransferEncoding();
        final Charset charset;
        if (mimeCharset == null || "".equals(mimeCharset)) {
            charset = Charset.forName("US-ASCII");
        } else {
            charset = Charset.forName(mimeCharset);
        }
        
        final InputStream inputStream;
        final InputStream transferEncodedStream = getInputStream();
        if (MimeUtil.isBase64Encoding(transferEncoding)) {
            inputStream = new Base64InputStream(transferEncodedStream);
        } else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
            inputStream = new QuotedPrintableInputStream(transferEncodedStream);
        } else {
            inputStream = transferEncodedStream;
        }
        final InputStreamReader result = new InputStreamReader(inputStream, charset);
        return result;
    }
    
    /**
     * This method is valid, if {@link #getState()} returns
     * {@link #T_BODY}, or {@link #T_START_MULTIPART}. It returns the current
     * entities body descriptor.
     */
    public BodyDescriptor getBodyDescriptor() {
        switch (getState()) {
            case T_BODY:
            case T_START_MULTIPART:
                return ((Entity) currentStateMachine).body;
            default:
                throw new IllegalStateException("Expected state to be T_BODY.");
        }
    }

    /**
     * This method advances the token stream to the next token.
     * @throws IllegalStateException The method has been called, although
     *   {@link #getState()} was already {@link #T_END_OF_STREAM}.
     */
    public int next() throws IOException, MimeException {
        if (state == T_END_OF_STREAM  ||  currentStateMachine == null) {
            throw new IllegalStateException("No more tokens are available.");
        }
        while (currentStateMachine != null) {
            state = currentStateMachine.next();
            if (state != T_END_OF_STREAM) {
                return state;
            }
            entities.remove(entities.size()-1);
            if (entities.size() == 0) {
                currentStateMachine = null;
            } else {
                currentStateMachine = (StateMachine) entities.get(entities.size()-1);
            }
        }
        state = T_END_OF_STREAM;
        return state;
    }

    /**
     * Creates a new instance of {@link BodyDescriptor}. Subclasses may override
     * this in order to create body descriptors, that provide more specific
     * information.
     */
    protected BodyDescriptor newBodyDescriptor(BodyDescriptor pParent) {
        return new DefaultBodyDescriptor(pParent);
    }
}
