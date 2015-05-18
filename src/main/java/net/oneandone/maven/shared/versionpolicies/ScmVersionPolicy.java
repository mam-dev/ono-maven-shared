package net.oneandone.maven.shared.versionpolicies;

import org.apache.maven.project.MavenProject;
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
        hint = "ONOScmVersionPolicy",
        description = "A VersionPolicy implementation that retrieves the latest version from the SCM and just increases."
)
public class ScmVersionPolicy implements VersionPolicy {

    @Requirement
    MavenProject mavenProject;

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        return null;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        return null;
    }
}
