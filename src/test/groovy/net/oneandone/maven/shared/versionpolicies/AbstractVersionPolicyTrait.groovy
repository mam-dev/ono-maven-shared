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

import org.apache.maven.project.MavenProject
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;

trait AbstractVersionPolicyTrait {

    static VersionPolicyRequest VPR_DOES_NOT_MATTER = new VersionPolicyRequest()

    MavenProject createMavenProject() {
        final MavenProject project = new MavenProject();
        project.setGroupId("net.oneandone.maven.poms");
        project.setArtifactId("foss-parent");
        project.setVersion("1-SNAPSHOT");
        return project;
    }

}
