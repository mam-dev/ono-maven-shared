package net.oneandone.maven.shared.profileactivators;

import org.apache.maven.model.profile.activation.ProfileActivator;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Instead of complete matching, only check if the property ends with a special value.
 */
@Component(role = ProfileActivator.class, hint = "ono-contains")
public class ContainsProfileActivator extends AbstractMatchesWithProfileActivator {

    private static final String CONTAINS = "contains:";

    public ContainsProfileActivator() {
        super(CONTAINS);
    }

    @Override
    protected boolean matches(String needle, String propertyToCheck) {
        return propertyToCheck.contains(needle);
    }
}
