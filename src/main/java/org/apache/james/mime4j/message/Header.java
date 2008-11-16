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
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.util.CharArrayBuffer;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.MessageUtils;


/**
 * The header of an entity (see RFC 2045).
 *
 * 
 * @version $Id: Header.java,v 1.3 2004/10/04 15:36:44 ntherning Exp $
 */
public class Header {
    private List<Field> fields = new LinkedList<Field>();
    private Map<String, List<Field>> fieldMap = new HashMap<String, List<Field>>();
    
    /**
     * Creates a new empty <code>Header</code>.
     */
    public Header() {
    }

    /**
     * Creates a new <code>Header</code> from the specified stream.
     * 
     * @param is the stream to read the header from.
     * 
     * @throws IOException on I/O errors.
     * @throws MimeIOException on MIME protocol violations.
     */
    public Header(InputStream is) 
            throws IOException, MimeIOException {
        final MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new AbstractContentHandler() {
            @Override
            public void endHeader() {
                parser.stop();
            }
            @Override
            public void field(String fieldData) throws MimeException {
                addField(Field.parse(fieldData));
            }
        });
        try {
            parser.parse(is);
        } catch (MimeException ex) {
            throw new MimeIOException(ex);
        }
    }

    /**
     * Adds a field to the end of the list of fields.
     * 
     * @param field the field to add.
     */
    public void addField(Field field) {
        List<Field> values = fieldMap.get(field.getName().toLowerCase());
        if (values == null) {
            values = new LinkedList<Field>();
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
    public List<Field> getFields() {
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
        List<Field> l = fieldMap.get(name.toLowerCase());
        if (l != null && !l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }
    
    /**
     * Gets all <code>Field</code>s having the specified field name. 
     * 
     * @param name the field name (e.g. From, Subject).
     * @return the list of fields.
     */
    public List<Field> getFields(final String name) {
        final String lowerCaseName = name.toLowerCase();
        final List<Field> l = fieldMap.get(lowerCaseName);
        final List<Field> results;
        if (l == null || l.isEmpty()) {
            results = Collections.emptyList();
        } else {
            results = Collections.unmodifiableList(l);
        }
        return results;
    }

    /**
     * Removes all <code>Field</code>s having the specified field name.
     * 
     * @param name
     *            the field name (e.g. From, Subject).
     * @return number of fields removed.
     */
    public int removeFields(String name) {
        final String lowerCaseName = name.toLowerCase();
        List<Field> removed = fieldMap.remove(lowerCaseName);
        if (removed == null || removed.isEmpty())
            return 0;

        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
            Field field = iterator.next();
            if (field.getName().equalsIgnoreCase(name))
                iterator.remove();
        }

        return removed.size();
    }

    /**
     * Sets or replaces a field. This method is useful for header fields such as
     * Subject or Message-ID that should not occur more than once in a message.
     * 
     * If this <code>Header</code> does not already contain a header field of
     * the same name as the given field then it is added to the end of the list
     * of fields (same behavior as {@link #addField(Field)}). Otherwise the
     * first occurrence of a field with the same name is replaced by the given
     * field and all further occurrences are removed.
     * 
     * @param field the field to set.
     */
    public void setField(Field field) {
        final String lowerCaseName = field.getName().toLowerCase();
        List<Field> l = fieldMap.get(lowerCaseName);
        if (l == null || l.isEmpty()) {
            addField(field);
            return;
        }

        l.clear();
        l.add(field);

        int firstOccurrence = -1;
        int index = 0;
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); index++) {
            Field f = iterator.next();
            if (f.getName().equalsIgnoreCase(field.getName())) {
                iterator.remove();

                if (firstOccurrence == -1)
                    firstOccurrence = index;
            }
        }

        fields.add(firstOccurrence, field);
    }

    /**
     * Return Header Object as String representation. Each headerline is
     * seperated by "\r\n"
     * 
     * @return headers
     */
    @Override
    public String toString() {
        CharArrayBuffer str = new CharArrayBuffer(128);
        for (Field field : fields) {
            str.append(field.toString());
            str.append("\r\n");
        }
        return str.toString();
    }
    
    
    /**
     * Write the Header to the given OutputStream. 
     * <p>
     * Compatibility mode:
     * <ul>
     *  <li>
     *   {@link MessageUtils#LENIENT}: use charset of the Content-Type header
     *  </li>
     *  <li>
     *   {@link MessageUtils#STRICT_ERROR}: use US-ASCII and throw {@link MimeException} 
     *    if a non ASCII character is encountered
     *   </li>
     *  <li>
     *   {@link MessageUtils#STRICT_ERROR}: ignore non ASCII characters if encountered
     *   </li>
     * </ul>
     * @param out the OutputStream to write to
     * @param mode compatibility mode:
     *   {@link MessageUtils#LENIENT}, {@link MessageUtils#STRICT_ERROR}, {@link MessageUtils#STRICT_IGNORE}  
     * 
     * @throws IOException if case of an I/O error
     * @throws MimeException if case of a MIME protocol violation
     */
    public void writeTo(final OutputStream out, Mode mode) throws IOException, MimeException {
        Charset charset = null;
        if (mode == Mode.LENIENT) {
            final ContentTypeField contentTypeField = ((ContentTypeField) getField(Field.CONTENT_TYPE));
            if (contentTypeField == null) {
                charset = MessageUtils.DEFAULT_CHARSET;
            } else {
                final String contentTypeFieldCharset = contentTypeField.getCharset();
                if (contentTypeField != null && contentTypeFieldCharset != null) {
                    charset = CharsetUtil.getCharset(contentTypeFieldCharset);
                } else {
                    charset = MessageUtils.ISO_8859_1;
                }
            }
        } else {
            charset = MessageUtils.DEFAULT_CHARSET;
        }
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out, charset), 8192);
        for (Field field : fields) {
            String fs = field.toString();
            if (mode == Mode.STRICT_ERROR && !MessageUtils.isASCII(fs)) {
                throw new MimeException("Header '" + fs + "' violates RFC 822");
            }
            writer.write(fs);
            writer.write(MessageUtils.CRLF);
        }
        writer.write(MessageUtils.CRLF);
        writer.flush();
    }
}
