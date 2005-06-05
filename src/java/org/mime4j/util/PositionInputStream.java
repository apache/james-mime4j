package org.mime4j.util;

import java.io.InputStream;
import java.io.IOException;

public class PositionInputStream extends InputStream {

    private final InputStream inputStream;
    protected long position = 0;
    private long markedPosition = 0;

    public PositionInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public long getPosition() {
        return position;
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public int read() throws IOException {
        int b = inputStream.read();
        if (b != -1)
            position++;
        return b;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public void reset() throws IOException {
        inputStream.reset();
        position = markedPosition;
    }

    public boolean markSupported() {
        return inputStream.markSupported();
    }

    public void mark(int readlimit) {
        inputStream.mark(readlimit);
        markedPosition = position;
    }

    public long skip(long n) throws IOException {
        final long c = inputStream.skip(n);
        position += c;
        return c;
    }

    public int read(byte b[]) throws IOException {
        final int c = inputStream.read(b);
        position += c;
        return c;
    }

    public int read(byte b[], int off, int len) throws IOException {
        final int c = inputStream.read(b, off, len);
        position += c;
        return c;
    }

}
