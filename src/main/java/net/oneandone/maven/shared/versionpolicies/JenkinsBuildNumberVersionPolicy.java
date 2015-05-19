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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Map;

/**
 * A {@link VersionPolicy} implementation that retrieves the BUILD_NUMBER from Jenkins and bases the next
 * releaseVersion on it.
 */
@Component(
        role = VersionPolicy.class,
        hint = "ONOJenkinsBuildNumberVersionPolicy",
        description = "Retrieves the BUILD_NUMBER from Jenkins and bases the next releaseVersion on it."
)
public class JenkinsBuildNumberVersionPolicy implements VersionPolicy {

    @Requirement
    MavenProject mavenProject;

    private Map<String, String> systemEnv;

    /**
     * For injection
     */
    public JenkinsBuildNumberVersionPolicy() {
        systemEnv = System.getenv();
    }

    /**
     * For tests
     */
    JenkinsBuildNumberVersionPolicy(MavenProject mavenProject, Map<String, String> systemEnv) {
        this.mavenProject = mavenProject;
        this.systemEnv = systemEnv;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final DefaultVersionInfo currentSnapshot = new DefaultVersionInfo(mavenProject.getVersion());
        final VersionPolicyResult result = new VersionPolicyResult();
        result.setVersion(currentSnapshot.getReleaseVersionString() + "." + systemEnv.get("BUILD_NUMBER"));
        return result;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }
}
