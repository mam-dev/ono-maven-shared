package net.oneandone.maven.shared.mojos

import net.oneandone.maven.shared.versionpolicies.AbstractVersionPolicyTrait
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

class VersionPolicyVersionsMojoTest extends Specification implements AbstractVersionPolicyTrait {
    def "Execute with default policyVersionId"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        def sut = new VersionPolicyVersionsMojo(project, session, createVersionPolicies(project), 'default')
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties['releaseVersion'] == '3'
        properties['developmentVersion'] == '4-SNAPSHOT'
        properties['ONOCurrentVersion'] == 'NOT_SUPPORTED'
        properties['tag'] == 'foo-3'
    }

    def "Execute with ChangesVersionPolicy"() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: true)
        @Subject
        def sut = new VersionPolicyVersionsMojo(project, session, createVersionPolicies(project), 'ONOChangesVersionPolicy')
        sut.log = createQuietLogger()

        when:
        sut.execute()

        then:
        properties['releaseVersion'] == '3.0.2'
        properties['developmentVersion'] == '3-SNAPSHOT'
        properties['ONOCurrentVersion'] == '3.0.1'
        properties['tag'] == 'foo-3.0.2'
    }

    def 'Do nothing for none executionRoot'() {
        given:
        def session = Mock(MavenSession)
        def properties = new Properties()
        session.getUserProperties() >> properties
        def project = new MavenProject(artifactId: "foo", version: "3-SNAPSHOT", executionRoot: false)
        @Subject
        def sut = new VersionPolicyVersionsMojo(project, session, null, null)
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
        session.getUserProperties() >> properties
        @Subject
        def sut = new VersionPolicyVersionsMojo(project, session, createVersionPolicies(project), 'unknown')
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

    Map<String, VersionPolicy> createVersionPolicies(MavenProject project) {
        return [
            'default': new DefaultVersionPolicy(),
            'ONOBuildNumberVersionPolicy': new BuildNumberVersionPolicy(project, ['BUILD_NUMBER': '1']),
            'ONOChangesVersionPolicy': new ChangesVersionPolicy(project, "target/test-classes/changes/twoversions.xml")
        ]
    }

}
