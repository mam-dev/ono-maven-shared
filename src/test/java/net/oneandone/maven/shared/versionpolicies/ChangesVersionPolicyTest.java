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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class ChangesVersionPolicyTest extends AbstractVersionPolicyTestSupport {

    // where
    @ParameterizedTest(name = "Get releaseVersion={1} and currentVersion={2} from {0}")
    @CsvSource({
            "'twoversions.xml', '3.0.2', '3.0.1'",
            "'oneversion.xml', '3.0.2', 'UNKNOWN'"
    })
    void getsReleaseVersionFromChangesXml(String changesFile, String releaseVersion, String currentVersion)
            throws Exception {
        // given
        MavenProject project = createMavenProject();
        ChangesVersionPolicy policy =
                new ChangesVersionPolicy(project, "target/test-classes/changes/" + changesFile);

        // expect
        assertAll(
                () -> assertEquals(releaseVersion,
                        policy.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion()),
                () -> assertEquals(currentVersion, policy.getCurrentVersion())
        );
    }

    @Test
    @DisplayName("Choke when changes.xml could not be found")
    void chokesWhenChangesXmlCouldNotBeFound() {
        // given
        ChangesVersionPolicy policy =
                new ChangesVersionPolicy(createMavenProject(), "target/test-classes/changes/does_not_exist.xml");

        // when / then
        PolicyException exception = assertThrows(PolicyException.class,
                () -> policy.getReleaseVersion(VPR_DOES_NOT_MATTER));
        assertEquals(FileNotFoundException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Choke when changes.xml is not valid")
    void chokesWhenChangesXmlIsNotValid() {
        // given
        ChangesVersionPolicy policy =
                new ChangesVersionPolicy(createMavenProject(), "target/test-classes/changes/wrong_changes.xml");

        // when / then
        PolicyException exception = assertThrows(PolicyException.class,
                () -> policy.getReleaseVersion(VPR_DOES_NOT_MATTER));
        assertEquals(XmlPullParserException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Choke when changes.xml has no body")
    void chokesWhenChangesXmlHasNoBody() {
        // given
        ChangesVersionPolicy policy =
                new ChangesVersionPolicy(createMavenProject(), "target/test-classes/changes/empty_body.xml");

        // when / then
        PolicyException exception = assertThrows(PolicyException.class,
                () -> policy.getReleaseVersion(VPR_DOES_NOT_MATTER));
        assertTrue(exception.getMessage().contains("No body found"));
    }

    @Test
    @DisplayName("Choke when changes.xml has no releases")
    void chokesWhenChangesXmlHasNoReleases() {
        // given
        ChangesVersionPolicy policy =
                new ChangesVersionPolicy(createMavenProject(), "target/test-classes/changes/empty_releases.xml");

        // when / then
        PolicyException exception = assertThrows(PolicyException.class,
                () -> policy.getReleaseVersion(VPR_DOES_NOT_MATTER));
        assertTrue(exception.getMessage().contains("No releases found"));
    }

    @Test
    @DisplayName("Assures development version is SNAPSHOT")
    void assuresDevelopmentVersionIsSnapshot() throws Exception {
        // given
        MavenProject project = createMavenProject();
        ChangesVersionPolicy policy = new ChangesVersionPolicy(project, null);

        // expect
        assertEquals(project.getVersion(),
                policy.getDevelopmentVersion(VPR_DOES_NOT_MATTER).getVersion());
    }

    @Test
    @DisplayName("Has a default constructor used with injection in Maven")
    void hasDefaultConstructorUsedWithInjectionInMaven() {
        // expect
        assertNotNull(new ChangesVersionPolicy());
    }
}
