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
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentLengthField;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MutableBodyDescriptor;
import org.apache.james.mime4j.stream.RawField;
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
    private Map<String, String> parameters = new HashMap<String, String>();

    private ContentTypeField contentTypeField;
    private ContentLengthField contentLengthField;
    private ContentTransferEncodingField contentTransferEncodingField;

    private final FieldParser<?> fieldParser;

    /**
     * Creates a new root <code>BodyDescriptor</code> instance.
     */
    public MinimalBodyDescriptor() {
        this(null);
    }

    /**
     * Creates a new root <code>BodyDescriptor</code> instance with the given parent
     */
    public MinimalBodyDescriptor(final BodyDescriptor parent) {
        this(parent, null, null);
    }

    /**
     * Creates a new <code>BodyDescriptor</code> instance.
     * 
     * @param parent the descriptor of the parent or <code>null</code> if this
     *        is the root descriptor.
     */
    public MinimalBodyDescriptor(final BodyDescriptor parent, final FieldParser<?> fieldParser, final DecodeMonitor monitor) {
        if (parent != null && MimeUtil.isSameMimeType("multipart/digest", parent.getMimeType())) {
            this.mimeType = EMAIL_MESSAGE_MIME_TYPE;
            this.subType = SUB_TYPE_EMAIL;
            this.mediaType = MEDIA_TYPE_MESSAGE;
        } else {
            this.mimeType = DEFAULT_MIME_TYPE;
            this.subType = DEFAULT_SUB_TYPE;
            this.mediaType = DEFAULT_MEDIA_TYPE;
        }
        this.fieldParser = fieldParser != null ? fieldParser : DefaultFieldParser.getParser();
        this.monitor = monitor != null ? monitor : DecodeMonitor.SILENT;
    }
    
    protected DecodeMonitor getDecodeMonitor() {
        return monitor;
    }
    
    protected FieldParser<?> getFieldParser() {
        return fieldParser;
    }
    
    public MutableBodyDescriptor newChild() {
        return new MinimalBodyDescriptor(this, getFieldParser(), getDecodeMonitor());
    }
    
    /**
     * Should be called for each <code>Content-</code> header field of 
     * a MIME message or part.
     * 
     * @param field the MIME field.
     */
    public Field addField(RawField field) throws MimeException {
        String name = field.getName();
        if (name.equalsIgnoreCase(FieldName.CONTENT_TRANSFER_ENCODING)&& contentTransferEncodingField == null) {
            return parseContentTransferEncoding(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_LENGTH) && contentLengthField == null) {
            return parseContentLength(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_TYPE) && contentTypeField == null) {
            return parseContentType(field);
        } else {
            return getFieldParser().parse(field, monitor);
        }
    }

    private ParsedField parseContentTransferEncoding(Field field) throws MimeException {
        contentTransferEncodingField = (ContentTransferEncodingField) fieldParser.parse(field, monitor);
        return contentTransferEncodingField;
    }

    private ParsedField parseContentLength(Field field) throws MimeException {
        contentLengthField = (ContentLengthField) fieldParser.parse(field, monitor);
        return contentLengthField;
    }

    private ParsedField parseContentType(Field field) throws MimeException {
        contentTypeField = (ContentTypeField) fieldParser.parse(field, monitor);

        String mimetype = contentTypeField.getMimeType();
        if (mimetype != null) {
            mimeType = mimetype;
            mediaType = contentTypeField.getMediaType();
            subType = contentTypeField.getSubType();
        }
        if (MimeUtil.isMultipart(mimeType)) {
            boundary = contentTypeField.getBoundary();
        }
        charset = contentTypeField.getCharset();
        if (charset == null && MEDIA_TYPE_TEXT.equalsIgnoreCase(mediaType)) {
            charset = US_ASCII;
        }
        parameters.putAll(contentTypeField.getParameters());
        parameters.remove("charset");
        parameters.remove("boundary");
        return contentTypeField;
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
        return contentTransferEncodingField != null ? contentTransferEncodingField.getEncoding() : 
            MimeUtil.ENC_7BIT;
    }
    
    @Override
    public String toString() {
        return mimeType;
    }

    public long getContentLength() {
        return contentLengthField != null ? contentLengthField.getContentLength() : -1;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getSubType() {
        return subType;
    }

}
