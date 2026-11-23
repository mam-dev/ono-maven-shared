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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

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
    void incrementsVersion(String snapshotVersion, String releaseVersion) throws Exception {
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
    void restartsWithZero(String snapshotVersion, String releaseVersion) throws Exception {
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
            "'0.1-SNAPSHOT', '0.1.0', NULL",
            "'1.6-SNAPSHOT', '1.6.0', NULL",
            "'2-SNAPSHOT', '2.0', NULL",
            "'2.0-SNAPSHOT', '2.0.0', NULL",
            "'1-SNAPSHOT', '1.0', NULL"
    }, nullValues = "NULL")
    void handlesNewArtifacts(String snapshotVersion, String releaseVersion, String currentVersion) throws Exception {
        // given
        MavenProject project = createMavenProject();
        ArtifactoryVersionPolicy policy = new ArtifactoryVersionPolicy(project, null) {
            @Override
            InputStream getInputStream(URL url) throws IOException {
                throw new FileNotFoundException("HTTP/1.1 404 Not Found");
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
        ArtifactoryVersionPolicy policy = new ArtifactoryVersionPolicy(createMavenProject(), null) {
            @Override
            InputStream getInputStream(URL url) throws IOException {
                throw new IOException("Could not open");
            }
        };

        // when / then
        PolicyException exception = assertThrows(PolicyException.class,
                () -> policy.getReleaseVersion(VPR_DOES_NOT_MATTER));
        assertEquals(IOException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Throws when Artifactory URL chokes during read")
    void throwsWhenArtifactoryUrlChokesDuringRead() {
        // given
        ArtifactoryVersionPolicy policy = new ArtifactoryVersionPolicy(createMavenProject(), null) {
            @Override
            InputStream getInputStream(URL url) {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("VP could not read");
                    }
                };
            }
        };

        // when / then
        PolicyException exception = assertThrows(PolicyException.class,
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
        Server server = new Server();
        server.setId("private-repo");
        server.setUsername("username");
        server.setPassword("password");
        Settings settings = new Settings();
        settings.addServer(server);
        MavenSession session = new MavenSession(null, settings, null, null, null, null, null, null, null);
        MavenProject project = createMavenProject();
        ArtifactoryVersionPolicyStub policy = new ArtifactoryVersionPolicyStub(project, session, "1.5.6");

        // when
        project.getProperties().setProperty("artifactory-version-policy-http",
                "http://artifactory.example.com/artifactory");
        project.getProperties().setProperty("artifactory-version-policy-server-id", "private-repo");
        project.getProperties().setProperty("artifactory-version-policy-repositories", "first-repo,second-repo");
        String urlString = policy.createUrlString();
        RecordingHttpURLConnection[] recordedConnection = new RecordingHttpURLConnection[1];
        URL testUrl = new URL(null, urlString, new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) {
                recordedConnection[0] = new RecordingHttpURLConnection(url);
                return recordedConnection[0];
            }
        });
        HttpURLConnection connection = policy.getHttpURLConnection(testUrl);

        // then
        assertAll(
                () -> assertEquals(
                        "http://artifactory.example.com/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=first-repo,second-repo",
                        urlString),
                () -> assertEquals("Basic dXNlcm5hbWU6cGFzc3dvcmQ=",
                        recordedConnection[0].requestProperties.get("Authorization"))
        );

        // cleanup
        connection.disconnect();
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
        InputStream getInputStream(URL url) {
            return new ByteArrayInputStream(releaseVersionFromArtifactory.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private static class RecordingHttpURLConnection extends HttpURLConnection {

        private final Map<String, String> requestProperties = new HashMap<>();

        RecordingHttpURLConnection(URL url) {
            super(url);
        }

        @Override
        public void setRequestProperty(String key, String value) {
            requestProperties.put(key, value);
        }

        @Override
        public void connect() {}

        @Override
        public void disconnect() {}

        @Override
        public boolean usingProxy() {
            return false;
        }
    }
}
