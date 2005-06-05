/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mime4j;

import java.io.InputStream;
import java.io.IOException;

/**
 * InputStream that shields its underlying input stream from
 * being closed.
 * 
 * @author Joe Cheng
 * @version $Id: CloseShieldInputStream.java,v 1.2 2004/10/02 12:41:10 ntherning Exp $
 */
public class CloseShieldInputStream extends InputStream {

    /**
     * Underlying InputStream
     */
    private InputStream is;

    public CloseShieldInputStream(InputStream is) {
        this.is = is;
    }

    public InputStream getUnderlyingStream() {
        return is;
    }

    public int read() throws IOException {
        checkIfClosed();
        return is.read();
    }

    public int available() throws IOException {
        checkIfClosed();
        return is.available();
    }

    public void close() throws IOException {
        is = null;
    }

    public synchronized void reset() throws IOException {
        checkIfClosed();
        is.reset();
    }

    public boolean markSupported() {
        if (is == null)
            return false;
        return is.markSupported();
    }

    public synchronized void mark(int readlimit) {
        if (is != null)
            is.mark(readlimit);
    }

    public long skip(long n) throws IOException {
        checkIfClosed();
        return is.skip(n);
    }

    public int read(byte b[]) throws IOException {
        checkIfClosed();
        return is.read(b);
    }

    public int read(byte b[], int off, int len) throws IOException {
        checkIfClosed();
        return is.read(b, off, len);
    }

    private void checkIfClosed() throws IOException {
        if (is == null)
            throw new IOException("Stream is closed");
    }
}
