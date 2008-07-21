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

package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import org.apache.james.mime4j.BodyDescriptor;
import org.apache.james.mime4j.CharArrayBuffer;
import org.apache.james.mime4j.ContentHandler;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeStreamParser;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.util.MessageUtils;


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
 * 
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
        try {
            parser.parse(is);
        } catch (MimeException e) {
            IllegalStateException ise = new IllegalStateException(
                    "Unexpected message processing error");
            ise.initCause(e);
            throw ise;
        }
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
    
    /**
     * Writes out the content of this message..
     * @param out not null
     * @param mode header out validation mode {@link MessageUtils}
     * @throws MimeException 
     * @see org.apache.james.mime4j.message.Entity#writeTo(java.io.OutputStream, int)
     */
    public void writeTo(OutputStream out, int mode) throws IOException, MimeException {
        getHeader().writeTo(out, mode);

        final Body body = getBody();
        body.writeTo(out, mode);
    }
    
    private class MessageBuilder implements ContentHandler {
        private Stack stack = new Stack();
        
        public MessageBuilder() {
        }
        
        private void expect(Class c) {
            if (!c.isInstance(stack.peek())) {
                throw new IllegalStateException("Internal stack error: "
                        + "Expected '" + c.getName() + "' found '"
                        + stack.peek().getClass().getName() + "'");
            }
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#startMessage()
         */
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
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#endMessage()
         */
        public void endMessage() {
            expect(Message.class);
            stack.pop();
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#startHeader()
         */
        public void startHeader() {
            stack.push(new Header());
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#field(java.lang.String)
         */
        public void field(String fieldData) {
            expect(Header.class);
            ((Header) stack.peek()).addField(Field.parse(fieldData));
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#endHeader()
         */
        public void endHeader() {
            expect(Header.class);
            Header h = (Header) stack.pop();
            expect(Entity.class);
            ((Entity) stack.peek()).setHeader(h);
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#startMultipart(org.apache.james.mime4j.BodyDescriptor)
         */
        public void startMultipart(final BodyDescriptor bd) {
            expect(Entity.class);
            
            final Entity e = (Entity) stack.peek();
            final String subType = bd.getSubType();
            final Multipart multiPart = new Multipart(subType);
            e.setBody(multiPart);
            stack.push(multiPart);
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#body(org.apache.james.mime4j.BodyDescriptor, java.io.InputStream)
         */
        public void body(BodyDescriptor bd, final InputStream is) throws IOException {
            expect(Entity.class);
            
            final String enc = bd.getTransferEncoding();
            
            final Body body;
            if (bd.getMimeType().startsWith("text/")) {
                body = new TempFileTextBody(is, bd.getCharset(), enc);
            } else {
                body = new TempFileBinaryBody(is, enc);
            }
            
            ((Entity) stack.peek()).setBody(body);
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#endMultipart()
         */
        public void endMultipart() {
            stack.pop();
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#startBodyPart()
         */
        public void startBodyPart() {
            expect(Multipart.class);
            
            BodyPart bodyPart = new BodyPart();
            ((Multipart) stack.peek()).addBodyPart(bodyPart);
            stack.push(bodyPart);
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#endBodyPart()
         */
        public void endBodyPart() {
            expect(BodyPart.class);
            stack.pop();
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#epilogue(java.io.InputStream)
         */
        public void epilogue(InputStream is) throws IOException {
            expect(Multipart.class);
            CharArrayBuffer sb = new CharArrayBuffer(128);
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char) b);
            }
            ((Multipart) stack.peek()).setEpilogue(sb.toString());
        }
        
        /**
         * @see org.apache.james.mime4j.ContentHandler#preamble(java.io.InputStream)
         */
        public void preamble(InputStream is) throws IOException {
            expect(Multipart.class);
            CharArrayBuffer sb = new CharArrayBuffer(128);
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char) b);
            }
            ((Multipart) stack.peek()).setPreamble(sb.toString());
        }
        
        /**
         * Unsupported.
         * @see org.apache.james.mime4j.ContentHandler#raw(java.io.InputStream)
         */
        public void raw(InputStream is) throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }

    }
}
