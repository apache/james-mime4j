package org.apache.james.mime4j.field.contentdisposition;

import junit.framework.TestCase;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.field.contentdisposition.parser.ParseException;

public class ContentDispositionTest extends TestCase {

    public void testExceptionTree() {
        // make sure that our ParseException extends MimeException.
        assertTrue(MimeException.class.isAssignableFrom(ParseException.class));
    }
    
}
