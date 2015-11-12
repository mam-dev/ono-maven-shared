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

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import spock.lang.Specification
import spock.lang.Subject

class ChangesVersionMojoTest extends Specification {

    def "Execute with given changes and two releases"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        def sut = new ChangesVersionMojo("target/test-classes/changes/twoversions.xml", project, session)
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties.getProperty(ChangesVersionMojo.DEVELOPMENT_VERSION) == "3-SNAPSHOT"
        properties.getProperty(ChangesVersionMojo.RELEASE_VERSION) == "3.0.2"
        properties.getProperty(ChangesVersionMojo.NEW_VERSION) == "3.0.2"
        properties.getProperty(ChangesVersionMojo.CURRENT_VERSION) == "3.0.1"
    }

    def "Execute with given changes and one release"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        def sut = new ChangesVersionMojo("target/test-classes/changes/oneversion.xml", project, session)
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties.getProperty(ChangesVersionMojo.DEVELOPMENT_VERSION) == "3-SNAPSHOT"
        properties.getProperty(ChangesVersionMojo.RELEASE_VERSION) == "3.0.2"
        properties.getProperty(ChangesVersionMojo.NEW_VERSION) == "3.0.2"
        properties.getProperty(ChangesVersionMojo.CURRENT_VERSION) == "UNKNOWN"
    }

    def "Choke with nochanges"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        def sut = new ChangesVersionMojo("target/test-classes/changes/empty_body.xml", project, session)
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        MojoExecutionException e = thrown()
    }

    def "Skip when not execution root"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(version: "3-SNAPSHOT", executionRoot: false)
        @Subject
        def sut = new ChangesVersionMojo("target/test-classes/changes/empty_body.xml", project, session)
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties.getProperty(ChangesVersionMojo.DEVELOPMENT_VERSION) == null
    }

    def 'Has a default constructor used with injection in Maven'() {
        given:
        @Subject
        def sut = new ChangesVersionMojo()

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
}
