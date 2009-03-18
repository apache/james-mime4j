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

package org.apache.james.mime4j.parser;

import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.util.ContentUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class to run comparison of parsed results
 */
class TestHandler implements ContentHandler {
    StringBuilder sb = new StringBuilder();

    private String escape(char c) {
        if (c == '&') {
            return "&amp;";
        }
        if (c == '>') {
            return "&gt;";
        }
        if (c == '<') {
            return "&lt;";
        }
        return "" + c;
    }
    
    private String escape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("<", "&lt;");
        return s;
    }
    
    public void epilogue(InputStream is) throws IOException {
        sb.append("<epilogue>\r\n");
        int b = 0;
        while ((b = is.read()) != -1) {
            sb.append(escape((char) b));
        }
        sb.append("</epilogue>\r\n");
    }
    public void preamble(InputStream is) throws IOException {
        sb.append("<preamble>\r\n");
        int b = 0;
        while ((b = is.read()) != -1) {
            sb.append(escape((char) b));
        }
        sb.append("</preamble>\r\n");
    }
    public void startMultipart(BodyDescriptor bd) {
        sb.append("<multipart>\r\n");
    }
    public void body(BodyDescriptor bd, InputStream is) throws IOException {
        sb.append("<body>\r\n");
        int b = 0;
        while ((b = is.read()) != -1) {
            sb.append(escape((char) b));
        }
        sb.append("</body>\r\n");
    }
    public void endMultipart() {
        sb.append("</multipart>\r\n");
    }
    public void startBodyPart() {
        sb.append("<body-part>\r\n");
    }
    public void endBodyPart() {
        sb.append("</body-part>\r\n");
    }
    public void startHeader() {
        sb.append("<header>\r\n");
    }
    public void field(Field field) {
        sb.append("<field>\r\n"
                + escape(ContentUtil.decode(field.getRaw()))
                + "</field>\r\n");
    }
    public void endHeader() {
        sb.append("</header>\r\n");
    }
    public void startMessage() {
        sb.append("<message>\r\n");
    }
    public void endMessage() {
        sb.append("</message>\r\n");
    }

    public void raw(InputStream is) throws IOException {
        MimeStreamParserExampleMessagesTest.fail("raw should never be called");
    }
}