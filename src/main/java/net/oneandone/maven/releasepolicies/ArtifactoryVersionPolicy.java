package net.oneandone.maven.releasepolicies;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.apache.maven.shared.utils.io.IOUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * A {@link VersionPolicy} implementation that retrieve the latest version from the SCM and just increases.
 */
@Component(
        role = VersionPolicy.class,
        hint = "ArtifactoryVersionPolicy",
        description = "A VersionPolicy implementation that retrieve the latest version from Artifactory"
)
public class ArtifactoryVersionPolicy implements VersionPolicy {

    private static final String HTTP_ARTIFACTORY = "http://repo.jfrog.org/artifactory";
    private static final String REPOSITORIES = "repo1-cache";

    @Requirement
    MavenProject mavenProject;


    // For injection.
    public ArtifactoryVersionPolicy() {}

    // Just for tests
    ArtifactoryVersionPolicy(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        final String groupId = mavenProject.getGroupId();
        final String artifactId = mavenProject.getArtifactId();
        final String currentVersion;
        try {
            final URL url = createURL(groupId, artifactId);
            try(final InputStream stream = getInputStream(url)) {
                currentVersion = IOUtil.toString(stream, "UTF-8");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to access " + HTTP_ARTIFACTORY, e);
        }
        final DefaultVersionInfo versionInfo = new DefaultVersionInfo(currentVersion);
        versionPolicyResult.setVersion(versionInfo.getNextVersion().getReleaseVersionString());
        return versionPolicyResult;
    }

    InputStream getInputStream(URL url) throws IOException {
        return url.openStream();
    }

    URL createURL(String groupId, String artifactId) throws MalformedURLException {
        return new URL(String.format(
                        Locale.ENGLISH,
                        HTTP_ARTIFACTORY + "/api/search/latestVersion?g=%s&a=%s&repos=%s",
                        groupId, artifactId, REPOSITORIES));
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }
}
