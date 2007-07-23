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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.decoder.Base64InputStream;
import org.apache.james.mime4j.decoder.QuotedPrintableInputStream;


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
    private static final Log log = LogFactory.getLog(MimeStreamParser.class);

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

    abstract static class StateMachine {
        int state;
        abstract int next() throws IOException, MimeException;
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
    }

    private abstract class Entity extends StateMachine {
        private final BodyDescriptor parent;
        private final InputStream contents;
        private final StringBuffer sb = new StringBuffer();
        private BodyDescriptor body;
        private int pos, start;
        private int lineNumber, startLineNumber;
        private final int endState;
        private MimeBoundaryInputStream mbis;
        InputStream stream;
        String field;

        Entity(InputStream contents, BodyDescriptor parent, int startState, int endState) {
            this.parent = parent;
            this.contents = contents;
            state = startState;
            this.endState = endState;
        }

        private void setParsingFieldState() {
            state = parseField() ? T_FIELD : T_END_HEADER;
        }

        private int setParseBodyPartState() throws IOException {
            mbis.consume();
            if (mbis.parentEOF()) {
                if (log.isWarnEnabled()) {
                    log.warn("Line " + rootInputStream.getLineNumber() 
                            + ": Body part ended prematurely. "
                            + "Higher level boundary detected or "
                            + "EOF reached.");
                }
            } else {
                if (mbis.hasMoreParts()) {
                    mbis = new MimeBoundaryInputStream(contents, body.getBoundary());
                    if (isRaw()) {
                        currentStateMachine = new RawEntity(mbis);
                    } else {
                        currentStateMachine = new BodyPart(mbis, body);
                    }
                    entities.add(currentStateMachine);
                    state = T_IN_BODYPART;
                    return currentStateMachine.state;
                }
            }
            state = T_EPILOGUE;
            stream = new CloseShieldInputStream(contents);
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
                    if (body.isMultipart()) {
                        state = T_START_MULTIPART;
                    } else if (body.isMessage()) {
                        InputStream is = contents;
                        if (body.isBase64Encoded()) {
                            log.warn("base64 encoded message/rfc822 detected");
                            is = new EOLConvertingInputStream(new Base64InputStream(contents));
                        } else if (body.isQuotedPrintableEncoded()) {
                            log.warn("quoted-printable encoded message/rfc822 detected");
                            is = new EOLConvertingInputStream(new QuotedPrintableInputStream(contents));
                        }
                        state = endState;
                        return parseMessage(is, body);
                    } else {
                        stream = new CloseShieldInputStream(contents);
                        state = T_BODY;
                        break;
                    }
                    break;
                case T_START_MULTIPART:
                    mbis = new MimeBoundaryInputStream(contents, body.getBoundary());
                    stream = new CloseShieldInputStream(mbis);
                    state = T_PREAMBLE;
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

        private void initHeaderParsing() throws IOException {
            body = new BodyDescriptor(parent);
            startLineNumber = lineNumber = rootInputStream.getLineNumber();

            int curr = 0;
            int prev = 0;
            while ((curr = contents.read()) != -1) {
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
            
            if (curr == -1 && log.isWarnEnabled()) {
                log.warn("Line " + rootInputStream.getLineNumber()  
                        + ": Unexpected end of headers detected. "
                        + "Boundary detected in header or EOF reached.");
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
                        String fieldName = field.substring(0, index).trim();
                        for (int i = 0; i < fieldName.length(); i++) {
                            if (!fieldChars.get(fieldName.charAt(i))) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            body.addField(fieldName, field.substring(index + 1));
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
        Message(InputStream contents, BodyDescriptor parent) {
            super(contents, parent, T_START_MESSAGE, T_END_MESSAGE);
        }
    }

    private class BodyPart extends Entity {
        BodyPart(InputStream contents, BodyDescriptor parent) {
            super(contents, parent, T_START_BODYPART, T_END_BODYPART);
        }
    }
    
    private int state = T_END_OF_STREAM;
    private RootInputStream rootInputStream;
    private StateMachine currentStateMachine;
    private final List entities = new ArrayList();
    private boolean raw;

    /** Instructs the {@code MimeTokenStream} to parse the given streams contents.
     * If the {@code MimeTokenStream} has already been in use, resets the streams
     * internal state.
     */
    public void parse(InputStream stream) {
        entities.clear();
        rootInputStream = new RootInputStream(stream);
        state = parseMessage(rootInputStream, null);
    }

    private int parseMessage(InputStream pStream, BodyDescriptor parent) {
        if (isRaw()) {
            currentStateMachine = new RawEntity(pStream);
        } else {
            currentStateMachine = new Message(pStream, parent);
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
        return raw;
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
     */
    public void setRaw(boolean raw) {
        this.raw = raw;
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
        rootInputStream.truncate();
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
     * This method is valid, if {@link #getState()} returns either of
     * {@link #T_RAW_ENTITY}, {@link #T_PREAMBLE}, or {@link #T_EPILOGUE}.
     * It returns the raw entity, preamble, or epilogue contents.
     * @return Data stream, depending on the current state.
     * @throws IllegalStateException {@link #getState()} returns an
     *   invalid value.
     */
    public InputStream getInputStream() {
        switch (getState()) {
            case T_RAW_ENTITY:
                return ((RawEntity) currentStateMachine).stream;
            case T_PREAMBLE:
            case T_EPILOGUE:
            case T_BODY:
                return ((Entity) currentStateMachine).stream;
            default:
                throw new IllegalStateException("Expected state to be either of T_RAW_ENTITY, T_PREAMBLE, or T_EPILOGUE.");
        }
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
}
