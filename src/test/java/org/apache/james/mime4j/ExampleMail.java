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

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.james.mime4j.util.CharsetUtil;

public class ExampleMail {
    
    public static final String MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MULTIPART_MIXED = "--4.66920160910299\r\n" + 
            "Content-Type: image/gif\r\n" + 
            "Content-Transfer-Encoding: base64\r\n" + 
            "MIME-Version: 1.0\r\n" + 
            "Content-ID: 238478934723847238947892374\r\n" + 
            "Content-Description: Bogus Image Data\r\n" + 
            "\r\n" + 
            "ABCDFEGHIJKLMNO\r\n" + 
            "\r\n" + 
            "--4.66920160910299\r\n" + 
            "Content-Type: message/rfc822\r\n" + 
            "\r\n" + 
            "From: Timothy Tayler <timothy@example.org>\r\n" + 
            "To: John Smith <john@example.org>\r\n" + 
            "Date: Sat, 16 Feb 2008 12:00:00 +0000 (GMT)\r\n" + 
            "Subject: Another Example Email\r\n" + 
            "Content-Type: multipart/mixed;boundary=2.50290787509\r\n" + 
            "\r\n" + 
            "Yet another preamble\r\n" + 
            "\r\n" + 
            "--2.50290787509\r\n" + 
            "Content-Type: text/plain\r\n" + 
            "\r\n" + 
            "Rhubard AND Custard!\r\n" + 
            "\r\n" + 
            "--2.50290787509\r\n" + 
            "Content-Type: multipart/alternative;boundary=3.243F6A8885A308D3\r\n" + 
            "\r\n" + 
            "--3.243F6A8885A308D3\r\n" + 
            "Content-Type: text/plain\r\n" + 
            "\r\n" + 
            "Rhubard?Custard?\r\n" + 
            "\r\n" + 
            "--3.243F6A8885A308D3\r\n" + 
            "\r\n" + 
            "Content-Type: text/richtext\r\n" + 
            "\r\n" + 
            "Rhubard?Custard?\r\n" + 
            "\r\n" + 
            "--3.243F6A8885A308D3--\r\n" + 
            "\r\n" + 
            "--2.50290787509--\r\n" + 
            "\r\n" + 
            "--4.66920160910299--";

    public static final String MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MAIL = "From: Timothy Tayler <timothy@example.org>\r\n" + 
            "To: Samual Smith <samual@example.org>\r\n" + 
            "Date: Thu, 14 Feb 2008 12:00:00 +0000 (GMT)\r\n" + 
            "Subject: A Multipart Alternative Email\r\n" + 
            "Content-Type: multipart/alternative;boundary=42\r\n" + 
            "\r\n" + 
            "This message has a premable\r\n" + 
            "\r\n" + 
            "--42\r\n" + 
            "Content-Type: text/plain; charset=US-ASCII\r\n" + 
            "\r\n" + 
            "Custard!\r\n" + 
            "\r\n" + 
            "--42\r\n" + 
            "Content-Type: application/octet-stream\r\n" + 
            "\r\n" + 
            "CUSTARDCUSTARDCUSTARD\r\n" + 
            "\r\n" + 
            "--42--\r\n";

    public static final String MIME_MULTIPART_EMBEDDED_MESSAGES_BODY = "Start with a preamble\r\n" + 
            "\r\n" + 
            "--1729\r\n" + 
            "Content-Type: text/plain; charset=US-ASCII\r\n" + 
            "\r\n" + 
            "Rhubarb!\r\n" + 
            "\r\n" + 
            "--1729\r\n" + 
            "Content-Type: application/octet-stream\r\n" + 
            "Content-Transfer-Encoding: base64\r\n" + 
            "\r\n" + 
            "987654321AHPLA\r\n" + 
            "\r\n" + 
            "--1729\r\n" + 
            "Content-Type: message/rfc822\r\n" + 
            "\r\n" + 
            MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MAIL + 
            "\r\n" + 
            "--1729\r\n" + 
            "Content-Type: multipart/mixed; boundary=4.66920160910299\r\n" + 
            "\r\n" + 
            MIME_MULTIPART_EMBEDDED_MESSAGES_INNER_MULTIPART_MIXED + "\r\n" +
            "--1729--\r\n" + 
            "\r\n";
    
    public static final String MD5_CONTENT = "Q2hlY2sgSW50ZWdyaXR5IQ==";
    public static final String CONTENT_DESCRIPTION = "Blah blah blah";
    public static final String CONTENT_ID = "<f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>";
    public static final Charset US_ASCII = CharsetUtil.US_ASCII;
    public static final Charset LATIN1 = CharsetUtil.ISO_8859_1;
    
    public static final String MIME_MULTIPART_EMBEDDED_MESSAGES = 
        "From: Timothy Tayler <timothy@example.org>\r\n" + 
        "To: Samual Smith <samual@example.org>\r\n" + 
        "Date: Thu, 14 Feb 2008 12:00:00 +0000 (GMT)\r\n" + 
        "Subject: A Multipart Email\r\n" + 
        "Content-Type: multipart/mixed;boundary=1729\r\n" + 
        "\r\n" + 
        MIME_MULTIPART_EMBEDDED_MESSAGES_BODY; 

    
    public static final String MULTIPART_WITH_CONTENT_LOCATION = 
        "From: Timothy Tayler <timothy@example.org>\r\n" +
        "To: Samual Smith <samual@example.org>\r\n" +
        "Date: Thu, 14 Feb 2008 12:00:00 +0000 (GMT)\r\n" +
        "Subject: A Multipart Email With Content-Location\r\n" +
        "Content-Type: multipart/mixed;boundary=1729\r\n\r\n" +
        "Start with a preamble\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: application/xhtml+xml\r\n" +
        "Content-Location: relative/url\r\n\r\n" +
        "<!DOCTYPE html\r\n" +
        "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\r\n" +
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n" +
        "<html><head><title>Rhubarb</title></head><body>Rhubarb!</body></html>\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: text/plain; charset=US-ASCII\r\n" +
        "Content-Location: http://www.example.org/absolute/rhubard.txt\r\n\r\n" +
        "Rhubarb!\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: text/html; charset=US-ASCII\r\n\r\n" +
        "<html><head><title>Rhubarb</title></head><body>Rhubarb!</body></html>\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: text/plain; charset=US-ASCII\r\n" +
        "Content-Location: (Some comment) \"http://www.example.org/absolute/comments/rhubard.txt\"(Another comment)\r\n\r\n" +
        "Rhubarb!\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: text/html; charset=US-ASCII\r\n" +
        "Content-Location:\"http://www.example.org/this/\r\n" +
        "          is/a/very/long/url/split/\r\n" +
        "          over/two/lines/\"\r\n\r\n" +
        "<html><head><title>Rhubarb</title></head><body>Rhubarb!</body></html>\r\n" +
        "\r\n--1729--\r\n" +
        "This is the epilogue\r\n";
    
    public static final String MULTIPART_WITH_BINARY_ATTACHMENTS = 
        "Return-Path: <robertburrelldonkin@blueyonder.co.uk>\r\n" +
        "Received: (qmail 18554 invoked from network); 25 May 2008 14:38:53 -0000\r\n" +
        "Received: from unknown (HELO p3presmtp01-16.prod.phx3.secureserver.net)\r\n" +
        "        ([208.109.80.165]) (envelope-sender <rdonkin-owner@locus.apache.org>) by\r\n" +
        "        smtp20-01.prod.mesa1.secureserver.net (qmail-1.03) with SMTP for\r\n" +
        "        <asf@xmlmapt.org>; 25 May 2008 14:38:53 -0000\r\n" +
        "Received: (qmail 9751 invoked from network); 25 May 2008 14:38:53 -0000\r\n" +
        "Received: from minotaur.apache.org ([140.211.11.9]) (envelope-sender\r\n" +
        "        <rdonkin-owner@locus.apache.org>) by\r\n" +
        "        p3presmtp01-16.prod.phx3.secureserver.net (qmail-ldap-1.03) with SMTP for\r\n" +
        "        <asf@xmlmapt.org>; 25 May 2008 14:38:50 -0000\r\n" +
        "Received: (qmail 46768 invoked by uid 1289); 25 May 2008 14:38:46 -0000\r\n" +
        "Delivered-To: rdonkin@locus.apache.org\r\n" +
        "Received: (qmail 46763 invoked from network); 25 May 2008 14:38:46 -0000\r\n" +
        "Received: from hermes.apache.org (HELO mail.apache.org) (140.211.11.2) by\r\n" +
        "        minotaur.apache.org with SMTP; 25 May 2008 14:38:46 -0000\r\n" +
        "Received: (qmail 61275 invoked by uid 500); 25 May 2008 14:38:48 -0000\r\n" +
        "Delivered-To: apmail-rdonkin@apache.org\r\n" +
        "Delivered-To: rob@localhost\r\n" +
        "Delivered-To: rob@localhost\r\n" +
        "Received: (qmail 61272 invoked by uid 99); 25 May 2008 14:38:48 -0000\r\n" +
        "Received: from athena.apache.org (HELO athena.apache.org) (140.211.11.136)\r\n" +
        "        by apache.org (qpsmtpd/0.29) with ESMTP; Sun, 25 May 2008 07:38:48 -0700\r\n" +
        "X-ASF-Spam-Status: No, hits=-0.0 required=10.0 tests=SPF_PASS\r\n" +
        "X-Spam-Check-By: apache.org\r\n" +
        "Received-SPF: pass (athena.apache.org: domain of\r\n" +
        "        robertburrelldonkin@blueyonder.co.uk designates 195.188.213.5 as permitted\r\n" +
        "        sender)\r\n" +
        "Received: from [195.188.213.5] (HELO smtp-out2.blueyonder.co.uk)\r\n" +
        "        (195.188.213.5) by apache.org (qpsmtpd/0.29) with ESMTP; Sun, 25 May 2008\r\n" +
        "        14:38:00 +0000\r\n" +
        "Received: from [172.23.170.140] (helo=anti-virus02-07) by\r\n" +
        "        smtp-out2.blueyonder.co.uk with smtp (Exim 4.52) id 1K0HMV-00087e-HY for\r\n" +
        "        rdonkin@apache.org; Sun, 25 May 2008 15:38:15 +0100\r\n" +
        "Received: from [82.38.65.6] (helo=[10.0.0.27]) by\r\n" +
        "        asmtp-out5.blueyonder.co.uk with esmtpa (Exim 4.52) id 1K0HMU-0001A2-3q for\r\n" +
        "        rdonkin@apache.org; Sun, 25 May 2008 15:38:14 +0100\r\n" +
        "Subject: This is an example of a multipart mixed email with image content\r\n" +
        "From: Robert Burrell Donkin <robertburrelldonkin@blueyonder.co.uk>\r\n" +
        "To: Robert Burrell Donkin <rdonkin@apache.org>\r\n" +
        "Content-Type: multipart/mixed; boundary=\"=-tIdGYVstQJghyEDATnJ+\"\r\n" +
        "Date: Sun, 25 May 2008 15:38:13 +0100\r\n" +
        "Message-Id: <1211726293.5772.10.camel@localhost>\r\n" +
        "Mime-Version: 1.0\r\n" +
        "X-Mailer: Evolution 2.12.3 \r\n" +
        "X-Virus-Checked: Checked by ClamAV on apache.org\r\n" +
        "X-Nonspam: None\r\n" +
        "X-fetched-from: mail.xmlmapt.org\r\n" +
        "X-Evolution-Source: imap://rob@thebes/\r\n" +
        "\r\n" +
        "\r\n" +
        "--=-tIdGYVstQJghyEDATnJ+\r\n" +
        "Content-Type: text/plain\r\n" +
        "Content-Transfer-Encoding: 7bit\r\n" +
        "\r\n" +
        "Licensed to the Apache Software Foundation (ASF) under one\r\n" +
        "or more contributor license agreements.  See the NOTICE file\r\n" +
        "distributed with this work for additional information\r\n" +
        "regarding copyright ownership.  The ASF licenses this file\r\n" +
        "to you under the Apache License, Version 2.0 (the\r\n" +
        "\"License\"); you may not use this file except in compliance\r\n" +
        "with the License.  You may obtain a copy of the License at\r\n" +
        "\r\n" +
        "    http://www.apache.org/licenses/LICENSE-2.0\r\n" +
        " \r\n" +
        "Unless required by applicable law or agreed to in writing,\r\n" +
        "software distributed under the License is distributed on an\r\n" +
        "\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\r\n" +
        "KIND, either express or implied.  See the License for the\r\n" +
        "specific language governing permissions and limitations\r\n" +
        "under the License.\r\n" +
        " \r\n" +
        "\r\n" +
        "--=-tIdGYVstQJghyEDATnJ+\r\n" +
        "Content-Disposition: attachment; filename=blob.png;\r\n   modification-date=\"Sun, 21 Jun 2008 15:32:18 +0000\"; " +
        "creation-date=\"Sat, 20 Jun 2008 10:15:09 +0000\"; read-date=\"Mon, 22 Jun 2008 12:08:56 +0000\";size=10234;\r\n" +
        "Content-Type: image/png; name=blob.png\r\n" +
        "Content-Transfer-Encoding: base64\r\n" +
        "\r\n" +
        "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAL\r\n" +
        "EwAACxMBAJqcGAAAAAd0SU1FB9gFGQ4iJ99ufcYAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRo\r\n" +
        "IEdJTVBXgQ4XAAAA0ElEQVQY02XMwUrDQBhF4XsnkyYhjWJaCloEN77/a/gERVwJLQiiNjYmbTqZ\r\n" +
        "/7qIG/VsPziMTw+23Wj/ovZdMQJgViCvWNVusfa23djuUf2nugbnI2RynkWF5a2Fwdvrs7q9vhqE\r\n" +
        "E2QAEIO6BhZBerUf6luMw49NyTR0OLw5kJD9sqk4Ipwc6GAREv5n5piXTDOQfy1JMSs8ZgXKq2kF\r\n" +
        "iwDgEriEecnLlefFEmGAIvqD4ggJJNMM85qLtXfX9xYGuEQ+4/kIi0g88zlXd66++QaQDG5GPZyp\r\n" +
        "rQAAAABJRU5ErkJggg==\r\n" +
        "\r\n" +
        "--=-tIdGYVstQJghyEDATnJ+\r\n" +
        "Content-Disposition: attachment; filename=blob.png\r\n" +
        "Content-Type: image/png; name=blob.png\r\n" +
        "Content-Transfer-Encoding: base64\r\n" +
        "\r\n" +
        "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAL\r\n" +
        "EwAACxMBAJqcGAAAAAd0SU1FB9gFGQ4iJ99ufcYAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRo\r\n" +
        "IEdJTVBXgQ4XAAAA0ElEQVQY02XMwUrDQBhF4XsnkyYhjWJaCloEN77/a/gERVwJLQiiNjYmbTqZ\r\n" +
        "/7qIG/VsPziMTw+23Wj/ovZdMQJgViCvWNVusfa23djuUf2nugbnI2RynkWF5a2Fwdvrs7q9vhqE\r\n" +
        "E2QAEIO6BhZBerUf6luMw49NyTR0OLw5kJD9sqk4Ipwc6GAREv5n5piXTDOQfy1JMSs8ZgXKq2kF\r\n" +
        "iwDgEriEecnLlefFEmGAIvqD4ggJJNMM85qLtXfX9xYGuEQ+4/kIi0g88zlXd66++QaQDG5GPZyp\r\n" +
        "rQAAAABJRU5ErkJggg==\r\n" +
        "\r\n" +
        "--=-tIdGYVstQJghyEDATnJ+\r\n" +
        "Content-Disposition: attachment; filename=rhubarb.txt\r\n" +
        "Content-Type: text/plain; name=rhubarb.txt; charset=us-ascii\r\n" +
        "Content-Language: en, en-US, en-CA\r\n" +
        "Content-Transfer-Encoding: quoted-printable\r\n" +
        "\r\n" +
        "Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhu=\r\n" +
        "barb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubar=\r\n" +
        "b Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb R=\r\n" +
        "hubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhub=\r\n" +
        "arb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb=\r\n" +
        " Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rh=\r\n" +
        "ubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhuba=\r\n" +
        "rb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb =\r\n" +
        "Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhu=\r\n" +
        "barb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubar=\r\n" +
        "b Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb R=\r\n" +
        "hubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhub=\r\n" +
        "arb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb=\r\n" +
        " Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rh=\r\n" +
        "ubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhuba=\r\n" +
        "rb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb =\r\n" +
        "Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhu=\r\n" +
        "barb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubar=\r\n" +
        "b Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb R=\r\n" +
        "hubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhub=\r\n" +
        "arb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb=\r\n" +
        " Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rh=\r\n" +
        "ubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhuba=\r\n" +
        "rb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb =\r\n" +
        "Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhu=\r\n" +
        "barb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubar=\r\n" +
        "b Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb R=\r\n" +
        "hubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhub=\r\n" +
        "arb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb=\r\n" +
        " Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rh=\r\n" +
        "ubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhuba=\r\n" +
        "rb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb =\r\n" +
        "Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhu=\r\n" +
        "barb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubar=\r\n" +
        "b Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb R=\r\n" +
        "hubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhub=\r\n" +
        "arb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb=\r\n" +
        " Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rh=\r\n" +
        "ubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhuba=\r\n" +
        "rb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb =\r\n" +
        "Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb Rhubarb\r\n" +
        "\r\n" +
        "--=-tIdGYVstQJghyEDATnJ+--\r\n";
    
    public static final String ONE_PART_MIME_ASCII_BODY = "A single part MIME mail.\r\n";

    public static final String RFC822_SIMPLE_BODY = "This is a very simple email.\r\n";
    
    public static final String ONE_PART_MIME_8859_BODY = "M\u00F6nchengladbach\r\n";
    
    public static final String ONE_PART_MIME_BASE64_LATIN1_BODY = "Hello Mo\u00F6nchengladbach\r\n";
    
    public static final String ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BODY = "Sonnet LXXXI By William Shakespeare\r\n" +
            "Or I shall live your epitaph to make,\r\n" +
            "Or you survive when I in earth am rotten;\r\n" +
            "From hence your memory death cannot take,\r\n" +
            "Although in me each part will be forgotten.\r\n" +
            "Your name from hence immortal life shall have,\r\n" +
            "Though I, once gone, to all the world must die:\r\n" +
            "The earth can yield me but a common grave,\r\n" +
            "When you entombed in men's eyes shall lie.\r\n" +
            "Your monument shall be my gentle verse,\r\n" +
            "Which eyes not yet created shall o'er-read;\r\n" +
            "And tongues to be, your being shall rehearse,\r\n" +
            "When all the breathers of this world are dead;\r\n" +
            "  You still shall live,--such virtue hath my pen,--\r\n" +
            "  Where breath most breathes, even in the mouths of men.\r\n"; 
    
    private static final byte[] ONE_PART_MIME_BASE64_LATIN1_ENCODED = EncodeUtils.toBase64(latin1(ONE_PART_MIME_BASE64_LATIN1_BODY));
    
    public static final String ONE_PART_MIME_BASE64_ASCII_BODY = "Hello, World!\r\n";
    
    public static final String ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS =
        "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
        "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
        "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
        "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
        "Subject: [Mime4J] getReader\r\n" +
        "MIME-Version: 1.0\r\n" +
        "Content-Type: text/plain; charset=US-ASCII\r\n" +
        "Content-Transfer-Encoding: 7bit\r\n" +
        "Content-Disposition: inline; foo=bar; one=1; param=value;\r\n" +
        "Content-MD5: " + MD5_CONTENT + "\r\n" +
        "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
        "\r\n" +
        ONE_PART_MIME_ASCII_BODY;

    private static final byte[] ONE_PART_MIME_BASE64_ASCII_ENCODED = EncodeUtils.toBase64(ascii(ONE_PART_MIME_BASE64_ASCII_BODY));

    public static final String ONE_PART_MIME_ASCII = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 1.0\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n" +
    "Content-Transfer-Encoding: 7bit\r\n" +
    "Content-Disposition: inline\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n" +
    ONE_PART_MIME_ASCII_BODY;
    
    public static final String ONE_PART_MIME_ASCII_COMMENT_IN_MIME_VERSION = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 2.(This is a comment)4\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n" +
    "Content-Transfer-Encoding: 7bit\r\n" +
    "Content-Disposition: inline\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n" + ONE_PART_MIME_ASCII_BODY;
    
    public static final String ONE_PART_MIME_ASCII_MIME_VERSION_SPANS_TWO_LINES = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 4.   \r\n" +
    "  1\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n" +
    "Content-Transfer-Encoding: 7bit\r\n" +
    "Content-Disposition: inline\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n" + ONE_PART_MIME_ASCII_BODY;
    
    public static final String INNER_MAIL = "From: Timothy Tayler <tim@example.org>\r\n" +
    "To: Joshua Tetley <joshua@example.org>\r\n" +
    "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
    "Subject: Multipart Without RFC822 Part\r\n" +
    "Content-Type: multipart/mixed;boundary=42\r\n\r\n" +
    "--42\r\n" +
    "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
    "First part of this mail\r\n" +
    "--42\r\n" +
    "Content-Type:text/plain; charset=US-ASCII\r\n\r\n" +
    "Second part of this mail\r\n" +
    "--42--\r\n";

    public static final String MAIL_WITH_RFC822_PART = "MIME-Version: 1.0\r\n" +
    "From: Timothy Tayler <tim@example.org>\r\n" +
    "To: Joshua Tetley <joshua@example.org>\r\n" +
    "Date: Tue, 12 Feb 2008 17:34:09 +0000 (GMT)\r\n" +
    "Subject: Multipart With RFC822 Part\r\n" +
    "Content-Type: multipart/mixed;boundary=1729\r\n\r\n" +
    "A short premable\r\n" +
    "--1729\r\n\r\n" +
    "First part has no headers\r\n" +
    "--1729\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
    "Second part is plain text\r\n" +
    "--1729\r\n" +
    "Content-Type: message/rfc822\r\n\r\n" +
    INNER_MAIL +
    "--1729\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
    "Last part is plain text\r\n" +
    "--1729--\r\n" +
    "The End";
        
    public static final String ONE_PART_MIME_8859 = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 1.0\r\n" +
    "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
    "Content-Transfer-Encoding: 8bit\r\n" +
    "Content-Disposition: inline\r\n" +
    "Content-ID: " + CONTENT_ID + "\r\n" +
    "Content-Description: " + CONTENT_DESCRIPTION + "\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n" +
    ONE_PART_MIME_8859_BODY;
    
    public static final String ONE_PART_MIME_BASE64_ASCII_HEADERS = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 1.0\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n" +
    "Content-Transfer-Encoding: base64\r\n" +
    "Content-Disposition: inline\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n";
    
    public static final String ONE_PART_MIME_BASE64_LATIN1_HEADERS = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 1.0\r\n" +
    "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
    "Content-Transfer-Encoding: base64\r\n" +
    "Content-Disposition: inline\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n";
    
    public static final String ONE_PART_MIME_QUOTED_PRINTABLE_ASCII = "Received: by 10.114.126.16 with HTTP; Thu, 6 Mar 2008 10:02:03 -0800 (PST)\r\n" +
    "Message-ID: <f470f68e0803061002n22bc4124he14015a4b6d6327f@mail.gmail.com>\r\n" +
    "Date: Thu, 6 Mar 2008 18:02:03 +0000\r\n" +
    "From: \"Robert Burrell Donkin\" <robertburrelldonkin@gmail.com>\r\n" +
    "To: \"James Developers List\" <server-dev@james.apache.org>\r\n" +
    "Subject: [Mime4J] getReader\r\n" +
    "MIME-Version: 1.0\r\n" +
    "Content-Type: text/plain; charset=US-ASCII\r\n" +
    "Content-Transfer-Encoding: Quoted-Printable\r\n" +
    "Content-Disposition: inline\r\n" +
    "Delivered-To: robertburrelldonkin@gmail.com\r\n" +
    "\r\n" + breakLines(ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BODY.replaceAll("\r\n", "=0D=0A"));
    
    
    public static final String RFC_SIMPLE = 
            "From: Timothy Tayler <timothy@example.org>\r\n" +
            "To: Samual Smith <samual@example.org>\r\n" +
            "Date: Thu, 14 Feb 2008 12:00:00 +0000 (GMT)\r\n" +
            "Subject: A Simple Email\r\n" +
            "\r\n" +
            RFC822_SIMPLE_BODY;

    public static final String MIME_RFC822_SIMPLE = 
        "From: Samual Smith <sam@example.org>\r\n" +
        "To: Joshua Tetley <josh@example.org>\r\n" +
        "Date: Thu, 14 Feb 2008 12:30:00 +0000 (GMT)\r\n" +
        "Subject: FW: A Simple Email\r\n" +
        "MIME-Version: 1.0\r\n" +
        "Content-Type: message/rfc822\r\n" +
        "\r\n" + RFC_SIMPLE;
    
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_7BIT = "Sonnet XXXIII By William Shakespeare\r\n" +
            "\r\n" +
            "Full many a glorious morning have I seen\r\n" +
            "Flatter the mountain tops with sovereign eye,\r\n" +
            "Kissing with golden face the meadows green,\r\n" +
            "Gilding pale streams with heavenly alchemy;\r\n" +
            "Anon permit the basest clouds to ride\r\n" +
            "With ugly rack on his celestial face,\r\n" +
            "And from the forlorn world his visage hide,\r\n" +
            "Stealing unseen to west with this disgrace:\r\n" +
            "Even so my sun one early morn did shine,\r\n" +
            "With all triumphant splendour on my brow;\r\n" +
            "But out! alack! he was but one hour mine,\r\n" +
            "The region cloud hath mask'd him from me now.\r\n" +
            "  Yet him for this my love no whit disdaineth;\r\n" +
            "  Suns of the world may stain when heaven's sun staineth.\r\n";
            
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_QUOTED_PRINTABLE = "Sonnet XXXV By William Shakespeare\r\n" +
            "\r\n" +
            "No more be griev'd at that which thou hast done:\r\n" +
            "Roses have thorns, and silver fountains mud:\r\n" +
            "Clouds and eclipses stain both moon and sun,\r\n" +
            "And loathsome canker lives in sweetest bud.\r\n" +
            "All men make faults, and even I in this,\r\n" +
            "Authorizing thy trespass with compare,\r\n" +
            "Myself corrupting, salving thy amiss,\r\n" +
            "Excusing thy sins more than thy sins are;\r\n" +
            "For to thy sensual fault I bring in sense,--\r\n" +
            "Thy adverse party is thy advocate,--\r\n" +
            "And 'gainst myself a lawful plea commence:\r\n" +
            "Such civil war is in my love and hate,\r\n" +
            "  That I an accessary needs must be,\r\n" +
            "  To that sweet thief which sourly robs from me.\r\n"; 
    
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BASE64 = "Sonnet XXXVIII By William Shakespeare\r\n" +
            "\r\n" +
            "How can my muse want subject to invent,\r\n" +
            "While thou dost breathe, that pour'st into my verse\r\n" +
            "Thine own sweet argument, too excellent\r\n" +
            "For every vulgar paper to rehearse?\r\n" +
            "O! give thy self the thanks, if aught in me\r\n" +
            "Worthy perusal stand against thy sight;\r\n" +
            "For who's so dumb that cannot write to thee,\r\n" +
            "When thou thy self dost give invention light?\r\n" +
            "Be thou the tenth Muse, ten times more in worth\r\n" +
            "Than those old nine which rhymers invocate;\r\n" +
            "And he that calls on thee, let him bring forth\r\n" +
            "Eternal numbers to outlive long date.\r\n" +
            "  If my slight muse do please these curious days,\r\n" +
            "  The pain be mine, but thine shall be the praise.\r\n";
        
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_ONE = 
            "From: Timothy Tayler <timothy@example.org>\r\n" +
            "To: Samual Smith <samual@example.org>\r\n" +
            "Date: Thu, 14 Feb 2008 12:00:00 +0000 (GMT)\r\n" +
            "Subject: A Multipart Email\r\n" +
            "Content-Type: multipart/mixed;boundary=1729\r\n" +
            "\r\n" +
            "Start with a preamble\r\n" +
            "\r\n" +
            "--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "Content-Transfer-Encoding: 7bit\r\n\r\n";
    
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_TWO = 
            "\r\n--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "Content-Transfer-Encoding: Quoted-Printable\r\n\r\n";
    
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_THREE = 
            "\r\n--1729\r\n" +
            "Content-Type: text/plain; charset=US-ASCII\r\n" +
            "Content-Transfer-Encoding: base64\r\n\r\n";
            
    public static final String MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_END = 
            "\r\n--1729--\r\n";
    
    public static final String MIME_MULTIPART_ALTERNATIVE = 
        "From: Timothy Tayler <timothy@example.org>\r\n" +
        "To: Samual Smith <samual@example.org>\r\n" +
        "Date: Thu, 14 Feb 2008 12:00:00 +0000 (GMT)\r\n" +
        "Subject: A Multipart Email\r\n" +
        "Content-Type: multipart/alternative;boundary=1729\r\n\r\n" +
        "Start with a preamble\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: application/xhtml+xml\r\n\r\n" +
        "<!DOCTYPE html\r\n" +
        "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\r\n" +
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n" +
        "<html><head><title>Rhubarb</title></head><body>Rhubarb!</body></html>\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: text/plain; charset=US-ASCII\r\n\r\n" +
        "Rhubarb!\r\n" +
        "\r\n--1729\r\n" +
        "Content-Type: text/html; charset=US-ASCII\r\n\r\n" +
        "<html><head><title>Rhubarb</title></head><body>Rhubarb!</body></html>\r\n" +
        "\r\n--1729--\r\n" +
        "This is the epilogue\r\n";
    
    
    private static final byte[][] MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTE_ARRAYS = {
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_ONE),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_7BIT),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_TWO),
        ascii(breakLines(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_QUOTED_PRINTABLE.replaceAll("\r\n", "=0D=0A"))),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_THREE),
        EncodeUtils.toBase64(ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BASE64)),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_END),
    };
    
    public static final byte[] MIME_RFC822_SIMPLE_BYTES = ascii(MIME_RFC822_SIMPLE);
    public static final byte[] MULTIPART_WITH_CONTENT_LOCATION_BYTES = ascii(MULTIPART_WITH_CONTENT_LOCATION);
    public static final byte[] ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS_BYTES = ascii(ONE_PART_MIME_WITH_CONTENT_DISPOSITION_PARAMETERS);
    public static final byte[] MIME_MULTIPART_ALTERNATIVE_BYTES = ascii(MIME_MULTIPART_ALTERNATIVE);
    public static final byte[] MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTES = join(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTE_ARRAYS);
    public static final byte[] ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BYTES = ascii(ONE_PART_MIME_QUOTED_PRINTABLE_ASCII);
    public static final byte[] ONE_PART_MIME_BASE64_LATIN1_UPPERCASE_BYTES = join(ascii(ONE_PART_MIME_BASE64_LATIN1_HEADERS.toUpperCase(Locale.UK)), ONE_PART_MIME_BASE64_LATIN1_ENCODED);
    public static final byte[] ONE_PART_MIME_BASE64_LATIN1_BYTES = join(ascii(ONE_PART_MIME_BASE64_LATIN1_HEADERS), ONE_PART_MIME_BASE64_LATIN1_ENCODED);
    public static final byte[] ONE_PART_MIME_BASE64_ASCII_BYTES = join(ascii(ONE_PART_MIME_BASE64_ASCII_HEADERS), ONE_PART_MIME_BASE64_ASCII_ENCODED);
    public static final byte[] RFC822_SIMPLE_BYTES = US_ASCII.encode(RFC_SIMPLE).array();
    public static final byte[] ONE_PART_MIME_ASCII_BYTES = US_ASCII.encode(ONE_PART_MIME_ASCII).array();
    public static final byte[] ONE_PART_MIME_8859_BYTES = LATIN1.encode(ONE_PART_MIME_8859).array();
    public static final byte[] MULTIPART_WITH_BINARY_ATTACHMENTS_BYTES = US_ASCII.encode(MULTIPART_WITH_BINARY_ATTACHMENTS).array();
    public static final byte[] ONE_PART_MIME_ASCII_COMMENT_IN_MIME_VERSION_BYTES = US_ASCII.encode(ONE_PART_MIME_ASCII_COMMENT_IN_MIME_VERSION).array();
    public static final byte[] ONE_PART_MIME_ASCII_MIME_VERSION_SPANS_TWO_LINES_BYTES = US_ASCII.encode(ONE_PART_MIME_ASCII_MIME_VERSION_SPANS_TWO_LINES).array();
    public static final byte[] MAIL_WITH_RFC822_PART_BYTES = ascii(MAIL_WITH_RFC822_PART);
    public static final byte[] MIME_MULTIPART_EMBEDDED_MESSAGES_BYTES = ascii(MIME_MULTIPART_EMBEDDED_MESSAGES);
    
    public static final byte[] ascii(String text) {
        
        return US_ASCII.encode(text).array();
    }
    
    public static final byte[] latin1(String text) {
        
        return LATIN1.encode(text).array();
    }
        
    public static final byte[] join(byte[] one, byte[] two) {
        byte[] results = new byte[one.length + two.length];
        System.arraycopy(one, 0, results, 0, one.length);
        System.arraycopy(two, 0, results, one.length, two.length);
        return results;
    }
    
    public static final byte[] join(byte[][] byteArrays) {
        int length = 0;
        for (byte[] bytes : byteArrays) {
            length += bytes.length;
        }
        byte[] results = new byte[length];
        int count = 0;
        for (byte[] bytes : byteArrays) {
            System.arraycopy(bytes, 0, results, count, bytes.length);
            count += bytes.length;
        }
        return results;
    }
    
    public static String breakLines(String original) {
        StringBuilder buffer = new StringBuilder(original);
        int count = 76;
        while(count < buffer.length()) {
            if (buffer.charAt(count) == '=') {
                count = count - 1;
            } else if (buffer.charAt(count-1) == '=') {
                count = count - 4;                
            } else if (buffer.charAt(count-2) == '=') {
                count = count - 3;
            }    
            buffer.insert(count, '\n');
            buffer.insert(count, '\r');
            buffer.insert(count, '=');
            count += 79;
        }
        final String result = buffer.toString();
        return result;
    }
}
