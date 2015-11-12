package net.oneandone.maven.shared.profileactivators;

import org.apache.maven.model.profile.activation.ProfileActivator;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Instead of complete matching, only check if the property startswith a special value.
 */
@Component(role = ProfileActivator.class, hint = "property")
class StartsWithProfileActivator extends AbstractMatchesWithProfileActivator {

    private static final String STARTSWITH = "startswith";

    public StartsWithProfileActivator() {
        super(STARTSWITH);
    }

    @Override
    protected boolean matches(String needle, String propertyToCheck) {
        return propertyToCheck.startsWith(needle);
    }

}
