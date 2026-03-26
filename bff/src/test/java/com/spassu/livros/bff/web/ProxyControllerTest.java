package com.spassu.livros.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spassu.livros.bff.config.RequestCorrelationWebFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProxyControllerTest {

    @Test
    void buildForwardHeaders_deveRemoverHeadersDeBrowserEPropagarCorrelacao() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.set(HttpHeaders.ORIGIN, "http://localhost:3000");
        incomingHeaders.set(HttpHeaders.HOST, "localhost:8083");
        incomingHeaders.set(HttpHeaders.ACCEPT, "application/json");
        incomingHeaders.set(RequestCorrelationWebFilter.REQUEST_ID_HEADER, "request-123");

        HttpHeaders result = ProxyController.buildForwardHeaders(incomingHeaders, "jwt-token", "request-123");

        assertFalse(result.getFirst(HttpHeaders.ORIGIN) != null);
        assertFalse(result.getFirst(HttpHeaders.HOST) != null);
        assertEquals("application/json", result.getFirst(HttpHeaders.ACCEPT));
        assertEquals("Bearer jwt-token", result.getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("request-123", result.getFirst(RequestCorrelationWebFilter.REQUEST_ID_HEADER));
    }

    @Test
    @DisplayName("proxy deve propagar resposta 200 do upstream")
    void proxy_devePropagarResposta200DoUpstream() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        WebClient webClient = WebClient.builder()
                .baseUrl("http://orchestration")
                .exchangeFunction(request -> {
                    capturedRequest.set(request);
                    return reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                            .body("pong")
                            .build());
                })
                .build();
        ProxyController controller = new ProxyController(webClient, new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/livros?page=0")
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .build());

        controller.proxy(exchange, jwtAuth()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getResponse().getBodyAsString().block()).isEqualTo("pong");
        assertThat(exchange.getResponse().getHeaders().getFirst(RequestCorrelationWebFilter.REQUEST_ID_HEADER)).isNotBlank();
        assertThat(capturedRequest.get().headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer jwt-token");
    }

    @Test
    @DisplayName("proxy deve normalizar erro do upstream")
    void proxy_deveNormalizarErroDoUpstream() throws Exception {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://orchestration")
                .exchangeFunction(request -> reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .body("bad request")
                        .build()))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        ProxyController controller = new ProxyController(webClient, objectMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/livros").build());

        controller.proxy(exchange, jwtAuth()).block();
        String body = exchange.getResponse().getBodyAsString().block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(objectMapper.readTree(body).get("code").asText()).isEqualTo(BffErrorCode.ORCHESTRATION_BAD_REQUEST.getCode());
    }

    @Test
    @DisplayName("proxy deve retornar internal error em falha inesperada")
    void proxy_deveRetornarInternalErrorEmFalhaInesperada() throws Exception {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://orchestration")
                .exchangeFunction(request -> reactor.core.publisher.Mono.error(new RuntimeException("boom")))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        ProxyController controller = new ProxyController(webClient, objectMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/livros").build());

        controller.proxy(exchange, jwtAuth()).block();
        String body = exchange.getResponse().getBodyAsString().block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(objectMapper.readTree(body).get("code").asText()).isEqualTo(BffErrorCode.INTERNAL_ERROR.getCode());
    }

    @Test
    @DisplayName("proxy deve retornar fallback quando serializacao falhar")
    void proxy_deveRetornarFallbackQuandoSerializacaoFalhar() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://orchestration")
                .exchangeFunction(request -> reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .body("bad request")
                        .build()))
                .build();
        ProxyController controller = new ProxyController(webClient, new FailingObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/livros").build());

        controller.proxy(exchange, jwtAuth()).block();
        String body = exchange.getResponse().getBodyAsString().block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isEqualTo("{\"error\":\"Serialization failure\"}");
    }

    private JwtAuthenticationToken jwtAuth() {
        Jwt jwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "none")
                .claim("sub", "admin")
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private static final class FailingObjectMapper extends ObjectMapper {
        @Override
        public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
            throw new JsonProcessingException("forced failure") {};
        }
    }
}
