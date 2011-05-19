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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.field.ContentTransferEncodingFieldImpl;
import org.apache.james.mime4j.field.ContentTypeFieldImpl;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MutableBodyDescriptor;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Encapsulates the values of the MIME-specific header fields 
 * (which starts with <code>Content-</code>). 
 */
public class MinimalBodyDescriptor implements MutableBodyDescriptor {
    
    private static final String US_ASCII = "us-ascii";

    private static final String SUB_TYPE_EMAIL = "rfc822";

    private static final String MEDIA_TYPE_TEXT = "text";

    private static final String MEDIA_TYPE_MESSAGE = "message";

    private static final String EMAIL_MESSAGE_MIME_TYPE = MEDIA_TYPE_MESSAGE + "/" + SUB_TYPE_EMAIL;

    private static final String DEFAULT_SUB_TYPE = "plain";

    private static final String DEFAULT_MEDIA_TYPE = MEDIA_TYPE_TEXT;

    private static final String DEFAULT_MIME_TYPE = DEFAULT_MEDIA_TYPE + "/" + DEFAULT_SUB_TYPE;

    private final DecodeMonitor monitor;
    
    private String mediaType = DEFAULT_MEDIA_TYPE;
    private String subType = DEFAULT_SUB_TYPE;
    private String mimeType = DEFAULT_MIME_TYPE;
    private String boundary = null;
    private String charset = US_ASCII;
    private String transferEncoding = "7bit";
    private Map<String, String> parameters = new HashMap<String, String>();
    private boolean contentTypeSet;
    private boolean contentTransferEncSet;
    private long contentLength = -1;
    
    /**
     * Creates a new root <code>BodyDescriptor</code> instance.
     */
    public MinimalBodyDescriptor() {
        this(null, null);
    }

    /**
     * Creates a new <code>BodyDescriptor</code> instance.
     * 
     * @param parent the descriptor of the parent or <code>null</code> if this
     *        is the root descriptor.
     */
    public MinimalBodyDescriptor(final BodyDescriptor parent, final DecodeMonitor monitor) {
        if (parent != null && MimeUtil.isSameMimeType("multipart/digest", parent.getMimeType())) {
            this.mimeType = EMAIL_MESSAGE_MIME_TYPE;
            this.subType = SUB_TYPE_EMAIL;
            this.mediaType = MEDIA_TYPE_MESSAGE;
        } else {
            this.mimeType = DEFAULT_MIME_TYPE;
            this.subType = DEFAULT_SUB_TYPE;
            this.mediaType = DEFAULT_MEDIA_TYPE;
        }
        this.monitor = monitor != null ? monitor : DecodeMonitor.SILENT;
    }
    
    protected DecodeMonitor getDecodeMonitor() {
        return monitor;
    }
    
    public MutableBodyDescriptor newChild() {
		return new MinimalBodyDescriptor(this, getDecodeMonitor());
    }
    
    /**
     * Should be called for each <code>Content-</code> header field of 
     * a MIME message or part.
     * 
     * @param field the MIME field.
     */
    public void addField(Field field) throws MimeException {
        String name = field.getName().toLowerCase(Locale.US);
        
        if (name.equals("content-transfer-encoding") && !contentTransferEncSet) {
            parseContentTransferEncoding(field);
        } else if (name.equals("content-length") && contentLength == -1) {
            parseContentLength(field);
        } else if (name.equals("content-type") && !contentTypeSet) {
            parseContentType(field);
        }
    }

    private void parseContentTransferEncoding(Field field) throws MimeException {
        contentTransferEncSet = true;
        ContentTransferEncodingField f;
        if (field instanceof ContentTransferEncodingField) {
            f = (ContentTransferEncodingField) field;
        } else {
            f = ContentTransferEncodingFieldImpl.PARSER.parse(
                    field.getName(), field.getBody(), field.getRaw(), monitor);
        }
        transferEncoding = f.getEncoding();
    }

    private void parseContentLength(Field field) throws MimeException {
        String value = field.getBody();
        if (value != null) {
            try {
                long v = Long.parseLong(value);
                if (v < 0) {
                    if (monitor.warn("Negative content length: " + value, 
                            "ignoring Content-Length header")) {
                        throw new MimeException("Negative Content-Length header: " + value);
                    }
                } else {
                    contentLength = v;
                }
            } catch (NumberFormatException e) {
                if (monitor.warn("Invalid content length: " + value, 
                        "ignoring Content-Length header")) {
                    throw new MimeException("Invalid Content-Length header: " + value);
                }
            }
        }
    }

    private void parseContentType(Field field) throws MimeException {
        contentTypeSet = true;
        ContentTypeField f;
        if (field instanceof ContentTypeField) {
            f = (ContentTypeField) field;
        } else {
            f = ContentTypeFieldImpl.PARSER.parse(
                    field.getName(), field.getBody(), field.getRaw(), monitor);
        }
        String mimetype = f.getMimeType();
        if (mimetype != null) {
            mimeType = mimetype;
            mediaType = f.getMediaType();
            subType = f.getSubType();
        }
        if (MimeUtil.isMultipart(mimeType)) {
            boundary = f.getBoundary();
        }
        charset = f.getCharset();
        if (charset == null && MEDIA_TYPE_TEXT.equalsIgnoreCase(mediaType)) {
            charset = US_ASCII;
        }
        parameters.putAll(f.getParameters());
        parameters.remove("charset");
        parameters.remove("boundary");
    }

    /**
     * Return the MimeType 
     * 
     * @return mimeType
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * Return the boundary
     * 
     * @return boundary
     */
    public String getBoundary() {
        return boundary;
    }
    
    /**
     * Return the charset
     * 
     * @return charset
     */
    public String getCharset() {
        return charset;
    }
    
    /**
     * Return all parameters for the BodyDescriptor
     * 
     * @return parameters
     */
    public Map<String, String> getContentTypeParameters() {
        return parameters;
    }
    
    /**
     * Return the TransferEncoding
     * 
     * @return transferEncoding
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }
    
    @Override
    public String toString() {
        return mimeType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getSubType() {
        return subType;
    }

}
