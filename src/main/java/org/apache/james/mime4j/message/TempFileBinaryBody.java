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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.decoder.Base64InputStream;
import org.apache.james.mime4j.decoder.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.CodecUtil;
import org.apache.james.mime4j.util.MimeUtil;
import org.apache.james.mime4j.util.TempFile;
import org.apache.james.mime4j.util.TempPath;
import org.apache.james.mime4j.util.TempStorage;


/**
 * Binary body backed by a {@link org.apache.james.mime4j.util.TempFile}.
 *
 * 
 * @version $Id: TempFileBinaryBody.java,v 1.2 2004/10/02 12:41:11 ntherning Exp $
 */
class TempFileBinaryBody extends AbstractBody implements BinaryBody {
    
    private Entity parent = null;
    private TempFile tempFile = null;
    private final String transferEncoding;

    /**
     * Use the given InputStream to build the TemporyFileBinaryBody
     * 
     * @param is the InputStream to use as source
     * @throws IOException
     */
    public TempFileBinaryBody(final InputStream is, final String transferEncoding) throws IOException {
        
        this.transferEncoding = transferEncoding;
        TempPath tempPath = TempStorage.getInstance().getRootTempPath();
        tempFile = tempPath.createTempFile("attachment", ".bin");
        
        OutputStream out = tempFile.getOutputStream();
        final InputStream decodedStream;
        if (MimeUtil.ENC_BASE64.equals(transferEncoding)) {
            decodedStream = new Base64InputStream(is);
        } else if (MimeUtil.ENC_QUOTED_PRINTABLE.equals(transferEncoding)) {
            decodedStream = new QuotedPrintableInputStream(is);
        } else {
            decodedStream = is;
        }
        IOUtils.copy(decodedStream, out);
        out.close();
    }
    
    /**
     * @see org.apache.james.mime4j.message.AbstractBody#getParent()
     */
    public Entity getParent() {
        return parent;
    }
    
    /**
     * @see org.apache.james.mime4j.message.AbstractBody#setParent(org.apache.james.mime4j.message.Entity)
     */
    public void setParent(Entity parent) {
        this.parent = parent;
    }
    
    /**
     * @see org.apache.james.mime4j.message.BinaryBody#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return tempFile.getInputStream();
    }
    
    /**
     * @see org.apache.james.mime4j.message.Body#writeTo(java.io.OutputStream, int)
     */
    public void writeTo(OutputStream out, int mode) throws IOException {
        final InputStream inputStream = getInputStream();
        if (MimeUtil.ENC_BASE64.equals(transferEncoding)) {
            CodecUtil.encodeBase64(inputStream, out);
            out.write(CodecUtil.CRLF_CRLF);
        } else if (MimeUtil.ENC_QUOTED_PRINTABLE.equals(transferEncoding)) {
            IOUtils.copy(inputStream,out);
        } else {
            IOUtils.copy(inputStream,out);
        }
    }
}
