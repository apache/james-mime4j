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

package org.apache.james.mime4j.dom;

/**
 * MIME entity. An entity has a header and a body (see RFC 2045).
 */
public interface Entity extends Disposable {

    /**
     * Gets the parent entity of this entity.
     * Returns <code>null</code> if this is the root entity.
     *
     * @return the parent or <code>null</code>.
     */
    Entity getParent();

    /**
     * Sets the parent entity of this entity.
     *
     * @param parent the parent entity or <code>null</code> if
     *        this will be the root entity.
     */
    void setParent(Entity parent);

    /**
     * Gets the entity header.
     *
     * @return the header.
     */
    Header getHeader();

    /**
     * Sets the entity header.
     *
     * @param header the header.
     */
    void setHeader(Header header);

    /**
     * Gets the body of this entity.
     *
     * @return the body,
     */
    Body getBody();

    /**
     * Sets the body of this entity.
     *
     * @param body the body.
     * @throws IllegalStateException if the body has already been set.
     */
    void setBody(Body body);

    /**
     * Removes and returns the body of this entity. The removed body may be
     * attached to another entity. If it is no longer needed it should be
     * {@link Disposable#dispose() disposed} of.
     *
     * @return the removed body or <code>null</code> if no body was set.
     */
    Body removeBody();

    /**
     * Determines if the MIME type of this <code>Entity</code> is
     * <code>multipart/*</code>. Since multipart-entities must have
     * a boundary parameter in the <code>Content-Type</code> field this
     * method returns <code>false</code> if no boundary exists.
     *
     * @return <code>true</code> on match, <code>false</code> otherwise.
     */
    boolean isMultipart();

    /**
     * Determines the MIME type of this <code>Entity</code>. The MIME type
     * is derived by looking at the parent's Content-Type field if no
     * Content-Type field is set for this <code>Entity</code>.
     *
     * @return the MIME type.
     */
    String getMimeType();

    /**
     * Determines the MIME character set encoding of this <code>Entity</code>.
     *
     * @return the MIME character set encoding.
     */
    String getCharset();

    /**
     * Determines the transfer encoding of this <code>Entity</code>.
     *
     * @return the transfer encoding.
     */
    String getContentTransferEncoding();

    /**
     * Return the disposition type of the content disposition of this
     * <code>Entity</code>.
     *
     * @return the disposition type or <code>null</code> if no disposition
     *         type has been set.
     */
    String getDispositionType();

    /**
     * Returns the filename parameter of the content disposition of this
     * <code>Entity</code>.
     *
     * @return the filename parameter of the content disposition or
     *         <code>null</code> if the filename has not been set.
     */
    String getFilename();

}
