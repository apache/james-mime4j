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
import java.util.BitSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract MIME entity.
 */
public abstract class AbstractEntity implements EntityStateMachine {

    protected final Log log;
    
    protected final RootInputStream rootStream;
    protected final BodyDescriptor parent;
    protected final int startState;
    protected final int endState;
    protected final boolean maximalBodyDescriptor;
    protected final boolean strictParsing;
    protected final MutableBodyDescriptor body;
    
    protected int state;

    private final StringBuffer sb = new StringBuffer();
    
    private int pos, start;
    private int lineNumber, startLineNumber;
    
    private String field, fieldName, fieldValue;

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
     * Internal state, not exposed.
     */
    private static final int T_IN_BODYPART = -2;
    /**
     * Internal state, not exposed.
     */
    private static final int T_IN_MESSAGE = -3;

    AbstractEntity(
            RootInputStream rootStream,
            BodyDescriptor parent,
            int startState, 
            int endState,
            boolean maximalBodyDescriptor,
            boolean strictParsing) {
        this.log = LogFactory.getLog(getClass());        
        this.rootStream = rootStream;
        this.parent = parent;
        this.state = startState;
        this.startState = startState; 
        this.endState = endState;
        this.maximalBodyDescriptor = maximalBodyDescriptor;
        this.strictParsing = strictParsing;
        this.body = newBodyDescriptor(parent);
    }

    public int getState() {
        return state;
    }
    
    /**
     * Creates a new instance of {@link BodyDescriptor}. Subclasses may override
     * this in order to create body descriptors, that provide more specific
     * information.
     */
    protected MutableBodyDescriptor newBodyDescriptor(BodyDescriptor pParent) {
        final MutableBodyDescriptor result;
        if (maximalBodyDescriptor) {
            result = new MaximalBodyDescriptor(pParent);
        } else {
            result = new DefaultBodyDescriptor(pParent);
        }
        return result;
    }
    
    protected abstract InputStream getDataStream();
    
    protected void initHeaderParsing() throws IOException, MimeException {
        startLineNumber = lineNumber = rootStream.getLineNumber();

        InputStream instream = getDataStream();
        
        int curr = 0;
        int prev = 0;
        while ((curr = instream.read()) != -1) {
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

    protected boolean parseField() {
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

    /**
     * <p>Gets a descriptor for the current entity.
     * This method is valid if {@link #getState()} returns:</p>
     * <ul>
     * <li>{@link #T_BODY}</li>
     * <li>{@link #T_START_MULTIPART}</li>
     * <li>{@link #T_EPILOGUE}</li>
     * <li>{@link #T_PREAMBLE}</li>
     * </ul>
     * @return <code>BodyDescriptor</code>, not nulls
     */
    public BodyDescriptor getBodyDescriptor() {
        switch (getState()) {
        case EntityStates.T_BODY:
        case EntityStates.T_START_MULTIPART:
        case EntityStates.T_PREAMBLE:
        case EntityStates.T_EPILOGUE:
            return body;
        default:
            throw new IllegalStateException("Invalid state :" + stateToString(state));
        }
    }

    /**
     * This method is valid, if {@link #getState()} returns {@link #T_FIELD}.
     * @return String with the fields raw contents.
     * @throws IllegalStateException {@link #getState()} returns another
     *   value than {@link #T_FIELD}.
     */
    public String getField() {
        switch (getState()) {
        case EntityStates.T_FIELD:
            return field;
        default:
            throw new IllegalStateException("Invalid state :" + stateToString(state));
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
        case EntityStates.T_FIELD:
            return fieldName;
        default:
            throw new IllegalStateException("Invalid state :" + stateToString(state));
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
        case EntityStates.T_FIELD:
            return fieldValue;
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
        String preamble = "Line " + rootStream.getLineNumber() + ": ";
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

    public String toString() {
        return "Current state: " + stateToString(state);
    }

    /**
     * Renders a state as a string suitable for logging.
     * @param state 
     * @return rendered as string, not null
     */
    public static final String stateToString(int state) {
        final String result;
        switch (state) {
            case EntityStates.T_END_OF_STREAM:
                result = "End of stream";
                break;
            case EntityStates.T_START_MESSAGE:
                result = "Start message";
                break;
            case EntityStates.T_END_MESSAGE:
                result = "End message";
                break;
            case EntityStates.T_RAW_ENTITY:
                result = "Raw entity";
                break;
            case EntityStates.T_START_HEADER:
                result = "Start header";
                break;
            case EntityStates.T_FIELD:
                result = "Field";
                break;
            case EntityStates.T_END_HEADER:
                result = "End header";
                break;
            case EntityStates.T_START_MULTIPART:
                result = "Start multipart";
                break;
            case EntityStates.T_END_MULTIPART:
                result = "End multipart";
                break;
            case EntityStates.T_PREAMBLE:
                result = "Premable";
                break;
            case EntityStates.T_EPILOGUE:
                result = "Epilogue";
                break;
            case EntityStates.T_START_BODYPART:
                result = "Start bodypart";
                break;
            case EntityStates.T_END_BODYPART:
                result = "End bodypart";
                break;
            case EntityStates.T_BODY:
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
    
}
