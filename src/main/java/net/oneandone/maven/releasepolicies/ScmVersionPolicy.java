package net.oneandone.maven.releasepolicies;

import org.apache.maven.shared.release.config.ReleaseDescriptorStore;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * A {@link VersionPolicy} implementation that retrieve the latest version from the SCM and just increases.
 */
@Component(
        role = VersionPolicy.class,
        hint = "ScmVersionPolicy",
        description = "A VersionPolicy implementation that Retrieve the latest version from the SCM and just increases."
)
public class ScmVersionPolicy implements VersionPolicy {

    @Requirement
    ReleaseDescriptorStore releaseDescriptorStore;

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        return null;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        return null;
    }
}
