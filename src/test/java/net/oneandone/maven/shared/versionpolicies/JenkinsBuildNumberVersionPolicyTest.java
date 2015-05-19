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

import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JenkinsBuildNumberVersionPolicyTest extends AbstractVersionPolicyTest {

    @Test
    public void testGetReleaseVersion() throws Exception {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("BUILD_NUMBER", "5");
        final JenkinsBuildNumberVersionPolicy subjectUnderTest = new JenkinsBuildNumberVersionPolicy(createMavenProject(), systemEnv);
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.5");
        systemEnv.put("BUILD_NUMBER", "6");
        assertThat(subjectUnderTest.getReleaseVersion(null).getVersion()).isEqualTo("1.6");
    }

    @Test
    public void testGetDevelopmentVersion() throws Exception {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("BUILD_NUMBER", "5");
        final JenkinsBuildNumberVersionPolicy subjectUnderTest = new JenkinsBuildNumberVersionPolicy(createMavenProject(), systemEnv);
        final VersionPolicyResult developmentVersion = subjectUnderTest.getDevelopmentVersion(null);
        assertThat(developmentVersion.getVersion()).isEqualTo("1-SNAPSHOT");
    }
}