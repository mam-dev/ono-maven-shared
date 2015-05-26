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
package net.oneandone.maven.shared.versionpolicies

import org.apache.maven.project.MavenProject
import spock.lang.Specification

@Mixin(AbstractVersionPolicyTrait)
class ArtifactoryVersionPolicy2Test extends Specification {

    def mavenProject = createMavenProject();

    def 'should increment minor when major or minor stays below or equals last release'() {

        given:
        def versionPolicy = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6');

        when:
        mavenProject.version = mavenVersion

        then:
        versionPolicy.getReleaseVersion(null).version == releaseVersion
        versionPolicy.getDevelopmentVersion(null).version == mavenVersion

        where:
        mavenVersion     | releaseVersion
        '1-SNAPSHOT'     |  '1.5.7'
        '1.0-SNAPSHOT'   |  '1.5.7'
        '1.5.6-SNAPSHOT' |  '1.5.7'
        '1.5.7-SNAPSHOT' |  '1.5.7'
    }

    def 'should restart with zero for new major or minor SNAPSHOT'() {

        given:
        def versionPolicy = new ArtifactoryVersionPolicyStub(mavenProject, '1.5.6');

        when:
        mavenProject.version = mavenVersion

        then:
        versionPolicy.getReleaseVersion(null).version == releaseVersion
        versionPolicy.getDevelopmentVersion(null).version == mavenVersion

        where:
        mavenVersion     | releaseVersion
        '1.6-SNAPSHOT'   |  '1.6.0'
        '2-SNAPSHOT'     |  '2.0'
        '2.0-SNAPSHOT'   |  '2.0.0'
    }
    static class ArtifactoryVersionPolicyStub extends ArtifactoryVersionPolicy {

        private final String releaseVersionFromArtifactory;

        public ArtifactoryVersionPolicyStub(MavenProject mavenProject, String releaseVersionFromArtifactory) {
            super(mavenProject);
            this.releaseVersionFromArtifactory = releaseVersionFromArtifactory;
        }

        @Override
        InputStream getInputStream(URL url) throws IOException {
            return new ByteArrayInputStream(releaseVersionFromArtifactory.getBytes("UTF-8"));
        }
    }

}
