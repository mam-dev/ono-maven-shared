package net.oneandone.maven.shared.versionpolicies;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class ScmVersionPolicyTest {

    @Test
    public void testGetReleaseVersion() throws Exception {
        final ScmVersionPolicy subjectUnderTest = new ScmVersionPolicy();
        assertThat(subjectUnderTest.getReleaseVersion(null)).isNull();
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        final ScmVersionPolicy subjectUnderTest = new ScmVersionPolicy();
        assertThat(subjectUnderTest.getDevelopmentVersion(null)).isNull();
    }
}