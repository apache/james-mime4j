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

import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.message.Header;

import junit.framework.TestCase;

public class HeaderTest extends TestCase {

    public static final String SUBJECT = "Subject: test";

    public static final String TO = "To: anyuser <any@user>";

    public void testHeader() {
        Header header = new Header();
        header.addField(Field.parse(SUBJECT));
        header.addField(Field.parse(TO));

        assertNotNull("Subject found", header.getField("Subject"));
        assertNotNull("To found", header.getField("To"));

        assertEquals("Headers equals", SUBJECT + "\r\n" + TO + "\r\n", header
                .toString());
    }
}
