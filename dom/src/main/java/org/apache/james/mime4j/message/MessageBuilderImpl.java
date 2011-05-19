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

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.stream.FieldParser;
import org.apache.james.mime4j.stream.MimeEntityConfig;
import org.apache.james.mime4j.stream.MutableBodyDescriptorFactory;

/**
 * Default MessageBuilder implementation delegating Message parsing to the "legacy"
 * MessageImpl object.
 */
public class MessageBuilderImpl implements MessageBuilder {

    private MimeBuilder mimeBuilder = null;
    private FieldParser<? extends ParsedField> fieldParser = null;
    private BodyFactory bodyFactory = null;
    private MimeEntityConfig mimeEntityConfig = null;
    private MutableBodyDescriptorFactory mutableBodyDescriptorFactory = null;
    private boolean contentDecoding = true;
    private boolean flatMode = false;
    private DecodeMonitor decodeMonitor = null;

    public MessageBuilderImpl() {
    }

    public Message newMessage() {
        return new MessageImpl();
    }

    private MimeBuilder getMimeBuilder() {
        if (this.mimeBuilder != null) {
            return this.mimeBuilder;
        } else {
            return MimeBuilder.DEFAULT;
        }
        
    }
    
    public Message newMessage(Message source) {
        return getMimeBuilder().copy(source);
    }

    public Message parse(InputStream source) throws MimeException, IOException {
        return getMimeBuilder().parse(source, 
                mimeEntityConfig,
                decodeMonitor,
                fieldParser,
                bodyFactory, 
                mutableBodyDescriptorFactory, 
                contentDecoding,
                flatMode);
    }
    
    public void setFieldParser(FieldParser<? extends ParsedField> fieldParser) {
        this.fieldParser = fieldParser;
    }

    public void setBodyFactory(BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
    }

    public void setMimeEntityConfig(MimeEntityConfig mimeEntityConfig) {
        this.mimeEntityConfig = mimeEntityConfig;
    }

    public void setMutableBodyDescriptorFactory(
            MutableBodyDescriptorFactory mutableBodyDescriptorFactory) {
        this.mutableBodyDescriptorFactory  = mutableBodyDescriptorFactory;
    }

    public void setMimeBuilder(MimeBuilder mimeBuilder) {
        this.mimeBuilder = mimeBuilder;
    }

    public void setDecodeMonitor(DecodeMonitor decodeMonitor) {
        this.decodeMonitor = decodeMonitor;
    }

    public void setContentDecoding(boolean contentDecoding) {
        this.contentDecoding = contentDecoding;
    }

    public void setFlatMode(boolean flatMode) {
        this.flatMode = flatMode;
    }
    
}
