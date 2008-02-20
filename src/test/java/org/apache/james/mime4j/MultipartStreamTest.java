package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public class MultipartStreamTest extends TestCase {

    private static final Charset US_ASCII = Charset.forName("us-ascii");
    
    private static final String BODY = "A Preamble\r\n" +
                "--1729\r\n\r\n" +
                "Simple plain text\r\n" +
                "--1729\r\n" +
                "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
                "Some more text\r\n" +
                "--1729--\r\n";
    public static final String MESSAGE = "To: Road Runner <runner@example.org>\r\n" +
            "From: Wile E. Cayote <wile@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Mail\r\n" +
            "Content-Type: multipart/mixed;boundary=1729\r\n\r\n" +
            BODY;
    
    public static final String COMPLEX_MESSAGE = "To: Wile E. Cayote <wile@example.org>\r\n" +
    "From: Road Runner <runner@example.org>\r\n" +
    "Date: Tue, 19 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
    "Subject: Mail\r\n" +
    "Content-Type: multipart/mixed;boundary=42\r\n\r\n" +
    "A little preamble\r\n" +
    "--42\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
    "Rhubard!\r\n" +
    "--42\r\n" +
    "Content-Type: message/rfc822\r\n\r\n" +
    MESSAGE +
    "\r\n" +
    "--42\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
    "Custard!" +
    "\r\n" +
    "--42--\r\n";
    
    MimeTokenStream parser;
    
    protected void setUp() throws Exception {
        super.setUp();
        parser = new MimeTokenStream();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testShouldSupplyInputStreamForSimpleBody() throws Exception {
        parser.parse(new ByteArrayInputStream(US_ASCII.encode(MESSAGE).array()));
        checkState(MimeTokenStream.T_START_HEADER);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_END_HEADER);
        checkState(MimeTokenStream.T_START_MULTIPART);
        InputStream out = parser.getInputStream();
        assertEquals(BODY, IOUtils.toString(out, "us-ascii"));
        checkState(MimeTokenStream.T_END_MULTIPART);
    }
    
    public void testInputStreamShouldReadOnlyMessage() throws Exception {
        parser.parse(new ByteArrayInputStream(US_ASCII.encode(COMPLEX_MESSAGE).array()));
        checkState(MimeTokenStream.T_START_HEADER);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_END_HEADER);
        checkState(MimeTokenStream.T_START_MULTIPART);
        checkState(MimeTokenStream.T_PREAMBLE);
        checkState(MimeTokenStream.T_START_BODYPART);
        checkState(MimeTokenStream.T_START_HEADER);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_END_HEADER);
        checkState(MimeTokenStream.T_BODY);
        checkState(MimeTokenStream.T_END_BODYPART);
        checkState(MimeTokenStream.T_START_BODYPART);
        checkState(MimeTokenStream.T_START_HEADER);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_END_HEADER);
        checkState(MimeTokenStream.T_START_MESSAGE);
        checkState(MimeTokenStream.T_START_HEADER);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_END_HEADER);
        checkState(MimeTokenStream.T_START_MULTIPART);
        InputStream out = parser.getInputStream();
        assertEquals(BODY, IOUtils.toString(out, "us-ascii"));
        checkState(MimeTokenStream.T_END_MULTIPART);
        checkState(MimeTokenStream.T_END_MESSAGE);
        //TODO: 
        //checkState(MimeTokenStream.T_END_BODYPART);
        checkState(MimeTokenStream.T_START_BODYPART);
        checkState(MimeTokenStream.T_START_HEADER);
        checkState(MimeTokenStream.T_FIELD);
        checkState(MimeTokenStream.T_END_HEADER);
        checkState(MimeTokenStream.T_BODY);
        checkState(MimeTokenStream.T_END_BODYPART);
        checkState(MimeTokenStream.T_EPILOGUE);
        checkState(MimeTokenStream.T_END_MULTIPART);
        checkState(MimeTokenStream.T_END_MESSAGE);
        checkState(MimeTokenStream.T_END_OF_STREAM);
    }

    private void checkState(final int state) throws IOException, MimeException {
        assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(parser.next()));
    }
}
