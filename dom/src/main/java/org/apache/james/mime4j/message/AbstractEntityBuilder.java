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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;
import org.apache.james.mime4j.util.MimeUtil;

abstract class AbstractEntityBuilder {

    private final List<Field> fields;
    private final Map<String, List<Field>> fieldMap;

    private Body body;

    AbstractEntityBuilder() {
        this.fields = new LinkedList<Field>();
        this.fieldMap = new HashMap<String, List<Field>>();
    }

    /**
     * Adds a field to the end of the list of fields.
     *
     * @param field the field to add.
     */
    public AbstractEntityBuilder addField(Field field) {
        List<Field> values = fieldMap.get(field.getName().toLowerCase());
        if (values == null) {
            values = new LinkedList<Field>();
            fieldMap.put(field.getName().toLowerCase(), values);
        }
        values.add(field);
        fields.add(field);
        return this;
    }

    /**
     * Gets the fields of this header. The returned list will not be
     * modifiable.
     *
     * @return the list of <code>Field</code> objects.
     */
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Gets a <code>Field</code> given a field name. If there are multiple
     * such fields defined in this header the first one will be returned.
     *
     * @param name the field name (e.g. From, Subject).
     * @return the field or <code>null</code> if none found.
     */
    public Field getField(String name) {
        List<Field> l = fieldMap.get(name.toLowerCase());
        if (l != null && !l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }

    /**
     * Returns <code>true<code/> if there is at least one explicitly
     * set field with the given name.
     *
     * @param name the field name (e.g. From, Subject).
     * @return <code>true<code/> if there is at least one explicitly
     * set field with the given name, <code>false<code/> otherwise.
     */
    public boolean containsField(String name) {
        List<Field> l = fieldMap.get(name.toLowerCase());
        return l != null && !l.isEmpty();
    }

    /**
     * Gets all <code>Field</code>s having the specified field name.
     *
     * @param name the field name (e.g. From, Subject).
     * @return the list of fields.
     */
    public List<Field> getFields(final String name) {
        final String lowerCaseName = name.toLowerCase();
        final List<Field> l = fieldMap.get(lowerCaseName);
        final List<Field> results;
        if (l == null || l.isEmpty()) {
            results = Collections.emptyList();
        } else {
            results = Collections.unmodifiableList(l);
        }
        return results;
    }

    /**
     * Removes all <code>Field</code>s having the specified field name.
     *
     * @param name
     *            the field name (e.g. From, Subject).
     */
    public AbstractEntityBuilder removeFields(String name) {
        final String lowerCaseName = name.toLowerCase();
        List<Field> removed = fieldMap.remove(lowerCaseName);
        if (removed == null || removed.isEmpty()) {
            return this;
        }
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
            Field field = iterator.next();
            if (field.getName().equalsIgnoreCase(name)) {
                iterator.remove();
            }
        }
        return this;
    }

    /**
     * Sets or replaces a field. This method is useful for header fields such as
     * Subject or Message-ID that should not occur more than once in a message.
     *
     * If this builder does not already contain a header field of
     * the same name as the given field then it is added to the end of the list
     * of fields (same behavior as {@link #addField(org.apache.james.mime4j.stream.Field)}). Otherwise the
     * first occurrence of a field with the same name is replaced by the given
     * field and all further occurrences are removed.
     *
     * @param field the field to set.
     */
    public AbstractEntityBuilder setField(Field field) {
        final String lowerCaseName = field.getName().toLowerCase();
        List<Field> l = fieldMap.get(lowerCaseName);
        if (l == null || l.isEmpty()) {
            addField(field);
            return this;
        }

        l.clear();
        l.add(field);

        int firstOccurrence = -1;
        int index = 0;
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); index++) {
            Field f = iterator.next();
            if (f.getName().equalsIgnoreCase(field.getName())) {
                iterator.remove();
                if (firstOccurrence == -1) {
                    firstOccurrence = index;
                }
            }
        }
        fields.add(firstOccurrence, field);
        return this;
    }

    /**
     * Clears all fields.
     */
    public AbstractEntityBuilder clearFields() {
        fields.clear();
        fieldMap.clear();
        return this;
    }

    @SuppressWarnings("unchecked")
    <F extends ParsedField> F obtainField(String fieldName) {
        return (F) getField(fieldName);
    }

    /**
     * Returns MIME type of this message.
     *
     * @return the MIME type or <code>null</code> if no MIME
     *         type has been set.
     */
    public String getMimeType() {
        ContentTypeField field = obtainField(FieldName.CONTENT_TYPE);
        return field != null ? field.getMimeType() : null;
    }

    /**
     * Returns MIME character set encoding of this message.
     *
     * @return the MIME character set encoding or <code>null</code> if no charset
     *         type has been set.
     */
    public String getCharset() {
        ContentTypeField field = obtainField(FieldName.CONTENT_TYPE);
        return field != null ? field.getCharset() : null;
    }

    /**
     * Sets transfer encoding of this message.
     *
     * @param mimeType MIME type of this message
     *            the MIME type to use.
     * @param parameters content type parameters to use.
     */
    public AbstractEntityBuilder setContentType(String mimeType, NameValuePair... parameters) {
        if (mimeType == null) {
            removeFields(FieldName.CONTENT_TYPE);
        } else {
            setField(Fields.contentType(mimeType, parameters));
        }
        return this;
    }

    /**
     * Returns transfer encoding of this message.
     *
     * @return the transfer encoding.
     */
    public String getContentTransferEncoding() {
        ContentTransferEncodingField field = obtainField(FieldName.CONTENT_TRANSFER_ENCODING);
        return field != null ? field.getEncoding() : null;
    }

    /**
     * Sets transfer encoding of this message.
     *
     * @param contentTransferEncoding
     *            transfer encoding to use.
     */
    public AbstractEntityBuilder setContentTransferEncoding(String contentTransferEncoding) {
        if (contentTransferEncoding == null) {
            removeFields(FieldName.CONTENT_TRANSFER_ENCODING);
        } else {
            setField(Fields.contentTransferEncoding(contentTransferEncoding));
        }
        return this;
    }

    /**
     * Return disposition type of this message.
     *
     * @return the disposition type or <code>null</code> if no disposition
     *         type has been set.
     */
    public String getDispositionType() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        return field != null ? field.getDispositionType() : null;
    }

    /**
     * Sets content disposition of this message to the
     * specified disposition type. No filename, size or date parameters
     * are included in the content disposition.
     *
     * @param dispositionType
     *            disposition type value (usually <code>inline</code> or
     *            <code>attachment</code>).
     */
    public AbstractEntityBuilder setContentDisposition(String dispositionType) {
        if (dispositionType == null) {
            removeFields(FieldName.CONTENT_DISPOSITION);
        } else {
            setField(Fields.contentDisposition(dispositionType));
        }
        return this;
    }

    /**
     * Sets content disposition of this message to the
     * specified disposition type and filename. No size or date parameters are
     * included in the content disposition.
     *
     * @param dispositionType
     *            disposition type value (usually <code>inline</code> or
     *            <code>attachment</code>).
     * @param filename
     *            filename parameter value or <code>null</code> if the
     *            parameter should not be included.
     */
    public AbstractEntityBuilder setContentDisposition(String dispositionType, String filename) {
        if (dispositionType == null) {
            removeFields(FieldName.CONTENT_DISPOSITION);
        } else {
            setField(Fields.contentDisposition(dispositionType, filename));
        }
        return this;
    }

    /**
     * Sets content disposition of this message to the
     * specified values. No date parameters are included in the content
     * disposition.
     *
     * @param dispositionType
     *            disposition type value (usually <code>inline</code> or
     *            <code>attachment</code>).
     * @param filename
     *            filename parameter value or <code>null</code> if the
     *            parameter should not be included.
     * @param size
     *            size parameter value or <code>-1</code> if the parameter
     *            should not be included.
     */
    public AbstractEntityBuilder setContentDisposition(String dispositionType, String filename,
                                      long size) {
        if (dispositionType == null) {
            removeFields(FieldName.CONTENT_DISPOSITION);
        } else {
            setField(Fields.contentDisposition(dispositionType, filename, size));
        }
        return this;
    }

    /**
     * Sets content disposition of this message to the
     * specified values.
     *
     * @param dispositionType
     *            disposition type value (usually <code>inline</code> or
     *            <code>attachment</code>).
     * @param filename
     *            filename parameter value or <code>null</code> if the
     *            parameter should not be included.
     * @param size
     *            size parameter value or <code>-1</code> if the parameter
     *            should not be included.
     * @param creationDate
     *            creation-date parameter value or <code>null</code> if the
     *            parameter should not be included.
     * @param modificationDate
     *            modification-date parameter value or <code>null</code> if
     *            the parameter should not be included.
     * @param readDate
     *            read-date parameter value or <code>null</code> if the
     *            parameter should not be included.
     */
    public AbstractEntityBuilder setContentDisposition(String dispositionType, String filename,
                                      long size, Date creationDate, Date modificationDate, Date readDate) {
        if (dispositionType == null) {
            removeFields(FieldName.CONTENT_DISPOSITION);
        } else {
            setField(Fields.contentDisposition(dispositionType, filename, size,
                    creationDate, modificationDate, readDate));
        }
        return this;
    }

    /**
     * Returns filename of the content disposition of this message.
     *
     * @return the filename parameter of the content disposition or
     *         <code>null</code> if the filename has not been set.
     */
    public String getFilename() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        return field != null ? field.getFilename() : null;
    }

    /**
     * Returns size of the content disposition of this message.
     *
     * @return the size parameter of the content disposition or
     *         <code>-1</code> if the filename has not been set.
     */
    public long getSize() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        return field != null ? field.getSize() : -1;
    }

    /**
     * Returns creation date of the content disposition of this message.
     *
     * @return the creation date parameter of the content disposition or
     *         <code>null</code> if the filename has not been set.
     */
    public Date getCreationDate() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        return field != null ? field.getCreationDate() : null;
    }

    /**
     * Returns modification date of the content disposition of this message.
     *
     * @return the modification date parameter of the content disposition or
     *         <code>null</code> if the filename has not been set.
     */
    public Date getModificationDate() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        return field != null ? field.getModificationDate() : null;
    }

    /**
     * Returns read date of the content disposition of this message.
     *
     * @return the read date parameter of the content disposition or
     *         <code>null</code> if the filename has not been set.
     */
    public Date getReadDate() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        return field != null ? field.getReadDate() : null;
    }

    /**
     * Sets body of this message.  Also sets the content type based on properties of
     * the given {@link org.apache.james.mime4j.dom.Body}.
     *
     * @param body
     *            the body.
     */
    public AbstractEntityBuilder setBody(Body body) {
        this.body = body;
        if (!containsField(FieldName.CONTENT_TYPE) && body != null) {
            if (body instanceof Message) {
                setField(Fields.contentType("message/rfc822"));
            } else if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                setField(Fields.contentType("multipart/" + multipart.getSubType(),
                        new NameValuePair("boundary", MimeUtil.createUniqueBoundary())));
            } else if (body instanceof TextBody) {
                TextBody textBody = (TextBody) body;
                String mimeCharset = textBody.getMimeCharset();
                if ("us-ascii".equalsIgnoreCase(mimeCharset)) {
                    mimeCharset = null;
                }
                if (mimeCharset != null) {
                    setField(Fields.contentType("text/plain", new NameValuePair("charset", mimeCharset)));
                } else {
                    setField(Fields.contentType("text/plain"));
                }
            }
        }
        return this;
    }

    /**
     * Returns message body.
     *
     * @return the message body.
     */
    public Body getBody() {
        return body;
    }

}
