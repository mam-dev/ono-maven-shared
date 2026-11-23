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
package net.oneandone.maven.shared.mojos;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangesVersionMojoTest {

    @Test
    @DisplayName("Execute with given changes and two releases")
    void executeWithGivenChangesAndTwoReleases() throws Exception {
        // given
        MavenSession session = mock(MavenSession.class);
        Properties properties = new Properties();
        when(session.getUserProperties()).thenReturn(properties);
        MavenProject project = new MavenProject();
        project.setArtifactId("foo");
        project.setVersion("3-SNAPSHOT");
        project.setExecutionRoot(true);
        ChangesVersionMojo subjectUnderTest =
                new ChangesVersionMojo("target/test-classes/changes/twoversions.xml", project, session);
        subjectUnderTest.setLog(createQuietLogger());

        // when
        subjectUnderTest.execute();

        // then
        assertAll(
                () -> assertEquals("3-SNAPSHOT",
                        properties.getProperty(ChangesVersionMojo.DEVELOPMENT_VERSION)),
                () -> assertEquals("3.0.2", properties.getProperty(ChangesVersionMojo.RELEASE_VERSION)),
                () -> assertEquals("3.0.2", properties.getProperty(ChangesVersionMojo.NEW_VERSION)),
                () -> assertEquals("3.0.2", properties.getProperty(ChangesVersionMojo.CHANGES_VERSION)),
                () -> assertEquals("3.0.1", properties.getProperty(ChangesVersionMojo.CURRENT_VERSION)),
                () -> assertEquals("foo-3.0.2", properties.getProperty(ChangesVersionMojo.TAG_PROPERTY))
        );
    }

    @Test
    @DisplayName("Execute with given changes and one release")
    void executeWithGivenChangesAndOneRelease() throws Exception {
        // given
        MavenSession session = mock(MavenSession.class);
        Properties properties = new Properties();
        when(session.getUserProperties()).thenReturn(properties);
        MavenProject project = new MavenProject();
        project.setVersion("3-SNAPSHOT");
        project.setExecutionRoot(true);
        ChangesVersionMojo subjectUnderTest =
                new ChangesVersionMojo("target/test-classes/changes/oneversion.xml", project, session);
        subjectUnderTest.setLog(createQuietLogger());

        // when
        subjectUnderTest.execute();

        // then
        assertAll(
                () -> assertEquals("3-SNAPSHOT",
                        properties.getProperty(ChangesVersionMojo.DEVELOPMENT_VERSION)),
                () -> assertEquals("3.0.2", properties.getProperty(ChangesVersionMojo.RELEASE_VERSION)),
                () -> assertEquals("3.0.2", properties.getProperty(ChangesVersionMojo.NEW_VERSION)),
                () -> assertEquals("UNKNOWN", properties.getProperty(ChangesVersionMojo.CURRENT_VERSION))
        );
    }

    @Test
    @DisplayName("Choke with nochanges")
    void chokesWithNoChanges() {
        // given
        MavenSession session = mock(MavenSession.class);
        Properties properties = new Properties();
        when(session.getUserProperties()).thenReturn(properties);
        MavenProject project = new MavenProject();
        project.setVersion("3-SNAPSHOT");
        project.setExecutionRoot(true);
        ChangesVersionMojo subjectUnderTest =
                new ChangesVersionMojo("target/test-classes/changes/empty_body.xml", project, session);
        subjectUnderTest.setLog(createQuietLogger());

        // when / then
        assertThrows(MojoExecutionException.class, subjectUnderTest::execute);
    }

    @Test
    @DisplayName("Skip when not execution root")
    void skipsWhenNotExecutionRoot() throws Exception {
        // given
        MavenSession session = mock(MavenSession.class);
        Properties properties = new Properties();
        when(session.getUserProperties()).thenReturn(properties);
        MavenProject project = new MavenProject();
        project.setVersion("3-SNAPSHOT");
        project.setExecutionRoot(false);
        ChangesVersionMojo subjectUnderTest =
                new ChangesVersionMojo("target/test-classes/changes/empty_body.xml", project, session);
        subjectUnderTest.setLog(createQuietLogger());

        // when
        subjectUnderTest.execute();

        // then
        assertNull(properties.getProperty(ChangesVersionMojo.DEVELOPMENT_VERSION));
    }

    @Test
    @DisplayName("Has a default constructor used with injection in Maven")
    void hasDefaultConstructorUsedWithInjectionInMaven() {
        // expect
        assertNotNull(new ChangesVersionMojo());
    }

    private Log createQuietLogger() {
        return mock(Log.class);
    }
}
