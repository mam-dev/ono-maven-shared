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
package net.oneandone.maven.shared.releasephases

import net.oneandone.maven.shared.versionpolicies.ArtifactoryVersionPolicy
import org.apache.maven.shared.release.config.ReleaseDescriptor
import org.apache.maven.shared.release.env.ReleaseEnvironment
import org.apache.maven.shared.release.phase.ReleasePhase
import spock.lang.Specification
import spock.lang.Subject

class InputVariablesAndAddPreviousVersionPhaseTest extends Specification {

    final def mockedInputVariablesPhase = Mock(ReleasePhase.class);
    final def versionPolicies = new HashMap<>();

    @Subject
    final
    def subjectUnderTest = new InputVariablesAndAddPreviousVersionPhase(versionPolicies, mockedInputVariablesPhase);

    def 'Returns "nothing" on execute when projectVersionPolicyId is default'() {
        given:
        def releaseDescriptor = new ReleaseDescriptor(projectVersionPolicyId: 'default', additionalArguments: 'nothing');

        when:
        subjectUnderTest.execute(releaseDescriptor, (ReleaseEnvironment) null, null);

        then:
        releaseDescriptor.additionalArguments == 'nothing'
    }

    def 'Returns "nothing" on simulate when projectVersionPolicyId is default'() {
        given:
        def releaseDescriptor = new ReleaseDescriptor(projectVersionPolicyId: 'default', additionalArguments: 'nothing');

        when:
        subjectUnderTest.simulate(releaseDescriptor, (ReleaseEnvironment) null, null);

        then:
        releaseDescriptor.additionalArguments == 'nothing'
    }

    def 'Returns "nothing -DONOArtifactoryVersionPolicy.latest=1.5.6" on execute when projectVersionPolicyId is ONOArtifactoryVersionPolicy'() {
        given:
        def releaseDescriptor = new ReleaseDescriptor(projectVersionPolicyId: 'ONOArtifactoryVersionPolicy', additionalArguments: 'nothing')
        def mockedVersionPolicy = Mock(ArtifactoryVersionPolicy.class);
        mockedVersionPolicy.currentVersion >> '1.5.6'
        versionPolicies['ONOArtifactoryVersionPolicy'] = mockedVersionPolicy

        when:
        subjectUnderTest.execute(releaseDescriptor, (ReleaseEnvironment) null, null);

        then:
        releaseDescriptor.additionalArguments == 'nothing -DONOArtifactoryVersionPolicy.latest=1.5.6'
    }

    def 'Has a default constructor used with injection in Maven'() {
        given:
        @Subject
        def subjectUnderTest = new InputVariablesAndAddPreviousVersionPhase()

        expect:
        subjectUnderTest != null
    }
}