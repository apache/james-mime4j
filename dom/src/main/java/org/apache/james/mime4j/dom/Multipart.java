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

import java.util.List;

import org.apache.james.mime4j.stream.NameValuePair;

/**
 * A MIME multipart body (as defined in RFC 2045). A multipart body has a ordered list of
 * body parts. The multipart body also has a preamble and epilogue. The preamble consists of
 * whatever characters appear before the first body part while the epilogue consists of whatever
 * characters come after the last body part.
 */
public interface Multipart extends Body {

    /**
     * Gets the multipart sub-type. E.g. <code>alternative</code> (the
     * default) or <code>parallel</code>. See RFC 2045 for common sub-types
     * and their meaning.
     *
     * @return the multipart sub-type.
     */
    String getSubType();

    /**
     * Returns the number of body parts.
     *
     * @return number of <code>Entity</code> objects.
     */
    int getCount();

    /**
     * Gets the list of body parts. The list is immutable.
     *
     * @return the list of <code>Entity</code> objects.
     */
    List<Entity> getBodyParts();

    /**
     * Sets the list of body parts.
     *
     * @param bodyParts
     *            the new list of <code>Entity</code> objects.
     */
    void setBodyParts(List<Entity> bodyParts);

    /**
     * Adds a body part to the end of the list of body parts.
     *
     * @param bodyPart
     *            the body part.
     */
    void addBodyPart(Entity bodyPart);

    /**
     * Inserts a body part at the specified position in the list of body parts.
     *
     * @param bodyPart
     *            the body part.
     * @param index
     *            index at which the specified body part is to be inserted.
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 || index &gt;
     *             getCount()).
     */
    void addBodyPart(Entity bodyPart, int index);

    /**
     * Removes the body part at the specified position in the list of body
     * parts.
     *
     * @param index
     *            index of the body part to be removed.
     * @return the removed body part.
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 || index &gt;=
     *             getCount()).
     */
    Entity removeBodyPart(int index);

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
    Entity replaceBodyPart(Entity bodyPart, int index);

    /**
     * Gets the preamble or null if the message has no preamble.
     *
     * @return the preamble.
     */
    String getPreamble();

    /**
     * Sets the preamble with a value or null to remove the preamble.
     *
     * @param preamble
     *            the preamble.
     */
    void setPreamble(String preamble);

    /**
     * Gets the epilogue or null if the message has no epilogue
     *
     * @return the epilogue.
     */
    String getEpilogue();

    /**
     * Sets the epilogue value, or remove it if the value passed is null.
     *
     * @param epilogue
     *            the epilogue.
     */
    void setEpilogue(String epilogue);

    List<NameValuePair> getContentTypeParameters();
}
