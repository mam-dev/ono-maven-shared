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

import com.google.common.base.Charsets;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtifactoryVersionPolicyTest extends AbstractVersionPolicyTest {

    @Test(expected = PolicyException.class)
    public void testGetReleaseVersionPolicyExceptionDuringOpen() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            InputStream getInputStream(URL url) throws IOException {
                throw new IOException("Could not open");
            }
        };
        subjectUnderTest.getReleaseVersion(null);
    }

    @Test(expected = PolicyException.class)
    public void testGetReleaseVersionPolicyExceptionDuringRead() throws Exception {
        final InputStream stream = mock(InputStream.class);
        when(stream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("VP could not read"));
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            InputStream getInputStream(URL url) throws IOException {
                return stream;
            }
        };
        subjectUnderTest.getReleaseVersion(null);
    }

    @Test
    public void testCreateUrlString() {
        final ArtifactoryVersionPolicy subjectUnderTest = createArtifactoryVersionPolicyWithResultFromArtifactory("1.5.6");
        final MavenProject mavenProject = subjectUnderTest.mavenProject;
        final Properties properties = mavenProject.getProperties();
        properties.setProperty("artifactory-version-policy-http", "http://artifactory.example.com/artifactory");
        properties.setProperty("artifactory-version-policy-repositories", "first-repo,second-repo");
        assertThat(subjectUnderTest.createUrlString()).isEqualTo(
                "http://artifactory.example.com/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=first-repo,second-repo");
    }

    @Test
    public void testCreateUrlStringDefault() {
        final ArtifactoryVersionPolicy subjectUnderTest = createArtifactoryVersionPolicyWithResultFromArtifactory("1.5.6");
        assertThat(subjectUnderTest.createUrlString()).isEqualTo(
                "http://repo.jfrog.org/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=repo1");
    }

    @Test
    public void testGetReleaseVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = createArtifactoryVersionPolicyWithResultFromArtifactory("1.5.6");
        final VersionPolicyResult releaseVersion = subjectUnderTest.getReleaseVersion(null);
        assertThat(releaseVersion.getVersion()).isEqualTo("1.5.7");
    }

    @Test
    public void shouldRestartWithZeroForNewMajorOrMinorSNAPSHOT() throws VersionParseException, PolicyException {
        final ArtifactoryVersionPolicy subjectUnderTest = createArtifactoryVersionPolicyWithResultFromArtifactory("1.5.6");
        subjectUnderTest.mavenProject.setVersion("1.6-SNAPSHOT");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.6.0");
        subjectUnderTest.mavenProject.setVersion("2-SNAPSHOT");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("2.0");
        subjectUnderTest.mavenProject.setVersion("2.0-SNAPSHOT");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("2.0.0");
        subjectUnderTest.mavenProject.setVersion("1.0-SNAPSHOT");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.5.7");
        subjectUnderTest.mavenProject.setVersion("1.5.6-SNAPSHOT");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.5.7");
        subjectUnderTest.mavenProject.setVersion("1.5.7-SNAPSHOT");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.5.7");
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = createArtifactoryVersionPolicyWithResultFromArtifactory("1.5.6");
        final VersionPolicyResult releaseVersion = subjectUnderTest.getDevelopmentVersion(null);
        assertThat(releaseVersion.getVersion()).isEqualTo("1-SNAPSHOT");
    }

    // Almost useless but good for line and instruction coverage.
    @Test
    public void testDefaultConstructors() {
        new ArtifactoryVersionPolicy();
    }

    ArtifactoryVersionPolicy createArtifactoryVersionPolicyWithResultFromArtifactory(String releaseVersionFromArtifactory) {
        return new ArtifactoryVersionPolicyStub(releaseVersionFromArtifactory);
    }

    private class ArtifactoryVersionPolicyStub extends ArtifactoryVersionPolicy {

        private final String releaseVersionFromArtifactory;

        public ArtifactoryVersionPolicyStub(String releaseVersionFromArtifactory) {
            super(ArtifactoryVersionPolicyTest.this.createMavenProject());
            this.releaseVersionFromArtifactory = releaseVersionFromArtifactory;
        }

        @Override
        InputStream getInputStream(URL url) throws IOException {
            return new ByteArrayInputStream(releaseVersionFromArtifactory.getBytes(Charsets.UTF_8));
        }
    }
}