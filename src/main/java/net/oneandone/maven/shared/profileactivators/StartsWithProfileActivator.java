package net.oneandone.maven.shared.profileactivators;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.codehaus.plexus.component.annotations.Component;

import java.util.Map;

/**
 * Instead of complete matching, only check if the property startswith a special value.
 */
@Component(role = ProfileActivator.class, hint = "property")
public class StartsWithProfileActivator implements ProfileActivator {

    private static final String STARTSWITH = "startswith:";

    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        final Activation activation = profile.getActivation();
        if (activation != null) {
            final ActivationProperty property = activation.getProperty();
            if (property != null) {
                final String name = property.getName();
                final String value = property.getValue();
                if (value.startsWith(STARTSWITH)) {
                    final String needle = value.split(STARTSWITH, 1)[1];
                    final Map<String, String> systemProperties = context.getSystemProperties();
                    final String propertyToCheck = systemProperties.get(name);
                    return propertyToCheck != null && propertyToCheck.startsWith(needle);
                }
            }

        }
        return false;
    }

    @Override
    public boolean presentInConfig(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        return false;
    }
}
