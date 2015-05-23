package net.oneandone.maven.shared.versionpolicies;

import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class VersionPolicyAssertions extends Assertions {
    public static class VersionPolicyAssert extends AbstractAssert<VersionPolicyAssert, VersionPolicy> {

        public VersionPolicyAssert(VersionPolicy actual) {
            super(actual, VersionPolicyAssert.class);
        }

        public VersionPolicyAssert releaseVersionCorrespondsTo(String version) throws VersionParseException, PolicyException {
            Assertions.assertThat(actual.getReleaseVersion(null).getVersion()).isEqualTo(version);
            return this;
        }

        public VersionPolicyAssert developmentVersionCorrespondsTo(String version) throws VersionParseException, PolicyException {
            Assertions.assertThat(actual.getDevelopmentVersion(null).getVersion()).isEqualTo(version);
            return this;
        }
    }

    public static VersionPolicyAssert assertThat(VersionPolicy actual) {
        return new VersionPolicyAssert(actual);
    }
}
