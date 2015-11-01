package net.oneandone.maven.shared.profileactivators;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;

import java.util.Map;

public abstract class AbstractMatchesWithProfileActivator implements ProfileActivator {

    private final String prefix;

    public AbstractMatchesWithProfileActivator(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        final Activation activation = profile.getActivation();
        if (activation != null) {
            final ActivationProperty property = activation.getProperty();
            if (property != null) {
                final String name = property.getName();
                final String value = property.getValue();
                if (value.startsWith(prefix)) {
                    final String needle = value.substring(prefix.length());
                    final Map<String, String> systemProperties = context.getSystemProperties();
                    final String propertyToCheck = systemProperties.get(name);
                    return propertyToCheck != null && matches(needle, propertyToCheck);
                }
            }
        }
        return false;
    }

    protected abstract boolean matches(String needle, String propertyToCheck);

    @Override
    public boolean presentInConfig(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        return false;
    }
}
