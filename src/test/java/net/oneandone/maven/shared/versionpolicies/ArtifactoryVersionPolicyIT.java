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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactoryVersionPolicyIT extends AbstractVersionPolicyTestSupport {

    private final ArtifactoryVersionPolicy subjectUnderTest =
            new ArtifactoryVersionPolicy(createMavenProject(), null);

    @Test
    @DisplayName("New foss-parent version should always be bigger then 1.5.6")
    void newFossParentVersionShouldAlwaysBeBiggerThan156() throws Exception {
        // given
        checkRepoJfrogIsReachable();
        DefaultVersionInfo oldVersionInfo = new DefaultVersionInfo("1.5.6");

        // when
        String version = subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion();
        DefaultVersionInfo newVersionInfo = new DefaultVersionInfo(version);

        // then
        assertAll(
                () -> assertFalse(newVersionInfo.isSnapshot()),
                () -> assertTrue(newVersionInfo.compareTo(oldVersionInfo) > 0)
        );
    }

    // where
    @ParameterizedTest(name = "Handles new artifacts {0} -> {1}")
    @CsvSource({
            "'0.1-SNAPSHOT', '0.1.0'",
            "'1.0-SNAPSHOT', '1.0.0'"
    })
    void noVersionFoundAtArtifactoryAsFirstRelease(String snapshotVersion, String releaseVersion) throws Exception {
        // given
        checkRepoJfrogIsReachable();
        subjectUnderTest.mavenProject.setArtifactId("unknown-artifact-id-i-do-not-exist");
        subjectUnderTest.mavenProject.setVersion(snapshotVersion);

        // when
        String version = subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion();
        String newSnapshotVersion = subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).getVersion();
        DefaultVersionInfo newVersionInfo = new DefaultVersionInfo(version);

        // then
        assertAll(
                () -> assertEquals(subjectUnderTest.mavenProject.getVersion(), newSnapshotVersion),
                () -> assertFalse(newVersionInfo.isSnapshot()),
                () -> assertEquals(releaseVersion, newVersionInfo.getReleaseVersionString())
        );
    }

    static void checkRepoJfrogIsReachable() {
        try {
            URLConnection urlConnection = new URL("https://repo.jfrog.org/artifactory").openConnection();
            try {
                urlConnection.setConnectTimeout(5000);
                urlConnection.getInputStream().close();
            } catch (IOException ex) {
                throw new TestAbortedException("Could not reach " + urlConnection.getURL(), ex);
            }
        } catch (MalformedURLException ex) {
            throw new TestAbortedException("Malformed URL", ex);
        } catch (IOException ex) {
            throw new TestAbortedException("Could not reach https://repo.jfrog.org/artifactory", ex);
        }
    }
}
