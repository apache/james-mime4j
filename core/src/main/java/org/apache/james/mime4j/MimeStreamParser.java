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
public interface MimeStreamParser {

    /**
     * Determines whether this parser automatically decodes body content
     * based on the on the MIME fields with the standard defaults.
     */
    boolean isContentDecoding();

    /**
     * Defines whether parser should automatically decode body content
     * based on the on the MIME fields with the standard defaults.
     */
    void setContentDecoding(boolean b);

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
    void parse(InputStream instream) throws MimeException, IOException;

    /**
     * Determines if this parser is currently in raw mode.
     *
     * @return <code>true</code> if in raw mode, <code>false</code>
     *         otherwise.
     * @see #setRaw()
     */
    boolean isRaw();

    /**
     * Enables raw mode. In raw mode all future entities (messages
     * or body parts) in the stream will be reported to the
     * {@link ContentHandler#raw(InputStream)} handler method only.
     * The stream will contain the entire unparsed entity contents
     * including header fields and whatever is in the body.
     */
    void setRaw();

    /**
     * Enables flat mode. In flat mode rfc822 parts are not recursively
     * parsed and multipart content is handled as a single "simple" stream.
     */
    void setFlat();

    /**
     * Enables recursive mode. In this mode rfc822 parts are recursively
     * parsed.
     */
    void setRecurse();

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
    void stop();

    /**
     * Sets the <code>ContentHandler</code> to use when reporting
     * parsing events.
     *
     * @param h the <code>ContentHandler</code>.
     */
    void setContentHandler(ContentHandler h);
}
