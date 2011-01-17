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

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

/**
 * Represents a MIME multipart body (see RFC 2045).A multipart body has a
 * ordered list of body parts. The multipart body also has a preamble and
 * epilogue. The preamble consists of whatever characters appear before the
 * first body part while the epilogue consists of whatever characters come after
 * the last body part.
 */
public class MultipartImpl extends MultipartBase {

    private ByteSequence preamble;
    private transient String preambleStrCache;
    private transient boolean preambleComputed = false;
    private ByteSequence epilogue;
    private transient String epilogueStrCache;
    private transient boolean epilogueComputed = false;

    /**
     * Creates a new empty <code>Multipart</code> instance.
     */
    public MultipartImpl(String subType) {
        super(subType);
        preamble = null;
        preambleStrCache = null;
        preambleComputed = true;
        epilogue = null;
        epilogueStrCache = null;
        epilogueComputed = true;
    }

    // package private for now; might become public someday
    public ByteSequence getPreambleRaw() {
        return preamble;
    }

    public void setPreambleRaw(ByteSequence preamble) {
        this.preamble = preamble;
        this.preambleStrCache = null;
        this.preambleComputed = false;
    }

    /**
     * Gets the preamble.
     * 
     * @return the preamble.
     */
    @Override
    public String getPreamble() {
        if (!preambleComputed) {
            preambleStrCache = preamble != null ? ContentUtil.decode(preamble) : null;
            preambleComputed = true;
        }
        return preambleStrCache;
    }

    /**
     * Sets the preamble.
     * 
     * @param preamble
     *            the preamble.
     */
    @Override
    public void setPreamble(String preamble) {
        this.preamble = preamble != null ? ContentUtil.encode(preamble) : null;
        this.preambleStrCache = preamble;
        this.preambleComputed = true;
    }

    // package private for now; might become public someday
    public ByteSequence getEpilogueRaw() {
        return epilogue;
    }

    public void setEpilogueRaw(ByteSequence epilogue) {
        this.epilogue = epilogue;
        this.epilogueStrCache = null;
        this.epilogueComputed = false;
    }

    /**
     * Gets the epilogue.
     * 
     * @return the epilogue.
     */
    @Override
    public String getEpilogue() {
        if (!epilogueComputed) {
            epilogueStrCache = epilogue != null ? ContentUtil.decode(epilogue) : null;
            epilogueComputed = true;
        }
        return epilogueStrCache;
    }

    /**
     * Sets the epilogue.
     * 
     * @param epilogue
     *            the epilogue.
     */
    @Override
    public void setEpilogue(String epilogue) {
        this.epilogue = epilogue != null ? ContentUtil.encode(epilogue) : null;
        this.epilogueStrCache = epilogue;
        this.epilogueComputed = true;
    }

}
