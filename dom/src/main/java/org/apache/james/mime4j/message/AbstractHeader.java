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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.stream.Field;

/**
 * Abstract MIME header.
 */
public abstract class AbstractHeader implements Header {

    private final List<Field> fields = new LinkedList<Field>();
    private final Map<String, List<Field>> fieldMap = new HashMap<String, List<Field>>();

    /**
     * Creates a new empty <code>Header</code>.
     */
    public AbstractHeader() {
    }

    /**
     * Creates a new <code>Header</code> from the specified
     * <code>Header</code>. The <code>Header</code> instance is initialized
     * with a copy of the list of {@link Field}s of the specified
     * <code>Header</code>. The <code>Field</code> objects are not copied
     * because they are immutable and can safely be shared between headers.
     *
     * @param other
     *            header to copy.
     */
    public AbstractHeader(Header other) {
        for (Field otherField : other.getFields()) {
            addField(otherField);
        }
    }

    /**
     * Adds a field to the end of the list of fields.
     *
     * @param field the field to add.
     */
    public void addField(Field field) {
        String lowerCaseFieldName = field.getNameLowerCase();
        List<Field> values = fieldMap.get(lowerCaseFieldName);
        if (values == null) {
            values = new LinkedList<Field>();
            fieldMap.put(lowerCaseFieldName, values);
        }
        values.add(field);
        fields.add(field);
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
     * Gets the fields of this header. The returned map will not be
     * modifiable. For each header name, values are ordered by which
     * they appear in the underlying entity.
     *
     * @return the map of <code>Field</code> objects indexed by names.
     */
    @Override
    public Map<String, List<Field>> getFieldsAsMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    /**
     * Gets a <code>Field</code> given a field name. If there are multiple
     * such fields defined in this header the first one will be returned.
     *
     * @param name the field name (e.g. From, Subject).
     * @return the field or <code>null</code> if none found.
     */
    public Field getField(String name) {
        List<Field> l = fieldMap.get(name.toLowerCase(Locale.US));
        if (l != null && !l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }

    /**
     * Gets a <code>Field</code> given a field name and of the given type.
     * If there are multiple such fields defined in this header the first
     * one will be returned.
     *
     * @param name the field name (e.g. From, Subject).
     * @param clazz the field class.
     * @return the field or <code>null</code> if none found.
     */
    public <F extends Field> F getField(final String name, final Class<F> clazz) {
        List<Field> l = fieldMap.get(name.toLowerCase(Locale.US));
        for (int i = 0; i < l.size(); i++) {
            Field field = l.get(i);
            if (clazz.isInstance(field)) {
                return clazz.cast(field);
            }
        }
        return null;
    }

    /**
     * Gets all <code>Field</code>s having the specified field name.
     *
     * @param name the field name (e.g. From, Subject).
     * @return the list of fields.
     */
    public List<Field> getFields(final String name) {
        final String lowerCaseName = name.toLowerCase(Locale.US);
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
     * Gets all <code>Field</code>s having the specified field name
     * and of the given type.
     *
     * @param name the field name (e.g. From, Subject).
     * @param clazz the field class.
     * @return the list of fields.
     */
    public <F extends Field> List<F> getFields(final String name, final Class<F> clazz) {
        final String lowerCaseName = name.toLowerCase(Locale.US);
        final List<Field> l = fieldMap.get(lowerCaseName);
        if (l == null) {
            return Collections.emptyList();
        }
        final List<F> results = new ArrayList<F>();
        for (int i = 0; i < l.size(); i++) {
            Field field = l.get(i);
            if (clazz.isInstance(field)) {
                results.add(clazz.cast(field));
            }
        }
        return results;
    }

    /**
     * Returns an iterator over the list of fields of this header.
     *
     * @return an iterator.
     */
    public Iterator<Field> iterator() {
        return Collections.unmodifiableList(fields).iterator();
    }

    /**
     * Removes all <code>Field</code>s having the specified field name.
     *
     * @param name
     *            the field name (e.g. From, Subject).
     * @return number of fields removed.
     */
    public int removeFields(String name) {
        final String lowerCaseName = name.toLowerCase(Locale.US);
        List<Field> removed = fieldMap.remove(lowerCaseName);
        if (removed == null || removed.isEmpty())
            return 0;

        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
            Field field = iterator.next();
            if (field.getName().equalsIgnoreCase(name))
                iterator.remove();
        }

        return removed.size();
    }

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
    public void setField(Field field) {
        final String lowerCaseName = field.getNameLowerCase();
        List<Field> l = fieldMap.get(lowerCaseName);
        if (l == null || l.isEmpty()) {
            addField(field);
            return;
        }

        l.clear();
        l.add(field);

        int firstOccurrence = -1;
        int index = 0;
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); index++) {
            Field f = iterator.next();
            if (f.getName().equalsIgnoreCase(field.getName())) {
                iterator.remove();

                if (firstOccurrence == -1)
                    firstOccurrence = index;
            }
        }

        fields.add(firstOccurrence, field);
    }

    /**
     * Return Header Object as String representation. Each headerline is
     * seperated by "\r\n"
     *
     * @return headers
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(128);
        for (Field field : fields) {
            str.append(field.toString());
            str.append("\r\n");
        }
        return str.toString();
    }

}
