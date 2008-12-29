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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.james.mime4j.decoder.CodecUtil;
import org.apache.james.mime4j.parser.MimeTokenStream;

public class LongMultipartReadBench {

    public static void main(String[] args) throws Exception {
        
        ClassLoader cl = LongMultipartReadBench.class.getClassLoader();
        
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        InputStream instream = cl.getResourceAsStream("long-multipart.msg");
        if (instream == null) {
            System.out.println("Test message not found");
            return;
        }
        try {
            CodecUtil.copy(instream, outstream);
        } finally {
            instream.close();
        }

        byte[] content = outstream.toByteArray();

        int reps = 25000;
        if (args.length > 0) {
            reps = Integer.parseInt(args[0]);
        }
        
        System.out.println("Multipart message read.");
        System.out.println("No of repetitions: " + reps);
        System.out.println("Content length: " + content.length);
        System.out.println("----------------------------");
        
        MimeTokenStream stream = new MimeTokenStream();
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            stream.parse(new ByteArrayInputStream(content));
            for (int state = stream.getState(); 
                state != MimeTokenStream.T_END_OF_STREAM; 
                state = stream.next()) {
            }
        }
        long finish = System.currentTimeMillis();
        System.out.println("Execution time: " 
                + ((double)(finish - start) / 1000) + " s" );
    }
    
}
