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

import java.util.Iterator;
import java.util.List;

import org.apache.james.mime4j.stream.Field;

/**
 * A header of an MIME entity (as defined in RFC 2045).
 */
public interface Header extends Iterable<Field> {

    /**
     * Adds a field to the end of the list of fields.
     *
     * @param field the field to add.
     */
    void addField(Field field);

    /**
     * Gets the fields of this header. The returned list will not be
     * modifiable.
     *
     * @return the list of <code>Field</code> objects.
     */
    List<Field> getFields();

    /**
     * Gets a <code>Field</code> given a field name. If there are multiple
     * such fields defined in this header the first one will be returned.
     *
     * @param name the field name (e.g. From, Subject).
     * @return the field or <code>null</code> if none found.
     */
    Field getField(String name);

    /**
     * Gets a <code>Field</code> given a field name and of the given type.
     * If there are multiple such fields defined in this header the first
     * one will be returned.
     *
     * @param name the field name (e.g. From, Subject).
     * @param clazz the field class.
     * @return the field or <code>null</code> if none found.
     */
    <F extends Field> F getField(String name, Class<F> clazz);

    /**
     * Gets all <code>Field</code>s having the specified field name.
     *
     * @param name the field name (e.g. From, Subject).
     * @return the list of fields.
     */
    List<Field> getFields(final String name);

    /**
     * Gets all <code>Field</code>s having the specified field name
     * and of the given type.
     *
     * @param name the field name (e.g. From, Subject).
     * @param clazz the field class.
     * @return the list of fields.
     */
    <F extends Field> List<F> getFields(final String name, Class<F> clazz);

    /**
     * Returns an iterator over the list of fields of this header.
     *
     * @return an iterator.
     */
    Iterator<Field> iterator();

    /**
     * Removes all <code>Field</code>s having the specified field name.
     *
     * @param name
     *            the field name (e.g. From, Subject).
     * @return number of fields removed.
     */
    int removeFields(String name);

    /**
     * Sets or replaces a field. This method is useful for header fields such as
     * Subject or Message-ID that should not occur more than once in a message.
     *
     * If this <code>Header</code> does not already contain a header field of
     * the same name as the given field then it is added to the end of the list
     * of fields (same behavior as {@link #addField(Field)}). Otherwise the
     * first occurrence of a field with the same name is replaced by the given
     * field and all further occurrences are removed.
     *
     * @param field the field to set.
     */
    void setField(Field field);

}
