package org.mime4j.util;

import java.io.InputStream;
import java.io.IOException;

public class PartialInputStream extends PositionInputStream {
    private final long limit;

    public PartialInputStream(InputStream inputStream, long offset, long length) throws IOException {
        super(inputStream);
        inputStream.skip(offset);
        this.limit = offset + length;
    }

    public int available() throws IOException {
        return Math.min(super.available(), getBytesLeft());
    }

    public int read() throws IOException {
        if (limit > position)
            return super.read();
        else
            return -1;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        len = Math.min(len, getBytesLeft());
        return super.read(b, off, len);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public long skip(long n) throws IOException {
        n = Math.min(n, getBytesLeft());
        return super.skip(n);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private int getBytesLeft() {
        return (int)Math.min(Integer.MAX_VALUE, limit - position);
    }
}
