Apache James MIME4J
===================

image:https://img.shields.io/badge/Join%20us-Mailing%20lists-purple.svg[link="https://james.apache.org/mail.html"]
link:https://gitter.im/apache/james-project[image:https://badges.gitter.im/apache/james-project.svg[Join the chat at link:https://gitter.im/apache/james-project]]
image:https://img.shields.io/badge/CI-Jenkins-blue.svg[link="https://ci-builds.apache.org/job/james/job/ApacheJames-Mime4J/"]
image:https://img.shields.io/badge/Documentation-green.svg[link="https://james.apache.org/mime4j/index.html"]
image:https://img.shields.io/badge/Downloads-0.8.8-yellow.svg[link="https://james.apache.org/download.cgi#Apache_Mime4J"]
image:https://img.shields.io/badge/Tickets-JIRA-blue.svg[link="https://issues.apache.org/jira/projects/MIME4J/issues"]
image:https://img.shields.io/badge/License-ApacheV2-orange.svg[link="https://www.apache.org/licenses/"]
image:https://img.shields.io/badge/Latests-news-red.svg[link="https://james.apache.org/index.html#posts"]

image::james-logo.png[link="https://james.apache.org"]

*Mime4j* can be used to parse e-mail message streams in plain
link:https://datatracker.ietf.org/doc/html/rfc822[rfc822] and MIME format
and to build a tree representation of an e-mail message.

The parser uses a callback mechanism to report parsing events such as the start of
an entity header, the start of a body. The parser has been designed to be extremely
tolerant against messages violating the standards.

Mime4j can also be used to build a tree representation of an e-mail message via the DOM API.

== How to contribute?

.Read more...
[%collapsible]
====
James is a project that lives from the contributions of its community! Anyone can contribute!

Read https://james.apache.org/index.html#third[how to contribute].

We more than welcome *articles* and *blog posts* about James. Contact us by https://james.apache.org/mail.html[email]
or on https://gitter.im/apache/james-project[Gitter] to share your experiences.

*Documentation* is an easy way to get started, and more than wanted! Check out the https://issues.apache.org/jira/issues/?jql=project%20%3D%MIME4J%20AND%20resolution%20%3D%20Unresolved%20AND%20labels%20%3D%20documentation%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC[~documentation] label on JIRA.

And to get started with *code contributions*, search out the
https://issues.apache.org/jira/issues/?jql=project%20%3D%20MIME4J%20AND%20resolution%20%3D%20Unresolved%20AND%20labels%20%3D%20newbie%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC[~newbie],
https://issues.apache.org/jira/issues/?jql=project%20%3D%20MIME4J%20AND%20resolution%20%3D%20Unresolved%20AND%20labels%20%3D%20easyfix%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC[~easyfix],
https://issues.apache.org/jira/issues/?jql=project%20%3D%20MIME4J%20AND%20resolution%20%3D%20Unresolved%20AND%20labels%20%3D%20feature%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC[~feature] labels on JIRA.

There is many other ways one can help us: packaging, communication, etc ...
====

== Maven dependencies

.Read more...
[%collapsible]
====
Add this maven dependency to import MIME4J core:

....
<dependency>
    <groupId>org.apache.james</groupId>
    <artifactId>apache-mime4j-core</artifactId>
    <version>0.8.8</version>
</dependency>
....

Add this maven dependency to import MIME4J dom:

....
<dependency>
    <groupId>org.apache.james</groupId>
    <artifactId>apache-mime4j-dom</artifactId>
    <version>0.8.8</version>
</dependency>
....
====

== How to compile the project

.Read more...
[%collapsible]
====
We require link:https://maven.apache.org[maven] version 3.6.0 minimum to build the project.

Simply run `mvn clean install` within this directory to compile the project.

Useful options includes:

- `-DskipTests` to skip the long to execute resource consuming test suite that requires a docker daemon.
- `-T 4` to parallelize the build on several CPUs.
====

== How to use MIME4J

We maintain a set of link:examples/src/main/java/org/apache/james/mime4j/samples[examples] detailing how one can use MIME4J.