package com.spassu.livros.bff.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BffErrorMappingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private ServerWebExchange exchange() {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/livros")
                        .header("X-Request-ID", "test-request-id"));
    }

    @Test
    void shouldMapServiceUnavailableToOrchestrationUnavailableCode() {
        WebClientResponseException ex = WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                HttpHeaders.EMPTY,
                "Service down".getBytes(),
                null);

        ProblemDetail pd = handler.handleUpstreamError(ex, exchange());

        assertEquals(BffErrorCode.ORCHESTRATION_UNAVAILABLE.getCode(), pd.getProperties().get("code"));
        assertEquals("https://spassu.com/problems/bff-upstream-error", pd.getType().toString());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), pd.getStatus());
        assertFalse(pd.getProperties().containsKey("orchestrationDetail"));
    }

    @Test
    void shouldMapBadRequestToOrchestrationBadRequestCode() {
        WebClientResponseException ex = WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpHeaders.EMPTY,
                "Bad request".getBytes(),
                null);

        ProblemDetail pd = handler.handleUpstreamError(ex, exchange());

        assertEquals(BffErrorCode.ORCHESTRATION_BAD_REQUEST.getCode(), pd.getProperties().get("code"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
    }

    @Test
    void shouldMapNotFoundToOrchestrationNotFoundCode() {
        WebClientResponseException ex = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                HttpHeaders.EMPTY,
                "Not found".getBytes(),
                null);

        ProblemDetail pd = handler.handleUpstreamError(ex, exchange());

        assertEquals(BffErrorCode.ORCHESTRATION_NOT_FOUND.getCode(), pd.getProperties().get("code"));
        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
    }

    @Test
    void shouldMapInternalServerErrorToOrchestrationInternalCode() {
        WebClientResponseException ex = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpHeaders.EMPTY,
                "Internal error".getBytes(),
                null);

        ProblemDetail pd = handler.handleUpstreamError(ex, exchange());

        assertEquals(BffErrorCode.ORCHESTRATION_INTERNAL.getCode(), pd.getProperties().get("code"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
    }

    @Test
    void shouldMapGatewayTimeoutToOrchestrationUnavailableCode() {
        WebClientResponseException ex = WebClientResponseException.create(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase(),
                HttpHeaders.EMPTY,
                "Timeout".getBytes(),
                null);

        ProblemDetail pd = handler.handleUpstreamError(ex, exchange());

        assertEquals(BffErrorCode.ORCHESTRATION_UNAVAILABLE.getCode(), pd.getProperties().get("code"));
        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), pd.getStatus());
    }

    @Test
    void shouldMapGenericExceptionToInternalCode() {
        ProblemDetail pd = handler.handleGeneric(new RuntimeException("boom"), exchange());

        assertEquals(BffErrorCode.INTERNAL_ERROR.getCode(), pd.getProperties().get("code"));
        assertEquals("https://spassu.com/problems/internal-error", pd.getType().toString());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
    }
}
