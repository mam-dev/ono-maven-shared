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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArtifactoryVersionPolicyFetchBodyTest extends AbstractVersionPolicyTestSupport {

    private static final URI URI_UNDER_TEST = URI.create("https://artifactory.example.com/api/search/latestVersion");

    private final HttpClient httpClient = mock(HttpClient.class);
    private final ArtifactoryVersionPolicy policy =
            new ArtifactoryVersionPolicy(createMavenProject(), null, httpClient);

    @BeforeEach
    void noAuthentication() {
        policy.serverId = "";
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> respond(int status, String body) throws Exception {
        final var response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        return response;
    }

    @Test
    @DisplayName("Returns the body and sends a GET request with timeout to the given URI")
    void returnsBody() throws Exception {
        // given
        respond(200, "1.2.3");

        // when
        final var body = policy.fetchBody(URI_UNDER_TEST);

        // then
        final var request = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(request.capture(), any(HttpResponse.BodyHandler.class));
        assertAll(
                () -> assertEquals("1.2.3", body),
                () -> assertEquals(URI_UNDER_TEST, request.getValue().uri()),
                () -> assertEquals("GET", request.getValue().method()),
                () -> assertTrue(request.getValue().timeout().isPresent())
        );
    }

    @Test
    @DisplayName("Returns empty string on 404")
    void returnsEmptyStringOn404() throws Exception {
        // given
        respond(404, "");

        // expect
        assertEquals("", policy.fetchBody(URI_UNDER_TEST));
    }

    @ParameterizedTest(name = "Throws IOException on status {0}")
    @ValueSource(ints = {199, 300, 401, 500})
    void throwsIOExceptionOnUnexpectedStatus(int status) throws Exception {
        // given
        respond(status, "");

        // when
        final var exception = assertThrows(IOException.class, () -> policy.fetchBody(URI_UNDER_TEST));

        // then
        assertTrue(exception.getMessage().contains(String.valueOf(status)));
    }

    @Test
    @DisplayName("Propagates IOException from the client")
    void propagatesIOException() throws Exception {
        // given
        final var failure = new IOException("connect timed out");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(failure);

        // expect
        assertSame(failure, assertThrows(IOException.class, () -> policy.fetchBody(URI_UNDER_TEST)));
    }

    @Test
    @DisplayName("Wraps InterruptedException and restores the interrupt flag")
    void wrapsInterruptedException() throws Exception {
        // given
        final var interrupted = new InterruptedException("stop");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(interrupted);

        try {
            // when
            final var exception = assertThrows(IOException.class, () -> policy.fetchBody(URI_UNDER_TEST));

            // then
            assertAll(
                    () -> assertSame(interrupted, exception.getCause()),
                    () -> assertTrue(Thread.currentThread().isInterrupted())
            );
        } finally {
            Thread.interrupted();
        }
    }
}
