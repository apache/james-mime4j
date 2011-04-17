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

import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.MessageServiceFactory;
import org.apache.james.mime4j.dom.MessageFormatter;
import org.apache.james.mime4j.stream.MimeEntityConfig;
import org.apache.james.mime4j.stream.MutableBodyDescriptorFactory;

/**
 * The default MessageBuilderFactory bundled with Mime4j.
 * 
 * Supports the "StorageProvider", "MimeEntityConfig" and "MutableBodyDescriptorFactory"
 * attributes.
 */
public class MessageServiceFactoryImpl extends MessageServiceFactory {

    private BodyFactory bodyFactory = null;
    private MimeEntityConfig mimeEntityConfig = null;
    private MutableBodyDescriptorFactory mutableBodyDescriptorFactory = null;
    private Boolean flatMode = null;
    private Boolean contentDecoding = null;

    @Override
    public MessageBuilder newMessageBuilder() {
        MessageBuilderImpl m = new MessageBuilderImpl();
        if (bodyFactory != null) m.setBodyFactory(bodyFactory);
        if (mimeEntityConfig != null) m.setMimeEntityConfig(mimeEntityConfig);
        if (mutableBodyDescriptorFactory != null) m.setMutableBodyDescriptorFactory(mutableBodyDescriptorFactory);
        if (flatMode != null) m.setFlatMode(flatMode.booleanValue());
        if (contentDecoding != null) m.setContentDecoding(contentDecoding.booleanValue());
        return m;
    }

    @Override
    public MessageFormatter newMessageFormatter() {
        MessageFormatterImpl m = new MessageFormatterImpl();
        return m;
    }
    
    @Override
    public void setAttribute(String name, Object value)
            throws IllegalArgumentException {
        if ("BodyFactory".equals(name)) {
            if (value instanceof BodyFactory) {
                this.bodyFactory  = (BodyFactory) value;
                return;
            } else throw new IllegalArgumentException("Unsupported attribute value type for "+name+", expected a BodyFactory");
        } else if ("MimeEntityConfig".equals(name)) {
            if (value instanceof MimeEntityConfig) {
                this.mimeEntityConfig = (MimeEntityConfig) value;
                return;
            } else throw new IllegalArgumentException("Unsupported attribute value type for "+name+", expected a MimeEntityConfig");
        } else if ("MutableBodyDescriptorFactory".equals(name)) {
            if (value instanceof MutableBodyDescriptorFactory) {
                this.mutableBodyDescriptorFactory  = (MutableBodyDescriptorFactory) value;
                return;
            } else throw new IllegalArgumentException("Unsupported attribute value type for "+name+", expected a MutableBodyDescriptorFactory");
        } else if ("FlatMode".equals(name)) {
            if (value instanceof Boolean) {
                this.flatMode  = (Boolean) value;
                return;
            } else throw new IllegalArgumentException("Unsupported attribute value type for "+name+", expected a Boolean");
        } else if ("ContentDecoding".equals(name)) {
            if (value instanceof Boolean) {
                this.contentDecoding = (Boolean) value;
                return;
            } else throw new IllegalArgumentException("Unsupported attribute value type for "+name+", expected a Boolean");
        }
            
        throw new IllegalArgumentException("Unsupported attribute: "+name);
        
    }

}
