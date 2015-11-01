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
 * Instead of complete matching, only check if the property ends with a special value.
 */
@Component(role = ProfileActivator.class, hint = "property")
public class EndsWithProfileActivator implements ProfileActivator {

    private static final String ENDSWITH = "endswith:";

    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        final Activation activation = profile.getActivation();
        if (activation != null) {
            final ActivationProperty property = activation.getProperty();
            if (property != null) {
                final String name = property.getName();
                final String value = property.getValue();
                if (value.startsWith(ENDSWITH)) {
                    final String needle = value.substring(ENDSWITH.length());
                    final Map<String, String> systemProperties = context.getSystemProperties();
                    final String propertyToCheck = systemProperties.get(name);
                    return propertyToCheck != null && propertyToCheck.endsWith(needle);
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
