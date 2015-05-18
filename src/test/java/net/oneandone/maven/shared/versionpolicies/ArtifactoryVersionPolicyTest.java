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
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactoryVersionPolicyTest {

    @Test
    public void testGetReleaseVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            InputStream getInputStream(URL url) throws IOException {
                return new ByteArrayInputStream("1.5.6".getBytes(Charsets.UTF_8));
            }
        };
        final VersionPolicyRequest versionPolicyRequest = createVersionPolicyRequest();
        final VersionPolicyResult releaseVersion = subjectUnderTest.getReleaseVersion(versionPolicyRequest);
        assertThat(releaseVersion.getVersion()).isEqualTo("1.5.7");
    }

    @Test(expected = RuntimeException.class)
    public void testGetReleaseVersionRTE() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()) {
            @Override
            InputStream getInputStream(URL url) throws IOException {
                throw new IOException("Oops");
            }
        };
        final VersionPolicyRequest versionPolicyRequest = createVersionPolicyRequest();
        subjectUnderTest.getReleaseVersion(versionPolicyRequest);
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject()){
            @Override
            InputStream getInputStream(URL url) throws IOException {
                return new ByteArrayInputStream("1.5.6".getBytes(Charsets.UTF_8));
            }
        };
        final VersionPolicyRequest versionPolicyRequest = createVersionPolicyRequest();
        final VersionPolicyResult releaseVersion = subjectUnderTest.getDevelopmentVersion(versionPolicyRequest);
        assertThat(releaseVersion.getVersion()).isEqualTo("1-SNAPSHOT");
    }

    @Test
    public void testCreateUrlString() {
        final MavenProject mavenProject = createMavenProject();
        final Properties properties = mavenProject.getProperties();
        properties.setProperty("artifactory-http", "http://artifactory.example.com/artifactory");
        properties.setProperty("artifactory-repositories", "first-repo,second-repo");
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(mavenProject);
        assertThat(subjectUnderTest.createUrlString(
                mavenProject)).isEqualTo(
                "http://artifactory.example.com/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=first-repo,second-repo");
    }

    @Test
    public void testCreateUrlStringDefaul() {
        final MavenProject mavenProject = createMavenProject();
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(mavenProject);
        assertThat(subjectUnderTest.createUrlString(
                mavenProject)).isEqualTo(
                "http://repo.jfrog.org/artifactory/api/search/latestVersion?g=net.oneandone.maven.poms&a=foss-parent&repos=repo1-cache");
    }

    private VersionPolicyRequest createVersionPolicyRequest() {
        final VersionPolicyRequest versionPolicyRequest = new VersionPolicyRequest();
        versionPolicyRequest.setVersion("1-SNAPSHOT");
        return versionPolicyRequest;
    }

    private MavenProject createMavenProject() {
        final MavenProject project = new MavenProject();
        project.setGroupId("net.oneandone.maven.poms");
        project.setArtifactId("foss-parent");
        project.setVersion("1-SNAPSHOT");
        return project;
    }
}