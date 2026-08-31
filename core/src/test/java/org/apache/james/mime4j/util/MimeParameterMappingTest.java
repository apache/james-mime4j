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

import org.junit.Assert;
import org.junit.Test;

public class MimeParameterMappingTest {

    @Test
    public void continuationSegmentsShouldBeConcatenatedInOrder() {
        MimeParameterMapping mapping = new MimeParameterMapping();
        mapping.addParameter("filename*0", "abc");
        mapping.addParameter("filename*1", "def");
        mapping.addParameter("filename*2", "ghi");
        Assert.assertEquals("abcdefghi", mapping.get("filename"));
    }

    @Test
    public void continuationSegmentsShouldBeReadableRepeatedly() {
        MimeParameterMapping mapping = new MimeParameterMapping();
        mapping.addParameter("filename*0", "abc");
        mapping.addParameter("filename*1", "def");
        Assert.assertEquals("abcdef", mapping.get("filename"));
        Assert.assertEquals("abcdef", mapping.get("filename"));
        Assert.assertEquals("abcdef", mapping.getParameters().get("filename"));
    }

    @Test(timeout = 15000)
    public void manyContinuationSegmentsShouldStayLinear() {
        int segments = 200000;
        MimeParameterMapping mapping = new MimeParameterMapping();
        for (int i = 0; i < segments; i++) {
            mapping.addParameter("filename*" + i, "0123456789");
        }
        Assert.assertEquals(segments * 10, mapping.get("filename").length());
    }
}
