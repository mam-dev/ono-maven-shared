# 1-and-1 :: FOSS Libraries useable by Apache Maven

* Currently some code is smeared and repeated in several plugins.
* Try to consolidate this in here.

[![Build Status](https://travis-ci.org/1and1/ono-maven-shared.svg?branch=master)](https://travis-ci.org/1and1/ono-maven-shared)
[![Build Status](https://gitlab.com/mfriedenhagen/ono-maven-shared/badges/master/build.svg)](https://gitlab.com/mfriedenhagen/ono-maven-shared/commits/master)
[![Release](https://img.shields.io/maven-central/v/net.oneandone.maven/ono-maven-shared*.svg?label=latest%20release)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.oneandone.maven%22%20AND%20a%3A%22ono-maven-shared%22)
[![GitHub license](https://img.shields.io/github/license/1and1/ono-maven-shared.svg)]()

## VersionPolicies

### Introduction

* The [maven-release-plugin][maven-release-plugin] offers a limited choice of policies how it determines
  the next release and development version.
* By default, `-SNAPSHOT` is stripped to determine the release version, the last numeric part if this release version
  is increased and extended by `-SNAPSHOT` again to get the next development version.
* `ono-maven-shared` offers some additional policies.
* To use these during `release` you need to include this component as dependency like this:

```xml
    <project>
        <build>
          <pluginManagement>
            <plugins>
              <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-release-plugin</artifactId>
                  <version>${maven-release-plugin.version}</version>
                  <dependencies>
                      <dependency>
                          <groupId>net.oneandone.maven</groupId>
                          <artifactId>ono-maven-shared</artifactId>
                          <version>2.X</version>
                      </dependency>
                  </dependencies>
              </plugin>
            </plugins>
          </pluginManagement>
        </build>
    </project>
```


### `ONOArtifactoryVersionPolicy`

* Say you have a schema, where you always want to use `MAJOR-SNAPSHOT` or `MAJOR.MINOR-SNAPSHOT` and the next release
  version will always be `MAJOR.MINOR` or `MAJOR.MINOR.MICRO`, e.g. you already released `1.54` and the next version
  should be `1.55`.
* [ArtifactoryVersionPolicy](src/main/java/net/oneandone/maven/shared/versionpolicies/ArtifactoryVersionPolicy.java)
  retrieves the number from Artifactory and sets the `releaseVersion` to the next number. The next 
  `developmentVersion` always stays the same until you change it yourself in the source. 
* When your SNAPSHOT version is bigger than the latest release version in Artifactory, it restarts with 0.
* Additionally the [maven-release-plugin arguments](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#arguments)
  are extended with `-DONOArtifactoryVersionPolicy.latest=LATEST_FOUND_RELEASE` and as of 2.5 `-DONOCurrentVersion=LATEST_FOUND_RELEASE`.
* As of version 2.4, you may provide a property `artifactory-version-policy-server-id` which is used for
  retrieving your credentials from `~/.m2/settings.xml`. This only works with plain text or 
  [centrally secure passwords](https://www.jfrog.com/confluence/display/RTF/Centrally+Secure+Passwords).

### Behaviour when the Artifact already exists

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

### Behaviour when the Artifact does not exist

When no version of the Artifact could be found, the latest version is always **0**.

Development Version   | Version of Next Release | Latest Version
----------------------|-------------------------|---------------
1.6-SNAPSHOT          | 1.6.0                   | 0
2-SNAPSHOT            | 2.0                     | 0
2.0-SNAPSHOT          | 2.0.0                   | 0


### Usage

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
        <!-- do not forget to include ono-maven-shared as dependency as stated above -->
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
        <!-- do not forget to include ono-maven-shared as dependency as stated above -->
    </project>
```

### `ONOChangesVersionPolicy`

* Since 2.6
* Say you want to use your `src/changes/changes.xml` as leading document while releasing.
* The topmost release found in `src/changes/changes.xml` will be used as `releaseVersion`.
* Additionally the [maven-release-plugin arguments](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#arguments)
  are extended with `-DONOCurrentVersion=LATEST_FOUND_RELEASE`.
* The next `developmentVersion` always stays the same until you change it yourself in the source.
* Include shared library as `dependency` to `maven-release-plugin`.
* Set `projectVersionPolicyId` to `ONOChangesVersionPolicy`.
* See #10 as well.

```xml
    <project>
        <properties>
            <projectVersionPolicyId>ONOChangesVersionPolicy</projectVersionPolicyId>
        </properties>
        <!-- do not forget to include ono-maven-shared as dependency as stated above -->
    </project>
```

## ChangesVersionMojo

* Since 2.7
* Reuses code from ONOChangesVersionPolicy.
* Sets the following properties in the reactor:
  * `developmentVersion`  - for `maven-release-plugin`
  * `releaseVersion`      - for `maven-release-plugin`
  * `newVersion`          - for `versions-maven-plugin`
  * `ONOCurrentVersion`   - for `maven-changes-plugin`
  * `tag`                 - for `maven-scm-plugin`
  * `changes.version`     - for `maven-changes-plugin`
* Say you do not want to use the `maven-release-plugin`.
* Run `mvn ono-maven-shared:changes-version versions:set deploy -DperformRelease=true`

[maven-release-plugin]: http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#projectVersionPolicyId
