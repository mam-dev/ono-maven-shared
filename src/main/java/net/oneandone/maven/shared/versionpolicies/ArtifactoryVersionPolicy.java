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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.eclipse.sisu.Description;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

/**
 * A {@link VersionPolicy} implementation that retrieves the latest release from Artifactory and
 * bases the next releaseVersion on it.
 */
@Named("ONOArtifactoryVersionPolicy")
@Singleton
@Description("Retrieves the latest release from Artifactory and bases the next releaseVersion on it.")
public class ArtifactoryVersionPolicy implements VersionPolicy, CurrentVersion {

    private static final String HTTP_ARTIFACTORY = "https://repo.jfrog.org/artifactory";
    private static final String REPOSITORIES = "repo1";

    static final String ARTIFACTORY_VERSION_POLICY_API = "artifactory-version-policy-http";

    static final String ARTIFACTORY_VERSION_POLICY_REPOSITORIES = "artifactory-version-policy-repositories";

    static final String ARTIFACTORY_VERSION_POLICY_SERVER_ID = "artifactory-version-policy-server-id";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    final HttpClient httpClient;

    @Inject
    MavenProject mavenProject;

    @Inject
    MavenSession mavenSession;

    String httpArtifactory;

    String artifactoryRepositories;

    String serverId;

    private String currentVersion = "";

    // For injection.
    public ArtifactoryVersionPolicy() {
        this.httpClient = DEFAULT_HTTP_CLIENT;
    }

    // Just for tests
    ArtifactoryVersionPolicy(MavenProject mavenProject, MavenSession mavenSession) {
        this(mavenProject, mavenSession, DEFAULT_HTTP_CLIENT);
    }

    // Just for tests
    ArtifactoryVersionPolicy(MavenProject mavenProject, MavenSession mavenSession, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final var versionPolicyResult = new VersionPolicyResult();
        final var urlString = createUrlString();
        try {
            currentVersion = fetchBody(URI.create(urlString));
        } catch (IOException e) {
            throw new PolicyException("Unable to access " + urlString, e);
        }
        final var nextVersion = currentVersion.isEmpty()
                ? new DefaultVersionInfo("0")
                : new DefaultVersionInfo(currentVersion).getNextVersion();
        final var currentSnapshot = new DefaultVersionInfo(mavenProject.getVersion());
        if (nextVersion.compareTo(currentSnapshot) < 0) {
            versionPolicyResult.setVersion(currentSnapshot.getReleaseVersionString() + ".0");
        } else {
            versionPolicyResult.setVersion(nextVersion.getReleaseVersionString());
        }
        return versionPolicyResult;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) {
        final var versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }

    String createUrlString() {
        final var properties = mavenProject.getProperties();
        httpArtifactory = properties.getProperty(ARTIFACTORY_VERSION_POLICY_API, HTTP_ARTIFACTORY);
        artifactoryRepositories = properties.getProperty(ARTIFACTORY_VERSION_POLICY_REPOSITORIES, REPOSITORIES);
        serverId = properties.getProperty(ARTIFACTORY_VERSION_POLICY_SERVER_ID, "");
        return String.format(
                        Locale.ENGLISH,
                        httpArtifactory + "/api/search/latestVersion?g=%s&a=%s&repos=%s",
                mavenProject.getGroupId(), mavenProject.getArtifactId(), artifactoryRepositories);
    }

    String fetchBody(URI uri) throws IOException {
        final HttpResponse<String> response;
        try {
            response = httpClient.send(createHttpRequest(uri),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while accessing " + uri, e);
        }
        if (response.statusCode() == 404) {
            return "";
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP request failed with status code " + response.statusCode());
        }
        return response.body();
    }

    HttpRequest createHttpRequest(URI uri) {
        final var request = HttpRequest.newBuilder(uri).timeout(READ_TIMEOUT).GET();
        if (!"".equals(serverId)) {
            final var server = mavenSession.getSettings().getServer(serverId);
            final var username = server.getUsername();
            final var password = server.getPassword();
            final var s = username + ":" + password;
            final var auth = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
            request.header("Authorization", "Basic " + auth);
        }
        return request.build();
    }

    @Override
    public String getCurrentVersion() {
        return currentVersion.isEmpty() ? UNKNOWN : currentVersion;
    }
}
