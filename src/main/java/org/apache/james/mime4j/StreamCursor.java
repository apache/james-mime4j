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

    protected final InputBuffer buffer;
    protected final BufferingInputStream bufferingInputStream;
    protected final RootInputStream rootInputStream;
    
    protected MimeBoundaryInputStream mbis;
    protected InputStream contentStream;
    
    /**
     * Constructs a child cursor.
     * @param parent <code>Cursor</code>, not null 
     */
    StreamCursor(StreamCursor parent) {
        this.buffer = parent.buffer;
        this.bufferingInputStream = parent.bufferingInputStream;
        this.rootInputStream = parent.rootInputStream;
    }
    
    /**
     * Constructs a new cursor from the given root contents.
     * @param stream <code>InputStream</code>, not null
     */
    StreamCursor(InputStream stream) {
        this.buffer = new InputBuffer(stream, 1024 * 4);
        this.bufferingInputStream = new BufferingInputStream(this.buffer);
        this.rootInputStream = new RootInputStream(this.bufferingInputStream);
        this.contentStream = this.rootInputStream;
    }
    
    /**
     * Constructs a cursor from the previous cursor.
     * @param previous <code>StreamCursor</code>, not null
     * @param contentStream <code>InputStream</code>, not null
     */
    StreamCursor(StreamCursor previous, InputStream contentStream) {
        this.buffer = previous.buffer;
        this.bufferingInputStream = previous.bufferingInputStream;
        this.rootInputStream = previous.rootInputStream;
        this.contentStream = contentStream;
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
        return new StreamCursor(this, 
                new EOLConvertingInputStream(new Base64InputStream(mbis)));
    }
    
    /**
     * @see Cursor#decodeQuotedPrintable()
     */
    public Cursor decodeQuotedPrintable() throws IOException {
        return new StreamCursor(this, 
                new EOLConvertingInputStream(new QuotedPrintableInputStream(mbis)));
    }
    
    /**
     * @see Cursor#advanceToBoundary()
     */
    public void advanceToBoundary() throws IOException {
        byte[] tmp = new byte[2048];
        while (mbis.read(tmp)!= -1) {
        }
    }

    /**
     * @see Cursor#isEnded()
     */
    public boolean isEnded() throws IOException {
        return mbis.eof();
    }

    /**
     * @see Cursor#moreMimeParts()
     */
    public boolean moreMimeParts() throws IOException {
        return !mbis.isLastPart();
    }

    /**
     * @see Cursor#boundary(String)
     */
    public void boundary(String boundary) throws IOException {
        mbis = new MimeBoundaryInputStream(buffer, boundary);
        contentStream = new CloseShieldInputStream(mbis);
    }

    /**
     * @see Cursor#nextMimePartCursor()
     */
    public Cursor nextMimePartCursor() {
        return new StreamCursor(this, mbis);
    }

    /**
     * @see Cursor#nextSection()
     */
    public InputStream nextSection() {
        return contentStream;
    }

    /**
     * @see Cursor#advance()
     */
    public byte advance() throws IOException {
        return (byte) nextSection().read();
    }

    public InputStream rest() {
        return contentStream;
    }

    public InputStream root() {
        return rootInputStream;
    }
    
}
