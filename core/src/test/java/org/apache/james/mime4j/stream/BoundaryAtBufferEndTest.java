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

package org.apache.james.mime4j.stream;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BoundaryAtBufferEndTest {
    private static final String NOT_A_DELIMITER = "Content-Type: multipart/mixed; boundary=\"foo\"\r\n\r\n"
        + "preamble\r\n--foo-x\r\nContent-Type: text/plain\r\n\r\nthis is not a body part\r\n--foo--\r\n";
    private static final String TWO_PARTS = "Content-Type: multipart/mixed; boundary=\"foo\"\r\n\r\n"
        + "preamble\r\n--foo\r\n\r\nfirst\r\n--foo \r\n\r\nsecond\r\n--foo--\r\nepilogue\r\n";
    private static final String ENDS_WITH_DELIMITER = "Content-Type: multipart/mixed; boundary=\"foo\"\r\n\r\n"
        + "--foo\r\n\r\nfirst\r\n--foo";
    private static final String NESTED = "Content-Type: multipart/mixed; boundary=\"foo\"\r\n\r\n"
        + "--foo\r\nContent-Type: multipart/mixed; boundary=\"bar\"\r\n\r\n"
        + "--bar\r\n\r\ninner\r\n--bar--\r\ninner epilogue\r\n--foo--\r\nouter epilogue\r\n";

    private static List<String> events(InputStream in) throws Exception {
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(in);
        List<String> out = new ArrayList<>();
        for (EntityState s = stream.getState(); s != EntityState.T_END_OF_STREAM; s = stream.next()) {
            if (s == EntityState.T_BODY || s == EntityState.T_PREAMBLE || s == EntityState.T_EPILOGUE) {
                out.add(s + " " + new String(org.apache.commons.io.IOUtils.toByteArray(stream.getInputStream()), StandardCharsets.US_ASCII));
            } else {
                out.add(s.toString());
            }
        }
        return out;
    }

    private static InputStream oneByteAtATime(byte[] b) {
        return new FilterInputStream(new ByteArrayInputStream(b)) {
            @Override
            public int read(byte[] buf, int off, int len) throws IOException {
                return super.read(buf, off, Math.min(len, 1));
            }
        };
    }

    private static void assertSameEventsWhateverTheReadSizes(String message) throws Exception {
        byte[] bytes = message.getBytes(StandardCharsets.US_ASCII);
        Assert.assertEquals(events(new ByteArrayInputStream(bytes)), events(oneByteAtATime(bytes)));
    }

    @Test
    public void aBoundaryPrefixAtTheEndOfTheBufferShouldNotBeTakenAsADelimiter() throws Exception {
        assertSameEventsWhateverTheReadSizes(NOT_A_DELIMITER);
    }

    @Test
    public void delimitersShouldBeFoundWhateverTheReadSizes() throws Exception {
        assertSameEventsWhateverTheReadSizes(TWO_PARTS);
    }

    @Test
    public void aDelimiterEndingTheStreamShouldBeFoundWhateverTheReadSizes() throws Exception {
        assertSameEventsWhateverTheReadSizes(ENDS_WITH_DELIMITER);
    }

    @Test
    public void nestedMultipartsShouldBeParsedTheSameWhateverTheReadSizes() throws Exception {
        assertSameEventsWhateverTheReadSizes(NESTED);
    }
}
