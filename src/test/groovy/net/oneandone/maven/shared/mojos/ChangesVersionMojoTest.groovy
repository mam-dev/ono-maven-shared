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
package net.oneandone.maven.shared.mojos

import net.oneandone.maven.shared.versionpolicies.BuildNumberVersionPolicy
import net.oneandone.maven.shared.versionpolicies.ChangesVersionPolicy
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.shared.release.policies.DefaultVersionPolicy
import org.apache.maven.shared.release.policy.version.VersionPolicy
import spock.lang.Specification
import spock.lang.Subject

class ChangesVersionMojoTest extends Specification {

    def "Execute with given changes and two releases"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        sut = new ChangesVersionMojo(project, session,
                createVersionPolicies(project, "target/test-classes/changes/twoversions.xml"))
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties.getProperty(VersionMojo.DEVELOPMENT_VERSION) == "3-SNAPSHOT"
        properties.getProperty(VersionMojo.RELEASE_VERSION) == "3.0.2"
        properties.getProperty(VersionMojo.NEW_VERSION) == "3.0.2"
        properties.getProperty(VersionMojo.CHANGES_VERSION) == "3.0.2"
        properties.getProperty(VersionMojo.CURRENT_VERSION) == "3.0.1"
        properties.getProperty(VersionMojo.TAG_PROPERTY) == "foo-3.0.2"
    }

    def "Execute with given changes and one release"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        sut = new ChangesVersionMojo(project, session,
                createVersionPolicies(project, "target/test-classes/changes/oneversion.xml"))
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties.getProperty(VersionMojo.DEVELOPMENT_VERSION) == "3-SNAPSHOT"
        properties.getProperty(VersionMojo.RELEASE_VERSION) == "3.0.2"
        properties.getProperty(VersionMojo.NEW_VERSION) == "3.0.2"
        properties.getProperty(VersionMojo.CURRENT_VERSION) == "UNKNOWN"
    }

    def "Choke with nochanges"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        sut = new ChangesVersionMojo(project, session,
                createVersionPolicies(project, "target/test-classes/changes/empty_body.xml"))
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        thrown(MojoExecutionException)
    }

    def "Skip when not execution root"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: false)
        @Subject
        sut = new ChangesVersionMojo(project, session,
                createVersionPolicies(project, 'target/test-classes/changes/empty_body.xml'))
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties.getProperty(VersionMojo.DEVELOPMENT_VERSION) == null
    }

    def 'Has a default constructor used with injection in Maven'() {
        given:
        new ChangesVersionMojo()

        expect:
        true
    }

    /**
     * Quiet logger
     * @return
     */
    Log createQuietLogger() {
        return Mock(Log)
    }

    Map<String, VersionPolicy> createVersionPolicies(MavenProject project, String changesFile) {
        return [
            'ONOChangesVersionPolicy': new ChangesVersionPolicy(project, changesFile)
        ]
    }

}
