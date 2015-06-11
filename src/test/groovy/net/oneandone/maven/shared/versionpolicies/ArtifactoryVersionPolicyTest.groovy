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
import org.apache.maven.settings.Server
import org.apache.maven.settings.Settings
import org.apache.maven.shared.release.policy.PolicyException
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ArtifactoryVersionPolicyTest extends Specification implements AbstractVersionPolicyTrait {

    static final String LATEST_VERSION_FROM_ARTIFACTORY = '1.5.6'

    def mavenProject = createMavenProject();

    @Subject
    def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, null, LATEST_VERSION_FROM_ARTIFACTORY);

    @Unroll('Increments minor when major or minor stays below or equals latest release  #snapshotVersion -> #releaseVersion')
    def 'increments version'(def snapshotVersion, def releaseVersion) {
        when:
        mavenProject.version = snapshotVersion

        then:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == snapshotVersion
        subjectUnderTest.currentVersion == LATEST_VERSION_FROM_ARTIFACTORY

        where:
        snapshotVersion  | releaseVersion
        '1-SNAPSHOT'     | '1.5.7'
        '1.0-SNAPSHOT'   | '1.5.7'
        '1.5.6-SNAPSHOT' | '1.5.7'
        '1.5.7-SNAPSHOT' | '1.5.7'
    }

    @Unroll('Restarts with zero for new major or minor SNAPSHOT #snapshotVersion -> #releaseVersion')
    def 'restarts with zero'(def snapshotVersion, def releaseVersion) {
        when:
        mavenProject.version = snapshotVersion

        then:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == snapshotVersion
        subjectUnderTest.currentVersion == LATEST_VERSION_FROM_ARTIFACTORY

        where:
        snapshotVersion | releaseVersion
        '1.6-SNAPSHOT'  | '1.6.0'
        '2-SNAPSHOT'    | '2.0'
        '2.0-SNAPSHOT'  | '2.0.0'
    }

    @Unroll('Handles new artifacts #snapShotVersion -> #releaseVersion')
    def 'handles new artifacts'(def snapShotVersion, def releaseVersion, def currentVersion) {
        given:
        def mavenProject = createMavenProject();
        @Subject
        def subjectUnderTest = new ArtifactoryVersionPolicy(mavenProject, null) {
            @Override
            def InputStream getInputStream(URL url) throws IOException {
                throw new FileNotFoundException("HTTP/1.1 404 Not Found")
            }
        }
        mavenProject.version = snapShotVersion

        expect:
        subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).version == releaseVersion
        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).version == snapShotVersion
        subjectUnderTest.currentVersion == currentVersion

        where:
        snapShotVersion | releaseVersion | currentVersion
        '1.6-SNAPSHOT'  | '1.6.0'        | '0'
        '2-SNAPSHOT'    | '2.0'          | '0'
        '2.0-SNAPSHOT'  | '2.0.0'        | '0'

    }

    def 'Throws when Artifactory URL could not be opened'() {
        given:
        @Subject
        def subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject(), null) {
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

    def 'Throws when Artifactory URL chokes during read'() {
        given:
        def mockedStream = Mock(InputStream)
        mockedStream.read(*_) >> { throw new IOException("VP could not read") }
        @Subject
        def subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject(), null) {
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

    def 'Defaults to jfrog and repo1 when neither Artifactory URL nor repositories are given'() {
        expect:
        subjectUnderTest.createUrlString() == 'http://repo.jfrog.org/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=repo1'
    }

    def 'Picks up properties for Artifactory'() {
        given:
        def stubServer = new Server(
            id: 'private-repo',
            username: 'username',
            password: 'password'
        )
        def stubSettings = new Settings()
        stubSettings.addServer(stubServer)
        def mavenProject = getMavenProject()
        @Subject
        def subjectUnderTest = new ArtifactoryVersionPolicyStub(mavenProject, stubSettings, '1.5.6')

        when:
        def properties = mavenProject.getProperties();
        properties['artifactory-version-policy-http'] = 'http://artifactory.example.com/artifactory'
        properties['artifactory-version-policy-server-id'] = 'private-repo'
        properties['artifactory-version-policy-repositories'] = 'first-repo,second-repo'
        def urlString = subjectUnderTest.createUrlString()
        def connection = subjectUnderTest.getHttpURLConnection(new URL(urlString))

        then:
        urlString == 'http://artifactory.example.com/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=first-repo,second-repo'
        connection.requests.headers['Authorization'][0] == 'Basic dXNlcm5hbWU6cGFzc3dvcmQ='

        cleanup:
        connection.disconnect()
    }

    def 'Has a default constructor used with injection in Maven'() {
        given:
        @Subject
        def subjectUnderTest = new ArtifactoryVersionPolicy()

        expect:
        subjectUnderTest != null
    }

    static class ArtifactoryVersionPolicyStub extends ArtifactoryVersionPolicy {

        private final String releaseVersionFromArtifactory;

        public ArtifactoryVersionPolicyStub(MavenProject mavenProject, Settings settings, String releaseVersionFromArtifactory) {
            super(mavenProject, settings);
            this.releaseVersionFromArtifactory = releaseVersionFromArtifactory;
        }

        @Override
        InputStream getInputStream(URL url) throws IOException {
            return new ByteArrayInputStream(releaseVersionFromArtifactory.getBytes("UTF-8"));
        }
    }

}
