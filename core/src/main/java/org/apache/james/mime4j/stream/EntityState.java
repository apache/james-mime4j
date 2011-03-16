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

package org.apache.james.mime4j.stream;

/**
 * Enumeration of states an entity is expected to go through
 * in the process of the MIME stream parsing.
 */
public enum EntityState {
    /**
     * This token indicates, that the MIME stream is currently
     * at the beginning of a message.
     */
    T_START_MESSAGE,
    /**
     * This token indicates, that the MIME stream is currently
     * at the end of a message.
     */
    T_END_MESSAGE,
    /**
     * This token indicates, that a raw entity is currently being processed.
     */
    T_RAW_ENTITY,
    /**
     * This token indicates, that a message parts headers are now
     * being parsed.
     */
    T_START_HEADER,
    /**
     * This token indicates, that a message parts field has now
     * been parsed. 
     */
    T_FIELD,
    /**
     * This token indicates, that part headers have now been
     * parsed.
     */
    T_END_HEADER,
    /**
     * This token indicates, that a multipart body is being parsed.
     */
    T_START_MULTIPART,
    /**
     * This token indicates, that a multipart body has been parsed.
     */
    T_END_MULTIPART,
    /**
     * This token indicates, that a multiparts preamble is being
     * parsed. 
     */
    T_PREAMBLE,
    /**
     * This token indicates, that a multiparts epilogue is being
     * parsed. 
     */
    T_EPILOGUE,
    /**
     * This token indicates, that the MIME stream is currently
     * at the beginning of a body part.
     */
    T_START_BODYPART,
    /**
     * This token indicates, that the MIME stream is currently
     * at the end of a body part.
     */
    T_END_BODYPART,
    /**
     * This token indicates, that an atomic entity is being parsed.
     */
    T_BODY,
    /**
     * This token indicates, that the MIME stream has been completely
     * and successfully parsed, and no more data is available.
     */
    T_END_OF_STREAM

}
