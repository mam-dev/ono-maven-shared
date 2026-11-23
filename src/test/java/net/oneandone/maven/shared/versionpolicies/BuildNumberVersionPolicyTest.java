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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BuildNumberVersionPolicyTest extends AbstractVersionPolicyTestSupport {

    private final Map<String, String> systemEnv = new HashMap<>();
    private final BuildNumberVersionPolicy subjectUnderTest =
            new BuildNumberVersionPolicy(createMavenProject(), systemEnv);

    // where
    @ParameterizedTest(name = "Set minor to BUILD_NUMBER={0}")
    @ValueSource(strings = {"1", "2"})
    void setsMinorToBuildNumber(String buildNumber) throws Exception {
        // given
        systemEnv.put("BUILD_NUMBER", buildNumber);

        // expect
        assertEquals("1." + buildNumber,
                subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion());
    }

    // where
    @ParameterizedTest(name = "Set minor to TRAVIS_BUILD_NUMBER={0}")
    @ValueSource(strings = {"1", "2"})
    void setsMinorToTravisBuildNumber(String buildNumber) throws Exception {
        // given
        String buildNumberIdentifier = "TRAVIS_BUILD_NUMBER";
        subjectUnderTest.mavenProject.getProperties()
                .setProperty("buildnumber-version-policy-identifier", buildNumberIdentifier);
        systemEnv.put(buildNumberIdentifier, buildNumber);

        // expect
        assertEquals("1." + buildNumber,
                subjectUnderTest.getReleaseVersion(VPR_DOES_NOT_MATTER).getVersion());
    }

    @Test
    @DisplayName("Assures development version is SNAPSHOT")
    void assuresDevelopmentVersionIsSnapshot() throws Exception {
        // given
        systemEnv.put("BUILD_NUMBER", "5");

        // expect
        assertEquals("1-SNAPSHOT",
                subjectUnderTest.getDevelopmentVersion(VPR_DOES_NOT_MATTER).getVersion());
    }

    @Test
    @DisplayName("Has a default constructor used with injection in Maven")
    void hasDefaultConstructorUsedWithInjectionInMaven() {
        // given
        BuildNumberVersionPolicy policy = new BuildNumberVersionPolicy();

        // expect
        assertNotNull(policy);
    }
}
