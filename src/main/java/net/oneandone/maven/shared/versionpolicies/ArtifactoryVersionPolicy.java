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
import java.util.Properties;

/**
 * A {@link VersionPolicy} implementation that retrieves the latest release from Artifactory and
 * bases the next releaseVersion on it.
 */
@Component(
        role = VersionPolicy.class,
        hint = "ONOArtifactoryVersionPolicy",
        description = "Retrieves the latest release from Artifactory and bases the next releaseVersion on it."
)
public class ArtifactoryVersionPolicy implements VersionPolicy {

    private static final String HTTP_ARTIFACTORY = "http://repo.jfrog.org/artifactory";
    private static final String REPOSITORIES = "repo1-cache";

    @Requirement
    MavenProject mavenProject;

    String httpArtifactory;

    String artifactoryRepositories;

    // For injection.
    public ArtifactoryVersionPolicy() {}

    // Just for tests
    ArtifactoryVersionPolicy(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        final String currentVersion;
        try {
            final URL url = createURL(mavenProject);
            try(final InputStream stream = getInputStream(url)) {
                currentVersion = IOUtil.toString(stream, "UTF-8");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to access " + httpArtifactory, e);
        }
        final DefaultVersionInfo versionInfo = new DefaultVersionInfo(currentVersion);
        versionPolicyResult.setVersion(versionInfo.getNextVersion().getReleaseVersionString());
        return versionPolicyResult;
    }

    private URL createURL(MavenProject mavenProject) throws MalformedURLException {
        return new URL(createUrlString(mavenProject));
    }

    String createUrlString(MavenProject mavenProject) {
        final Properties properties = this.mavenProject.getProperties();
        httpArtifactory = properties.getProperty("artifactory-http", HTTP_ARTIFACTORY);
        artifactoryRepositories = properties.getProperty("artifactory-repositories", REPOSITORIES);
        return String.format(
                        Locale.ENGLISH,
                        httpArtifactory + "/api/search/latestVersion?g=%s&a=%s&repos=%s",
                mavenProject.getGroupId(), mavenProject.getArtifactId(), artifactoryRepositories);
    }

    InputStream getInputStream(URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }
}
