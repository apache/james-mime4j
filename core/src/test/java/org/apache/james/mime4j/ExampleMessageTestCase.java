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

import java.io.File;
import java.net.URL;

import org.apache.james.mime4j.stream.MimeConfig;

import junit.framework.TestCase;

public abstract class ExampleMessageTestCase extends TestCase {

    private final File file;
    private final URL resource;

    protected ExampleMessageTestCase(final File file, final URL resource) {
        super(file.getName());
        this.file = file;
        this.resource = resource;
    }

    public File getSourceFile() {
        return file;
    }

    public URL getResource() {
        return resource;
    }

    public String getResourceBase() {
        String r = resource.toString();
        String s = file.getName();
        if (r.endsWith(s)) {
            return r.substring(0, r.length() - s.length()) + getFilenameBase();
        } else {
            return r;
        }
    }

    public String getFilenameBase() {
        String s = file.getName();
        int idx = s.indexOf('.');
        if (idx != -1) {
            return s.substring(0, idx);
        } else {
            return s;
        }
    }

    public MimeConfig getConfig() {
        MimeConfig.Builder b = MimeConfig.custom();
        if (file.getName().startsWith("malformedHeaderStartsBody")) {
            b.setMalformedHeaderStartsBody(true);
        }
        b.setMaxLineLen(-1);
        return b.build();
    }

}
