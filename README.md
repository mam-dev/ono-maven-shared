# 1-and-1 :: FOSS Libraries useable by Apache Maven

* Currently some code is smeared and repeated in several plugins.
* Try to consolidate this in here.

## VersionPolicies

### Introduction

* The [maven-release-plugin][maven-release-plugin] offers a limited choice of policies how it determines
  the next release and development version.
* By default, `-SNAPSHOT` is stripped to determine the release version, the last numeric part if this release version
  is increased and extended by `-SNAPSHOT` again to get the next development version.
* Say you have a schema, where you always want to use `MAJOR-SNAPSHOT` or `MAJOR.MINOR-SNAPSHOT` and the next release
  version will always be `MAJOR.MINOR` or `MAJOR.MINOR.MICRO`, e.g. you already released `1.54` and the next version
  should be `1.55`.
* [ArtifactoryVersionPolicy](src/main/java/net/oneandone/maven/shared/versionpolicies/ArtifactoryVersionPolicy.java)
  retrieves the number from Artifactory and sets the `releaseVersion` to the next number. The next 
  `developmentVersion` always stays the same until you change it yourself in the source. 

### Usage 

```xml
<project>
    <properties>
        <projectVersionPolicyId>ONOArtifactoryVersionPolicy</projectVersionPolicyId>
    </properties>
    <build><pluginManagement><plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-release-plugin</artifactId>
        <version>${maven-release-plugin.version}</version>
        <dependencies>
            <dependency>
                <groupId>net.oneandone.maven</groupId>
                <artifactId>ono-maven-shared</artifactId>
                <version>1.XX</version>
            </dependency>
        </dependencies>
    </plugin>
    </plugins></pluginManagement></build>
</project>
```

[maven-release-plugin]: http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#projectVersionPolicyId