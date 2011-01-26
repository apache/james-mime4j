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
import org.apache.james.mime4j.dom.ParseParams;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.stream.MimeEntityConfig;
import org.apache.james.mime4j.stream.MutableBodyDescriptorFactory;

/**
 * Default MessageBuilder implementation delegating Message parsing to the "legacy"
 * MessageImpl object.
 */
public class MessageBuilderImpl implements MessageBuilder {

    private MimeBuilder mimeBuilder = null;
    private StorageProvider storageProvider = null;
    private MimeEntityConfig mimeEntityConfig = null;
    private MutableBodyDescriptorFactory mutableBodyDescriptorFactory = null;

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
                storageProvider, 
                mutableBodyDescriptorFactory, 
                null,
                null);
    }
    
    public Message parse(
            InputStream source, 
            ParseParams params, DecodeMonitor decodeMonitor) throws MimeException, IOException {
        return getMimeBuilder().parse(source, 
                mimeEntityConfig, 
                storageProvider, 
                mutableBodyDescriptorFactory, 
                params,
                decodeMonitor);
    }
    
    public void setStorageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
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

}
