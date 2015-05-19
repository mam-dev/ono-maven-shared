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

import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactoryVersionPolicyIT extends AbstractVersionPolicyTest {

    @Test
    public void testGetReleaseVersion() throws Exception {
        final ArtifactoryVersionPolicy subjectUnderTest = new ArtifactoryVersionPolicy(createMavenProject());
        final String version = subjectUnderTest.getReleaseVersion(null).getVersion();
        final DefaultVersionInfo newVersionInfo = new DefaultVersionInfo(version);
        final DefaultVersionInfo oldVersionInfo = new DefaultVersionInfo("1.5.6");
        assertThat(newVersionInfo.isSnapshot()).as("Should not be a SNAPSHOT").isFalse();
        assertThat(newVersionInfo).as("Should be newer than 1.5.6").isGreaterThan(oldVersionInfo);
    }

}
