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

package org.apache.james.mime4j.dom.field;

import java.util.Date;
import java.util.Map;

public interface ContentDispositionField extends ParsedField {

    /** The <code>inline</code> disposition type. */
    String DISPOSITION_TYPE_INLINE = "inline";
    /** The <code>attachment</code> disposition type. */
    String DISPOSITION_TYPE_ATTACHMENT = "attachment";
    /** The name of the <code>filename</code> parameter. */
    String PARAM_FILENAME = "filename";
    /** The name of the <code>creation-date</code> parameter. */
    String PARAM_CREATION_DATE = "creation-date";
    /** The name of the <code>modification-date</code> parameter. */
    String PARAM_MODIFICATION_DATE = "modification-date";
    /** The name of the <code>read-date</code> parameter. */
    String PARAM_READ_DATE = "read-date";
    /** The name of the <code>size</code> parameter. */
    String PARAM_SIZE = "size";

    /**
     * Gets the disposition type defined in this Content-Disposition field.
     *
     * @return the disposition type or an empty string if not set.
     */
    String getDispositionType();

    /**
     * Gets the value of a parameter. Parameter names are case-insensitive.
     *
     * @param name
     *            the name of the parameter to get.
     * @return the parameter value or <code>null</code> if not set.
     */
    String getParameter(String name);

    /**
     * Gets all parameters.
     *
     * @return the parameters.
     */
    Map<String, String> getParameters();

    /**
     * Determines if the disposition type of this field matches the given one.
     *
     * @param dispositionType
     *            the disposition type to match against.
     * @return <code>true</code> if the disposition type of this field
     *         matches, <code>false</code> otherwise.
     */
    boolean isDispositionType(String dispositionType);

    /**
     * Return <code>true</code> if the disposition type of this field is
     * <i>inline</i>, <code>false</code> otherwise.
     *
     * @return <code>true</code> if the disposition type of this field is
     *         <i>inline</i>, <code>false</code> otherwise.
     */
    boolean isInline();

    /**
     * Return <code>true</code> if the disposition type of this field is
     * <i>attachment</i>, <code>false</code> otherwise.
     *
     * @return <code>true</code> if the disposition type of this field is
     *         <i>attachment</i>, <code>false</code> otherwise.
     */
    boolean isAttachment();

    /**
     * Gets the value of the <code>filename</code> parameter if set.
     *
     * @return the <code>filename</code> parameter value or <code>null</code>
     *         if not set.
     */
    String getFilename();

    /**
     * Gets the value of the <code>creation-date</code> parameter if set and
     * valid.
     *
     * @return the <code>creation-date</code> parameter value or
     *         <code>null</code> if not set or invalid.
     */
    Date getCreationDate();

    /**
     * Gets the value of the <code>modification-date</code> parameter if set
     * and valid.
     *
     * @return the <code>modification-date</code> parameter value or
     *         <code>null</code> if not set or invalid.
     */
    Date getModificationDate();

    /**
     * Gets the value of the <code>read-date</code> parameter if set and
     * valid.
     *
     * @return the <code>read-date</code> parameter value or <code>null</code>
     *         if not set or invalid.
     */
    Date getReadDate();

    /**
     * Gets the value of the <code>size</code> parameter if set and valid.
     *
     * @return the <code>size</code> parameter value or <code>-1</code> if
     *         not set or invalid.
     */
    long getSize();

}
