package com.spassu.livros.bff.web;

import com.spassu.livros.bff.config.RequestCorrelationWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

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
}