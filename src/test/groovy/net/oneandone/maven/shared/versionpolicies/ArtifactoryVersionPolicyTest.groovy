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
import spock.lang.Unroll

class ArtifactoryVersionPolicyTest extends Specification implements AbstractVersionPolicyTrait {

    def mavenProject = createMavenProject();

    @Unroll
    def 'Should increment minor when major or minor stays below or equals latest release  #mavenVersion -> #releaseVersion'() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6');

        when:
        mavenProject.version = mavenVersion

        then:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == mavenVersion
        subjectUnderTest.getCurrentVersion() == '1.5.6'

        where:
        mavenVersion     | releaseVersion
        '1-SNAPSHOT'     | '1.5.7'
        '1.0-SNAPSHOT'   | '1.5.7'
        '1.5.6-SNAPSHOT' | '1.5.7'
        '1.5.7-SNAPSHOT' | '1.5.7'
    }

    @Unroll
    def 'Should restart with zero for new major or minor SNAPSHOT #mavenVersion -> #releaseVersion'() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6')

        when:
        mavenProject.version = mavenVersion

        then:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == mavenVersion
        subjectUnderTest.getCurrentVersion() == '1.5.6'

        where:
        mavenVersion   | releaseVersion
        '1.6-SNAPSHOT' | '1.6.0'
        '2-SNAPSHOT'   | '2.0'
        '2.0-SNAPSHOT' | '2.0.0'
    }

    def 'Should throw when URL could not be opened'() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            def InputStream getInputStream(URL url) throws IOException {
                throw new IOException("Could not open")
            }
        }

        when:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER)

        then:
        PolicyException e = thrown()
        e.cause.class == IOException.class
    }

    def 'Should throw when URL could not be read'() {
        given:
        def mockedStream = Mock(InputStream)
        mockedStream.read(*_) >> { throw new IOException("VP could not read") }
        def subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            def InputStream getInputStream(URL url) throws IOException {
                return mockedStream
            }
        }

        when:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER)

        then:
        PolicyException e = thrown()
        e.cause.class == IOException.class
    }

    def 'Should default to jfrog and repo1 when neither Artifactory URL nor repositories are given'() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6')

        expect:
        subjectUnderTest.createUrlString() == 'http://repo.jfrog.org/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=repo1'
    }

    def "Should pick up properties for Artifactory URL and repositories"() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6')

        when:
        def properties = mavenProject.getProperties();
        properties['artifactory-version-policy-http'] = 'http://artifactory.example.com/artifactory'
        properties['artifactory-version-policy-repositories'] = 'first-repo,second-repo'

        then:
        subjectUnderTest.createUrlString() == 'http://artifactory.example.com/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=first-repo,second-repo'
    }

    def 'Should have a default constructor used with injection in Maven'() {
        given:
        def subjectUnderTest = new ArtifactoryVersionPolicy()

        expect:
        subjectUnderTest != null
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
