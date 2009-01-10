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

package org.apache.james.mime4j.message;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.StorageProvider;

import java.io.IOException;
import java.io.InputStream;


/**
 * Represents a MIME message. The following code parses a stream into a 
 * <code>Message</code> object.
 * 
 * <pre>
 *      Message msg = new Message(new FileInputStream("mime.msg"));
 * </pre>
 * 
 *
 * 
 * @version $Id: Message.java,v 1.3 2004/10/02 12:41:11 ntherning Exp $
 */
public class Message extends Entity implements Body {
    
    /**
     * Creates a new empty <code>Message</code>.
     */
    public Message() {
    }

    /**
     * Creates a new <code>Message</code> from the specified
     * <code>Message</code>. The <code>Message</code> instance is initialized
     * with copies of header and body of the specified <code>Message</code>.
     * The parent entity of the new message is <code>null</code>.
     * 
     * @param other
     *            message to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link Message}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public Message(Message other) {
        super(other);
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance.
     * 
     * @param is the stream to parse.
     * @throws IOException on I/O errors.
     * @throws MimeIOException on MIME protocol violations.
     */
    public Message(InputStream is) throws IOException, MimeIOException {
        this(is, null, DefaultStorageProvider.getInstance());
    }
    
    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig}.
     * 
     * @param is the stream to parse.
     * @throws IOException on I/O errors.
     * @throws MimeIOException on MIME protocol violations.
     */
    public Message(InputStream is, MimeEntityConfig config) 
            throws IOException, MimeIOException {
        this(is, config, DefaultStorageProvider.getInstance());
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig} and {@link StorageProvider}.
     * 
     * @param is the stream to parse.
     * @param config {@link MimeEntityConfig} to use.
     * @param storageProvider {@link StorageProvider} to use for storing text
     *        and binary message bodies.
     * @throws IOException on I/O errors.
     * @throws MimeIOException on MIME protocol violations.
     */
     public Message(InputStream is, MimeEntityConfig config,
            StorageProvider storageProvider) throws IOException,
            MimeIOException {
        try {
            MimeStreamParser parser = new MimeStreamParser(config);
            parser.setContentHandler(new MessageBuilder(this, storageProvider));
            parser.parse(is);
        } catch (MimeException e) {
            throw new MimeIOException(e);
        }
    }
    
    /**
     * Gets the <code>Subject</code> field.
     * 
     * @return the <code>Subject</code> field or <code>null</code> if it
     *         doesn't exist.
     */
    public UnstructuredField getSubject() {
        return (UnstructuredField) getHeader().getField(Field.SUBJECT);
    }
    
}
