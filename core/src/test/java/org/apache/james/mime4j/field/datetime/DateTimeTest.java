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

package org.apache.james.mime4j.field.datetime;

import junit.framework.TestCase;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.field.datetime.parser.ParseException;

import java.io.StringReader;

public class DateTimeTest extends TestCase {
    
    public void testExceptionTree() {
        // make sure that our ParseException extends MimeException.
        assertTrue(MimeException.class.isAssignableFrom(ParseException.class));
    }

    public void testNormalDate() throws ParseException {
        new DateTimeParser(new StringReader("Fri, 21 Nov 1997 09:55:06 -0600")).parseAll();
        new DateTimeParser(new StringReader("21 Nov 97 09:55:06 GMT")).parseAll();


        ensureAllEqual(new String[] {
           "Fri, 21 Nov 1997 09:55:06 -0600", // baseline
           "Fri, 21 Nov 97 09:55:06 -0600",   // 2-digit year
           "Fri, 21 Nov 097 09:55:06 -0600",  // 3-digit year
           "Fri, 21 Nov 1997 10:55:06 -0500", // shift time zone
           "Fri, 21 Nov 1997 19:25:06 +0330", // shift time zone
           "21 Nov 1997 09:55:06 -0600"       // omit day of week
        });

        ensureAllEqual(new String[] {
            "Thu, 16 Sep 2019 14:37:22 +0000", // baseline
            "Thu, 16 Sep 19 14:37:22 +0000",   // 2-digit year
            "Thu, 16 Sep 119 14:37:22 +0000",  // 3-digit year
            "Thu, 16 Sep 2019 14:37:22 -0000", // minus-zero zone
            "Thu, 16 Sep 2019 14:37:22 GMT",   // alternate zone
            "Thu, 16 Sep 2019 14:37:22 UT"     // alternate zone
        });

        ensureAllEqual(new String[] {
            "Fri, 21 Nov 1997 12:00:00 GMT",
            "Fri, 21 Nov 1997 07:00:00 EST",
            "Fri, 21 Nov 1997 08:00:00 EDT",
            "Fri, 21 Nov 1997 06:00:00 CST",
            "Fri, 21 Nov 1997 07:00:00 CDT",
            "Fri, 21 Nov 1997 05:00:00 MST",
            "Fri, 21 Nov 1997 06:00:00 MDT",
            "Fri, 21 Nov 1997 04:00:00 PST",
            "Fri, 21 Nov 1997 05:00:00 PDT",

            // make sure military zones are ignored, per RFC2822 instructions
            "Fri, 21 Nov 1997 12:00:00 A",
            "Fri, 21 Nov 1997 12:00:00 B",
            "Fri, 21 Nov 1997 12:00:00 C",
            "Fri, 21 Nov 1997 12:00:00 D",
            "Fri, 21 Nov 1997 12:00:00 E",
            "Fri, 21 Nov 1997 12:00:00 F",
            "Fri, 21 Nov 1997 12:00:00 G",
            "Fri, 21 Nov 1997 12:00:00 H",
            "Fri, 21 Nov 1997 12:00:00 I",
            "Fri, 21 Nov 1997 12:00:00 K",
            "Fri, 21 Nov 1997 12:00:00 L",
            "Fri, 21 Nov 1997 12:00:00 M",
            "Fri, 21 Nov 1997 12:00:00 N",
            "Fri, 21 Nov 1997 12:00:00 O",
            "Fri, 21 Nov 1997 12:00:00 P",
            "Fri, 21 Nov 1997 12:00:00 Q",
            "Fri, 21 Nov 1997 12:00:00 R",
            "Fri, 21 Nov 1997 12:00:00 S",
            "Fri, 21 Nov 1997 12:00:00 T",
            "Fri, 21 Nov 1997 12:00:00 U",
            "Fri, 21 Nov 1997 12:00:00 V",
            "Fri, 21 Nov 1997 12:00:00 W",
            "Fri, 21 Nov 1997 12:00:00 X",
            "Fri, 21 Nov 1997 12:00:00 Y",
            "Fri, 21 Nov 1997 12:00:00 Z",
        });
    }

    private void ensureAllEqual(String[] dateStrings) throws ParseException {
        for (int i = 0; i < dateStrings.length - 1; i++) {
            assertEquals(
                    new DateTimeParser(new StringReader(dateStrings[i])).parseAll().getDate().getTime(),
                    new DateTimeParser(new StringReader(dateStrings[i + 1])).parseAll().getDate().getTime()
            );
        }
    }

}
