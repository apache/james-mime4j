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

import org.apache.james.mime4j.decoder.Base64InputStream;
import org.apache.james.mime4j.decoder.QuotedPrintableInputStream;

/**
 * Stream based cursor. 
 */
public class StreamCursor implements Cursor {

    final RootInputStream rootInputStream;
    final InputStream contents;
    MimeBoundaryInputStream mbis;
    InputStream stream;
    
    /**
     * Constructs a child cursor.
     * @param parent <code>Cursor</code>, not null 
     * @param contents <code>InputStream</code> contents, not null
     */
    StreamCursor(StreamCursor parent, InputStream contents) {
        this.rootInputStream = parent.rootInputStream;
        this.contents = contents;
    }
    
    /**
     * Constructs a new cursor from the given root contents.
     * @param stream <code>InputStream</code>, not null
     */
    StreamCursor(InputStream stream) {
        rootInputStream = new RootInputStream(stream);
        contents = rootInputStream;
    }
    
    /**
     * Constructs a cursor with the given root and contents.
     * @param rootInputStream <code>RootInputStream</code>, not null
     * @param contents <code>InputStream</code>, not null
     */
    StreamCursor(RootInputStream rootInputStream, InputStream contents) {
        this.rootInputStream = rootInputStream;
        this.contents = contents;
    }
    
    /**
     * @see Cursor#stop()
     */
    public void stop() {
        rootInputStream.truncate();
    }
    
    /**
     * @see Cursor#getLineNumber()
     */
    public int getLineNumber() {
        return rootInputStream.getLineNumber();
    }

    /**
     * @see Cursor#decodeBase64()
     */
    public Cursor decodeBase64() throws IOException {
        InputStream is = new EOLConvertingInputStream(new Base64InputStream(contents));
        return new StreamCursor(rootInputStream, is);
    }
    
    /**
     * @see Cursor#decodeQuotedPrintable()
     */
    public Cursor decodeQuotedPrintable() throws IOException {
        InputStream is = new EOLConvertingInputStream(new QuotedPrintableInputStream(contents));
        return new StreamCursor(rootInputStream, is);
    }
    
    /**
     * @see Cursor#advanceToBoundary()
     */
    public void advanceToBoundary() throws IOException {
        mbis.consume();
        stream = null;
    }

    /**
     * @see Cursor#isEnded()
     */
    public boolean isEnded() throws IOException {
        return mbis.parentEOF();
    }

    /**
     * @see Cursor#moreMimeParts()
     */
    public boolean moreMimeParts() throws IOException {
        return mbis.hasMoreParts();
    }

    /**
     * @see Cursor#boundary(String)
     */
    public void boundary(String boundary) throws IOException {
        mbis = new MimeBoundaryInputStream(contents, boundary);
        stream = new CloseShieldInputStream(mbis);
    }

    /**
     * @see Cursor#nextMimePartCursor()
     */
    public Cursor nextMimePartCursor() {
        return new StreamCursor(rootInputStream, mbis);
    }

    /**
     * @see Cursor#nextSection()
     */
    public InputStream nextSection() {
        InputStream result = stream;
        if (result == null)
        {
            result = contents;
        }
        return result;
    }

    /**
     * @see Cursor#advance()
     */
    public byte advance() throws IOException {
        return (byte) nextSection().read();
    }

    public InputStream rest() {
        return contents;
    }
    
    
}
