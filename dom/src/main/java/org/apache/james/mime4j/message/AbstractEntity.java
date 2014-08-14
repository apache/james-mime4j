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

import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Disposable;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.dom.field.ParsedField;

/**
 * Abstract MIME entity.
 */
public abstract class AbstractEntity implements Entity {
    private Header header = null;
    private Body body = null;
    private Entity parent = null;

    /**
     * Creates a new <code>Entity</code>. Typically invoked implicitly by a
     * subclass constructor.
     */
    protected AbstractEntity() {
    }

    /**
     * Gets the parent entity of this entity.
     * Returns <code>null</code> if this is the root entity.
     *
     * @return the parent or <code>null</code>.
     */
    public Entity getParent() {
        return parent;
    }

    /**
     * Sets the parent entity of this entity.
     *
     * @param parent the parent entity or <code>null</code> if
     *        this will be the root entity.
     */
    public void setParent(Entity parent) {
        this.parent = parent;
    }

    /**
     * Gets the entity header.
     *
     * @return the header.
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Sets the entity header.
     *
     * @param header the header.
     */
    public void setHeader(Header header) {
        this.header = header;
    }

    /**
     * Gets the body of this entity.
     *
     * @return the body,
     */
    public Body getBody() {
        return body;
    }

    /**
     * Sets the body of this entity.
     *
     * @param body the body.
     * @throws IllegalStateException if the body has already been set.
     */
    public void setBody(Body body) {
        if (this.body != null)
            throw new IllegalStateException("body already set");

        this.body = body;
        body.setParent(this);
    }

    /**
     * Removes and returns the body of this entity. The removed body may be
     * attached to another entity. If it is no longer needed it should be
     * {@link Disposable#dispose() disposed} of.
     *
     * @return the removed body or <code>null</code> if no body was set.
     */
    public Body removeBody() {
        if (body == null)
            return null;

        Body body = this.body;
        this.body = null;
        body.setParent(null);

        return body;
    }

    public String getMimeType() {
        ContentTypeField childType = getContentTypeField();
        Entity parent = getParent();
        ContentTypeField parentType = parent != null ? (ContentTypeField) (parent).getHeader().getField(FieldName.CONTENT_TYPE) : null;
        return calcMimeType(childType, parentType);
    }

    private ContentTypeField getContentTypeField() {
        return (ContentTypeField) getHeader().getField(FieldName.CONTENT_TYPE);
    }

    /**
     * Determines the MIME character set encoding of this <code>Entity</code>.
     *
     * @return the MIME character set encoding.
     */
    public String getCharset() {
        return calcCharset((ContentTypeField) getHeader().getField(FieldName.CONTENT_TYPE));
    }

    /**
     * Determines the transfer encoding of this <code>Entity</code>.
     *
     * @return the transfer encoding.
     */
    public String getContentTransferEncoding() {
        ContentTransferEncodingField f = (ContentTransferEncodingField)
                        getHeader().getField(FieldName.CONTENT_TRANSFER_ENCODING);

        return calcTransferEncoding(f);
    }

    /**
     * Return the disposition type of the content disposition of this
     * <code>Entity</code>.
     *
     * @return the disposition type or <code>null</code> if no disposition
     *         type has been set.
     */
    public String getDispositionType() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        if (field == null)
            return null;

        return field.getDispositionType();
    }

    /**
     * Returns the filename parameter of the content disposition of this
     * <code>Entity</code>.
     *
     * @return the filename parameter of the content disposition or
     *         <code>null</code> if the filename has not been set.
     */
    public String getFilename() {
        ContentDispositionField field = obtainField(FieldName.CONTENT_DISPOSITION);
        if (field == null)
            return null;

        return field.getFilename();
    }

    /**
     * Determines if the MIME type of this <code>Entity</code> is
     * <code>multipart/*</code>. Since multipart-entities must have
     * a boundary parameter in the <code>Content-Type</code> field this
     * method returns <code>false</code> if no boundary exists.
     *
     * @return <code>true</code> on match, <code>false</code> otherwise.
     */
    public boolean isMultipart() {
        ContentTypeField f = getContentTypeField();
        return f != null
                && f.getBoundary() != null
                && getMimeType().startsWith(
                        ContentTypeField.TYPE_MULTIPART_PREFIX);
    }

    /**
     * Disposes of the body of this entity. Note that the dispose call does not
     * get forwarded to the parent entity of this Entity.
     *
     * Subclasses that need to free resources should override this method and
     * invoke super.dispose().
     *
     * @see org.apache.james.mime4j.dom.Disposable#dispose()
     */
    public void dispose() {
        if (body != null) {
            body.dispose();
        }
    }

    /**
     * Obtains the header of this entity. Creates and sets a new header if this
     * entity's header is currently <code>null</code>.
     *
     * @return the header of this entity; never <code>null</code>.
     */
    Header obtainHeader() {
        if (header == null) {
            header = new HeaderImpl();
        }
        return header;
    }

    /**
     * Obtains the header field with the specified name.
     *
     * @param <F>
     *            concrete field type.
     * @param fieldName
     *            name of the field to retrieve.
     * @return the header field or <code>null</code> if this entity has no
     *         header or the header contains no such field.
     */
    <F extends ParsedField> F obtainField(String fieldName) {
        Header header = getHeader();
        if (header == null)
            return null;

        return (F) header.getField(fieldName);
    }

    protected abstract String calcMimeType(ContentTypeField child, ContentTypeField parent);

    protected abstract String calcTransferEncoding(ContentTransferEncodingField f);

    protected abstract String calcCharset(ContentTypeField contentType);
}
