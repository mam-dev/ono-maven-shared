package net.oneandone.maven.releasepolicies;

import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class ArtifactoryVersionPolicyTest {

    @Test
    public void testGetReleaseVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy();
        final VersionPolicyResult releaseVersion = subjectUnderTest.getReleaseVersion(null);
        assertThat(releaseVersion.getVersion()).isEqualTo("1.5.7");
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy();
        final VersionPolicyResult releaseVersion = subjectUnderTest.getDevelopmentVersion(null);
        assertThat(releaseVersion.getVersion()).isEqualTo("1-SNAPSHOT");
    }
}