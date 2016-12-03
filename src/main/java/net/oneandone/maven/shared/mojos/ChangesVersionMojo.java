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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.version.VersionPolicy;

import java.util.Map;

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

    public ChangesVersionMojo() {
        versionPolicyId = "ONOChangesVersionPolicy";
    }

    ChangesVersionMojo(MavenProject project, MavenSession session, Map<String, VersionPolicy> versionPolicies) {
        super(project, session, versionPolicies, "ONOChangesVersionPolicy");
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().warn("Deprecated since 2.8, use VersionMojo and set the property projectVersionPolicyId to ONOChangesVersionPolicy");
        super.execute();
    }
}
