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

package org.apache.james.mime4j.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.Field;

/**
 * An object, which may be used to implement header, or parameter
 * maps. The maps keys are the header or parameter names. The
 * maps values are strings (single value), lists, or arrays.
 * <p>
 * Note that this class is not directly used anywhere in Mime4j.
 * Instead a user might choose to use it instead of {@link Header}
 * and {@link Field} in a custom {@link ContentHandler} implementation.
 * See also MIME4j-24.
 */
public class StringArrayMap implements Serializable {
    private static final long serialVersionUID = -5833051164281786907L;
    private final Map<String, Object> map = new HashMap<String, Object>();

    /**
     * <p>Converts the given object into a string. The object may be either of:
     * <ul>
     *   <li>a string, which is returned without conversion</li>
     *   <li>a list of strings, in which case the first element is returned</li>
     *   <li>an array of strings, in which case the first element is returned</li>
     * </ul>
     */
    public static String asString(Object pValue) {
        if (pValue == null) {
            return null;
        }
        if (pValue instanceof String) {
            return (String) pValue;
        }
        if (pValue instanceof String[]) {
            return ((String[]) pValue)[0];
        }
        if (pValue instanceof List) {
            return (String) ((List<?>) pValue).get(0);
        }
        throw new IllegalStateException("Invalid parameter class: " + pValue.getClass().getName());
    }

    /**
     * <p>Converts the given object into a string array. The object may be either of:
     * <ul>
     *   <li>a string, which is returned as an array with one element</li>
     *   <li>a list of strings, which is being converted into a string array</li>
     *   <li>an array of strings, which is returned without conversion</li>
     * </ul>
     */
    public static String[] asStringArray(Object pValue) {
        if (pValue == null) {
            return null;
        }
        if (pValue instanceof String) {
            return new String[]{(String) pValue};
        }
        if (pValue instanceof String[]) {
            return (String[]) pValue;
        }
        if (pValue instanceof List) {
            final List<?> l = (List<?>) pValue;
            return l.toArray(new String[l.size()]);
        }
        throw new IllegalStateException("Invalid parameter class: " + pValue.getClass().getName());
    }

    /**
     * <p>Converts the given object into a string enumeration. The object may be either of:
     * <ul>
     *   <li>a string, which is returned as an enumeration with one element</li>
     *   <li>a list of strings, which is being converted into a string enumeration</li>
     *   <li>an array of strings, which is being converted into a string enumeration</li>
     * </ul>
     */
    public static Enumeration<String> asStringEnum(final Object pValue) {
        if (pValue == null) {
            return null;
        }
        if (pValue instanceof String) {
            return new Enumeration<String>(){
                private Object value = pValue;
                public boolean hasMoreElements() {
                    return value != null;
                }
                public String nextElement() {
                    if (value == null) {
                        throw new NoSuchElementException();
                    }
                    final String s = (String) value;
                    value = null;
                    return s;
                }
            };
        }
        if (pValue instanceof String[]) {
            final String[] values = (String[]) pValue;
            return new Enumeration<String>() {
                private int offset;
                public boolean hasMoreElements() {
                    return offset < values.length;
                }
                public String nextElement() {
                    if (offset >= values.length) {
                        throw new NoSuchElementException();
                    }
                    return values[offset++];
                }
            };
        }
        if (pValue instanceof List) {
            @SuppressWarnings("unchecked")
            final List<String> stringList = (List<String>) pValue; 
            return Collections.enumeration(stringList);
        }
        throw new IllegalStateException("Invalid parameter class: " + pValue.getClass().getName());
    }

    /**
     * Converts the given map into a string array map: The map values
     * are string arrays.
     */
    public static Map<String, String[]> asMap(final Map<String, Object> pMap) {
        Map<String, String[]> result = new HashMap<String, String[]>(pMap.size());
        for (Map.Entry<String, Object> entry : pMap.entrySet()) {
            final String[] value = asStringArray(entry.getValue());
            result.put(entry.getKey(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Adds a value to the given map.
     */
    protected void addMapValue(Map<String, Object> pMap, String pName, String pValue) {
        Object o = pMap.get(pName);
        if (o == null) {
            o = pValue;
        } else if (o instanceof String) {
            final List<Object> list = new ArrayList<Object>();
            list.add(o);
            list.add(pValue);
            o = list;
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            final List<String> stringList = (List<String>) o; 
            stringList.add(pValue);
        } else if (o instanceof String[]) {
            final List<String> list = new ArrayList<String>();
            final String[] arr = (String[]) o;
            for (String str : arr) {
                list.add(str);
            }
            list.add(pValue);
            o = list;
        } else {
            throw new IllegalStateException("Invalid object type: " + o.getClass().getName());
        }
        pMap.put(pName, o);
    }

    /**
     * Lower cases the given name.
     */
    protected String convertName(String pName) {
        return pName.toLowerCase();
    }

    /**
     * Returns the requested value.
     */
    public String getValue(String pName) {
        return asString(map.get(convertName(pName)));
    }

    /**
     * Returns the requested values as a string array.
     */
    public String[] getValues(String pName) {
        return asStringArray(map.get(convertName(pName)));
    }

    /**
     * Returns the requested values as an enumeration.
     */
    public Enumeration<String> getValueEnum(String pName) {
        return asStringEnum(map.get(convertName(pName)));
    }

    /**
     * Returns the set of registered names as an enumeration.
     * @see #getNameArray()
     */
    public Enumeration<String> getNames() {
        return Collections.enumeration(map.keySet());
    }

    /**
     * Returns an unmodifiable map of name/value pairs. The map keys
     * are the lower cased parameter/header names. The map values are
     * string arrays.
     */
    public Map<String, String[]> getMap() {
        return asMap(map);
    }

    /**
     * Adds a new name/value pair.
     */
    public void addValue(String pName, String pValue) {
        addMapValue(map, convertName(pName), pValue);
    }

    /**
     * Returns the set of registered names.
     * @see #getNames()
     */
    public String[] getNameArray() {
        final Collection<String> c = map.keySet();
        return c.toArray(new String[c.size()]);
    }
}
