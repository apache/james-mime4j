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
package org.mime4j;

import org.mime4j.decoder.Base64InputStream;
import org.mime4j.decoder.QuotedPrintableInputStream;
import org.mime4j.message.Header;
import org.mime4j.field.Field;

import java.io.InputStream;
import java.io.IOException;

/**
 * Abstract implementation of ContentHandler that automates common
 * tasks. Currently performs header parsing and applies content-transfer
 * decoding to body parts.
 *
 * @author Joe Cheng
 */
public abstract class SimpleContentHandler implements ContentHandler {

    /**
     * Called after headers are parsed.
     */
    public abstract void headers(Header header);

    /**
     * Called when the body of a discrete (non-multipart) entity is encountered.

     * @param bd encapsulates the values (either read from the
     *        message stream or, if not present, determined implictly
     *        as described in the
     *        MIME rfc:s) of the <code>Content-Type</code> and
     *        <code>Content-Transfer-Encoding</code> header fields.
     * @param is the contents of the body. Base64 or quoted-printable
     *        decoding will be applied transparently.
     * @throws IOException should be thrown on I/O errors.
     */
    public abstract void bodyDecoded(BodyDescriptor bd, InputStream is) throws IOException;

    /* Subclassers: override these as necessary. */

    /**
     * @see ContentHandler#startMessage()
     */
    public void startMessage() {
    }

    /**
     * @see ContentHandler#endMessage()
     */
    public void endMessage() {
    }

    /**
     * @see ContentHandler#startBodyPart()
     */
    public void startBodyPart() {
    }

    /**
     * @see ContentHandler#endBodyPart()
     */
    public void endBodyPart() {
    }

    /**
     * @see ContentHandler#preamble(java.io.InputStream)
     */
    public void preamble(InputStream is) throws IOException {
    }

    /**
     * @see ContentHandler#epilogue(java.io.InputStream)
     */
    public void epilogue(InputStream is) throws IOException {
    }

    /**
     * @see ContentHandler#startMultipart(BodyDescriptor)
     */
    public void startMultipart(BodyDescriptor bd) {
    }

    /**
     * @see ContentHandler#endMultipart()
     */
    public void endMultipart() {
    }

    /**
     * @see ContentHandler#raw(java.io.InputStream)  
     */
    public void raw(InputStream is) throws IOException {
    }



    /* Implement introduced callbacks. */

    private Header currHeader;

    public final void startHeader() {
        currHeader = new Header();
    }

    public final void field(String fieldData) {
        currHeader.addField(Field.parse(fieldData));
    }

    public final void endHeader() {
        Header tmp = currHeader;
        currHeader = null;
        headers(tmp);
    }

    public final void body(BodyDescriptor bd, InputStream is) throws IOException {
        if (bd.isBase64Encoded()) {
            bodyDecoded(bd, new Base64InputStream(is));
        }
        else if (bd.isQuotedPrintableEncoded()) {
            bodyDecoded(bd, new QuotedPrintableInputStream(is));
        }
        else {
            bodyDecoded(bd, is);
        }
    }
}
