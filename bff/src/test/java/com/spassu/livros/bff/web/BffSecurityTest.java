package com.spassu.livros.bff.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Security integration tests: verifies that the BFF correctly rejects
 * unauthenticated and incorrectly authorized requests before they reach
 * the orchestration layer.
 *
 * <p>These tests start the full Spring context but override Vault and Keycloak
 * via test properties.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.import=optional:vault://",
                "spring.cloud.vault.enabled=false",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/master",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/master/protocol/openid-connect/certs",
                "spassu.orchestration.url=http://localhost:9999",
                "spassu.allowed-origins=http://localhost:3000"
        }
)
class BffSecurityTest {

        @LocalServerPort
        private int port;

        private WebTestClient webTestClient() {
                return WebTestClient.bindToServer()
                                .baseUrl("http://localhost:" + port)
                                .build();
        }

    @Test
    void semToken_deveRetornar401() {
                webTestClient().get()
                .uri("/api/livros")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actuatorHealth_semToken_deveRetornar200() {
                webTestClient().get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
