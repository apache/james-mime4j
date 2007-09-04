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

import java.util.Map;

/**
 * Encapsulates the values of the MIME-specific header fields 
 * (which starts with <code>Content-</code>). 
 *
 * 
 * @version $Id: BodyDescriptor.java,v 1.4 2005/02/11 10:08:37 ntherning Exp $
 */
public interface BodyDescriptor {
    /**
     * Returns the body descriptors boundary.
     * @return Boundary string, if known, or null. The latter may be the
     *   case, in particular, if the body is no multipart entity.
     */
    String getBoundary();

    /**
     * Adds a field to the body descriptor.
     * @param pFieldName The fields name.
     * @param pFieldValue The unparsed fields value.
     */
    void addField(String pFieldName, String pFieldValue);

    /**
     * Returns the body descriptors MIME type.
     * @return The MIME type, which has been parsed from the
     *   content-type definition. Must not be null, but
     *   "text/plain", if no content-type was specified.
     */
    String getMimeType();

    /**
     * The body descriptors character set.
     * @return Character set, which has been parsed from the
     *   content-type definition. Must not be null, but "US-ASCII",
     *   if no content-type was specified.
     */
    String getCharset();

    /**
     * Returns the body descriptors transfer encoding.
     * @return The transfer encoding. Must not be null, but "7bit",
     *   if no transfer-encoding was specified.
     */
    String getTransferEncoding();

    /**
     * Returns the map of parameters of the content-type header.
     */
    Map getParameters();

    /**
     * Returns the body descriptors content-length.
     * @return Content length, if known, or -1, to indicate the absence of a
     *   content-length header.
     */
    long getContentLength();
}
