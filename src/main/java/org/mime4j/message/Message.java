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

package org.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import org.mime4j.BodyDescriptor;
import org.mime4j.ContentHandler;
import org.mime4j.MimeStreamParser;
import org.mime4j.decoder.Base64InputStream;
import org.mime4j.decoder.QuotedPrintableInputStream;
import org.mime4j.field.Field;
import org.mime4j.field.UnstructuredField;


/**
 * Represents a MIME message. The following code parses a stream into a 
 * <code>Message</code> object.
 * 
 * <pre>
 *      Message msg = new Message(new BufferedInputStream(
 *                                      new FileInputStream("mime.msg")));
 * </pre>
 * 
 *
 * @author Niklas Therning
 * @version $Id: Message.java,v 1.3 2004/10/02 12:41:11 ntherning Exp $
 */
public class Message extends Entity implements Body {
    
    /**
     * Creates a new empty <code>Message</code>.
     */
    public Message() {
    }
    
    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance.
     * 
     * @param is the stream to parse.
     * @throws IOException on I/O errors.
     */
    public Message(InputStream is) throws IOException {
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new MessageBuilder());
        parser.parse(is);
    }
    
    /**
     * Gets the <code>Subject</code> field.
     * 
     * @return the <code>Subject</code> field or <code>null</code> if it
     *         doesn't exist.
     */
    public UnstructuredField getSubject() {
        return (UnstructuredField) getHeader().getField(Field.SUBJECT);
    }

    public void writeTo(OutputStream out) throws IOException {
    }
    
    private class MessageBuilder implements ContentHandler {
        private Stack stack = new Stack();
        private Message root = null;
        
        public MessageBuilder() {
        }
        
        private void expect(Class c) {
            if (!c.isInstance(stack.peek())) {
                throw new IllegalStateException("Internal stack error: "
                        + "Expected '" + c.getName() + "' found '"
                        + stack.peek().getClass().getName() + "'");
            }
        }
        
        public void startMessage() {
            if (stack.isEmpty()) {
                stack.push(Message.this);
            } else {
                expect(Entity.class);
                Message m = new Message();
                ((Entity) stack.peek()).setBody(m);
                stack.push(m);
            }
        }
        
        public void endMessage() {
            expect(Message.class);
            stack.pop();
        }
        
        public void startHeader() {
            stack.push(new Header());
        }
        
        public void field(String fieldData) {
            expect(Header.class);
            ((Header) stack.peek()).addField(Field.parse(fieldData));
        }
        
        public void endHeader() {
            expect(Header.class);
            Header h = (Header) stack.pop();
            expect(Entity.class);
            ((Entity) stack.peek()).setHeader(h);
        }
        
        public void startMultipart(BodyDescriptor bd) {
            expect(Entity.class);
            
            Entity e = (Entity) stack.peek();
            Multipart multiPart = new Multipart();
            e.setBody(multiPart);
            stack.push(multiPart);
        }
        
        public void body(BodyDescriptor bd, InputStream is) throws IOException {
            expect(Entity.class);
            
            String enc = bd.getTransferEncoding();
            if ("base64".equals(enc)) {
                is = new Base64InputStream(is);
            } else if ("quoted-printable".equals(enc)) {
                is = new QuotedPrintableInputStream(is);
            }
            
            Body body = null;
            if (bd.getMimeType().startsWith("text/")) {
                body = new TempFileTextBody(is, bd.getCharset());
            } else {
                body = new TempFileBinaryBody(is);
            }
            
            ((Entity) stack.peek()).setBody(body);
        }
        
        public void endMultipart() {
            stack.pop();
        }
        
        public void startBodyPart() {
            expect(Multipart.class);
            
            BodyPart bodyPart = new BodyPart();
            ((Multipart) stack.peek()).addBodyPart(bodyPart);
            stack.push(bodyPart);
        }
        
        public void endBodyPart() {
            expect(BodyPart.class);
            stack.pop();
        }
        
        public void epilogue(InputStream is) throws IOException {
            expect(Multipart.class);
            StringBuffer sb = new StringBuffer();
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char) b);
            }
            ((Multipart) stack.peek()).setEpilogue(sb.toString());
        }
        
        public void preamble(InputStream is) throws IOException {
            expect(Multipart.class);
            StringBuffer sb = new StringBuffer();
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char) b);
            }
            ((Multipart) stack.peek()).setPreamble(sb.toString());
        }
        
        public void raw(InputStream is) throws IOException {
        }

    }
}
