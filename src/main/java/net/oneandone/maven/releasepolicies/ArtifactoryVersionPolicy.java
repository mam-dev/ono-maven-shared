package net.oneandone.maven.releasepolicies;

import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.component.annotations.Component;

/**
 * A {@link VersionPolicy} implementation that retrieve the latest version from the SCM and just increases.
 */
@Component(
        role = VersionPolicy.class,
        hint = "ArtifactoryyVersionPolicy",
        description = "A VersionPolicy implementation that retrieve the latest version from Artifactory"
)
public class ArtifactoryVersionPolicy implements VersionPolicy {

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        return null;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        return null;
    }
}
