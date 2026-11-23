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
package net.oneandone.maven.shared.releasephases;

import net.oneandone.maven.shared.versionpolicies.ArtifactoryVersionPolicy;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class InputVariablesAndAddPreviousVersionPhaseTest {

    private final ReleasePhase mockedInputVariablesPhase = mock(ReleasePhase.class);
    private final Map<String, VersionPolicy> versionPolicies = new HashMap<>();
    private final InputVariablesAndAddPreviousVersionPhase subjectUnderTest =
            new InputVariablesAndAddPreviousVersionPhase(versionPolicies, mockedInputVariablesPhase);

    @Test
    @DisplayName("Returns \"nothing\" on execute when projectVersionPolicyId is default")
    void returnsNothingOnExecuteWhenProjectVersionPolicyIdIsDefault() throws Exception {
        // given
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setProjectVersionPolicyId("default");
        releaseDescriptor.setAdditionalArguments("nothing");

        // when
        subjectUnderTest.execute(releaseDescriptor, (ReleaseEnvironment) null, null);

        // then
        assertEquals("nothing", releaseDescriptor.getAdditionalArguments());
    }

    @Test
    @DisplayName("Returns \"nothing\" on simulate when projectVersionPolicyId is default")
    void returnsNothingOnSimulateWhenProjectVersionPolicyIdIsDefault() throws Exception {
        // given
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setProjectVersionPolicyId("default");
        releaseDescriptor.setAdditionalArguments("nothing");

        // when
        subjectUnderTest.simulate(releaseDescriptor, (ReleaseEnvironment) null, null);

        // then
        assertEquals("nothing", releaseDescriptor.getAdditionalArguments());
    }

    @Test
    @DisplayName("Returns \"nothing -DONOArtifactoryVersionPolicy.latest=1.5.6\" on execute when projectVersionPolicyId is ONOArtifactoryVersionPolicy")
    void returnsNothingWithLatestPropertyOnExecuteForArtifactoryPolicy() throws Exception {
        // given
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setProjectVersionPolicyId("ONOArtifactoryVersionPolicy");
        releaseDescriptor.setAdditionalArguments("nothing");
        ArtifactoryVersionPolicy mockedVersionPolicy = mock(ArtifactoryVersionPolicy.class);
        when(mockedVersionPolicy.getCurrentVersion()).thenReturn("1.5.6");
        versionPolicies.put("ONOArtifactoryVersionPolicy", mockedVersionPolicy);

        // when
        subjectUnderTest.execute(releaseDescriptor, (ReleaseEnvironment) null, null);

        // then
        assertEquals(
                "nothing -DONOArtifactoryVersionPolicy.latest=1.5.6 -DONOCurrentVersion=1.5.6",
                releaseDescriptor.getAdditionalArguments());
    }

    @Test
    @DisplayName("Has a default constructor used with injection in Maven")
    void hasDefaultConstructorUsedWithInjectionInMaven() {
        // expect
        assertNotNull(new InputVariablesAndAddPreviousVersionPhase());
    }
}
