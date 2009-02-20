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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Writes a message (or a part of a message) to an output stream.
 * <p>
 * This class cannot be instantiated; instead the three static instances
 * {@link #STRICT_ERROR}, {@link #STRICT_IGNORE} or {@link #LENIENT} implement
 * different strategies for encoding header fields and preamble and epilogue of
 * a multipart.
 * <p>
 * This class can also be subclassed to implement custom strategies for writing
 * messages.
 */
public class MessageWriter {

    private static final Charset LENIENT_FALLBACK_CHARSET = CharsetUtil.ISO_8859_1;
    private static final String CRLF = CharsetUtil.CRLF;
    private static final int WRITER_BUFFER_SIZE = 4096;

    /**
     * A message writer that uses US-ASCII for encoding and throws
     * {@link MimeIOException} if a non ASCII character is encountered.
     */
    public static final MessageWriter STRICT_ERROR = new MessageWriter();

    /**
     * A message writer that uses US-ASCII for encoding but ignores non ASCII
     * characters.
     */
    public static final MessageWriter STRICT_IGNORE = new MessageWriter() {
        @Override
        protected CharsetEncoder getCharsetEncoder(ContentTypeField contentType) {
            return ignoreEncoder(CharsetUtil.DEFAULT_CHARSET);
        }
    };

    /**
     * A message writer that uses the charset of the Content-Type header for
     * encoding.
     */
    public static final MessageWriter LENIENT = new MessageWriter() {
        @Override
        protected CharsetEncoder getCharsetEncoder(ContentTypeField contentType) {
            if (contentType == null) {
                return ignoreEncoder(LENIENT_FALLBACK_CHARSET);
            } else {
                String charset = contentType.getCharset();
                if (charset != null) {
                    return ignoreEncoder(CharsetUtil.getCharset(charset));
                } else {
                    return ignoreEncoder(LENIENT_FALLBACK_CHARSET);
                }
            }
        }
    };

    /**
     * Protected constructor prevents direct instantiation.
     */
    protected MessageWriter() {
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
     * @throws MimeIOException
     *             in case of a MIME protocol violation
     */
    public void writeBody(Body body, OutputStream out) throws IOException,
            MimeIOException {
        if (body instanceof Message) {
            writeEntity((Message) body, out);
        } else if (body instanceof Multipart) {
            writeMultipart((Multipart) body, out);
        } else if (body instanceof SingleBody) {
            ((SingleBody) body).writeTo(out);
        } else
            throw new IllegalArgumentException("Unsupported body class");

        out.flush();
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
     * @throws MimeIOException
     *             in case of a MIME protocol violation
     */
    public void writeEntity(Entity entity, OutputStream out)
            throws IOException, MimeIOException {
        final Header header = entity.getHeader();
        if (header == null)
            throw new IllegalArgumentException("Missing header");

        writeHeader(header, out);
        out.flush();

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
     * Write the specified <code>Multipart</code> to the specified
     * <code>OutputStream</code>.
     * 
     * @param multipart
     *            the <code>Multipart</code> to write.
     * @param out
     *            the OutputStream to write to.
     * @throws IOException
     *             if an I/O error occurs.
     * @throws MimeIOException
     *             in case of a MIME protocol violation
     */
    public void writeMultipart(Multipart multipart, OutputStream out)
            throws IOException, MimeIOException {
        ContentTypeField contentType = getContentType(multipart);

        String boundary = getBoundary(contentType);

        Writer writer = getWriter(contentType, out);

        try {
            writer.write(multipart.getPreamble());
            writer.write(CRLF);

            for (BodyPart bodyPart : multipart.getBodyParts()) {
                writer.write("--");
                writer.write(boundary);
                writer.write(CRLF);
                writer.flush();

                writeEntity(bodyPart, out);
                writer.write(CRLF);
            }

            writer.write("--");
            writer.write(boundary);
            writer.write("--");
            writer.write(CRLF);

            writer.write(multipart.getEpilogue());

            writer.flush();
        } catch (CharacterCodingException e) {
            throw new MimeIOException("Multipart violates RFC 822");
        }
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
     * @throws MimeIOException
     *             in case of a MIME protocol violation
     */
    public void writeHeader(Header header, OutputStream out)
            throws IOException, MimeIOException {
        Writer writer = getWriter((ContentTypeField) header
                .getField(AbstractField.CONTENT_TYPE), out);

        try {
            for (Field field : header) {
                writer.write(field.getRaw());
                writer.write(CRLF);
            }

            writer.write(CRLF);
            writer.flush();
        } catch (CharacterCodingException e) {
            throw new MimeIOException("Header violates RFC 822");
        }
    }

    CharsetEncoder getCharsetEncoder(ContentTypeField contentType) {
        return CharsetUtil.DEFAULT_CHARSET.newEncoder();
    }

    CharsetEncoder ignoreEncoder(Charset charset) {
        return charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    private OutputStream encodeStream(OutputStream out, String encoding,
            boolean binaryBody) throws IOException {
        if (MimeUtil.isBase64Encoding(encoding)) {
            return CodecUtil.wrapBase64(out);
        } else if (MimeUtil.isQuotedPrintableEncoded(encoding)) {
            return CodecUtil.wrapQuotedPrintable(out, binaryBody);
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
                .getField(AbstractField.CONTENT_TYPE);
        if (contentType == null)
            throw new IllegalArgumentException(
                    "Content-Type field not specified");

        return contentType;
    }

    private String getBoundary(ContentTypeField contentType) {
        String boundary = contentType.getBoundary();
        if (boundary == null)
            throw new IllegalArgumentException(
                    "Multipart boundary not specified");

        return boundary;
    }

    private Writer getWriter(ContentTypeField contentType, OutputStream out) {
        CharsetEncoder encoder = getCharsetEncoder(contentType);

        return new BufferedWriter(new OutputStreamWriter(out, encoder),
                WRITER_BUFFER_SIZE);
    }

}
