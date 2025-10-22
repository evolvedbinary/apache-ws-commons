# Fork of Apache Web Services Commons
[![CircleCI](https://circleci.com/gh/evolvedbinary/apache-ws-commons/tree/main.svg?style=svg)](https://circleci.com/gh/evolvedbinary/apache-ws-commons/tree/main)
[![Java 8](https://img.shields.io/badge/java-8+-blue.svg)](https://adoptopenjdk.net/)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://opensource.org/licenses/Apache2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.evolvedbinary.thirdparty.org.apache.ws.commons.util/ws-commons-util/badge.svg)](https://search.maven.org/search?q=g:com.evolvedbinary.thirdparty.org.apache.ws.commons.util)


[Apache WS-Commons](https://ws.apache.org/commons/) is no longer officially maintained by Apache.
This is a simple fork for the purposes of:
1. Providing a new bug free implementation of `org.apache.ws.commons.util.NamespaceContextImpl`

* The Apache WS-Commons source code was imported to Git from the archived SVN Apache WS-Common repository at: https://svn.apache.org/repos/asf/webservices/commons/

*NOTE*: This fork was created for our own purposes, and we offer no guarantee that we will maintain it beyond our own requirements.

*NOTE*: As the SVN release tags appear to have been deleted, we have tried to reconstruct the following git tags:
* `ws-commons-util-1.0.2` contains only the original source code that we have inferred made up the last Apache release of WS-Commons Util.
* `ws-commons-1.0.1` contains only the original source code that we have inferred made up the last Apache release of WS-Commons Util and Java 5.

However, if you want an Apache WS-Commons that has been improved over the last Apache version, then this fork's artifacts are available from Maven Central as:

## WS-Commons Util
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.ws.commons.util</groupId>
        <artifactId>ws-commons-util</artifactId>
        <version>1.1.0</version>
    </dependency>
```

## WS-Commons Java 5
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.ws.commons.java5</groupId>
        <artifactId>ws-commons-java5</artifactId>
        <version>1.1.0</version>
    </dependency>
```

## Perfoming a Release
To release a new version for Evolved Binary to Maven Central, simply run:
``` bash
mvn -Dmaven.site.skip=true -Dmaven.site.deploy.skip=true -Dgpg.keyname=D4A08A8AB731BF576354A8183EF2B4866A540119 -Darguments="-Dmaven.site.skip=true -Dmaven.site.deploy.skip=true -Dgpg.keyname=D4A08A8AB731BF576354A8183EF2B4866A540119" release:prepare

...

mvn -Dmaven.site.skip=true -Dmaven.site.deploy.skip=true -Dgpg.keyname=D4A08A8AB731BF576354A8183EF2B4866A540119 -Darguments="-Dmaven.site.skip=true -Dmaven.site.deploy.skip=true -Dgpg.keyname=D4A08A8AB731BF576354A8183EF2B4866A540119" release:perform
```

Then visit https://central.sonatype.com/ and login, and release the staged artifacts to Maven Central

