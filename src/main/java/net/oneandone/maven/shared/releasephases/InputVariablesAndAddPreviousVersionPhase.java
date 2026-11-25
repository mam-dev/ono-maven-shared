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

import net.oneandone.maven.shared.versionpolicies.CurrentVersion;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.eclipse.sisu.Description;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * A replacement for the standard {@link org.apache.maven.shared.release.phase.InputVariablesPhase} which
 * extends exec.additionalArguments with the latest released version from Artifactory when
 * the {@code projectVersionPolicyId} is {@code ONOArtifactoryVersionPolicy}
 */
@Named("input-variables")
@Singleton
@Description("Add latestVersion and input any variables that were not yet configured.")
public class InputVariablesAndAddPreviousVersionPhase extends AbstractReleasePhase {

    @Inject
    Map<String, VersionPolicy> versionPolicies;

    @Inject
    @Named("maven-release-manager-input-variables")
    ReleasePhase inputVariablesPhase;

    public InputVariablesAndAddPreviousVersionPhase() {
        // for injection
    }

    InputVariablesAndAddPreviousVersionPhase(Map<String, VersionPolicy> versionPolicies, ReleasePhase inputVariablesPhase) {
        this.versionPolicies = versionPolicies;
        this.inputVariablesPhase = inputVariablesPhase;
    }

    @Override
    public ReleaseResult execute(ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment, List<MavenProject> reactorProjects) throws ReleaseExecutionException, ReleaseFailureException {
        addLatestProperty(releaseDescriptor);
        return inputVariablesPhase.execute(releaseDescriptor, releaseEnvironment, reactorProjects);
    }

    @Override
    public ReleaseResult simulate(ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment, List<MavenProject> reactorProjects) throws ReleaseExecutionException, ReleaseFailureException {
        addLatestProperty(releaseDescriptor);
        return inputVariablesPhase.simulate(releaseDescriptor, releaseEnvironment, reactorProjects);
    }

    private void addLatestProperty(ReleaseDescriptor releaseDescriptor) {
        final String policyId = releaseDescriptor.getProjectVersionPolicyId();
        final VersionPolicy versionPolicy = versionPolicies.get(policyId);
        if (versionPolicy instanceof CurrentVersion) {
            final CurrentVersion currentVersionPolicy = (CurrentVersion) versionPolicy;
            final String currentVersion = currentVersionPolicy.getCurrentVersion();
            final String property = " -DONOArtifactoryVersionPolicy.latest=" + currentVersion;
            releaseDescriptor.setAdditionalArguments(
                    releaseDescriptor.getAdditionalArguments() + property + " -DONOCurrentVersion=" + currentVersion);
        }
    }
}
