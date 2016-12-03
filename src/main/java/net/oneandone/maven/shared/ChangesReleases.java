/*
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
package net.oneandone.maven.shared;

import net.oneandone.maven.shared.changes.model.Body;
import net.oneandone.maven.shared.changes.model.ChangesDocument;
import net.oneandone.maven.shared.changes.model.Release;
import net.oneandone.maven.shared.changes.model.io.xpp3.ChangesXpp3Reader;
import org.apache.maven.shared.release.policy.PolicyException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Get releases from changes.xml.
 */
public class ChangesReleases {

    private final String changesXml;

    public ChangesReleases(String changesXml) {
        this.changesXml = changesXml;
    }

    public List<Release> getReleases() throws PolicyException {
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
        return releases;
    }
}
