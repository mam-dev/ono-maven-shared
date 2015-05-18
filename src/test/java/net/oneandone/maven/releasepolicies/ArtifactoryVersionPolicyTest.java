package net.oneandone.maven.releasepolicies;

import com.google.common.base.Charsets;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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