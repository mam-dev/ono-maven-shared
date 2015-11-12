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

import net.oneandone.maven.shared.ChangesReleases;
import net.oneandone.maven.shared.changes.model.Release;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.List;

/**
 * A {@link VersionPolicy} implementation that retrieves the latest version from the changes file.
 *
 * @since 2.5
 */
@Component(
        role = VersionPolicy.class,
        hint = "ONOChangesVersionPolicy",
        description = "A VersionPolicy implementation that retrieves the latest version from the changes file."
)
public class ChangesVersionPolicy implements VersionPolicy, CurrentVersion {

    @Requirement
    MavenProject mavenProject;

    private static final String CHANGES_XML = "src/changes/changes.xml";

    private final String changesXml;

    private String currentVersion;

    // For injection.
    public ChangesVersionPolicy() {
        changesXml = CHANGES_XML;
    }

    // For tests
    ChangesVersionPolicy(MavenProject mavenProject, String changesXml) {
        this.mavenProject = mavenProject;
        this.changesXml = changesXml;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final List<Release> releases = new ChangesReleases(changesXml).getReleases();
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(releases.get(0).getVersion());
        if (releases.size() > 1) {
            currentVersion = releases.get(1).getVersion();
        } else {
            currentVersion = "UNKNOWN";
        }
        return versionPolicyResult;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }

    @Override
    public String getCurrentVersion() {
        return currentVersion;
    }
}
