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

package org.apache.james.mime4j.stream;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Encapsulates the values of the MIME-specific header fields 
 * (which starts with <code>Content-</code>). 
 */
class FallbackBodyDescriptorBuilder implements BodyDescriptorBuilder {
    
    private static final String US_ASCII = "us-ascii";
    private static final String SUB_TYPE_EMAIL = "rfc822";
    private static final String MEDIA_TYPE_TEXT = "text";
    private static final String MEDIA_TYPE_MESSAGE = "message";
    private static final String EMAIL_MESSAGE_MIME_TYPE = MEDIA_TYPE_MESSAGE + "/" + SUB_TYPE_EMAIL;
    private static final String DEFAULT_SUB_TYPE = "plain";
    private static final String DEFAULT_MEDIA_TYPE = MEDIA_TYPE_TEXT;
    private static final String DEFAULT_MIME_TYPE = DEFAULT_MEDIA_TYPE + "/" + DEFAULT_SUB_TYPE;

    private final String parentMimeType;
    private final DecodeMonitor monitor;
    
    private String mediaType;
    private String subType;
    private String mimeType;
    private String boundary;
    private String charset;
    private String transferEncoding;
    private long contentLength;
    
    /**
     * Creates a new root <code>BodyDescriptor</code> instance.
     */
    public FallbackBodyDescriptorBuilder() {
        this(null, null);
    }

    /**
     * Creates a new <code>BodyDescriptor</code> instance.
     * 
     * @param parent the descriptor of the parent or <code>null</code> if this
     *        is the root descriptor.
     */
    public FallbackBodyDescriptorBuilder(final String parentMimeType, final DecodeMonitor monitor) {
        super();
        this.parentMimeType = parentMimeType;
        this.monitor = monitor != null ? monitor : DecodeMonitor.SILENT;
        reset();
    }
    
    public void reset() {
        mimeType = null;
        subType = null;
        mediaType = null;
        boundary = null;        
        charset = null;   
        transferEncoding = null;
        contentLength = -1;
    }

    public BodyDescriptorBuilder newChild() {
		return new FallbackBodyDescriptorBuilder(mimeType, monitor);
    }
    
    public BodyDescriptor build() {
        String actualMimeType = mimeType;
        String actualMediaType = mediaType;
        String actualSubType = subType;
        String actualCharset = charset;
        if (actualMimeType == null) {
            if (MimeUtil.isSameMimeType("multipart/digest", parentMimeType)) {
                actualMimeType = EMAIL_MESSAGE_MIME_TYPE;
                actualMediaType = MEDIA_TYPE_MESSAGE;
                actualSubType = SUB_TYPE_EMAIL;
            } else {
                actualMimeType = DEFAULT_MIME_TYPE;
                actualMediaType = DEFAULT_MEDIA_TYPE;
                actualSubType = DEFAULT_SUB_TYPE;
            }
        } 
        if (actualCharset == null && MEDIA_TYPE_TEXT.equals(actualMediaType)) {
            actualCharset = US_ASCII;
        }
        return new BasicBodyDescriptor(actualMimeType, actualMediaType, actualSubType, 
                boundary, actualCharset, 
                transferEncoding != null ? transferEncoding : "7bit", 
                contentLength);
    }

    /**
     * Should be called for each <code>Content-</code> header field of 
     * a MIME message or part.
     * 
     * @param field the MIME field.
     */
    public Field addField(RawField field) throws MimeException {
        String name = field.getName().toLowerCase(Locale.US);
        
        if (name.equals("content-transfer-encoding") && transferEncoding == null) {
            String value = field.getBody();
            if (value != null) {
                value = value.trim().toLowerCase(Locale.US);
                if (value.length() > 0) {
                    transferEncoding = value;
                }
            }
        } else if (name.equals("content-length") && contentLength == -1) {
            String value = field.getBody();
            if (value != null) {
                value = value.trim();
                try {
                    contentLength = Long.parseLong(value.trim());
                } catch (NumberFormatException e) {
                    if (monitor.warn("Invalid content length: " + value, 
                            "ignoring Content-Length header")) {
                        throw new MimeException("Invalid Content-Length header: " + value);
                    }
                }
            }
        } else if (name.equals("content-type") && mimeType == null) {
            parseContentType(field);
        }
        return null;
    }

    private void parseContentType(Field field) throws MimeException {
        RawField rawfield;
        if (field instanceof RawField) {
            rawfield = ((RawField) field);
        } else {
            rawfield = new RawField(field.getName(), field.getBody());
        }
        RawBody body = RawFieldParser.DEFAULT.parseRawBody(rawfield);
        String main = body.getValue();
        Map<String, String> params = new HashMap<String, String>();
        for (NameValuePair nmp: body.getParams()) {
            String name = nmp.getName().toLowerCase(Locale.US);
            params.put(name, nmp.getValue());
        }
        
        String type = null;
        String subtype = null;
        if (main != null) {
            main = main.toLowerCase().trim();
            int index = main.indexOf('/');
            boolean valid = false;
            if (index != -1) {
                type = main.substring(0, index).trim();
                subtype = main.substring(index + 1).trim();
                if (type.length() > 0 && subtype.length() > 0) {
                    main = type + "/" + subtype;
                    valid = true;
                }
            }
            
            if (!valid) {
                main = null;
                type = null;
                subtype = null;
            }
        }
        String b = params.get("boundary");
        
        if (main != null 
                && ((main.startsWith("multipart/") && b != null) 
                        || !main.startsWith("multipart/"))) {
            mimeType = main;
            mediaType = type;
            subType = subtype;
        }
        
        if (MimeUtil.isMultipart(mimeType)) {
            boundary = b;
        }
        
        String c = params.get("charset");
        charset = null;
        if (c != null) {
            c = c.trim();
            if (c.length() > 0) {
                charset = c;
            }
        }
    }

}
