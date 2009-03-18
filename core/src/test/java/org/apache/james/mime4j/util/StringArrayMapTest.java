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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

public class StringArrayMapTest extends TestCase {
    private StringArrayMap getSampleMap() {
        final StringArrayMap map = new StringArrayMap();
        map.addValue("xYz", "a");
        map.addValue("Xyz", "B");
        map.addValue("xyz", "c");
        map.addValue("xs", "1");
        map.addValue("XS", "2");
        map.addValue("foo", "bAr");
        return map;
    }

    public void testGetMap() {
        final Map<String, String[]> map = getSampleMap().getMap();
        final List<String> keys = new ArrayList<String>(map.keySet());
        assertEquals(keys.size(), 3);
        Collections.sort(keys);
        assertEquals("foo", keys.get(0));
        assertEquals("xs", keys.get(1));
        assertEquals("xyz", keys.get(2));
        final String[] foo = map.get("foo");
        assertEquals("bAr", foo[0]);
        final String[] xs = map.get("xs");
        assertEquals("1", xs[0]);
        assertEquals("2", xs[1]);
        final String[] xyz = map.get("xyz");
        assertEquals(3, xyz.length);
        assertEquals("a", xyz[0]);
        assertEquals("B", xyz[1]);
        assertEquals("c", xyz[2]);
    }

    public void testGetNameArray() {
        final String[] names = getSampleMap().getNameArray();
        assertEquals(3, names.length);
        Arrays.sort(names);
        assertEquals("foo", names[0]);
        assertEquals("xs", names[1]);
        assertEquals("xyz", names[2]);
    }

    public void testGetNames() {
        final Enumeration<String> names = getSampleMap().getNames();
        assertEquals(new String[]{"foo", "xs", "xyz"}, names);
    }

    public void testGetValue() {
        final StringArrayMap map = getSampleMap();
        assertEquals("bAr", map.getValue("foo"));
        assertEquals("bAr", map.getValue("FOO"));
        assertEquals("1", map.getValue("xs"));
        assertEquals("a", map.getValue("xyz"));
        assertEquals("a", map.getValue("xyZ"));
        assertNull(map.getValue("xz"));
    }

    public void testGetValues() {
        final StringArrayMap map = getSampleMap();
        final String[] foo = map.getValues("foo");
        assertEquals(1, foo.length);
        assertEquals("bAr", foo[0]);
        final String[] FOO = map.getValues("FOO");
        assertEquals(1, FOO.length);
        assertEquals("bAr", FOO[0]);
        final String[] xs = map.getValues("xs");
        assertEquals(2, xs.length);
        assertEquals("1", xs[0]);
        assertEquals("2", xs[1]);
        final String[] XS = map.getValues("XS");
        assertEquals(2, XS.length);
        assertEquals("1", XS[0]);
        assertEquals("2", XS[1]);
        final String[] xyz = map.getValues("xyz");
        assertEquals("a", xyz[0]);
        assertEquals("B", xyz[1]);
        assertEquals("c", xyz[2]);
        final String[] XYZ = map.getValues("XYZ");
        assertEquals("a", XYZ[0]);
        assertEquals("B", XYZ[1]);
        assertEquals("c", XYZ[2]);
        assertNull(map.getValues("xz"));
    }

    private void assertEquals(String[] pArray, Enumeration<String> pEnum) {
        final List<String> list = new ArrayList<String>();
        while (pEnum.hasMoreElements()) {
            list.add(pEnum.nextElement());
        }
        Collections.sort(list, Collator.getInstance(Locale.US));
        assertEquals(pArray.length, list.size());
        for (int i = 0;  i < pArray.length;  i++) {
            assertEquals(pArray[i], list.get(i));
        }
    }

    public void testGetValueEnum() {
        final StringArrayMap map = getSampleMap();
        assertEquals(new String[]{"bAr"}, map.getValueEnum("foo"));
        assertEquals(new String[]{"bAr"}, map.getValueEnum("FOO"));
        assertEquals(new String[]{"1", "2"}, map.getValueEnum("xs"));
        assertEquals(new String[]{"1", "2"}, map.getValueEnum("Xs"));
        assertEquals(new String[]{"a", "B", "c"}, map.getValueEnum("xyz"));
        assertEquals(new String[]{"a", "B", "c"}, map.getValueEnum("XYZ"));
        assertNull(map.getValues("xz"));
    }
}
