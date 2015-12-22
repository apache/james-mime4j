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

package org.apache.james.mime4j.utils.search;

import com.google.common.collect.Lists;
import com.sun.org.apache.bcel.internal.util.ClassLoader;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSearcherTest {

    @Test
    public void isFoundInShouldBeAbleToLocateTextFragments() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("as attachment !"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void isFoundInShouldReturnFalseWhenTextIsAbsent() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("Not in the mail"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isFalse();
    }

    @Test
    public void isFoundInShouldReturnFalseWhenSearchingHeaderTextOutsideHeaders() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("message/rfc822"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isFalse();
    }

    @Test
    public void isFoundInShouldReturnFalseWhenSearchingTextLocatedInOtherMimeParts() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("as attachment !"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .contentTypes(Lists.newArrayList("invalid"))
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isFalse();
    }

    @Test
    public void isFoundInShouldReturnTrueWhenSearchingTextLocatedInSpecifiedMimePart() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("as attachment !"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .contentTypes(Lists.newArrayList("text/plain"))
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void isFoundInShouldBeAbleToRecognizedMimeTypes() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList(""))
            .caseInsensitive(true)
            .includeHeaders(false)
            .contentTypes(Lists.newArrayList("text/plain"))
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void isFoundInShouldNotBeAffectedByInvalidMimeTypes() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("as attachment !"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .contentTypes(Lists.newArrayList("text/plain", "invalid"))
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void caseSensitivenessShouldBeTakenIntoAccountWhenTurnedOn() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("as aTtAchment !"))
            .caseInsensitive(true)
            .includeHeaders(false)
            .contentTypes(Lists.newArrayList("text/plain", "invalid"))
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void caseSensitivenessShouldBeIgnoredWhenTurnedOff() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("as aTtAchment !"))
            .caseInsensitive(false)
            .includeHeaders(false)
            .contentTypes(Lists.newArrayList("text/plain", "invalid"))
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isFalse();
    }

    @Test
    public void headerShouldBeMatchedWhenHeaderMatchingIsTurnedOn() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("message/rfc822"))
            .caseInsensitive(true)
            .includeHeaders(true)
            .contentTypes(Lists.<String>newArrayList())
            .build();
        assertThat(messageSearcher.isFoundIn(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void headerShouldBeMatchedWhenIgnoringMime() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("message/rfc822"))
            .build();
        assertThat(messageSearcher.isFoundInIgnoringMime(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void isFoundInIgnoringMimeShouldIgnoreMimeStructure() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("ail signature )\n\n--------------0004"))
            .build();
        assertThat(messageSearcher.isFoundInIgnoringMime(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isTrue();
    }

    @Test
    public void isFoundInIgnoringMimeShouldReturnFalseOnNonContainedText() throws Exception {
        MessageSearcher messageSearcher = MessageSearcher.builder()
            .searchContents(Lists.<CharSequence>newArrayList("invalid"))
            .build();
        assertThat(messageSearcher.isFoundInIgnoringMime(ClassLoader.getSystemResourceAsStream("documents/sampleMail.eml"))).isFalse();
    }

}
