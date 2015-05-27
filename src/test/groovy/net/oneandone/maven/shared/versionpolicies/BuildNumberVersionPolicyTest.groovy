/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.maven.shared.versionpolicies

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

public class BuildNumberVersionPolicyTest extends Specification implements AbstractVersionPolicyTrait {

    private def systemEnv = new HashMap<String, String>()

    @Subject
    def subjectUnderTest = new BuildNumberVersionPolicy(createMavenProject(), systemEnv);

    @Unroll
    def 'Set minor to BUILD_NUMBER=#buildNumber'(def buildNumber) {
        given:
        systemEnv['BUILD_NUMBER'] = buildNumber

        expect:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == '1.' + buildNumber

        where:
        buildNumber << ['1', '2']
    }

    @Unroll
    def 'Set minor to TRAVIS_BUILD_NUMBER=#buildNumber'(def buildNumber) {
        given:
        def buildNumberIdentifier = "TRAVIS_BUILD_NUMBER";
        subjectUnderTest.mavenProject.properties['buildnumber-version-policy-identifier'] = buildNumberIdentifier
        systemEnv[buildNumberIdentifier] = buildNumber

        expect:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == '1.' + buildNumber

        where:
        buildNumber << ['1', '2']
    }


    def 'Assures development version is SNAPSHOT'() {
        given:
        systemEnv.put("BUILD_NUMBER", "5");

        expect:
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == '1-SNAPSHOT'
    }



    def 'Has a default constructor used with injection in Maven'() {
        given:
        def subjectUnderTest = new BuildNumberVersionPolicy()

        expect:
        subjectUnderTest != null;
    }
}