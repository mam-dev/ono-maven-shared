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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;

/**
 *
 */
@Mojo(name = "changes-version", requiresDirectInvocation = true, requiresProject = true)
public class ChangesVersionMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ChangesReleases changesReleases = new ChangesReleases("src/changes/changes.xml");
        try {
            changesReleases.getReleases();
        } catch (PolicyException e) {
            throw new MojoExecutionException("Could not get releases", e);
        }
        System.getProperty("developmentVersion", project.getVersion());
        getLog().info("Setting developmentVersion=" + project.getVersion());
    }
}
