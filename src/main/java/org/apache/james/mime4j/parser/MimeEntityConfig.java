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

import org.apache.james.mime4j.util.CharArrayBuffer;

/**
 * MIME entity configuration
 */
public final class MimeEntityConfig implements Cloneable {

    private boolean maximalBodyDescriptor;
    private boolean strictParsing;
    private int maxLineLen;
    
    public MimeEntityConfig() {
        super();
        this.maximalBodyDescriptor = false;
        this.strictParsing = false;
        this.maxLineLen = 1000;
    }
    
    public boolean isMaximalBodyDescriptor() {
        return this.maximalBodyDescriptor;
    }
    
    public void setMaximalBodyDescriptor(boolean maximalBodyDescriptor) {
        this.maximalBodyDescriptor = maximalBodyDescriptor;
    }
    
    public boolean isStrictParsing() {
        return this.strictParsing;
    }
    
    public void setStrictParsing(boolean strictParsing) {
        this.strictParsing = strictParsing;
    }

    public void setMaxLineLen(int maxLineLen) {
        this.maxLineLen = maxLineLen;
    }
    
    public int getMaxLineLen() {
        return this.maxLineLen;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    public String toString() {
        CharArrayBuffer buffer = new CharArrayBuffer(128);
        buffer.append("[max body descriptor: ");
        buffer.append(Boolean.toString(this.maximalBodyDescriptor));
        buffer.append("][strict parsing: ");
        buffer.append(Boolean.toString(this.strictParsing));
        buffer.append("][max header length: ");
        buffer.append(Integer.toString(this.maxLineLen));
        buffer.append("]");
        return buffer.toString();
    }
    
}
