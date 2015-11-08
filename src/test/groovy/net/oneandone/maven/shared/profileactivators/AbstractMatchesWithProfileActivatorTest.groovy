package net.oneandone.maven.shared.profileactivators

import org.apache.maven.model.Activation
import org.apache.maven.model.ActivationProperty
import org.apache.maven.model.Profile
import org.apache.maven.model.profile.ProfileActivationContext
import spock.lang.Specification
import spock.lang.Subject

class AbstractMatchesWithProfileActivatorTest extends Specification {

    def 'Check a property starts with a value but system property does not exist'() {
        given:
        def activation = new Activation()
        activation.property = new ActivationProperty()
        activation.property.name = 'bar'
        activation.property.value = 'ono:startswith:foo'
        def profile = Mock(Profile)
        profile.activation >> activation
        def profileActivationContext = Mock(ProfileActivationContext)
        profileActivationContext.userProperties >> [:]
        profileActivationContext.systemProperties >> ['foo': 'foofoo']
        @Subject
        def sut = create()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }

    def 'Check no activation given'() {
        given:
        def profile = Mock(Profile)
        def profileActivationContext = Mock(ProfileActivationContext)
        @Subject
        def sut = create()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }

    def 'Check a property does not start with a value'() {
        given:
        def activation = new Activation()
        activation.property = new ActivationProperty()
        activation.property.name = 'bar'
        activation.property.value = 'ono:startswith:foo'
        def profile = Mock(Profile)
        profile.activation >> activation
        def profileActivationContext = Mock(ProfileActivationContext)
        profileActivationContext.userProperties >> [:]
        profileActivationContext.systemProperties >> ['bar': 'bar']
        @Subject
        def sut = create()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }

    def 'Check a property that does not start with a value'() {
        given:
        def activation = new Activation()
        activation.property = new ActivationProperty()
        activation.property.name = 'bar'
        activation.property.value = 'foo'
        def profile = Mock(Profile)
        profile.activation >> activation
        def profileActivationContext = Mock(ProfileActivationContext)
        profileActivationContext.userProperties >> [:]
        profileActivationContext.systemProperties >> [:]
        @Subject
        def sut = create()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }

    def 'Check no property'() {
        given:
        def activation = new Activation()
        activation.property = null
        def profile = Mock(Profile)
        profile.activation >> activation
        def profileActivationContext = Mock(ProfileActivationContext)
        @Subject
        def sut = create()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }

    def 'Check not presentInConfig'() {
        given:
        def profile = Mock(Profile)
        def profileActivationContext = Mock(ProfileActivationContext)
        @Subject
        def sut = create()
        expect:
        !sut.presentInConfig(profile, profileActivationContext, null)
    }

    AbstractMatchesWithProfileActivator create() {
        return new AbstractMatchesWithProfileActivator("startswith:") {
            @Override
            protected boolean matches(String needle, String propertyToCheck) {
                return false
            }
        }
    }
}
