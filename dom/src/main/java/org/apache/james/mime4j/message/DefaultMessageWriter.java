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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.james.mime4j.Field;
import org.apache.james.mime4j.codec.Base64OutputStream;
import org.apache.james.mime4j.codec.QuotedPrintableOutputStream;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Default implementation of {@link MessageWriter}.
 */
public class DefaultMessageWriter implements MessageWriter {

    private static final byte[] CRLF = { '\r', '\n' };
    private static final byte[] DASHES = { '-', '-' };

    public static byte[] asBytes(Message message) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DefaultMessageWriter writer = new DefaultMessageWriter();
        writer.writeMessage(message, buffer);
        return buffer.toByteArray();
    }

    /**
     * Protected constructor prevents direct instantiation.
     */
    public DefaultMessageWriter() {
    }

    /**
     * Write the specified <code>Body</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param body
     *            the <code>Body</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeBody(Body body, OutputStream out) throws IOException {
        if (body instanceof Message) {
            writeEntity((Message) body, out);
        } else if (body instanceof Multipart) {
            writeMultipart((Multipart) body, out);
        } else if (body instanceof SingleBody) {
            ((SingleBody) body).writeTo(out);
        } else
            throw new IllegalArgumentException("Unsupported body class");
    }

    /**
     * Write the specified <code>Entity</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param entity
     *            the <code>Entity</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeEntity(Entity entity, OutputStream out) throws IOException {
        final Header header = entity.getHeader();
        if (header == null)
            throw new IllegalArgumentException("Missing header");

        writeHeader(header, out);

        final Body body = entity.getBody();
        if (body == null)
            throw new IllegalArgumentException("Missing body");

        boolean binaryBody = body instanceof BinaryBody;
        OutputStream encOut = encodeStream(out, entity
                .getContentTransferEncoding(), binaryBody);

        writeBody(body, encOut);

        // close if wrapped (base64 or quoted-printable)
        if (encOut != out)
            encOut.close();
    }

    /**
     * Write the specified <code>Message</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param message
     *            the <code>Message</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeMessage(Message message, OutputStream out) throws IOException {
        writeEntity(message, out);
    }

    /**
     * Write the specified <code>Multipart</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param multipart
     *            the <code>Multipart</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeMultipart(Multipart multipart, OutputStream out)
            throws IOException {
        ContentTypeField contentType = getContentType(multipart);

        ByteSequence boundary = getBoundary(contentType);

        ByteSequence preamble;
        ByteSequence epilogue;
        if (multipart instanceof MultipartImpl) {
            preamble = ((MultipartImpl) multipart).getPreambleRaw();
            epilogue = ((MultipartImpl) multipart).getEpilogueRaw();
        } else {
            preamble = multipart.getPreamble() != null ? ContentUtil.encode(multipart.getPreamble()) : null;
            epilogue = multipart.getEpilogue() != null ? ContentUtil.encode(multipart.getEpilogue()) : null;
        }
        if (preamble != null) {
            writeBytes(preamble, out);
            out.write(CRLF);
        }

        for (Entity bodyPart : multipart.getBodyParts()) {
            out.write(DASHES);
            writeBytes(boundary, out);
            out.write(CRLF);

            writeEntity(bodyPart, out);
            out.write(CRLF);
        }

        out.write(DASHES);
        writeBytes(boundary, out);
        out.write(DASHES);
        out.write(CRLF);
        if (epilogue != null) {
            writeBytes(epilogue, out);
        }
    }

    /**
     * Write the specified <code>Field</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param field
     *            the <code>Field</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeField(Field field, OutputStream out) throws IOException {
        ByteSequence raw = field.getRaw();
        if (raw == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(field.getName());
            buf.append(": ");
            String body = field.getBody();
            if (body != null) {
                buf.append(body);
            }
            raw = ContentUtil.encode(MimeUtil.fold(buf.toString(), 0));
        }
        writeBytes(raw, out);
        out.write(CRLF);
    }

    /**
     * Write the specified <code>Header</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param header
     *            the <code>Header</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeHeader(Header header, OutputStream out) throws IOException {
        for (Field field : header) {
            writeField(field, out);
        }

        out.write(CRLF);
    }

    protected OutputStream encodeStream(OutputStream out, String encoding,
            boolean binaryBody) throws IOException {
        if (MimeUtil.isBase64Encoding(encoding)) {
            return new Base64OutputStream(out);
        } else if (MimeUtil.isQuotedPrintableEncoded(encoding)) {
            return new QuotedPrintableOutputStream(out, binaryBody);
        } else {
            return out;
        }
    }

    private ContentTypeField getContentType(Multipart multipart) {
        Entity parent = multipart.getParent();
        if (parent == null)
            throw new IllegalArgumentException(
                    "Missing parent entity in multipart");

        Header header = parent.getHeader();
        if (header == null)
            throw new IllegalArgumentException(
                    "Missing header in parent entity");

        ContentTypeField contentType = (ContentTypeField) header
                .getField(FieldName.CONTENT_TYPE);
        if (contentType == null)
            throw new IllegalArgumentException(
                    "Content-Type field not specified");

        return contentType;
    }

    private ByteSequence getBoundary(ContentTypeField contentType) {
        String boundary = contentType.getBoundary();
        if (boundary == null)
            throw new IllegalArgumentException(
                    "Multipart boundary not specified. Mime-Type: "+contentType.getMimeType()+", Raw: "+contentType.toString());

        return ContentUtil.encode(boundary);
    }

    private void writeBytes(ByteSequence byteSequence, OutputStream out)
            throws IOException {
        if (byteSequence instanceof ByteArrayBuffer) {
            ByteArrayBuffer bab = (ByteArrayBuffer) byteSequence;
            out.write(bab.buffer(), 0, bab.length());
        } else {
            out.write(byteSequence.toByteArray());
        }
    }

}
