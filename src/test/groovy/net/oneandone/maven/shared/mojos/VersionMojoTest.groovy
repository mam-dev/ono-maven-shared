package net.oneandone.maven.shared.mojos

import net.oneandone.maven.shared.versionpolicies.AbstractVersionPolicyTrait
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.MojoExecutionException
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

    def 'Do nothing for none executionRoot'() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: false)
        @Subject
        def sut = new VersionMojo(project, session, null, null)
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties['releaseVersion'] == null
    }

    def 'Fail when invalid invalid policyVersionId is given'() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: true)
        def versionPolicies = new HashMap<String, VersionPolicy>()
        versionPolicies['default'] = new DefaultVersionPolicy()
        session.getUserProperties() >> properties
        @Subject
        def sut = new VersionMojo(project, session, versionPolicies, 'unknown')
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        def e = thrown(MojoExecutionException)
        e.message.contains('Unknown projectVersionPolicyId')
    }

    /**
     * Quiet logger
     * @return
     */
    Log createQuietLogger() {
        return Mock(Log)
    }

}
