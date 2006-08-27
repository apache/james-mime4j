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

package org.mime4j.field;

import org.mime4j.field.ContentTransferEncodingField;
import org.mime4j.field.ContentTypeField;
import org.mime4j.field.Field;
import org.mime4j.field.UnstructuredField;

import junit.framework.TestCase;

/**
 * 
 *
 * @author Niklas Therning
 * @version $Id: FieldTest.java,v 1.3 2004/10/25 07:26:47 ntherning Exp $
 */
public class FieldTest extends TestCase {

    public void testGetName() {
        Field f = null;
        
        f = Field.parse("Subject: Yada yada yada");
        assertEquals("Testing simple field", "Subject", f.getName());
        
        f = Field.parse("X-yada-yada: Yada yada yada");
        assertEquals("Testing an X- field", "X-yada-yada", f.getName());
        
        try {
            f = Field.parse("Yada yada yada");
            fail("IllegalArgumentException not thrown when using an invalid "
                    + "field");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testParse() {
        Field f = null;
        
        f = Field.parse("Subject: Yada yada yada");
        assertTrue("Field should be UnstructuredField", 
                        f instanceof UnstructuredField);
        f = Field.parse("Content-Type: text/plain");
        assertTrue("Field should be ContentTypeField", 
                        f instanceof ContentTypeField);
        f = Field.parse("Content-Transfer-Encoding: 7bit");
        assertTrue("Field should be ContentTransferEncodingField", 
                        f instanceof ContentTransferEncodingField);
    }
    
}
