/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mime4j.field;

import org.mime4j.decoder.DecoderUtil;


/**
 * Simple unstructured field such as <code>Subject</code>.
 *
 * @author Niklas Therning
 * @version $Id: UnstructuredField.java,v 1.3 2004/10/25 07:26:46 ntherning Exp $
 */
public class UnstructuredField extends Field {
    private String value;
    
    protected UnstructuredField() {
    }
    
    protected void parseBody(String body) {
        value = DecoderUtil.decodeEncodedWords(body);
    }
    
    public String getValue() {
        return value;
    }
}
