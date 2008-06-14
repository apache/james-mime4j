/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.james.mime4j;

import java.io.StringReader;

import org.apache.james.mime4j.field.mimeversion.MimeVersionParser;
import org.apache.james.mime4j.util.MimeUtil;


/**
 * Parses and stores values for standard MIME header values.
 * 
 */
public class MaximalBodyDescriptor extends DefaultBodyDescriptor {

    private static final int DEFAULT_MINOR_VERSION = 0;
    private static final int DEFAULT_MAJOR_VERSION = 1;
    private boolean mimeVersionSet;
    private int mimeMinorVersion;
    private int mimeMajorVersion;
    private MimeException mimeVersionException;
    
    protected MaximalBodyDescriptor() {
        this(null);
    }

    protected MaximalBodyDescriptor(BodyDescriptor parent) {
        super(parent);
        mimeVersionSet = false;
        mimeMajorVersion = DEFAULT_MAJOR_VERSION;
        mimeMinorVersion = DEFAULT_MINOR_VERSION;
    }
    
    
    
    public void addField(String name, String value) {
        name = name.trim().toLowerCase();
        if (MimeUtil.MIME_HEADER_MIME_VERSION.equals(name) && !mimeVersionSet) {
            parseMimeVersion(value);
        } else {
            super.addField(name, value);
        }
    }

    private void parseMimeVersion(String value) {
        final StringReader reader = new StringReader(value);
        final MimeVersionParser parser = new MimeVersionParser(reader);
        try {
            parser.parse();
            final int major = parser.getMajorVersion();
            if (major != MimeVersionParser.INITIAL_VERSION_VALUE) {
                mimeMajorVersion = major;
            }
            final int minor = parser.getMinorVersion();
            if (minor != MimeVersionParser.INITIAL_VERSION_VALUE) {
                mimeMinorVersion = minor;
            }
        } catch (MimeException e) {
            this.mimeVersionException = e;
        }
        mimeVersionSet = true;
    }
    
    /**
     * Gets the MIME major version
     * as specified by the <code>MIME-Version</code>
     * header.
     * Defaults to one.
     * @return positive integer
     */
    public int getMimeMajorVersion() {
        return mimeMajorVersion;
    }
    
    /**
     * Gets the MIME minor version
     * as specified by the <code>MIME-Version</code>
     * header. 
     * Defaults to zero.
     * @return positive integer
     */
    public int getMimeMinorVersion() {
        return mimeMinorVersion;
    }
    
    public MimeException getMimeVersionParseException() {
        return mimeVersionException;
    }
} 
