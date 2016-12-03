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
package net.oneandone.maven.shared.mojos;

import net.oneandone.maven.shared.versionpolicies.CurrentVersion;
import org.apache.commons.lang3.Validate;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;

import java.util.Map;
import java.util.Properties;

/**
 * Sets {@literal developmentVersion, releaseVersion, newVersion, ONOCurrentVersion, tag, changes.version}
 * for a {@link org.apache.maven.shared.release.policy.version.VersionPolicy}.
 *
 * Invoke like {@literal ono-maven-shared:version versions:set deploy changes:announcement-generate}
 *
 * @since 2.8
 */
@Mojo(name = "version", requiresDirectInvocation = true, requiresProject = true)
public class VersionMojo extends AbstractMojo {

    static final String DEVELOPMENT_VERSION = "developmentVersion";
    static final String RELEASE_VERSION = "releaseVersion";
    static final String NEW_VERSION = "newVersion";
    static final String CURRENT_VERSION = "ONOCurrentVersion";
    static final String TAG_PROPERTY = "tag";
    static final String CHANGES_VERSION = "changes.version";

    private static final String[] VERSION_STRINGS = {
            DEVELOPMENT_VERSION,
            RELEASE_VERSION,
            NEW_VERSION,
            CURRENT_VERSION,
            TAG_PROPERTY,
            CHANGES_VERSION
    };

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${projectVersionPolicyId}", readonly = true)
    private String versionPolicyId;

    @Component
    Map<String, VersionPolicy> versionPolicies;

    public VersionMojo() {
    }

    /** For testing */
    VersionMojo(MavenProject project, MavenSession session, Map<String, VersionPolicy> versionPolicies, String versionPolicyId) {
        this.project = project;
        this.session = session;
        this.versionPolicies = versionPolicies;
        this.versionPolicyId = versionPolicyId;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!project.isExecutionRoot()) {
            getLog().debug("version: skipping because " + project + " is not execution root");
            return;
        }
        final VersionPolicy versionPolicy = versionPolicies.get(versionPolicyId);
        Validate.notNull(versionPolicy, "Unknown projectVersionPolicyId %s, known: %s", versionPolicyId, versionPolicies.keySet());
        final VersionPolicyRequest versionPolicyRequest = new VersionPolicyRequest();
        versionPolicyRequest.setVersion(project.getVersion());
        final String releaseVersion;
        final String developmentVersion;
        try {
            releaseVersion = versionPolicy.getReleaseVersion(versionPolicyRequest).getVersion();
            developmentVersion = versionPolicy.getDevelopmentVersion(versionPolicyRequest).getVersion();
        } catch (PolicyException | VersionParseException e) {
            throw new MojoExecutionException("Could not retrieve the version", e);
        }
        final Properties userProperties = session.getUserProperties();
        userProperties.setProperty(RELEASE_VERSION, releaseVersion);
        userProperties.setProperty(DEVELOPMENT_VERSION, developmentVersion);
        userProperties.setProperty(NEW_VERSION, releaseVersion);
        userProperties.setProperty(CHANGES_VERSION, releaseVersion);
        userProperties.setProperty(TAG_PROPERTY, project.getArtifactId() + "-" + releaseVersion);
        final String currentVersion;
        if (versionPolicy instanceof CurrentVersion) {
            currentVersion = ((CurrentVersion) versionPolicy).getCurrentVersion();
        } else {
            currentVersion = "NOT_SUPPORTED";
        }
        userProperties.setProperty(CURRENT_VERSION, currentVersion);
        for (final String versionString : VERSION_STRINGS) {
            getLog().info("changes-version: setting " + versionString + "=" + userProperties.getProperty(versionString));
        }
    }
}
