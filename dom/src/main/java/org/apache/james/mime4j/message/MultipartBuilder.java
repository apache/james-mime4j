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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.io.InputStreams;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.NameValuePair;

/**
 * {@link org.apache.james.mime4j.dom.Multipart} builder.
 */
public class MultipartBuilder {

    private final List<Entity> bodyParts;
    private String subType;
    private String preamble;
    private String epilogue;

    private BodyFactory bodyFactory;
    private List<NameValuePair> parameters;

    public static MultipartBuilder create(String subType) {
        return new MultipartBuilder().setSubType(subType);
    }

    public static MultipartBuilder createCopy(Multipart other) {
        return new MultipartBuilder().copy(other);
    }

    public static MultipartBuilder create() {
        return new MultipartBuilder();
    }

    private MultipartBuilder() {
        this.bodyParts = new LinkedList<Entity>();
        this.parameters = new LinkedList<NameValuePair>();
        this.subType = "alternative"; // the default value; see getSubType() JavaDoc
    }

    public MultipartBuilder use(final BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
        return this;
    }

    /**
     * Gets the multipart sub-type. E.g. <code>alternative</code> (the
     * default) or <code>parallel</code>. See RFC 2045 for common sub-types
     * and their meaning.
     *
     * @return the multipart sub-type.
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets the multipart sub-type. E.g. <code>alternative</code> or
     * <code>parallel</code>. See RFC 2045 for common sub-types and their
     * meaning.
     *
     * @param subType
     *            the sub-type.
     */
    public MultipartBuilder setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    /**
     * Returns the number of body parts.
     *
     * @return number of <code>Entity</code> objects.
     */
    public int getCount() {
        return bodyParts.size();
    }

    /**
     * Gets the list of body parts. The list is immutable.
     *
     * @return the list of <code>Entity</code> objects.
     */
    public List<Entity> getBodyParts() {
        return Collections.unmodifiableList(bodyParts);
    }

    /**
     * Adds a body part to the end of the list of body parts.
     *
     * @param bodyPart
     *            the body part.
     */
    public MultipartBuilder addBodyPart(Entity bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException();
        }
        bodyParts.add(bodyPart);
        return this;
    }

    public MultipartBuilder addBodyPart(BodyPartBuilder bodyPart) {
        return this.addBodyPart(bodyPart.build());
    }

    /**
     * Inserts a body part at the specified position in the list of body parts.
     *
     * @param bodyPart
     *            the body part.
     * @param index
     *            index at which the specified body part is to be inserted.
     */
    public MultipartBuilder addBodyPart(Entity bodyPart, int index) {
        if (bodyPart == null) {
            throw new IllegalArgumentException();
        }
        bodyParts.add(index, bodyPart);
        return this;
    }

    /**
     * Removes the body part at the specified position in the list of body
     * parts.
     *
     * @param index
     *            index of the body part to be removed.
     * @return the removed body part.
     */
    public MultipartBuilder removeBodyPart(int index) {
        bodyParts.remove(index);
        return this;
    }

    /**
     * Replaces the body part at the specified position in the list of body
     * parts with the specified body part.
     *
     * @param bodyPart
     *            body part to be stored at the specified position.
     * @param index
     *            index of body part to replace.
     * @return the replaced body part.
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 || index &gt;=
     *             getCount()).
     */
    public MultipartBuilder replaceBodyPart(Entity bodyPart, int index) {
        if (bodyPart == null) {
            throw new IllegalArgumentException();
        }
        bodyParts.set(index, bodyPart);
        return this;
    }

    /**
     * Returns preamble.
     *
     * @return the preamble.
     */
    public String getPreamble() {
        return preamble;
    }

    /**
     * Sets the preamble.
     *
     * @param preamble
     *            the preamble.
     */
    public MultipartBuilder setPreamble(String preamble) {
        this.preamble = preamble;
        return this;
    }

    /**
     * Returns epilogue.
     *
     * @return the epilogue.
     */
    public String getEpilogue() {
        return epilogue;
    }

    /**
     * Sets the epilogue.
     *
     * @param epilogue
     *            the epilogue.
     */
    public MultipartBuilder setEpilogue(String epilogue) {
        this.epilogue = epilogue;
        return this;
    }

    public MultipartBuilder addContentTypeParameter(NameValuePair parameter) {
        this.parameters.add(parameter);
        return this;
    }

    public MultipartBuilder addTextPart(String text, Charset charset) throws IOException {
        Charset cs = charset != null ? charset : Charsets.ISO_8859_1;
        TextBody body = bodyFactory != null ? bodyFactory.textBody(
                InputStreams.create(text, cs), cs.name()) : BasicBodyFactory.INSTANCE.textBody(text, cs);
        BodyPart bodyPart = BodyPartBuilder.create()
                .setBody(body)
                .setContentType("text/plain", new NameValuePair("charset", cs.name()))
                .setContentTransferEncoding(Charsets.US_ASCII.equals(cs) ? "7bit" : "quoted-printable")
                .build();
        return addBodyPart(bodyPart);
    }

    public MultipartBuilder addBinaryPart(byte[] bin, String mimeType) throws IOException {
        BinaryBody body = bodyFactory != null ? bodyFactory.binaryBody(InputStreams.create(bin)) :
                BasicBodyFactory.INSTANCE.binaryBody(bin);
        BodyPart bodyPart = BodyPartBuilder.create()
                .setBody(body)
                .setContentType(mimeType != null ? mimeType : "application/octet-stream")
                .setContentTransferEncoding("base64")
                .build();
        return addBodyPart(bodyPart);
    }

    public MultipartBuilder copy(Multipart other) {
        if (other == null) {
            return this;
        }
        subType = other.getSubType();
        bodyParts.clear();
        final List<Entity> otherParts = other.getBodyParts();
        for (Entity otherPart: otherParts) {
            BodyPart bodyPart = new BodyPart();
            Header otherHeader = otherPart.getHeader();
            if (otherHeader != null) {
                HeaderImpl header = new HeaderImpl();
                for (Field otherField : otherHeader.getFields()) {
                    header.addField(otherField);
                }
                bodyPart.setHeader(header);
            }
            final Body otherBody = otherPart.getBody();
            if (otherBody != null) {
                Body body = null;
                if (otherBody instanceof Message) {
                    body = MessageBuilder.createCopy((Message) otherBody).build();
                } else if (otherBody instanceof Multipart) {
                    body = MultipartBuilder.createCopy((Multipart) otherBody).build();
                } else if (otherBody instanceof SingleBody) {
                    body = ((SingleBody) otherBody).copy();
                }
                bodyPart.setBody(body);
            }
            bodyParts.add(bodyPart);
        }
        preamble = other.getPreamble();
        epilogue = other.getEpilogue();
        return this;
    }

    public Multipart build() {
        MultipartImpl multipart = new MultipartImpl(subType, parameters);
        for (Entity part : bodyParts) {
            multipart.addBodyPart(part);
        }
        multipart.setPreamble(preamble);
        multipart.setEpilogue(epilogue);
        return multipart;
    }

}
