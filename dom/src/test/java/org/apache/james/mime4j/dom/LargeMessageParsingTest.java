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

package org.apache.james.mime4j.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.io.MaxHeaderLengthLimitException;
import org.apache.james.mime4j.io.MaxHeaderLimitException;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.junit.Assert;
import org.junit.Test;

public class LargeMessageParsingTest {

    @Test
    public void parsingALargeMessageWithPermissiveConfigShouldSucceed() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100 * 1024 * 1024);
        // as many headers as the permissive profile allows
        for (int i = 0; i < MimeConfig.PERMISSIVE.getMaxHeaderCount(); i++) {
            outputStream.write("header: static important value\r\n".getBytes());
        }
        outputStream.write("\r\n".getBytes());
        // 38 * 1.600.000 = ~ 58 Mo of body
        for (int i = 0; i < 1600000; i++) {
            outputStream.write("abcdeghijklmnopqrstuvwxyz0123456789\r\n".getBytes());
        }

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);
        messageBuilder.parseMessage(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    @Test
    public void parsingAHeaderFloodWithPermissiveConfigShouldBeRejected() throws Exception {
        // A header field costs a handful of bytes on the wire but is retained as an
        // object graph by the DOM, so an unbounded header count lets a small message
        // exhaust the heap. MIME4J-269 introduced the permissive profile to be
        // "very permissive while still denying a single email to use all JVM
        // memory"; bounding the header count is part of that second half.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(32 * 1024 * 1024);
        for (int i = 0; i < 1000000; i++) {
            outputStream.write("header: static important value\r\n".getBytes());
        }
        outputStream.write("\r\n".getBytes());

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);
        try {
            messageBuilder.parseMessage(new ByteArrayInputStream(outputStream.toByteArray()));
            Assert.fail("MimeIOException expected");
        } catch (MimeIOException e) {
            Assert.assertTrue(e.getCause() instanceof MaxHeaderLimitException);
        }
    }

    @Test
    public void parsingAMessageWithLongLinesWithPermissiveConfigShouldSucceed() throws Exception {
        ByteArrayOutputStream longLineOutputStream = new ByteArrayOutputStream(1024 * 1024);
        ByteArrayOutputStream longHeaderOutputStream = new ByteArrayOutputStream(1024 * 1024);

        longHeaderOutputStream.write("header: ".getBytes());
        // Each header stays just under the permissive per header cap
        while (longHeaderOutputStream.size() < MimeConfig.PERMISSIVE.getMaxHeaderLen() - 32) {
            longHeaderOutputStream.write("0123456789".getBytes());
        }
        longHeaderOutputStream.write("\r\n".getBytes());

        // Each line is ~ 1Mo: long lines are still unbounded under the permissive profile
        for (int i = 0; i < 100 * 1024; i++) {
            longLineOutputStream.write("0123456789".getBytes());
        }
        longLineOutputStream.write("\r\n".getBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100 * 1024 * 1024);
        // as many ~64 Ko headers as the total header budget allows
        long headers = MimeConfig.PERMISSIVE.getMaxTotalHeaderLen() / longHeaderOutputStream.size();
        for (int i = 0; i < headers; i++) {
            outputStream.write(longHeaderOutputStream.toByteArray());
        }
        outputStream.write("\r\n".getBytes());
        // 60 * 1 = ~ 60 Mo of body
        for (int i = 0; i < 60; i++) {
            outputStream.write(longLineOutputStream.toByteArray());
        }

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);
        messageBuilder.parseMessage(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    @Test
    public void parsingAnOversizedHeaderWithPermissiveConfigShouldBeRejected() throws Exception {
        // An address, group or parameter list is retained as one object per item, so
        // a single unbounded header amplifies without limit. 500 Ko in one field used
        // to be accepted; MIME4J-269's "denying a single email to use all JVM memory"
        // covers this too.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 1024);
        outputStream.write("header: ".getBytes());
        for (int i = 0; i < 50 * 1024; i++) {
            outputStream.write("0123456789".getBytes());
        }
        outputStream.write("\r\n\r\nbody\r\n".getBytes());

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);
        try {
            messageBuilder.parseMessage(new ByteArrayInputStream(outputStream.toByteArray()));
            Assert.fail("MimeIOException expected");
        } catch (MimeIOException e) {
            Assert.assertTrue(e.getCause() instanceof MaxHeaderLengthLimitException);
        }
    }
}
