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
package net.oneandone.maven.shared.versionpolicies;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.policy.PolicyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactoryVersionPolicyTest extends AbstractVersionPolicyTestSupport {

    private static final String LATEST_VERSION_FROM_ARTIFACTORY = "1.5.6";

    private final MavenProject mavenProject = createMavenProject();
    private final ArtifactoryVersionPolicyStub subjectUnderTest =
            new ArtifactoryVersionPolicyStub(mavenProject, null, LATEST_VERSION_FROM_ARTIFACTORY);

    // where
    @ParameterizedTest(name = "Increments minor when major or minor stays below or equals latest release {0} -> {1}")
    @CsvSource({
            "'1-SNAPSHOT', '1.5.7'",
            "'1.0-SNAPSHOT', '1.5.7'",
            "'1.5.6-SNAPSHOT', '1.5.7'",
            "'1.5.7-SNAPSHOT', '1.5.7'"
    })
    void incrementsVersion(String snapshotVersion, String releaseVersion) {
        // when
        mavenProject.setVersion(snapshotVersion);

        // then
        assertAll(
                () -> assertEquals(releaseVersion, subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(snapshotVersion,
                        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(LATEST_VERSION_FROM_ARTIFACTORY, subjectUnderTest.getCurrentVersion())
        );
    }

    // where
    @ParameterizedTest(name = "Restarts with zero for new major or minor SNAPSHOT {0} -> {1}")
    @CsvSource({
            "'1.6-SNAPSHOT', '1.6.0'",
            "'2-SNAPSHOT', '2.0'",
            "'2.0-SNAPSHOT', '2.0.0'"
    })
    void restartsWithZero(String snapshotVersion, String releaseVersion) {
        // when
        mavenProject.setVersion(snapshotVersion);

        // then
        assertAll(
                () -> assertEquals(releaseVersion, subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(snapshotVersion,
                        subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(LATEST_VERSION_FROM_ARTIFACTORY, subjectUnderTest.getCurrentVersion())
        );
    }

    // where
    @ParameterizedTest(name = "Handles new artifacts {0} -> {1}")
    @CsvSource(value = {
            "'0.1-SNAPSHOT', '0.1.0', 'UNKNOWN'",
            "'1.6-SNAPSHOT', '1.6.0', 'UNKNOWN'",
            "'2-SNAPSHOT', '2.0', 'UNKNOWN'",
            "'2.0-SNAPSHOT', '2.0.0', 'UNKNOWN'",
            "'1-SNAPSHOT', '1.0', 'UNKNOWN'"
    })
    void handlesNewArtifacts(String snapshotVersion, String releaseVersion, String currentVersion) {
        // given
        final var project = createMavenProject();
        final var policy = new ArtifactoryVersionPolicy(project, null) {
            @Override
            String fetchBody(URI uri) {
                return "";
            }
        };
        project.setVersion(snapshotVersion);

        // expect
        assertAll(
                () -> assertEquals(releaseVersion, policy.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(snapshotVersion,
                        policy.getDevelopmentVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(currentVersion, policy.getCurrentVersion())
        );
    }

    @Test
    @DisplayName("Throws when Artifactory URL could not be opened")
    void throwsWhenArtifactoryUrlCouldNotBeOpened() {
        // given
        final var policy = new ArtifactoryVersionPolicy(createMavenProject(), null) {
            @Override
            String fetchBody(URI uri) throws IOException {
                throw new IOException("Could not open");
            }
        };

        // when / then
        final var exception = assertThrows(PolicyException.class,
                () -> policy.getReleaseVersion(VPR_DOES_NOT_MATTER));
        assertEquals(IOException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Defaults to jfrog and repo1 when neither Artifactory URL nor repositories are given")
    void defaultsToJfrogAndRepo1WhenNeitherArtifactoryUrlNorRepositoriesAreGiven() {
        // expect
        assertEquals("https://repo.jfrog.org/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=repo1",
                subjectUnderTest.createUrlString());
    }

    @Test
    @DisplayName("Picks up properties for Artifactory")
    void picksUpPropertiesForArtifactory() throws Exception {
        // given
        final var server = new Server();
        server.setId("private-repo");
        server.setUsername("username");
        server.setPassword("password");
        final var settings = new Settings();
        settings.addServer(server);
        final var session = new MavenSession(null, settings, null, null, null, null, null, null, null);
        final var project = createMavenProject();
        final var policy = new ArtifactoryVersionPolicyStub(project, session, "1.5.6");

        // when
        final var properties = project.getProperties();
        properties.setProperty("artifactory-version-policy-http",
                "http://artifactory.example.com/artifactory");
        properties.setProperty("artifactory-version-policy-server-id", "private-repo");
        properties.setProperty("artifactory-version-policy-repositories", "first-repo,second-repo");
        final var urlString = policy.createUrlString();
        final var request = policy.createHttpRequest(URI.create(urlString));

        // then
        assertAll(
                () -> assertEquals(
                        "http://artifactory.example.com/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=first-repo,second-repo",
                        request.uri().toString()),
                () -> assertEquals("Basic dXNlcm5hbWU6cGFzc3dvcmQ=",
                        request.headers().firstValue("Authorization").orElseThrow())
        );
    }

    @Test
    @DisplayName("Has a default constructor used with injection in Maven")
    void hasDefaultConstructorUsedWithInjectionInMaven() {
        // expect
        assertNotNull(new ArtifactoryVersionPolicy());
    }

    static class ArtifactoryVersionPolicyStub extends ArtifactoryVersionPolicy {

        private final String releaseVersionFromArtifactory;

        ArtifactoryVersionPolicyStub(MavenProject mavenProject, MavenSession mavenSession,
                                     String releaseVersionFromArtifactory) {
            super(mavenProject, mavenSession);
            this.releaseVersionFromArtifactory = releaseVersionFromArtifactory;
        }

        @Override
        String fetchBody(URI uri) {
            return releaseVersionFromArtifactory;
        }
    }

}
