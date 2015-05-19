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
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildNumberVersionPolicyTest extends AbstractVersionPolicyTest {

    @Test
    public void testGetReleaseVersionJenkins() throws Exception {
        final String buildNumberIdentifier = "BUILD_NUMBER";
        testGetReleaseVersion(buildNumberIdentifier, createMavenProject());
    }

    @Test
    public void testGetReleaseVersionTravis() throws Exception {
        final String buildNumberIdentifier = "TRAVIS_BUILD_NUMBER";
        final MavenProject mavenProject = createMavenProject();
        mavenProject.getProperties().setProperty("buildnumber-versions-policy-identifier", buildNumberIdentifier);
        testGetReleaseVersion(buildNumberIdentifier, mavenProject);
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("BUILD_NUMBER", "5");
        final BuildNumberVersionPolicy subjectUnderTest = new BuildNumberVersionPolicy(createMavenProject(), systemEnv);
        final VersionPolicyResult developmentVersion = subjectUnderTest.getDevelopmentVersion(null);
        assertThat(developmentVersion.getVersion()).isEqualTo("1-SNAPSHOT");
    }

    // Almost useless but good for line and instruction coverage.
    @Test
    public void testDefaultConstructors() {
        new BuildNumberVersionPolicy();
    }

    void testGetReleaseVersion(String buildNumberIdentifier, MavenProject mavenProject) throws PolicyException, VersionParseException {
        final Map<String, String> systemEnv = new HashMap<>();
        final BuildNumberVersionPolicy subjectUnderTest = new BuildNumberVersionPolicy(mavenProject, systemEnv);
        systemEnv.put(buildNumberIdentifier, "5");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.5");
        systemEnv.put(buildNumberIdentifier, "6");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.6");
    }

}