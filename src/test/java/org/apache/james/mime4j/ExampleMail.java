/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.james.mime4j;

import java.nio.charset.Charset;

public class ExampleMail {
    
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset LATIN1 = Charset.forName("ISO-8859-1");
    
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
    
    private static final byte[][] MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTE_ARRAYS = {
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_ONE),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_7BIT),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_TWO),
        ascii(breakLines(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_QUOTED_PRINTABLE.replaceAll("\r\n", "=0D=0A"))),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_THREE),
        EncodeUtils.toBase64(ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BASE64)),
        ascii(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_END),
    };
    public static final byte[] MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTES = join(MIME_MIXED_MULTIPART_VARIOUS_ENCODINGS_BYTE_ARRAYS);
    public static final byte[] ONE_PART_MIME_QUOTED_PRINTABLE_ASCII_BYTES = ascii(ONE_PART_MIME_QUOTED_PRINTABLE_ASCII);
    public static final byte[] ONE_PART_MIME_BASE64_LATIN1_BYTES = join(ascii(ONE_PART_MIME_BASE64_LATIN1_HEADERS), ONE_PART_MIME_BASE64_LATIN1_ENCODED);
    public static final byte[] ONE_PART_MIME_BASE64_ASCII_BYTES = join(ascii(ONE_PART_MIME_BASE64_ASCII_HEADERS), ONE_PART_MIME_BASE64_ASCII_ENCODED);
    public static final byte[] RFC822_SIMPLE_BYTES = US_ASCII.encode(RFC_SIMPLE).array();
    public static final byte[] ONE_PART_MIME_ASCII_BYTES = US_ASCII.encode(ONE_PART_MIME_ASCII).array();
    public static final byte[] ONE_PART_MIME_8859_BYTES = LATIN1.encode(ONE_PART_MIME_8859).array();
    
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
        for (int i = 0; i < byteArrays.length; i++) {
            byte[] bytes = byteArrays[i];
            length += bytes.length;
        }
        byte[] results = new byte[length];
        int count = 0;
        for (int i = 0; i < byteArrays.length; i++) {
            byte[] bytes = byteArrays[i];
            System.arraycopy(bytes, 0, results, count, bytes.length);
            count += bytes.length;
        }
        return results;
    }
    
    public static String breakLines(String original) {
        StringBuffer buffer = new StringBuffer(original);
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
