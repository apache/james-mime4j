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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.io.LineNumberInputStream;
import org.apache.james.mime4j.util.CharsetUtil;

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
 *      InputStream instream = new FileInputStream("mime.msg");
 *      try {
 *          stream.parse(instream);
 *          for (int state = stream.getState();
 *              state != MimeTokenStream.T_END_OF_STREAM;
 *              state = stream.next()) {
 *              switch (state) {
 *              case MimeTokenStream.T_BODY:
 *                  System.out.println("Body detected, contents = "
 *                  + stream.getInputStream() + ", header data = "
 *                  + stream.getBodyDescriptor());
 *                  break;
 *              case MimeTokenStream.T_FIELD:
 *                  System.out.println("Header field detected: "
 *                  + stream.getField());
 *                  break;
 *              case MimeTokenStream.T_START_MULTIPART:
 *                  System.out.println("Multipart message detexted,"
 *                  + " header data = "
 *                  + stream.getBodyDescriptor());
 *              ...
 *              }
 *          }
 *      } finally {
 *          instream.close();
 *      }
 * </pre>
 * <p>Instances of {@link MimeTokenStream} are reusable: Invoking the
 * method {@link #parse(InputStream)} resets the token streams internal
 * state. However, they are definitely <em>not</em> thread safe. If you
 * have a multi threaded application, then the suggested use is to have
 * one instance per thread.</p>
 */
public class MimeTokenStream {

    private final MimeConfig config;
    private final DecodeMonitor monitor;
    private final FieldBuilder fieldBuilder;
    private final BodyDescriptorBuilder bodyDescBuilder;
    private final LinkedList<EntityStateMachine> entities = new LinkedList<EntityStateMachine>();

    private EntityState state = EntityState.T_END_OF_STREAM;
    private EntityStateMachine currentStateMachine;
    private RecursionMode recursionMode = RecursionMode.M_RECURSE;
    private MimeEntity rootentity;

    /**
     * Constructs a standard (lax) stream.
     * Optional validation events will be logged only.
     * Use {@link MimeConfig.Builder#setStrictParsing(boolean)} to turn on strict
     * parsing mode and pass the config object to
     * {@link MimeTokenStream#MimeTokenStream(MimeConfig)} to create
     * a stream that strictly validates the input.
     */
    public MimeTokenStream() {
        this(null);
    }

    public MimeTokenStream(final MimeConfig config) {
        this(config, null, null, null);
    }

    public MimeTokenStream(
            final MimeConfig config,
            final BodyDescriptorBuilder bodyDescBuilder) {
        this(config, null, null, bodyDescBuilder);
    }

    public MimeTokenStream(
            final MimeConfig config,
            final DecodeMonitor monitor,
            final BodyDescriptorBuilder bodyDescBuilder) {
        this(config, monitor, null, bodyDescBuilder);
    }

    public MimeTokenStream(
            final MimeConfig config,
            final DecodeMonitor monitor,
            final FieldBuilder fieldBuilder,
            final BodyDescriptorBuilder bodyDescBuilder) {
        super();
        this.config = config != null ? config : MimeConfig.DEFAULT;
        this.fieldBuilder = fieldBuilder != null ? fieldBuilder :
            new DefaultFieldBuilder(this.config.getMaxHeaderLen());
        this.monitor = monitor != null ? monitor :
            (this.config.isStrictParsing() ? DecodeMonitor.STRICT : DecodeMonitor.SILENT);
        this.bodyDescBuilder = bodyDescBuilder != null ? bodyDescBuilder :
            new FallbackBodyDescriptorBuilder();
    }

    /** Instructs the {@code MimeTokenStream} to parse the given streams contents.
     * If the {@code MimeTokenStream} has already been in use, resets the streams
     * internal state.
     */
    public void parse(InputStream stream) {
        doParse(stream, EntityState.T_START_MESSAGE);
    }

    /**
     * <p>Instructs the {@code MimeTokenStream} to parse the given content with
     * the content type. The message stream is assumed to have no message header
     * and is expected to begin with a message body. This can be the case when
     * the message content is transmitted using a different transport protocol
     * such as HTTP.</p>
     * <p>If the {@code MimeTokenStream} has already been in use, resets the
     * streams internal state.</p>
     * @return a parsed Field representing the input contentType
     */
    public Field parseHeadless(InputStream stream, String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type may not be null");
        }
        Field newContentType;
        try {
            RawField rawContentType = new RawField("Content-Type", contentType);
            newContentType = bodyDescBuilder.addField(rawContentType);
            if (newContentType == null) newContentType = rawContentType;
        } catch (MimeException ex) {
            // should never happen
            throw new IllegalArgumentException(ex.getMessage());
        }

        doParse(stream, EntityState.T_END_HEADER);
        try {
            next();
        } catch (IOException e) {
            // Should never happend: the first next after END_HEADER does not produce IO
            throw new IllegalStateException(e);
        } catch (MimeException e) {
            // This should never happen
            throw new IllegalStateException(e);
        }
        return newContentType;
    }

    private void doParse(InputStream stream, EntityState start) {
        if (config.isCountLineNumbers()) {
            LineNumberInputStream lnstream = new LineNumberInputStream(stream);
            rootentity = new MimeEntity(
                    lnstream,
                    lnstream,
                    config,
                    start,
                    EntityState.T_END_MESSAGE,
                    monitor,
                    fieldBuilder,
                    bodyDescBuilder);
        } else {
            rootentity = new MimeEntity(
                    null,
                    stream,
                    config,
                    start,
                    EntityState.T_END_MESSAGE,
                    monitor,
                    fieldBuilder,
                    bodyDescBuilder);
        }

        rootentity.setRecursionMode(recursionMode);
        currentStateMachine = rootentity;
        entities.clear();
        entities.add(currentStateMachine);
        state = currentStateMachine.getState();
    }

    /**
     * Determines if this parser is currently in raw mode.
     *
     * @return <code>true</code> if in raw mode, <code>false</code>
     *         otherwise.
     * @see #setRecursionMode(RecursionMode)
     */
    public boolean isRaw() {
        return recursionMode == RecursionMode.M_RAW;
    }

    /**
     * Gets the current recursion mode.
     * The recursion mode specifies the approach taken to parsing parts.
     * {@link RecursionMode#M_RAW}  mode does not parse the part at all.
     * {@link RecursionMode#M_RECURSE} mode recursively parses each mail
     * when an <code>message/rfc822</code> part is encountered;
     * {@link RecursionMode#M_NO_RECURSE} does not.
     * @return {@link RecursionMode#M_RECURSE}, {@link RecursionMode#M_RAW} or
     *   {@link RecursionMode#M_NO_RECURSE}
     */
    public RecursionMode getRecursionMode() {
        return recursionMode;
    }

    /**
     * Sets the current recursion.
     * The recursion mode specifies the approach taken to parsing parts.
     * {@link RecursionMode#M_RAW}  mode does not parse the part at all.
     * {@link RecursionMode#M_RECURSE} mode recursively parses each mail
     * when an <code>message/rfc822</code> part is encountered;
     * {@link RecursionMode#M_NO_RECURSE} does not.
     * @param mode {@link RecursionMode#M_RECURSE}, {@link RecursionMode#M_RAW} or
     *   {@link RecursionMode#M_NO_RECURSE}
     */
    public void setRecursionMode(RecursionMode mode) {
        recursionMode = mode;
        if (currentStateMachine != null) {
            currentStateMachine.setRecursionMode(mode);
        }
    }

    /**
     * Finishes the parsing and stops reading lines.
     * NOTE: No more lines will be parsed but the parser
     * will still trigger 'end' events to match previously
     * triggered 'start' events.
     */
    public void stop() {
        rootentity.stop();
    }

    /**
     * Returns the current state.
     */
    public EntityState getState() {
        return state;
    }

    /**
     * <p>
     * This method returns the raw entity, preamble, or epilogue contents.
     * </p>
     * <p>
     * This method is valid, if {@link #getState()} returns either of
     * {@link EntityState#T_RAW_ENTITY}, {@link EntityState#T_PREAMBLE}, or
     * {@link EntityState#T_EPILOGUE}.
     * </p>
     *
     * @return Data stream, depending on the current state.
     * @throws IllegalStateException {@link #getState()} returns an
     *   invalid value.
     */
    public InputStream getInputStream() {
        return currentStateMachine.getContentStream();
    }

    /**
     * <p>
     * This method returns a transfer decoded stream based on the MIME
     * fields with the standard defaults.
     * </p>
     * <p>
     * This method is valid, if {@link #getState()} returns either of
     * {@link EntityState#T_RAW_ENTITY}, {@link EntityState#T_PREAMBLE}, or
     * {@link EntityState#T_EPILOGUE}.
     * </p>
     *
     * @return Data stream, depending on the current state.
     * @throws IllegalStateException {@link #getState()} returns an
     *   invalid value.
     */
    public InputStream getDecodedInputStream() {
        return currentStateMachine.getDecodedContentStream();
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
     * @throws UnsupportedEncodingException if there is no JVM support
     * for decoding the charset
     */
    public Reader getReader() throws UnsupportedEncodingException {
        final BodyDescriptor bodyDescriptor = getBodyDescriptor();
        final String mimeCharset = bodyDescriptor.getCharset();
        final Charset charset;
        if (mimeCharset == null || "".equals(mimeCharset)) {
            charset = Charsets.US_ASCII;
        } else {
            charset = CharsetUtil.lookup(mimeCharset);
            if (charset == null) {
                throw new UnsupportedEncodingException(mimeCharset);
            }
        }
        final InputStream instream = getDecodedInputStream();
        return new InputStreamReader(instream, charset);
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
        return currentStateMachine.getBodyDescriptor();
    }

    /**
     * This method is valid, if {@link #getState()} returns {@link EntityState#T_FIELD}.
     * @return String with the fields raw contents.
     * @throws IllegalStateException {@link #getState()} returns another
     *   value than {@link EntityState#T_FIELD}.
     */
    public Field getField() {
        return currentStateMachine.getField();
    }

    /**
     * This method advances the token stream to the next token.
     * @throws IllegalStateException The method has been called, although
     *   {@link #getState()} was already {@link EntityState#T_END_OF_STREAM}.
     */
    public EntityState next() throws IOException, MimeException {
        if (state == EntityState.T_END_OF_STREAM  ||  currentStateMachine == null) {
            throw new IllegalStateException("No more tokens are available.");
        }
        while (currentStateMachine != null) {
            EntityStateMachine next = currentStateMachine.advance();
            if (next != null) {
                entities.add(next);
                currentStateMachine = next;
            }
            state = currentStateMachine.getState();
            if (state != EntityState.T_END_OF_STREAM) {
                return state;
            }
            entities.removeLast();
            if (entities.isEmpty()) {
                currentStateMachine = null;
            } else {
                currentStateMachine = entities.getLast();
                currentStateMachine.setRecursionMode(recursionMode);
            }
        }
        state = EntityState.T_END_OF_STREAM;
        return state;
    }

    /**
     * Renders a state as a string suitable for logging.
     * @param state
     * @return rendered as string, not null
     */
    public static String stateToString(EntityState state) {
        return MimeEntity.stateToString(state);
    }


    public MimeConfig getConfig() {
        return config;
    }
}
