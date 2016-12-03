package net.oneandone.maven.shared.mojos

import net.oneandone.maven.shared.versionpolicies.AbstractVersionPolicyTrait
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.shared.release.policies.DefaultVersionPolicy
import org.apache.maven.shared.release.policy.version.VersionPolicy
import spock.lang.Specification
import spock.lang.Subject

class VersionMojoTest extends Specification implements AbstractVersionPolicyTrait {
    def "Execute with given changes and two releases"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        def versionPolicies = new HashMap<String, VersionPolicy>()
        versionPolicies['default'] = new DefaultVersionPolicy()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        def sut = new VersionMojo(project, session, versionPolicies, 'default')
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties['releaseVersion'] == '3'
        properties['developmentVersion'] == '4-SNAPSHOT'
        properties['ONOCurrentVersion'] == 'NOT_SUPPORTED'
        properties['tag'] == 'foo-3'
    }

    /**
     * Quiet logger
     * @return
     */
    Log createQuietLogger() {
        return Mock(Log)
    }

}
