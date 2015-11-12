package net.oneandone.maven.shared.mojos;

import net.oneandone.maven.shared.versionpolicies.ChangesVersionPolicy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 */
@Mojo(name = "changes-version", requiresDirectInvocation = true, requiresProject = true)
public class ChangesVersionMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ChangesVersionPolicy policy = new ChangesVersionPolicy();
        System.getProperty("developmentVersion", project.getVersion());
        getLog().info("Setting developmentVersion=" + project.getVersion());
    }
}
