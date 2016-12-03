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

import net.oneandone.maven.shared.ChangesReleases;
import net.oneandone.maven.shared.changes.model.Release;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;

import java.util.List;
import java.util.Properties;

/**
 * Sets {@literal developmentVersion, releaseVersion, newVersion, ONOCurrentVersion, tag, changes.version}
 * from values in {@literal src/changes/changes.xml}.
 *
 * Invoke like {@literal ono-maven-shared:changes-version versions:set deploy changes:announcement-generate}
 *
 * Deprecated: Use {@literal ono-maven-shared:version -DprojectVersionPolicyId=ONOChangesVersionPolicy} instead.
 */
@Mojo(name = "changes-version", requiresDirectInvocation = true, requiresProject = true)
@Deprecated
public class ChangesVersionMojo extends VersionMojo {

    private static final String CHANGES_XML = "src/changes/changes.xml";

    private final String changesXml;

    public ChangesVersionMojo() {
        changesXml = CHANGES_XML;
    }

    ChangesVersionMojo(MavenProject project, MavenSession session, String changesXml) {
        super(project, session, null, "ONOChangesVersionPolicy");
        this.changesXml = changesXml;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!project.isExecutionRoot()) {
            getLog().debug("changes-version: skipping because " + project + " is not execution root");
            return;
        }
        getLog().warn("Deprecated since 2.8, use VersionMojo and set the property projectVersionPolicyId to ONOChangesVersionPolicy");
        final ChangesReleases changesReleases = new ChangesReleases(changesXml);
        final String release;
        final String current;
        try {
            final List<Release> releases = changesReleases.getReleases();
            release = releases.get(0).getVersion();
            current = releases.size() > 1 ? releases.get(1).getVersion() : "UNKNOWN";
        } catch (PolicyException e) {
            throw new MojoExecutionException("Could not get releases", e);
        }
        final Properties userProperties = session.getUserProperties();
        userProperties.setProperty(RELEASE_VERSION, release);
        userProperties.setProperty(DEVELOPMENT_VERSION, project.getVersion());
        userProperties.setProperty(NEW_VERSION, release);
        userProperties.setProperty(CHANGES_VERSION, release);
        userProperties.setProperty(CURRENT_VERSION, current);
        userProperties.setProperty(TAG_PROPERTY, project.getArtifactId() + "-" + release);
        for (final String versionString : VERSION_STRINGS) {
            getLog().info("changes-version: setting " + versionString + "=" + userProperties.getProperty(versionString));
        }
    }
}
