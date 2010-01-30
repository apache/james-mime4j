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

import java.io.InputStream;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.stream.BasicMimeTokenStream;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.DefaultBodyDescriptor;
import org.apache.james.mime4j.stream.MimeEntityConfig;
import org.apache.james.mime4j.stream.MutableBodyDescriptor;

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
 *      stream.parse(new FileInputStream("mime.msg"));
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
 * <p>Instances of {@link MimeTokenStream} are reusable: Invoking the
 * method {@link #parse(InputStream)} resets the token streams internal
 * state. However, they are definitely <em>not</em> thread safe. If you
 * have a multi threaded application, then the suggested use is to have
 * one instance per thread.</p>
 */
public class MimeTokenStream extends BasicMimeTokenStream {
    
    /**
     * Creates a stream that creates a more detailed body descriptor.
     * @return <code>MimeTokenStream</code>, not null
     */
    public static final MimeTokenStream createMaximalDescriptorStream() {
        MimeEntityConfig config = new MimeEntityConfig();
        config.setMaximalBodyDescriptor(true);
        return new MimeTokenStream(config);
    }
    
    /**
     * Creates a stream that strictly validates the input.
     * @return <code>MimeTokenStream</code> which throws a 
     * <code>MimeException</code> whenever possible issues 
     * are dedicated in the input
     */
    public static final MimeTokenStream createStrictValidationStream() {
        MimeEntityConfig config = new MimeEntityConfig();
        config.setStrictParsing(true);
        return new MimeTokenStream(config);
    }
    
    /**
     * Constructs a standard (lax) stream.
     * Optional validation events will be logged only.
     * Use {@link #createStrictValidationStream()} to create
     * a stream that strictly validates the input.
     */
    public MimeTokenStream() {
        this(new MimeEntityConfig());
    }
    
    public MimeTokenStream(final MimeEntityConfig config) {
        this(config, null);
    }
    
    public MimeTokenStream(final MimeEntityConfig config, DecodeMonitor monitor) {
        super(config, monitor);
    }

    /**
     * Creates a new instance of {@link BodyDescriptor}. Subclasses may override
     * this in order to create body descriptors, that provide more specific
     * information.
     */
    protected MutableBodyDescriptor newBodyDescriptor() {
        final MutableBodyDescriptor result;
        if (getConfig().isMaximalBodyDescriptor()) {
            result = new MaximalBodyDescriptor(null);
        } else {
            result = new DefaultBodyDescriptor(null);
        }
        return result;
    }

}
