/*
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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.apache.maven.shared.utils.io.IOUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.Base64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
public class ArtifactoryVersionPolicy implements VersionPolicy, CurrentVersion {

    private static final String HTTP_ARTIFACTORY = "http://repo.jfrog.org/artifactory";
    private static final String REPOSITORIES = "repo1";

    static final String ARTIFACTORY_VERSION_POLICY_API = "artifactory-version-policy-http";

    static final String ARTIFACTORY_VERSION_POLICY_REPOSITORIES = "artifactory-version-policy-repositories";

    static final String ARTIFACTORY_VERSION_POLICY_SERVER_ID = "artifactory-version-policy-server-id";

    @Requirement
    MavenProject mavenProject;

    @Requirement
    MavenSession mavenSession;

    String httpArtifactory;

    String artifactoryRepositories;

    String serverId;

    private String currentVersion;

    // For injection.
    public ArtifactoryVersionPolicy() {}

    // Just for tests
    ArtifactoryVersionPolicy(MavenProject mavenProject, MavenSession mavenSession) {
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        final String urlString = createUrlString();
        try {
            final URL url = new URL(urlString);
            final InputStream stream = getInputStream(url);
            try {
                currentVersion = IOUtil.toString(stream, "UTF-8");
            } finally {
                stream.close();
            }
        } catch (FileNotFoundException e) {
            currentVersion = "0"; //mavenProject.getVersion().replace("-SNAPSHOT", "");
        } catch (IOException e) {
            throw new PolicyException("Unable to access " + urlString, e);
        }
        final VersionInfo versionInfo = new DefaultVersionInfo(currentVersion);
        final VersionInfo nextVersion = versionInfo.getNextVersion();
        final VersionInfo currentSnapshot = new DefaultVersionInfo(mavenProject.getVersion());
        if (nextVersion.compareTo(currentSnapshot) < 0) {
            versionPolicyResult.setVersion(currentSnapshot.getReleaseVersionString() + ".0");
        } else {
            versionPolicyResult.setVersion(nextVersion.getReleaseVersionString());
        }
        return versionPolicyResult;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }

    String createUrlString() {
        final Properties properties = mavenProject.getProperties();
        httpArtifactory = properties.getProperty(ARTIFACTORY_VERSION_POLICY_API, HTTP_ARTIFACTORY);
        artifactoryRepositories = properties.getProperty(ARTIFACTORY_VERSION_POLICY_REPOSITORIES, REPOSITORIES);
        serverId = properties.getProperty(ARTIFACTORY_VERSION_POLICY_SERVER_ID, "");
        return String.format(
                        Locale.ENGLISH,
                        httpArtifactory + "/api/search/latestVersion?g=%s&a=%s&repos=%s",
                mavenProject.getGroupId(), mavenProject.getArtifactId(), artifactoryRepositories);
    }

    InputStream getInputStream(URL url) throws IOException {
        final HttpURLConnection connection = getHttpURLConnection(url);
        return connection.getInputStream();
    }

    HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (!"".equals(serverId)) {
            final Server server = mavenSession.getSettings().getServer(serverId);
            final String username = server.getUsername();
            final String password = server.getPassword();
            final String s = username + ":" + password;
            final String auth = new String(Base64.encodeBase64(s.getBytes("UTF-8")), "UTF-8");
            connection.setRequestProperty("Authorization", "Basic " + auth);
        }
        return connection;
    }

    @Override
    public String getCurrentVersion() {
        return currentVersion;
    }
}
