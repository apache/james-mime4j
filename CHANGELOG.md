# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)

## [Unreleased]

Changes included in this release:

Performance enhancements:

 - MIME4J-303 Base64OutputStream should be based on faster Java8 iplementation
 - MIME4J-298 Convert DateTimeFieldLenientImpl to DateTimeFormatter
 - MIME4J-301 ContentTypeFieldImpl do fill a stacktrace when ended by a semicolon
 - MIME4J-302 Reduce calls to toLowerCase upon parsing
 - Pre-compile ContentLocationFieldImpl REGEX
 - Pre-compile EncoderUtil::quote REGEX
 - Remove redundant string operations
 
Fixes:
 
 - MessageBuilder::getReadDate infinitely recurse
 
Changes:

 - MIME4J-300 Set up an automated build for Apache MIME4J
 - Various small refactorings including:
   - Remove unnecessary toString calls
   - Group: iterations can be replaced by Collection::addAll
   - Manual calls to Math.max
   - Unneeded array boxing
   - Remove unneeded redundant interface qualifiers
   - Remove unused import
 
Additions:

 - MIME4J-299 Access to the Header map
 - MIME4J-255 Add method to allow setting of "no recurse" mode to underlying mime token stream

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.5)

## [0.8.4] - 2021-04-21

Changes included in this release:

 - MIME4J-233 bad parsing of mbox files using MboxIterator
 - MIME4J-273 Update EncoderUtil#encodeB encoding string splitting point
 - MIME4J-287 JAVA 8 JDK
 - MIME4J-289 Libraries upgrade
 - MIME4J-292 ContentTypeField strong typing
 - MIME4J-295 Allow for overriding the charset when decoding q or b encoded words
 - MIME4J-296 MboxIterator should use Path instead of File

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.4)

## [0.8.3] - 2019-03-21

Changes included in this release:

 - MIME4J-270: Using "alternative" as default subtype
 - MIME4J-263: decoding encoded words with empty encoded-text
 - MIME4J-279: Fixed JavaDoc errors to comply with Java8
 - MIME4J-280: Improve exception handling
 - MIME4J-283: DecoderUtil performance fix

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.3)

## [0.8.2] - 2018-04-27

This release solves the following bugs:

 - MIME4J-267 MIME4J DOM parsing errors on specific formats
 - MIME4J-273 Correcting encoder splitting point

The following feature were added:

 - MIME4J-269 Introduce a safe to use, PERMISSIVE configuration
 - MIME4J-268 DefaultMessageWriter: expose a convenient *asBytes* method
 - MIME4J-271 Make possible to define a Content-Type parameter
 - MIME4J-272 Implicit DOM builder call

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.2)

## [0.8.1] - 2017-06-27

This release includes:

 - MIME4J-262: Work on the MIME4J DOM date:
   - provide a way to know the header Date is absent
   - correction of header parsing when century is absent

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.1)

## [0.8.0] - 2016-10-12

Changed were tracked on [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.0)

## [earlier]

Too many untracked changes, sorry. But you can have a look at our [JIRA](https://issues.apache.org/jira/browse/MIME4J)

## [0.7.2]

The 0.7.2 release fixes several non-critical bugs found since release 0.7.1. 


## [0.7.1]

The 0.7.1 release fixes several non-critical bugs found since release 0.7. 


## [0.7.0]

The 0.7 release brings another round of API enhancements, bug fixes and performance optimizations. 
A major effort has been put in code reorganization, separating parsing code from DOM manipulation
code. Mime4J has been restructured into three separate modules: 'core', 'dom' and 'storage'. 
The 'core' package provides an event-driven SAX style parser that relies on a callback mechanism 
to report parsing events such as the start of an entity header the start of a body, etc. 
The 'dom' package contains base/abstract classes and interfaces for MIME-DOM manipulation aiming 
to provide the base for a full featured traversable DOM. Per default the Mime4J DOM builder stores 
content of individual body parts in memory. The 'storage' package provides support for more
complex storage backends such on-disk storage systems, overflow on max limit, or encrypted storage 
through JSSE API. 

Mime4J 0.7 improves support for headless messages, malformed separation between headers and body
and adds support for "obsolete" rfc822 syntax (e.g: "Header<somespace>: " style). Parsing 
performance for quoted printable streams have been considerably improved. A "DecodeMonitor" object 
has been introduced in most code to define how to deal with malformed input (Lenient vs Strict 
behaviours). Mime4J 0.7 also provides LenientFieldParser as an alternative to DefaultFieldParser
when a higher degree of tolerance to non-severe MIME field format violations is desired. 

### Upgrade Notes

 * The default field parsing logic has been moved from AbstractField to DefaultFieldParser. 
 * Low level MIME stream classes have been moved from org.apache.james.mime4j.parser to 
   org.apache.james.mime4j.stream package (Field, RawField, MimeTokenStream, ...)
 * "dom" classes/interfaces have been moved from the .message and .field package to the .dom
   package tree.
 * The method decodeBaseQuotedPrintable() of class o.a.j.mime4j.codec.DecoderUtil has been renamed
   in decodeQuotedPrintable().
 * Preamble and Epilogue are now correctly handled as optionals and the parser invoke their
   tokens/events only when they are present in the message. So if your code rely on that events
   being always called make sure to fix it.
 * preamble and epilogue Strings in Multipart DOM object are now nullable: an empty preamble is 
   different from no preamble, so we had to update the dom contract to support this difference.
   Make sure to add null checks if code using multipart.getPreamble and multipart.getEpilogue.
 * the first event for headless parsing in MimeTokenStream is not the first BODY event.
   You should not expect T_START_HEADER/T_END_HEADER any more.   

Please also note that as of this release Mime4j requires a Java 1.5 compatible runtime.

## [0.6.0]

The 0.6 release brings another round of API enhancements and performance optimizations. There has 
been a number of notable improvements in the DOM support. MIME stream parser is expected to be
50% faster when line counting is disabled. Please also note that as of this release Mime4j 
requires a Java 1.5 compatible runtime.

### Notes

 * Mime4j API is still considered unstable and is likely to change in future releases
 * The DOM API has been now been comprehensively refactored and the known limitations 
   addressed. Please report any remaining issues to 
   https://issues.apache.org/jira/browse/MIME4J.
 * Some low level functions are available only in the pull parser (recommended for
   advanced users)
 * 0.6 contains a mixture of approaches to the parsing of advanced MIME field types. 
   Limitations are known with these approaches with some relatively uncommon use cases. 
   A consistent and comprehensive rewrite is planned for 0.7 which should consolidate 
   and address these.
 * The former interfaces TextBody and BinaryBody have been changed into abstract subclasses
   of class SingleBody. Code that implements these interfaces has to be changed accordingly.
   [https://issues.apache.org/jira/browse/MIME4J-111]
 * A dedicated class for writing a message has been introduced. Class MessageWriter has now
   to be used instead of Body.writeTo(OutputStream, int). A short-cut method
   Message.writeTo(OutputStream) without a mode parameter is also available.
   [https://issues.apache.org/jira/browse/MIME4J-110]
 * Class NamedMailbox has been removed. Class Mailbox now has an additional name property.
   [https://issues.apache.org/jira/browse/MIME4J-107]
 * Class MessageUtils has been removed. The methods and constants can now be found in class
   CharsetUtil in the same package.
   [https://issues.apache.org/jira/browse/MIME4J-106]
 * Package org.apache.james.mime4j.decoder has been renamed in org.apache.james.mime4j.codec.
   [https://issues.apache.org/jira/browse/MIME4J-105]
 * Class AbstractBody has been superseded by SingleBody. AbstractBody has been removed.
 * BodyFactory introduced allowing more flexible storage for Message parts. TempFileTextBody
   and TempFileBinaryBody removed.
   [https://issues.apache.org/jira/browse/MIME4J-87]
 * Mime4j now has a more flexible mechanism for storing message bodies. Class TempStorage
   has been superseded by StorageProvider in package org.apache.james.mime4j.storage.
   The classes TempStorage, TempPath, TempFile and SimpleTempStorage have been removed.
   [https://issues.apache.org/jira/browse/MIME4J-83]
 * Temporary text body storage for Message parts now defaults to US-ASCII (was ISO-8859-1)

Detailed change log can be found here:

http://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12310521&styleName=Html&version=12313434


## [0.5.0]

The 0.5 release addresses a number of important issues discovered since 0.4. In 
particular, it improves Mime4j ability to deal with malformed data streams including 
those intentionally crafted to cause excessive CPU and memory utilization that can 
lead to DoS conditions.

This release also fixes a serious bug that can prevent Mime4j from correctly 
processing binary content.

Detailed change log can be found here:

https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12310521&styleName=Html&version=12313178

### Notes

 * Mime4j API is still considered unstable and is likely to change in future releases
 * DOM support has known limitations and some roundtrip issues remain to be resolved
 * Some low level functions are available only in the pull parser (recommended for 
   advanced users)

## [0.4.0]

The 0.4 release brings a number of significant improvements in terms of 
supported capabilities, flexibility and performance: 

* Revised and improved public API with support for pull parsing

* Support for parsing of 'headless' messages transmitted using non SMTP 
  transports such as HTTP

* Reduced external dependencies. Mime4j is no longer directly dependent on log4j 
  and commons-io

* Improved parsing performance (up to 10x for large messages)

* More comprehensive header parsing including support for RFC1864, RFC2045, 
  RFC2183, RFC2557 and RFC3066

* Revised packaging and exception hierarchy. MimeException now extends
  IOException.

Detailed change log can be found here:

http://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12310521&styleName=Html&version=12312483

### Notes

 * 0.4 contains numerous API improvements and is not binary compatible with 0.3
 * Mime4j API is still considered unstable and is likely to change in future releases
 * DOM support has known limitations and some roundtrip issues remain to be resolved
 * Some low level functions are available only in the pull parser (recommended for 
   advanced users)

