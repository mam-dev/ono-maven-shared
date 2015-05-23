# 1-and-1 :: FOSS Libraries useable by Apache Maven

* Currently some code is smeared and repeated in several plugins.
* Try to consolidate this in here.

Latest Travis-Build: [![Build Status](https://travis-ci.org/1and1/ono-maven-shared.svg?branch=master)](https://travis-ci.org/1and1/ono-maven-shared)

## VersionPolicies

### Introduction

* The [maven-release-plugin][maven-release-plugin] offers a limited choice of policies how it determines
  the next release and development version.
* By default, `-SNAPSHOT` is stripped to determine the release version, the last numeric part if this release version
  is increased and extended by `-SNAPSHOT` again to get the next development version.

### `ONOArtifactoryVersionPolicy`

* Say you have a schema, where you always want to use `MAJOR-SNAPSHOT` or `MAJOR.MINOR-SNAPSHOT` and the next release
  version will always be `MAJOR.MINOR` or `MAJOR.MINOR.MICRO`, e.g. you already released `1.54` and the next version
  should be `1.55`.
* [ArtifactoryVersionPolicy](src/main/java/net/oneandone/maven/shared/versionpolicies/ArtifactoryVersionPolicy.java)
  retrieves the number from Artifactory and sets the `releaseVersion` to the next number. The next 
  `developmentVersion` always stays the same until you change it yourself in the source. 
* When your SNAPSHOT version is bigger than the latest release version in Artifactory, it restarts with 0.

Latest deployed release is always **1.5.6**

Development Version | Version of Next Release
--------------------|-------------------
  1-SNAPSHOT        |  1.5.7
  1.0-SNAPSHOT      |  1.5.7
  1.5.6-SNAPSHOT    |  1.5.7
  1.5.7-SNAPSHOT    |  1.5.7
  1.6-SNAPSHOT      |  1.6.0
  2-SNAPSHOT        |  2.0
  2.0-SNAPSHOT      |  2.0.0

* Include shared library as `dependency` to `maven-release-plugin`.
* Set `projectVersionPolicyId` to `ONOArtifactoryVersionPolicy`.
* Optionally set:
 * `artifactory-version-policy-http`: Base-URL of Artifactory (without trailing slash, defaults to http://repo.jfrog.org/artifactory) 
 * `artifactory-version-policy-repositories`: Comma separated list of repositories to search (defaults to: repo1) 


```xml
    <project>
        <properties>
            <projectVersionPolicyId>ONOArtifactoryVersionPolicy</projectVersionPolicyId>
            <!-- for inhouse repositories -->
            <artifactory-version-policy-http>http://artifactory.example.com/artifactory</artifactory-version-policy-http>
            <!-- for inhouse repositories -->
            <artifactory-version-policy-repositories>first-repo,second-repo</artifactory-version-policy-repositories>
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
                    <version>0.X</version>
                </dependency>
            </dependencies>
        </plugin>
        </plugins></pluginManagement></build>
    </project>
```

### `ONOBuildNumberVersionPolicy`

* Say you have a schema, where you always want to use `MAJOR-SNAPSHOT` or `MAJOR.MINOR-SNAPSHOT` and the next release
  version will always be `MAJOR.MINOR` or `MAJOR.MINOR.MICRO` and you want to use the `BUILD_NUMBER` available
  from your CI job to be used as variable.
* [BuildNumberVersionPolicy](src/main/java/net/oneandone/maven/shared/versionpolicies/BuildNumberVersionPolicy.java)
  attaches the `BUILD_NUMBER` as `MINOR` or `MICRO` version. The next 
  `developmentVersion` always stays the same until you change it yourself in the source. 
* Include shared library as `dependency` to `maven-release-plugin`.
* Set `projectVersionPolicyId` to `ONOBuildNumberVersionPolicy`.
* Optionally set `<buildnumber-version-policy-identifier>` when the environment name of your build number is
  something else, e.g. `TRAVIS_BUILD_NUMBER`.
* See #2 as well.


```xml
    <project>
        <properties>
            <projectVersionPolicyId>ONOBuildNumberVersionPolicy</projectVersionPolicyId>
            <buildnumber-version-policy-identifier>TRAVIS_BUILD_NUMBER<buildnumber-version-policy-identifier>
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
                    <version>1.X</version>
                </dependency>
            </dependencies>
        </plugin>
        </plugins></pluginManagement></build>
    </project>
```

[maven-release-plugin]: http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#projectVersionPolicyId