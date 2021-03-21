package org.apache.james.mime4j.mboxiterator;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteBufferInputStreamTest {
    private InputStream createTestUtf8Stream() {
        return new CharBufferWrapper(CharBuffer.wrap("ABCDE§")).asInputStream(StandardCharsets.UTF_8);
    }

    @Test
    public void testSingleRead() throws IOException {
        InputStream stream = createTestUtf8Stream();
        Assert.assertEquals(0x41, stream.read());
        Assert.assertEquals(0x42, stream.read());
        Assert.assertEquals(0x43, stream.read());
        Assert.assertEquals(0x44, stream.read());
        Assert.assertEquals(0x45, stream.read());
        Assert.assertEquals(0xC2, stream.read());
        Assert.assertEquals(0xA7, stream.read());
        Assert.assertEquals(-1, stream.read());
    }

    @Test
    public void testBulkRead() throws IOException {
        InputStream stream = createTestUtf8Stream();

        {
            byte[] byteArr = new byte[3];
            int bytesRead = stream.read(byteArr);
            Assert.assertEquals(3, bytesRead);
            Assert.assertArrayEquals(new byte[]{ 0x41, 0x42, 0x43 }, byteArr);
        }

        {
            byte[] byteArr = new byte[5];
            Arrays.fill(byteArr, (byte) -1);

            int bytesRead = stream.read(byteArr);
            Assert.assertEquals(4, bytesRead);
            Assert.assertArrayEquals(new byte[]{ 0x44, 0x45, (byte) 0xC2, (byte) 0xA7, (byte) - 1 }, byteArr);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArray() throws IOException {
        createTestUtf8Stream().read(null, 0, 12);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeOffset() throws IOException {
        createTestUtf8Stream().read(new byte[12], -12, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeLength() throws IOException {
        createTestUtf8Stream().read(new byte[12], 0, -12);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testLongLength() throws IOException {
        createTestUtf8Stream().read(new byte[12], 4, 13);
    }
}
