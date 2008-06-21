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
public class MaximalBodyDescriptor extends DefaultBodyDescriptor implements RFC2045Descriptor {

    private static final int DEFAULT_MINOR_VERSION = 0;
    private static final int DEFAULT_MAJOR_VERSION = 1;
    private boolean isMimeVersionSet;
    private int mimeMinorVersion;
    private int mimeMajorVersion;
    private MimeException mimeVersionException;
    private String contentId;
    private boolean isContentIdSet;
    private String contentDescription;
    private boolean isContentDescriptionSet;
    
    protected MaximalBodyDescriptor() {
        this(null);
    }

    protected MaximalBodyDescriptor(BodyDescriptor parent) {
        super(parent);
        isMimeVersionSet = false;
        mimeMajorVersion = DEFAULT_MAJOR_VERSION;
        mimeMinorVersion = DEFAULT_MINOR_VERSION;
        this.contentId = null;
        this.isContentIdSet = false;
        this.contentDescription = null;
        this.isContentDescriptionSet = false;
    }
    
    public void addField(String name, String value) {
        name = name.trim().toLowerCase();
        if (MimeUtil.MIME_HEADER_MIME_VERSION.equals(name) && !isMimeVersionSet) {
            parseMimeVersion(value);
        } else if (MimeUtil.MIME_HEADER_CONTENT_ID.equals(name) && !isContentIdSet) {
            parseContentId(value);
        } else if (MimeUtil.MIME_HEADER_CONTENT_DESCRIPTION.equals(name) && !isContentDescriptionSet) {
            parseContentDescription(value);
        } else {
            super.addField(name, value);
        }
    }

    private void parseContentDescription(String value) {
        if (value == null) {
            contentDescription = "";
        } else {
            contentDescription = value.trim();
        }
        isContentDescriptionSet = true;
    }

    private void parseContentId(final String value) {
        if (value == null) {
            contentId = "";
        } else {
            contentId = value.trim();
        }
        isContentIdSet = true;
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
        isMimeVersionSet = true;
    }
    
    /**
     * @see org.apache.james.mime4j.RFC2045Descriptor#getMimeMajorVersion()
     */
    public int getMimeMajorVersion() {
        return mimeMajorVersion;
    }
    
    /**
     * @see org.apache.james.mime4j.RFC2045Descriptor#getMimeMinorVersion()
     */
    public int getMimeMinorVersion() {
        return mimeMinorVersion;
    }
    
    /**
     * @see org.apache.james.mime4j.RFC2045Descriptor#getMimeVersionParseException()
     */
    public MimeException getMimeVersionParseException() {
        return mimeVersionException;
    }
    
    /**
     * @see org.apache.james.mime4j.RFC2045Descriptor#getContentDescription()
     */
    public String getContentDescription() {
        return contentDescription;
    }
    
    /**
     * @see org.apache.james.mime4j.RFC2045Descriptor#getContentId()
     */
    public String getContentId() {
        return contentId;
    }
} 
