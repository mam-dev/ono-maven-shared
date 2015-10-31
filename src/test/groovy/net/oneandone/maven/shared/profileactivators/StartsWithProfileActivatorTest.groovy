package net.oneandone.maven.shared.profileactivators

import org.apache.maven.model.Activation
import org.apache.maven.model.ActivationProperty
import org.apache.maven.model.Profile
import org.apache.maven.model.profile.ProfileActivationContext
import spock.lang.Specification
import spock.lang.Subject

class StartsWithProfileActivatorTest extends Specification {

    def 'Check no activation given'() {
        given:
        def profile = Mock(Profile)
        def profileActivationContext = Mock(ProfileActivationContext)
        @Subject
        def sut = new StartsWithProfileActivator()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }

    def 'Check a property starts with a value'() {
        given:
        def activation = new Activation()
        activation.property = new ActivationProperty()
        activation.property.name = 'bar'
        activation.property.value = 'startswith:foo'
        def profile = Mock(Profile)
        profile.activation >> activation
        def profileActivationContext = Mock(ProfileActivationContext)
        profileActivationContext.systemProperties >> ['bar': 'foofoo']
        @Subject
        def sut = new StartsWithProfileActivator()
        expect:
        sut.isActive(profile, profileActivationContext, null)
    }
}
