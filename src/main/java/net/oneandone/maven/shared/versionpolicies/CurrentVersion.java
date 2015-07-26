package net.oneandone.maven.shared.versionpolicies;

/**
 * Implementors return the latest known version.
 */
public interface CurrentVersion {
    /**
     *
     * @return the latest known version.
     */
    String getCurrentVersion();
}
