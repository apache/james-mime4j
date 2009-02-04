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

package org.apache.james.mime4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class TestUtil {
    public static final String[] TEST_MESSAGES = new String[] {
            "2002_06_12_doublebound",
            "ak-0696",
            "bluedot-postcard",
            "bluedot-simple",
            "double-bound-with-embedded",
            "double-bound",
            "dup-names",
            "frag",
            "german",
            "hdr-fakeout",
            "multi-2evil",
            "multi-2gifs",
            "multi-clen",
            "multi-digest",
            "multi-frag",
            "multi-igor",
            "multi-igor2",
            "multi-nested",
            "multi-nested2",
            "multi-nested3",
            "multi-simple",
            "multi-weirdspace",
            "re-fwd",
            "russian",
            "simple",
            "uu-junk-target",
            "uu-junk",
            "uu-zeegee"
    };
    
    public static String readResource(String resource, String charset) 
            throws IOException {
        
        return IOUtils.toString(readResourceAsStream(resource), charset);
    }

    public static InputStream readResourceAsStream(String resource) 
            throws IOException {

        return new BufferedInputStream(
                TestUtil.class.getResource(resource).openStream());
    }
    
}
