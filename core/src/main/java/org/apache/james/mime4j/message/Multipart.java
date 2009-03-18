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

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Represents a MIME multipart body (see RFC 2045).A multipart body has a
 * ordered list of body parts. The multipart body also has a preamble and
 * epilogue. The preamble consists of whatever characters appear before the
 * first body part while the epilogue consists of whatever characters come after
 * the last body part.
 */
public class Multipart implements Body {

    private List<BodyPart> bodyParts = new LinkedList<BodyPart>();
    private Entity parent = null;

    private ByteSequence preamble;
    private transient String preambleStrCache;
    private ByteSequence epilogue;
    private transient String epilogueStrCache;

    private String subType;

    /**
     * Creates a new empty <code>Multipart</code> instance.
     */
    public Multipart(String subType) {
        preamble = ByteSequence.EMPTY;
        preambleStrCache = "";
        epilogue = ByteSequence.EMPTY;
        epilogueStrCache = "";

        this.subType = subType;
    }

    /**
     * Creates a new <code>Multipart</code> from the specified
     * <code>Multipart</code>. The <code>Multipart</code> instance is
     * initialized with copies of preamble, epilogue, sub type and the list of
     * body parts of the specified <code>Multipart</code>. The parent entity
     * of the new multipart is <code>null</code>.
     * 
     * @param other
     *            multipart to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link Message}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public Multipart(Multipart other) {
        preamble = other.preamble;
        preambleStrCache = other.preambleStrCache;
        epilogue = other.epilogue;
        epilogueStrCache = other.epilogueStrCache;

        for (BodyPart otherBodyPart : other.bodyParts) {
            BodyPart bodyPartCopy = new BodyPart(otherBodyPart);
            addBodyPart(bodyPartCopy);
        }

        subType = other.subType;
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
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * @see org.apache.james.mime4j.message.Body#getParent()
     */
    public Entity getParent() {
        return parent;
    }

    /**
     * @see org.apache.james.mime4j.message.Body#setParent(org.apache.james.mime4j.message.Entity)
     */
    public void setParent(Entity parent) {
        this.parent = parent;
        for (BodyPart bodyPart : bodyParts) {
            bodyPart.setParent(parent);
        }
    }

    /**
     * Returns the number of body parts.
     * 
     * @return number of <code>BodyPart</code> objects.
     */
    public int getCount() {
        return bodyParts.size();
    }

    /**
     * Gets the list of body parts. The list is immutable.
     * 
     * @return the list of <code>BodyPart</code> objects.
     */
    public List<BodyPart> getBodyParts() {
        return Collections.unmodifiableList(bodyParts);
    }

    /**
     * Sets the list of body parts.
     * 
     * @param bodyParts
     *            the new list of <code>BodyPart</code> objects.
     */
    public void setBodyParts(List<BodyPart> bodyParts) {
        this.bodyParts = bodyParts;
        for (BodyPart bodyPart : bodyParts) {
            bodyPart.setParent(parent);
        }
    }

    /**
     * Adds a body part to the end of the list of body parts.
     * 
     * @param bodyPart
     *            the body part.
     */
    public void addBodyPart(BodyPart bodyPart) {
        if (bodyPart == null)
            throw new IllegalArgumentException();

        bodyParts.add(bodyPart);
        bodyPart.setParent(parent);
    }

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
    public void addBodyPart(BodyPart bodyPart, int index) {
        if (bodyPart == null)
            throw new IllegalArgumentException();

        bodyParts.add(index, bodyPart);
        bodyPart.setParent(parent);
    }

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
    public BodyPart removeBodyPart(int index) {
        BodyPart bodyPart = bodyParts.remove(index);
        bodyPart.setParent(null);
        return bodyPart;
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
    public BodyPart replaceBodyPart(BodyPart bodyPart, int index) {
        if (bodyPart == null)
            throw new IllegalArgumentException();

        BodyPart replacedBodyPart = bodyParts.set(index, bodyPart);
        if (bodyPart == replacedBodyPart)
            throw new IllegalArgumentException(
                    "Cannot replace body part with itself");

        bodyPart.setParent(parent);
        replacedBodyPart.setParent(null);

        return replacedBodyPart;
    }

    // package private for now; might become public someday
    ByteSequence getPreambleRaw() {
        return preamble;
    }

    void setPreambleRaw(ByteSequence preamble) {
        this.preamble = preamble;
        this.preambleStrCache = null;
    }

    /**
     * Gets the preamble.
     * 
     * @return the preamble.
     */
    public String getPreamble() {
        if (preambleStrCache == null) {
            preambleStrCache = ContentUtil.decode(preamble);
        }
        return preambleStrCache;
    }

    /**
     * Sets the preamble.
     * 
     * @param preamble
     *            the preamble.
     */
    public void setPreamble(String preamble) {
        this.preamble = ContentUtil.encode(preamble);
        this.preambleStrCache = preamble;
    }

    // package private for now; might become public someday
    ByteSequence getEpilogueRaw() {
        return epilogue;
    }

    void setEpilogueRaw(ByteSequence epilogue) {
        this.epilogue = epilogue;
        this.epilogueStrCache = null;
    }

    /**
     * Gets the epilogue.
     * 
     * @return the epilogue.
     */
    public String getEpilogue() {
        if (epilogueStrCache == null) {
            epilogueStrCache = ContentUtil.decode(epilogue);
        }
        return epilogueStrCache;
    }

    /**
     * Sets the epilogue.
     * 
     * @param epilogue
     *            the epilogue.
     */
    public void setEpilogue(String epilogue) {
        this.epilogue = ContentUtil.encode(epilogue);
        this.epilogueStrCache = epilogue;
    }

    /**
     * Disposes of the BodyParts of this Multipart. Note that the dispose call
     * does not get forwarded to the parent entity of this Multipart.
     * 
     * @see org.apache.james.mime4j.message.Disposable#dispose()
     */
    public void dispose() {
        for (BodyPart bodyPart : bodyParts) {
            bodyPart.dispose();
        }
    }

}
