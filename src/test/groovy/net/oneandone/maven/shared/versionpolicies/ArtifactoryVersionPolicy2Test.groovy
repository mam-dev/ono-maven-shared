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

import org.apache.maven.project.MavenProject
import org.apache.maven.shared.release.policy.PolicyException
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ArtifactoryVersionPolicy2Test extends Specification implements AbstractVersionPolicyTrait {

    def mavenProject = createMavenProject();

    @Subject
    def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6');

    @Unroll
    def 'increments minor when major or minor stays below or equals last release  #mavenVersion -> #releaseVersion'() {
        when:
        mavenProject.version = mavenVersion

        then:
        subjectUnderTest.getReleaseVersion(null).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(null).version == mavenVersion
        subjectUnderTest.currentVersion == '1.5.6'

        where:
        mavenVersion     | releaseVersion
        '1-SNAPSHOT'     |  '1.5.7'
        '1.0-SNAPSHOT'   |  '1.5.7'
        '1.5.6-SNAPSHOT' |  '1.5.7'
        '1.5.7-SNAPSHOT' |  '1.5.7'
    }

    @Unroll
    def 'restarts with zero for new major or minor SNAPSHOT #mavenVersion -> #releaseVersion'() {
        when:
        mavenProject.version = mavenVersion

        then:
        subjectUnderTest.getReleaseVersion(null).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(null).version == mavenVersion
        subjectUnderTest.currentVersion == '1.5.6'

        where:
        mavenVersion     | releaseVersion
        '1.6-SNAPSHOT'   |  '1.6.0'
        '2-SNAPSHOT'     |  '2.0'
        '2.0-SNAPSHOT'   |  '2.0.0'
    }

    def 'throws when URL could not be opened'() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            def InputStream getInputStream(URL url) throws IOException {
                throw new IOException("Could not open")
            }
        }

        when:
        subjectUnderTest.getReleaseVersion(null)

        then:
        thrown(PolicyException)
    }


    static class ArtifactoryVersionPolicyStub extends ArtifactoryVersionPolicy {

        private final String releaseVersionFromArtifactory;

        public ArtifactoryVersionPolicyStub(MavenProject mavenProject, String releaseVersionFromArtifactory) {
            super(mavenProject);
            this.releaseVersionFromArtifactory = releaseVersionFromArtifactory;
        }

        @Override
        InputStream getInputStream(URL url) throws IOException {
            return new ByteArrayInputStream(releaseVersionFromArtifactory.getBytes("UTF-8"));
        }
    }

}
