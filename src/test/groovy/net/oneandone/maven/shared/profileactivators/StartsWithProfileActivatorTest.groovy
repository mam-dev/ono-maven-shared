package net.oneandone.maven.shared.profileactivators

import org.apache.maven.model.Activation
import org.apache.maven.model.Profile
import org.apache.maven.model.profile.ProfileActivationContext
import spock.lang.Specification
import spock.lang.Subject

class StartsWithProfileActivatorTest extends Specification {

    def 'Check a property starts with a value'() {
        given:
        def activation = Mock(Activation)
        def profile = Mock(Profile)
        profile.activation = activation
        def profileActivationContext = Mock(ProfileActivationContext)
        //profileActivationContext.systemProperties = ['bar': 'startswith:foo']
        @Subject
        def sut = new StartsWithProfileActivator()
        expect:
        !sut.isActive(profile, profileActivationContext, null)
    }
}
