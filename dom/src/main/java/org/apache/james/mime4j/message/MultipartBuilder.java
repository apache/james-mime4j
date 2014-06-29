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
import java.util.LinkedList;
import java.util.List;

import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Multipart;

/**
 * {@link org.apache.james.mime4j.dom.Multipart} builder.
 */
public class MultipartBuilder {

    private final List<Entity> bodyParts;
    private String subType;
    private String preamble;
    private String epilogue;

    public static MultipartBuilder create(String subType) {
        return new MultipartBuilder().setSubType(subType);
    }

    public static MultipartBuilder create() {
        return new MultipartBuilder();
    }

    private MultipartBuilder() {
        this.bodyParts = new LinkedList<Entity>();
    }

    /**
     * Gets the multipart sub-type. E.g. <code>alternative</code> (the
     * default) or <code>parallel</code>. See RFC 2045 for common sub-types
     * and their meaning.
     *
     * @return the multipart sub-type.
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets the multipart sub-type. E.g. <code>alternative</code> or
     * <code>parallel</code>. See RFC 2045 for common sub-types and their
     * meaning.
     *
     * @param subType
     *            the sub-type.
     */
    public MultipartBuilder setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    /**
     * Returns the number of body parts.
     *
     * @return number of <code>Entity</code> objects.
     */
    public int getCount() {
        return bodyParts.size();
    }

    /**
     * Gets the list of body parts. The list is immutable.
     *
     * @return the list of <code>Entity</code> objects.
     */
    public List<Entity> getBodyParts() {
        return Collections.unmodifiableList(bodyParts);
    }

    /**
     * Adds a body part to the end of the list of body parts.
     *
     * @param bodyPart
     *            the body part.
     */
    public MultipartBuilder addBodyPart(Entity bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException();
        }
        bodyParts.add(bodyPart);
        return this;
    }

    /**
     * Inserts a body part at the specified position in the list of body parts.
     *
     * @param bodyPart
     *            the body part.
     * @param index
     *            index at which the specified body part is to be inserted.
     */
    public MultipartBuilder addBodyPart(Entity bodyPart, int index) {
        if (bodyPart == null) {
            throw new IllegalArgumentException();
        }
        bodyParts.add(index, bodyPart);
        return this;
    }

    /**
     * Removes the body part at the specified position in the list of body
     * parts.
     *
     * @param index
     *            index of the body part to be removed.
     * @return the removed body part.
     */
    public MultipartBuilder removeBodyPart(int index) {
        bodyParts.remove(index);
        return this;
    }

    /**
     * Replaces the body part at the specified position in the list of body
     * parts with the specified body part.
     *
     * @param bodyPart
     *            body part to be stored at the specified position.
     * @param index
     *            index of body part to replace.
     * @return the replaced body part.
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 || index &gt;=
     *             getCount()).
     */
    public MultipartBuilder replaceBodyPart(Entity bodyPart, int index) {
        if (bodyPart == null) {
            throw new IllegalArgumentException();
        }
        bodyParts.set(index, bodyPart);
        return this;
    }

    /**
     * Returns preamble.
     *
     * @return the preamble.
     */
    public String getPreamble() {
        return preamble;
    }

    /**
     * Sets the preamble.
     *
     * @param preamble
     *            the preamble.
     */
    public MultipartBuilder setPreamble(String preamble) {
        this.preamble = preamble;
        return this;
    }

    /**
     * Returns epilogue.
     *
     * @return the epilogue.
     */
    public String getEpilogue() {
        return epilogue;
    }

    /**
     * Sets the epilogue.
     *
     * @param epilogue
     *            the epilogue.
     */
    public MultipartBuilder setEpilogue(String epilogue) {
        this.epilogue = epilogue;
        return this;
    }

    public Multipart build() {
        MultipartImpl multipart = new MultipartImpl(subType);
        for (Entity part : bodyParts) {
            multipart.addBodyPart(part);
        }
        multipart.setPreamble(preamble);
        multipart.setEpilogue(epilogue);
        return multipart;
    }

}
