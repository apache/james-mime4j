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
 * Manages parser input.
 */
public interface Cursor {
    
    /**
     * Stops processing.
     */
    public void stop();

    /**
     * Gets the number of CR LF sequences passed.
     * @return number of lines passed
     * @throws IOException
     */
    public int getLineNumber() throws IOException;

    /**
     * Decodes the next MIME part as BASE 64 
     * and returns a cursor for the contents.
     * @return <code>Cursor</code>, not null
     * @throws IOException TODO: subclass IOException with state exception
     */
    public Cursor decodeBase64() throws IOException;

    /**
     * Decodes the next MIME part as Quoted Printable
     * and returns a cursor for the contents.
     * @return <code>Cursor</code>, not null
     * @throws IOException TODO: subclass IOException with state exception
     */
    public Cursor decodeQuotedPrintable() throws IOException;

    /**
     * Advances the cursor to the next boundary.
     * @throws IOException TODO: subclass IOException with state exception
     */
    public void advanceToBoundary() throws IOException;

    /**
     * Is this cursor at the end of the input?
     * @return true if the input is at the end,
     * false otherwise
     * @throws IOException
     */
    public boolean isEnded() throws IOException;

    /**
     * Are more parts available?
     * @return true if this cursor is reading a MIME message and 
     * there are more parts available,
     * false otherwise
     * @throws IOException TODO: subclass IOException with state exception
     */
    public boolean moreMimeParts() throws IOException;

    /**
     * Sets the MIME boundary.
     * 
     * @param boundary TODO: should this be a byte sequence?
     * @throws IOException
     */
    public void boundary(String boundary) throws IOException;

    /**
     * Gets a cursor for the next mime part.
     * @return <code>Cursor</code>, not null
     * @throws IOException TODO: subclass IOException with state exception
     */
    public Cursor nextMimePartCursor() throws IOException;

    /**
     * Advances the cursor.
     * @return the next byte
     * @throws IOException
     * @see #nextSection()
     */
    public byte advance() throws IOException;
    
    /**
     * Gets a stream to read the contents of the next section of the message.
     * @return <code>InputStream</code>, not null
     */
    public InputStream nextSection();

    /**
     * Gets a stream to read the contents of the rest of this message.
     * @return <code>InputStream</code>, not null
     */
    public InputStream rest();

    /**
     * Gets root stream for this message.
     * @return <code>InputStream</code>, not null
     */
    public InputStream root();

}
