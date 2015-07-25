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

import org.apache.maven.shared.release.policy.PolicyException
import spock.lang.Specification
import spock.lang.Subject

class ChangesVersionPolicyTest extends Specification implements AbstractVersionPolicyTrait {


    def 'Get releaseVersion from changes.xml'() {
        given:
        def mavenProject = createMavenProject();
        @Subject
        def subjectUnderTest = new ChangesVersionPolicy(mavenProject, 'target/test-classes/changes/changes.xml');

        expect:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == '3.0.2'
    }

    def 'Choke when changes.xml could not be found'() {
        given:
        def mavenProject = createMavenProject();
        @Subject
        def subjectUnderTest = new ChangesVersionPolicy(mavenProject, 'target/test-classes/changes/does_not_exist.xml');

        when:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER)

        then:
        thrown(PolicyException)
    }

    def 'Choke when changes.xml is not valid'() {
        given:
        def mavenProject = createMavenProject();
        @Subject
        def subjectUnderTest = new ChangesVersionPolicy(mavenProject, 'target/test-classes/changes/wrong_changes.xml');

        when:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER)

        then:
        thrown(PolicyException)
    }

    def 'Choke when changes.xml has no releases'() {
        given:
        def mavenProject = createMavenProject();
        @Subject
        def subjectUnderTest = new ChangesVersionPolicy(mavenProject, 'target/test-classes/changes/empty_changes.xml');

        when:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER)

        then:
        PolicyException e = thrown()
        e.message.contains('No body found')
    }

    def 'Assures development version is SNAPSHOT'() throws Exception {
        given:
        def mavenProject = createMavenProject();
        @Subject
        def subjectUnderTest = new ChangesVersionPolicy(mavenProject, null);

        expect:
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == mavenProject.getVersion()
    }

    def 'Has a default constructor used with injection in Maven'() {
        given:
        @Subject
        def subjectUnderTest = new ChangesVersionPolicy()

        expect:
        subjectUnderTest != null
    }

}