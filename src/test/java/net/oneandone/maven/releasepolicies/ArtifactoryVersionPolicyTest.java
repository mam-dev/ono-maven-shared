package net.oneandone.maven.releasepolicies;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class ArtifactoryVersionPolicyTest {

    @Test
    public void testGetReleaseVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy();
        final VersionPolicyRequest versionPolicyRequest = createVersionPolicyRequest();
        final VersionPolicyResult releaseVersion = subjectUnderTest.getReleaseVersion(versionPolicyRequest);
        assertThat(releaseVersion.getVersion()).isEqualTo("1.5.7");
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy();
        final VersionPolicyRequest versionPolicyRequest = createVersionPolicyRequest();
        final VersionPolicyResult releaseVersion = subjectUnderTest.getDevelopmentVersion(versionPolicyRequest);
        assertThat(releaseVersion.getVersion()).isEqualTo("1-SNAPSHOT");
    }

    private VersionPolicyRequest createVersionPolicyRequest() {
        final VersionPolicyRequest versionPolicyRequest = new VersionPolicyRequest();
        versionPolicyRequest.setVersion("1-SNAPSHOT");
        final Metadata metaData = new Metadata();
        metaData.setGroupId("net.oneandone.maven.poms");
        metaData.setArtifactId("foss-parent");
        metaData.setVersion("1-SNAPSHOT");
        versionPolicyRequest.setMetaData(metaData);
        return versionPolicyRequest;
    }
}