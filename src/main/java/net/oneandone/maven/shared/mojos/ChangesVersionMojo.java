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
 *
 */
@Mojo(name = "changes-version", requiresDirectInvocation = true, requiresProject = true)
public class ChangesVersionMojo extends AbstractMojo {

    private static final String CHANGES_XML = "src/changes/changes.xml";
    static final String DEVELOPMENT_VERSION = "developmentVersion";
    static final String RELEASE_VERSION = "releaseVersion";
    static final String NEW_VERSION = "newVersion";
    static final String CURRENT_VERSION = "ONOCurrentVersion";

    private static final String[] VERSION_STRINGS = {
            DEVELOPMENT_VERSION,
            RELEASE_VERSION,
            NEW_VERSION,
            CURRENT_VERSION
    };

    private final String changesXml;
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

    public ChangesVersionMojo() {
        changesXml = CHANGES_XML;
    }

    ChangesVersionMojo(String changesXml, MavenProject project, MavenSession session) {
        this.changesXml = changesXml;
        this.project = project;
        this.session= session;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!project.isExecutionRoot()) {
            getLog().debug("changes-version: skipping because " + project + " is not execution root");
            return;
        }
        final ChangesReleases changesReleases = new ChangesReleases(changesXml);
        final String release;
        final String current;
        try {
            final List<Release> releases = changesReleases.getReleases();
            release = releases.get(0).getVersion();
            current = releases.size()>1 ? releases.get(1).getVersion() : "UNKNOWN";
        } catch (PolicyException e) {
            throw new MojoExecutionException("Could not get releases", e);
        }
        final Properties userProperties = session.getUserProperties();
        userProperties.setProperty(RELEASE_VERSION, release);
        userProperties.setProperty(DEVELOPMENT_VERSION, project.getVersion());
        userProperties.setProperty(NEW_VERSION, release);
        userProperties.setProperty(CURRENT_VERSION, current);
        for (final String versionString : VERSION_STRINGS) {
            getLog().info("changes-version: setting " + versionString + "=" + userProperties.getProperty(versionString));
        }
    }
}
