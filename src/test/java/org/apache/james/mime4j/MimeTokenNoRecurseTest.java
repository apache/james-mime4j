package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import junit.framework.TestCase;

public class MimeTokenNoRecurseTest extends TestCase {

    private static final String INNER_MAIL = "From: Timothy Tayler <tim@example.org>\r\n" +
                "To: Joshua Tetley <joshua@example.org>\r\n" +
                "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
                "Subject: Multipart Without RFC822 Part\r\n" +
                "Content-Type: multipart/mixed;boundary=42\r\n\r\n" +
                "--42\r\n" +
                "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
                "First part of this mail\r\n" +
                "--42\r\n" +
                "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
                "Second part of this mail\r\n" +
                "--42\r\n";

    private static final String MAIL_WITH_RFC822_PART = "MIME-Version: 1.0\r\n" +
            "From: Timothy Tayler <tim@example.org>\r\n" +
            "To: Joshua Tetley <joshua@example.org>\r\n" +
            "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
            "Subject: Multipart With RFC822 Part\r\n" +
            "Content-Type: multipart/mixed;boundary=1729\r\n\r\n" +
            "A short premable\r\n" +
            "--1729\r\n\r\n" +
            "First part has no headers\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
            "Second part is plain text\r\n" +
            "--1729\r\n" +
            "Content-Type: message/rfc822\r\n\r\n" +
            INNER_MAIL +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
            "Last part is plain text\r\n" +
            "--1729--\r\n" +
            "The End";
    
    MimeTokenStream stream;
    
    protected void setUp() throws Exception {
        super.setUp();
        stream = new MimeTokenStream();
        byte[] bytes = Charset.forName("us-ascii").encode(MAIL_WITH_RFC822_PART).array();
        InputStream in = new ByteArrayInputStream(bytes);
        stream.parse(in);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWhenRecurseShouldRecurseInnerMail() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_RECURSE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(false);
        
        nextShouldBeStandardPart(true);
        
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MESSAGE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
        nextIs(MimeTokenStream.T_END_MESSAGE);
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
    }
    

    public void testWhenRecurseShouldTreatInnerMailAsAnyOtherPart() throws Exception {
        stream.setRecursionMode(MimeTokenStream.M_NO_RECURSE);
        nextIs(MimeTokenStream.T_START_HEADER);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_FIELD);
        nextIs(MimeTokenStream.T_END_HEADER);
        
        nextIs(MimeTokenStream.T_START_MULTIPART);
        nextIs(MimeTokenStream.T_PREAMBLE);
        nextShouldBeStandardPart(false);
        
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextShouldBeStandardPart(true);
        nextIs(MimeTokenStream.T_EPILOGUE);
        nextIs(MimeTokenStream.T_END_MULTIPART);
    }

    private void nextShouldBeStandardPart(boolean withHeader) throws Exception {
        nextIs(MimeTokenStream.T_START_BODYPART);
        nextIs(MimeTokenStream.T_START_HEADER);
        if (withHeader) {
            nextIs(MimeTokenStream.T_FIELD);
        }
        nextIs(MimeTokenStream.T_END_HEADER);
        nextIs(MimeTokenStream.T_BODY);
        nextIs(MimeTokenStream.T_END_BODYPART);
    }
    
    private void nextIs(int state) throws Exception {
        assertEquals(MimeTokenStream.stateToString(state), MimeTokenStream.stateToString(stream.next()));
    }
}
