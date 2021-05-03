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

package org.apache.james.mime4j.parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RecursionMode;

/**
 * <p>
 * Parses MIME (or RFC822) message streams of bytes or characters and reports
 * parsing events to a {@link ContentHandler} instance.
 * </p>
 * <p>
 * Typical usage:<br>
 * <pre>
 *      ContentHandler handler = new MyHandler();
 *      MimeConfig config = new MimeConfig();
 *      MimeStreamParser parser = new MimeStreamParser(config);
 *      parser.setContentHandler(handler);
 *      InputStream instream = new FileInputStream("mime.msg");
 *      try {
 *          parser.parse(instream);
 *      } finally {
 *          instream.close();
 *      }
 * </pre>
 */
public class MimeStreamParser {

    private ContentHandler handler = null;
    private boolean contentDecoding;

    private final MimeTokenStream mimeTokenStream;

    public MimeStreamParser(MimeTokenStream tokenStream) {
        super();
        this.mimeTokenStream = tokenStream;
        this.contentDecoding = false;
    }

    public MimeStreamParser(
            final MimeConfig config,
            final DecodeMonitor monitor,
            final BodyDescriptorBuilder bodyDescBuilder) {
        this(new MimeTokenStream(config != null ? config : MimeConfig.DEFAULT,
                monitor, bodyDescBuilder));
    }

    public MimeStreamParser(final MimeConfig config) {
        this(config, null, null);
    }

    public MimeStreamParser() {
        this(new MimeTokenStream(MimeConfig.DEFAULT, null, null));
    }

    /**
     * Determines whether this parser automatically decodes body content
     * based on the on the MIME fields with the standard defaults.
     */
    public boolean isContentDecoding() {
        return contentDecoding;
    }

    /**
     * Defines whether parser should automatically decode body content
     * based on the on the MIME fields with the standard defaults.
     */
    public void setContentDecoding(boolean b) {
        this.contentDecoding = b;
    }

    /**
     * Parses a stream of bytes containing a MIME message. Please note that if the
     * {@link MimeConfig} associated with the mime stream returns a not null Content-Type
     * value from its {@link MimeConfig#getHeadlessParsing()} method, the message is
     * assumed to have no head section and the headless parsing mode will be used.
     *
     * @param instream the stream to parse.
     * @throws MimeException if the message can not be processed
     * @throws IOException on I/O errors.
     */
    public void parse(InputStream instream) throws MimeException, IOException {
        MimeConfig config = mimeTokenStream.getConfig();
        if (config.getHeadlessParsing() != null) {
            Field contentType = mimeTokenStream.parseHeadless(
                    instream, config.getHeadlessParsing());
            handler.startMessage();
            handler.startHeader();
            handler.field(contentType);
            handler.endHeader();
        } else {
            mimeTokenStream.parse(instream);
        }
        OUTER: for (;;) {
            EntityState state = mimeTokenStream.getState();
            switch (state) {
                case T_BODY:
                    BodyDescriptor desc = mimeTokenStream.getBodyDescriptor();
                    InputStream bodyContent;
                    if (contentDecoding) {
                        bodyContent = mimeTokenStream.getDecodedInputStream();
                    } else {
                        bodyContent = mimeTokenStream.getInputStream();
                    }
                    handler.body(desc, bodyContent);
                    break;
                case T_END_BODYPART:
                    handler.endBodyPart();
                    break;
                case T_END_HEADER:
                    handler.endHeader();
                    break;
                case T_END_MESSAGE:
                    handler.endMessage();
                    break;
                case T_END_MULTIPART:
                    handler.endMultipart();
                    break;
                case T_END_OF_STREAM:
                    break OUTER;
                case T_EPILOGUE:
                    handler.epilogue(mimeTokenStream.getInputStream());
                    break;
                case T_FIELD:
                    handler.field(mimeTokenStream.getField());
                    break;
                case T_PREAMBLE:
                    handler.preamble(mimeTokenStream.getInputStream());
                    break;
                case T_RAW_ENTITY:
                    handler.raw(mimeTokenStream.getInputStream());
                    break;
                case T_START_BODYPART:
                    handler.startBodyPart();
                    break;
                case T_START_HEADER:
                    handler.startHeader();
                    break;
                case T_START_MESSAGE:
                    handler.startMessage();
                    break;
                case T_START_MULTIPART:
                    handler.startMultipart(mimeTokenStream.getBodyDescriptor());
                    break;
                default:
                    throw new IllegalStateException("Invalid state: " + state);
            }
            state = mimeTokenStream.next();
        }
    }

    /**
     * Determines if this parser is currently in raw mode.
     *
     * @return <code>true</code> if in raw mode, <code>false</code>
     *         otherwise.
     * @see #setRaw()
     */
    public boolean isRaw() {
        return mimeTokenStream.isRaw();
    }

    /**
     * Enables raw mode. In raw mode all future entities (messages
     * or body parts) in the stream will be reported to the
     * {@link ContentHandler#raw(InputStream)} handler method only.
     * The stream will contain the entire unparsed entity contents
     * including header fields and whatever is in the body.
     */
    public void setRaw() {
        mimeTokenStream.setRecursionMode(RecursionMode.M_RAW);
    }

    /**
     * Enables flat mode. In flat mode rfc822 parts are not recursively
     * parsed and multipart content is handled as a single "simple" stream.
     */
    public void setFlat() {
        mimeTokenStream.setRecursionMode(RecursionMode.M_FLAT);
    }

    /**
     * Enables recursive mode. In this mode rfc822 parts are recursively
     * parsed.
     */
    public void setRecurse() {
        mimeTokenStream.setRecursionMode(RecursionMode.M_RECURSE);
    }

    /**
     * Disables recursive mode. In this mode rfc822 parts are not
     * recursively parsed.
     */
    public void setNoRecurse() {
        mimeTokenStream.setRecursionMode(RecursionMode.M_NO_RECURSE);
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
        mimeTokenStream.stop();
    }

    /**
     * Sets the <code>ContentHandler</code> to use when reporting
     * parsing events.
     *
     * @param h the <code>ContentHandler</code>.
     */
    public void setContentHandler(ContentHandler h) {
        this.handler = h;
    }

}
