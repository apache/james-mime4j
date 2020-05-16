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

import org.apache.james.mime4j.MimeConfig;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.junit.Test;

public class LargeMessageParsingTest {

    @Test
    public void parsingALargeMessageWithPermissiveConfigShouldSucceed() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100 * 1024 * 1024);
        // 32 * 1.000.000 = ~ 30,5 Mo of headers
        for (int i = 0; i < 1000000; i++) {
            outputStream.write(String.format("header: static important value\r\n", i, i).getBytes());
        }
        outputStream.write("\r\n".getBytes());
        // 38 * 1.600.000 = ~ 58 Mo of body
        for (int i = 0; i < 1600000; i++) {
            outputStream.write(String.format("abcdeghijklmnopqrstuvwxyz0123456789\r\n", i, i).getBytes());
        }

        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);
        messageBuilder.parseMessage(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    @Test
    public void parsingAMessageWithLongLinesWithPermissiveConfigShouldSucceed() throws Exception {
        ByteArrayOutputStream longLineOutputStream = new ByteArrayOutputStream( 1024 * 1024);
        ByteArrayOutputStream longHeaderOutputStream = new ByteArrayOutputStream( 1024 * 1024);

        longHeaderOutputStream.write("header: ".getBytes());
        // Each header is ~ 500 Ko
        for (int i = 0; i < 50 * 1024; i++) {
            longHeaderOutputStream.write("0123456789".getBytes());
        }
        longHeaderOutputStream.write("\r\n".getBytes());

        // Each line is ~ 1Mo
        for (int i = 0; i < 100 * 1024; i++) {
            longLineOutputStream.write("0123456789".getBytes());
        }
        longLineOutputStream.write("\r\n".getBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100 * 1024 * 1024);
        // 60 * 0.5 = ~ 30 Mo of headers
        for (int i = 0; i < 60; i++) {
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
}
