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

package org.apache.james.mime4j.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.james.mime4j.field.address.DefaultAddressParser;
import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;
import org.apache.james.mime4j.field.contenttype.parser.ContentTypeParser;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.junit.Test;

/**
 * The comment-nesting counter of the JavaCC token managers belongs to one parser:
 * parsers running on other threads must not change how a nested comment ends.
 */
public class CommentNestThreadSafetyTest {

    private static final int THREADS = 8;
    private static final int ITERATIONS = 3000;

    private static String deepComment() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('(');
        }
        sb.append('x');
        for (int i = 0; i < 200; i++) {
            sb.append(')');
        }
        return sb.toString();
    }

    private interface Check {
        void run(String comment) throws Exception;
    }

    private static final Check ADDRESS = comment -> {
        AddressList list = DefaultAddressParser.DEFAULT.parseAddressList("a@b " + comment + ", c@d");
        assertEquals(2, list.size());
        Mailbox first = (Mailbox) list.get(0);
        assertEquals("a@b", first.getAddress());
        assertEquals("c@d", ((Mailbox) list.get(1)).getAddress());
    };

    private static final Check CONTENT_TYPE = comment -> {
        ContentTypeParser parser = new ContentTypeParser(
                new StringReader("text/plain " + comment + "; charset=us-ascii"));
        parser.parseAll();
        assertEquals("text", parser.getType());
        assertEquals("plain", parser.getSubType());
        assertEquals("charset", parser.getParamNames().get(0));
        assertEquals("us-ascii", parser.getParamValues().get(0));
    };

    private static final Check CONTENT_DISPOSITION = comment -> {
        ContentDispositionParser parser = new ContentDispositionParser(
                new StringReader("attachment " + comment + "; filename=f"));
        parser.parseAll();
        assertEquals("attachment", parser.getDispositionType());
        assertEquals("f", parser.getParameters().get("filename"));
    };

    private static final Check DATE_TIME = comment -> {
        DateTime dt = new DateTimeParser(
                new StringReader("Thu, 1 Jan 2026 10:20:30 +0100 " + comment)).parseAll();
        assertEquals(2026, dt.getYear());
        assertEquals(30, dt.getSecond());
        assertEquals(100, dt.getTimeZone());
    };

    @Test
    public void nestedCommentsParseTheSameOnConcurrentThreads() throws Exception {
        final Check[] checks = {ADDRESS, CONTENT_TYPE, CONTENT_DISPOSITION, DATE_TIME};
        final String[] comments = {"(x (y) z)", deepComment()};
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            final CountDownLatch start = new CountDownLatch(1);
            List<Future<Void>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                final int thread = t;
                futures.add(executor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        start.await();
                        for (int i = 0; i < ITERATIONS; i++) {
                            Check check = checks[(thread + i) % checks.length];
                            check.run(comments[thread % comments.length]);
                        }
                        return null;
                    }
                }));
            }
            start.countDown();
            for (Future<Void> future : futures) {
                future.get(2, TimeUnit.MINUTES);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static final Pattern TOKEN_MGR_DECLS = Pattern.compile(
            "TOKEN_MGR_DECLS\\s*:\\s*\\{(.*?)\\n\\}", Pattern.DOTALL);
    private static final Pattern STATIC_FIELD = Pattern.compile("(?m)^\\s*static\\s");

    @Test
    public void grammarsDeclareNoStaticTokenManagerField() throws IOException {
        List<File> grammars = new ArrayList<>();
        collectGrammars(new File("src/main"), grammars);
        assertTrue("no grammar found under " + new File("src/main").getAbsolutePath(), grammars.size() >= 7);
        for (File grammar : grammars) {
            String content = new String(Files.readAllBytes(grammar.toPath()), StandardCharsets.UTF_8);
            if (!content.contains("STATIC=false") && !content.contains("STATIC = false")) {
                continue;
            }
            Matcher decls = TOKEN_MGR_DECLS.matcher(content);
            while (decls.find()) {
                assertTrue(grammar + " declares a static field in TOKEN_MGR_DECLS",
                        !STATIC_FIELD.matcher(decls.group(1)).find());
            }
        }
    }

    private static void collectGrammars(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collectGrammars(child, out);
            } else if (child.getName().endsWith(".jj") || child.getName().endsWith(".jjt")) {
                out.add(child);
            }
        }
    }
}
