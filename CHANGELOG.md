# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)

## [Unreleased]

Changes includes in this release:

 - MIME4J-233 bad parsing of mbox files using MboxIterator
 - MIME4J-273 Update EncoderUtil#encodeB encoding string splitting point
 - MIME4J-287 JAVA 8 JDK
 - MIME4J-289 Libraries upgrade
 - MIME4J-292 ContentTypeField strong typing
 - MIME4J-295 Allow for overriding the charset when decoding q or b encoded words
 - MIME4J-296 MboxIterator should use Path instead of File

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.4)

## [0.8.3] - 2019-03-21

Changes includes in this release:

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

 - Work on the MIME4J DOM date:
   - provide a way to know the header Date is absent
   - correction of header parsing when century is absent

More information on the [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.1)

## [0.8.0] - 2016-10-12

Changed were tracked on [JIRA](https://issues.apache.org/jira/browse/MIME4J-297?jql=project%20%3D%20MIME4J%20AND%20fixVersion%20%3D%200.8.0)

## [earlier]

Too many untracked changes, sorry. But you can have a look at our [JIRA](https://issues.apache.org/jira/browse/MIME4J)
