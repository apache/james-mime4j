package org.apache.james.mime4j.dom;

import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

/**
 * This class has a test that replicates the issue described in MIME4J-327. Namely, when a recipient name contains
 * Korean characters, and the value is _not_ base64-encoded, it is decoded incorrectly.
 */
public class KoreanCharsDecodeTest {
    // This test passes as the value is base64-encoded.
    @Test
    public void testKoreanCharsDecodeBase64Encoded() throws Exception {
        String sb = "From: foo@bar.com\r\n" +
                "To: =?UTF-8?B?7Iuc7ZeY?= <koreantest@example.com>\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "<div>foo bar</div>\r\n";

        Message parsed = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(sb.getBytes()));
        String to = ((Mailbox) parsed.getTo().get(0)).getName();
        assertEquals("시험", to);
    }

    // This test fails as the value is not base64-encoded.
    @Test
    public void testKoreanCharsDecodeNotBase64Encoded() throws Exception {
        String sb = "From: foo@bar.com\r\n" +
                "To: \"시험\" <koreantest@example.com>\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "<div>foo bar</div>\r\n";

        Message parsed = new DefaultMessageBuilder().parseMessage(new ByteArrayInputStream(sb.getBytes()));
        String to = ((Mailbox) parsed.getTo().get(0)).getName();
        assertEquals("시험", to);
    }
}
