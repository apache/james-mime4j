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

package org.apache.james.mime4j.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class LimitedInputStreamTest extends TestCase {

    public void testUpToLimitRead() throws IOException {
        byte[] data = new byte[] {'0', '1', '2', '3', '4', '5', '6'};
        ByteArrayInputStream instream = new ByteArrayInputStream(data);
        LimitedInputStream limitedStream = new LimitedInputStream(instream, 3);
        assertEquals(0, limitedStream.getPosition());
        assertTrue(limitedStream.read() != -1);
        assertEquals(1, limitedStream.getPosition());
        byte[] tmp = new byte[3];
        assertEquals(2, limitedStream.read(tmp));
        assertEquals(3, limitedStream.getPosition());
        try {
            limitedStream.read();
            fail("IOException should have been thrown");
        } catch (IOException ex) {
        }
        try {
            limitedStream.read(tmp);
            fail("IOException should have been thrown");
        } catch (IOException ex) {
        }
        try {
            limitedStream.skip(2);
            fail("IOException should have been thrown");
        } catch (IOException ex) {
        }
    }
    
}
