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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.field.ContentDescriptionField;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentIdField;
import org.apache.james.mime4j.dom.field.ContentLanguageField;
import org.apache.james.mime4j.dom.field.ContentLocationField;
import org.apache.james.mime4j.dom.field.ContentMD5Field;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.MimeVersionField;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.field.MimeVersionFieldImpl;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MutableBodyDescriptor;
import org.apache.james.mime4j.stream.RawField;

/**
 * Parses and stores values for standard MIME header values.
 * 
 */
public class MaximalBodyDescriptor extends MinimalBodyDescriptor {

    private MimeVersionField mimeVersionField;
    private ContentIdField contentIdField;
    private ContentDescriptionField contentDescriptionField;
    private ContentDispositionField contentDispositionField;
    private ContentLanguageField contentLanguageField;
    private ContentLocationField contentLocationField;
    private ContentMD5Field contentMD5Field;
    
    protected MaximalBodyDescriptor() {
        this(null);
    }
    
    protected MaximalBodyDescriptor(final BodyDescriptor parent) {
        this(parent, null, null);
    }

    public MaximalBodyDescriptor(final BodyDescriptor parent, final FieldParser<?> fieldParser, final DecodeMonitor monitor) {
        super(parent, fieldParser, monitor);
    }

    @Override
    public MutableBodyDescriptor newChild() {
        return new MaximalBodyDescriptor(this, getFieldParser(), getDecodeMonitor());
    }

    @Override
    public Field addField(RawField field) throws MimeException {
        String name = field.getName();
        if (name.equalsIgnoreCase(FieldName.MIME_VERSION) && mimeVersionField == null) {
            return parseMimeVersion(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_ID) && contentIdField == null) {
            return parseContentId(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_DESCRIPTION) && contentDescriptionField == null) {
            return parseContentDescription(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_DISPOSITION) && contentDispositionField == null) {
            return parseContentDisposition(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_LANGUAGE) && contentLanguageField == null) {
            return parseLanguage(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_LOCATION) && contentLocationField == null) {
            return parseLocation(field);
        } else if (name.equalsIgnoreCase(FieldName.CONTENT_MD5) && contentMD5Field == null) {
            return parseMD5(field);
        } else {
            return super.addField(field);
        }
    }
    
    private ContentMD5Field parseMD5(final Field field) {
        contentMD5Field = (ContentMD5Field) getFieldParser().parse(field, getDecodeMonitor());
        return contentMD5Field;
    }

    private ContentLocationField parseLocation(final Field field) {
        contentLocationField = (ContentLocationField) getFieldParser().parse(field, getDecodeMonitor());
        return contentLocationField;
    }
    
    private ParsedField parseLanguage(final Field field) {
        contentLanguageField = (ContentLanguageField) getFieldParser().parse(field, getDecodeMonitor());
        return contentLanguageField;
    }

    private ParsedField parseContentDisposition(final Field field) throws MimeException {
        contentDispositionField = (ContentDispositionField) getFieldParser().parse(field, getDecodeMonitor());
        return contentDispositionField;
    }

    private ParsedField parseContentDescription(final Field field) {
        contentDescriptionField = (ContentDescriptionField) getFieldParser().parse(field, getDecodeMonitor());
        return contentDescriptionField;
    }

    private ContentIdField parseContentId(final Field field) {
        contentIdField = (ContentIdField) getFieldParser().parse(field, getDecodeMonitor());
        return contentIdField;
    }

    private MimeVersionField parseMimeVersion(final Field field) {
        mimeVersionField = (MimeVersionField) getFieldParser().parse(field, getDecodeMonitor());
        return mimeVersionField;
    }
    
    /**
     * Gets the MIME major version
     * as specified by the <code>MIME-Version</code>
     * header.
     * Defaults to one.
     * @return positive integer
     */
    public int getMimeMajorVersion() {
        return mimeVersionField != null ? mimeVersionField.getMajorVersion() : 
            MimeVersionFieldImpl.DEFAULT_MAJOR_VERSION;
    }
    
    /**
     * Gets the MIME minor version
     * as specified by the <code>MIME-Version</code>
     * header. 
     * Defaults to zero.
     * @return positive integer
     */
    public int getMimeMinorVersion() {
        return mimeVersionField != null ? mimeVersionField.getMinorVersion() : 
            MimeVersionFieldImpl.DEFAULT_MINOR_VERSION;
    }
    

    /**
     * Gets the value of the <a href='http://www.faqs.org/rfcs/rfc2045'>RFC</a> 
     * <code>Content-Description</code> header.
     * @return value of the <code>Content-Description</code> when present,
     * null otherwise
     */
    public String getContentDescription() {
        return contentDescriptionField != null ? contentDescriptionField.getDescription() : null;
    }
    
    /**
     * Gets the value of the <a href='http://www.faqs.org/rfcs/rfc2045'>RFC</a> 
     * <code>Content-ID</code> header.
     * @return value of the <code>Content-ID</code> when present,
     * null otherwise
     */
    public String getContentId() {
        return contentIdField != null ? contentIdField.getId() : null;
    }
    
    /**
     * Gets the disposition type of the <code>content-disposition</code> field.
     * The value is case insensitive and will be converted to lower case.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return content disposition type, 
     * or null when this has not been set
     */
    public String getContentDispositionType() {
        return contentDispositionField != null ? contentDispositionField.getDispositionType() : null;
    }
    
    /**
     * Gets the parameters of the <code>content-disposition</code> field.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return parameter value strings indexed by parameter name strings,
     * not null
     */
    public Map<String, String> getContentDispositionParameters() {
        return contentDispositionField != null ? contentDispositionField.getParameters() : 
            Collections.<String, String>emptyMap();
    }
    
    /**
     * Gets the <code>filename</code> parameter value of the <code>content-disposition</code> field.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return filename parameter value, 
     * or null when it is not present
     */
    public String getContentDispositionFilename() {
        return contentDispositionField != null ? contentDispositionField.getFilename() : null;
    }
    
    /**
     * Gets the <code>modification-date</code> parameter value of the <code>content-disposition</code> field.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return modification-date parameter value,
     * or null when this is not present
     */
    public Date getContentDispositionModificationDate() {
        return contentDispositionField != null ? contentDispositionField.getModificationDate() : null;
    }
    
    /**
     * Gets the <code>creation-date</code> parameter value of the <code>content-disposition</code> field.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return creation-date parameter value,
     * or null when this is not present
     */
    public Date getContentDispositionCreationDate() {
        return contentDispositionField != null ? contentDispositionField.getCreationDate() : null;
    }
    
    /**
     * Gets the <code>read-date</code> parameter value of the <code>content-disposition</code> field.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return read-date parameter value,
     * or null when this is not present
     */
    public Date getContentDispositionReadDate() {
        return contentDispositionField != null ? contentDispositionField.getReadDate() : null;
    }
    
    /**
     * Gets the <code>size</code> parameter value of the <code>content-disposition</code> field.
     * See <a href='http://www.faqs.org/rfcs/rfc2183.html'>RFC2183</a>.
     * @return size parameter value,
     * or -1 if this size has not been set
     */
    public long getContentDispositionSize() {
        return contentDispositionField != null ? contentDispositionField.getSize() : -1;
    }
    
    /**
     * Get the <code>content-language</code> header values.
     * Each applicable language tag will be returned in order.
     * See <a href='http://tools.ietf.org/html/rfc4646'>RFC4646</a> 
     * <cite>http://tools.ietf.org/html/rfc4646</cite>.
     * @return list of language tag Strings,
     * or null if no header exists
     */
    public List<String> getContentLanguage() {
        return contentLanguageField != null ? contentLanguageField.getLanguages() : 
            Collections.<String>emptyList();
    }

    /**
     * Get the <code>content-location</code> header value.
     * See <a href='http://tools.ietf.org/html/rfc2557'>RFC2557</a> 
     * @return the URL content-location
     * or null if no header exists
     */
    public String getContentLocation() {
        return contentLocationField != null ? contentLocationField.getLocation() : null;
    }
    
    /**
     * Gets the raw, Base64 encoded value of the
     * <code>Content-MD5</code> field.
     * See <a href='http://tools.ietf.org/html/rfc1864'>RFC1864</a>.
     * @return raw encoded content-md5
     * or null if no header exists
     */
    public String getContentMD5Raw() {
        return contentMD5Field != null ? contentMD5Field.getMD5Raw() : null;
    }
    
} 
