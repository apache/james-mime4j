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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.james.mime4j.AbstractContentHandler;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeStreamParser;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.MessageUtils;


/**
 * The header of an entity (see RFC 2045).
 *
 * 
 * @version $Id: Header.java,v 1.3 2004/10/04 15:36:44 ntherning Exp $
 */
public class Header {
    private List fields = new LinkedList();
    private HashMap fieldMap = new HashMap();
    
    /**
     * Creates a new empty <code>Header</code>.
     */
    public Header() {
    }

    /**
     * Creates a new <code>Header</code> from the specified stream.
     * 
     * @param is the stream to read the header from.
     */
    public Header(InputStream is) throws IOException {
        final MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            public void endHeader() {
                parser.stop();
            }
            public void field(String fieldData) {
                addField(Field.parse(fieldData));
            }
        });
        try {
            parser.parse(is);
        } catch (MimeException e) {
            IllegalStateException ise = new IllegalStateException(
                    "Unexpected header processing error");
            ise.initCause(e);
            throw ise;
        }
    }

    /**
     * Adds a field to the end of the list of fields.
     * 
     * @param field the field to add.
     */
    public void addField(Field field) {
        List values = (List) fieldMap.get(field.getName().toLowerCase());
        if (values == null) {
            values = new LinkedList();
            fieldMap.put(field.getName().toLowerCase(), values);
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
    public List getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Gets a <code>Field</code> given a field name. If there are multiple
     * such fields defined in this header the first one will be returned.
     * 
     * @param name the field name (e.g. From, Subject).
     * @return the field or <code>null</code> if none found.
     */
    public Field getField(String name) {
        List l = (List) fieldMap.get(name.toLowerCase());
        if (l != null && !l.isEmpty()) {
            return (Field) l.get(0);
        }
        return null;
    }
    
    /**
     * Gets all <code>Field</code>s having the specified field name. 
     * 
     * @param name the field name (e.g. From, Subject).
     * @return the list of fields.
     */
    public List getFields(final String name) {
        final String lowerCaseName = name.toLowerCase();
        final List l = (List) fieldMap.get(lowerCaseName);
        final List results;
        if (l == null || l.isEmpty()) {
            results = Collections.EMPTY_LIST;
        } else {
            results = Collections.unmodifiableList(l);
        }
        return results;
    }
    
    /**
     * Return Header Object as String representation. Each headerline is
     * seperated by "\r\n"
     * 
     * @return headers
     */
    public String toString() {
        StringBuffer str = new StringBuffer();
        for (Iterator it = fields.iterator(); it.hasNext();) {
            str.append(it.next().toString());
            str.append("\r\n");
        }
        return str.toString();
    }
    
    
    /**
     * Write the Header to the given OutputStream. 
     * 
     * @param out the OutputStream to write to
     * @param mode compatibility mode
     * 
     * @throws IOException if case of an I/O error
     * @throws MimeException if case of a MIME protocol violation
     */
    public void writeTo(final OutputStream out, int mode) throws IOException, MimeException {
        Charset charset = null;
        if (mode == MessageUtils.LENIENT) {
            ContentTypeField cf = ((ContentTypeField) getField(Field.CONTENT_TYPE));
            if (cf != null && cf.getCharset() != null) {
                charset = CharsetUtil.getCharset(cf.getCharset());
            } else {
                charset = MessageUtils.ISO_8859_1;
            }
        } else {
            charset = MessageUtils.DEFAULT_CHARSET;
        }
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out, charset), 8192);
        for (Iterator it = fields.iterator(); it.hasNext();) {
            Field field = (Field) it.next();
            String fs = field.toString();
            if (mode == MessageUtils.STRICT_ERROR && !MessageUtils.isASCII(fs)) {
                throw new MimeException("Header '" + fs + "' violates RFC 822");
            }
            writer.write(fs);
            writer.write(MessageUtils.CRLF);
        }
        writer.write(MessageUtils.CRLF);
        writer.flush();
    }

    /**
     * Write the Header to the given OutputStream
     * 
     * @param out the OutputStream to write to
     * @throws IOException
     */
    public void writeTo(final OutputStream out) throws IOException {
        try {
            writeTo(out, MessageUtils.LENIENT);
        } catch (MimeException ex) {
            throw new IOException(ex.getMessage());
        }
    }

}
