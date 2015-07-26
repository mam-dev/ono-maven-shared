/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.maven.shared.versionpolicies;

import net.oneandone.maven.shared.changes.model.Body;
import net.oneandone.maven.shared.changes.model.ChangesDocument;
import net.oneandone.maven.shared.changes.model.Release;
import net.oneandone.maven.shared.changes.model.io.xpp3.ChangesXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * A {@link VersionPolicy} implementation that retrieves the latest version from the changes file.
 *
 * @since 2.5
 */
@Component(
        role = VersionPolicy.class,
        hint = "ONOChangesVersionPolicy",
        description = "A VersionPolicy implementation that retrieves the latest version from the changes file."
)
public class ChangesVersionPolicy implements VersionPolicy {

    @Requirement
    MavenProject mavenProject;

    private static final String CHANGES_XML = "src/changes/changes.xml";
    private final String changesXml;

    // For injection.
    public ChangesVersionPolicy() {
        changesXml = CHANGES_XML;
    }

    // For tests
    ChangesVersionPolicy(MavenProject mavenProject, String changesXml) {
        this.mavenProject = mavenProject;
        this.changesXml = changesXml;
    }

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final ChangesXpp3Reader reader = new ChangesXpp3Reader();
        final ChangesDocument document;
        try {
            // do *not* use a Reader here as ChangesXpp3Reader uses clever XML declaration guessing when getting a stream.
            final FileInputStream in = new FileInputStream(changesXml);
            try {
                document = reader.read(in, true);
            } finally {
                in.close();
            }
        } catch (IOException | XmlPullParserException e) {
            throw new PolicyException("Could not read changes from " + changesXml, e);
        }
        final Body body = document.getBody();
        if (body == null) {
            throw new PolicyException("No body found in " + changesXml);
        }
        final List<Release> releases = body.getReleases();
        if (releases.isEmpty()) {
            throw new PolicyException("No releases found in " + changesXml);
        }
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(releases.get(0).getVersion());
        return versionPolicyResult;
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        final VersionPolicyResult versionPolicyResult = new VersionPolicyResult();
        versionPolicyResult.setVersion(mavenProject.getVersion());
        return versionPolicyResult;
    }
}
