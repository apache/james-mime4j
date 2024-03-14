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

package org.apache.james.mime4j.stream;

class BasicBodyDescriptor implements BodyDescriptor {

    private final String mimeType;
    private final String mediaType;
    private final String subType;
    private final String boundary;
    private final String charset;
    private final String transferEncoding;
    private final long contentLength;
    public final int headerStartByte;
    public final int bodyStartByte;
    public final int bodyEndByte;

    BasicBodyDescriptor(
      final String mimeType,
      final String mediaType,
      final String subType,
      final String boundary,
      final String charset,
      final String transferEncoding,
      final long contentLength,
      int partHeaderStartByte,
      int partBodyStartByte, int bodyEndByte) {
        super();
        this.mimeType = mimeType;
        this.mediaType = mediaType;
        this.subType = subType;
        this.boundary = boundary;
        this.charset = charset;
        this.transferEncoding = transferEncoding;
        this.contentLength = contentLength;
        this.headerStartByte = partHeaderStartByte;
        this.bodyStartByte = partBodyStartByte;
        this.bodyEndByte = bodyEndByte;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getSubType() {
        return subType;
    }

    public String getBoundary() {
        return boundary;
    }

    public String getCharset() {
        return charset;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public long getContentLength() {
        return contentLength;
    }

    @Override
    public int getHeaderStartByte() {
        return headerStartByte;
    }

    @Override
    public int getBodyStartByte() {
        return bodyStartByte;
    }

  @Override
  public int getBodyEndByte() {
    return bodyEndByte;
  }

  @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[mimeType=");
        sb.append(mimeType);
        sb.append(", mediaType=");
        sb.append(mediaType);
        sb.append(", subType=");
        sb.append(subType);
        sb.append(", boundary=");
        sb.append(boundary);
        sb.append(", charset=");
        sb.append(charset);
        sb.append("]");
        return sb.toString();
    }

}
