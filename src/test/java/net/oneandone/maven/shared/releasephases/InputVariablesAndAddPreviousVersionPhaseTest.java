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
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InputVariablesAndAddPreviousVersionPhaseTest {

    private final ReleasePhase mockedInputVariablesPhase = mock(ReleasePhase.class);
    private final HashMap<String, VersionPolicy> versionPolicies = new HashMap<>();

    @Test
    public void shouldNotSetAnyThingWhenVersionPolicyIsNotArtifactoryVersionPolicy() throws ReleaseFailureException, ReleaseExecutionException {
        final InputVariablesAndAddPreviousVersionPhase subjectUnderTest = new InputVariablesAndAddPreviousVersionPhase(versionPolicies, mockedInputVariablesPhase);
        final ReleaseDescriptor releaseDescriptor = createReleaseDescriptor("default");
        subjectUnderTest.execute(releaseDescriptor, (ReleaseEnvironment)null, null);
        assertThat(releaseDescriptor.getAdditionalArguments()).isEqualTo("nothing");
        subjectUnderTest.simulate(releaseDescriptor, (ReleaseEnvironment) null, null);
        assertThat(releaseDescriptor.getAdditionalArguments()).isEqualTo("nothing");}

    @Test
    public void shouldSetLatestWhenVersionPolicyIsNotArtifactoryVersionPolicy() throws ReleaseFailureException, ReleaseExecutionException {
        addMockedArtifactoryVersionPolicy();
        final InputVariablesAndAddPreviousVersionPhase subjectUnderTest = new InputVariablesAndAddPreviousVersionPhase(versionPolicies, mockedInputVariablesPhase);
        final ReleaseDescriptor releaseDescriptor = createReleaseDescriptor("ONOArtifactoryVersionPolicy");
        subjectUnderTest.execute(releaseDescriptor, (ReleaseEnvironment) null, null);
        assertThat(releaseDescriptor.getAdditionalArguments()).isEqualTo("nothing -DONOArtifactoryVersionPolicy.latest=1.5.6");
    }

    void addMockedArtifactoryVersionPolicy() {
        final ArtifactoryVersionPolicy mockedVersionPolicy = mock(ArtifactoryVersionPolicy.class);
        when(mockedVersionPolicy.getCurrentVersion()).thenReturn("1.5.6");
        versionPolicies.put("ONOArtifactoryVersionPolicy", mockedVersionPolicy);
    }

    ReleaseDescriptor createReleaseDescriptor(String projectVersionPolicyId) {
        final ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setAdditionalArguments("nothing");
        releaseDescriptor.setProjectVersionPolicyId(projectVersionPolicyId);
        return releaseDescriptor;
    }

}