package com.spassu.livros.bff.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

/**
 * Error code table (BFF -> Frontend):
 *
 * BFF_0001: Unauthorized (401, auth missing/invalid)
 * BFF_0002: Forbidden (403, permissions)
 * BFF_0800: Orchestration bad request (400)
 * BFF_0804: Orchestration not found (404)
 * BFF_1002: Orchestration unavailable (502/503)
 * BFF_1003: Orchestration internal error (500)
 * BFF_9000: BFF internal error (unhandled)
 *
 * O BFF não deve repassar payloads de erro direto da orquestracao (ex.: exceptions de negocio em texto).
 * Sempre retorna objeto consistente para o frontend.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleUnauthorized(AuthenticationException ex, ServerWebExchange exchange) {
        log.error("Authentication failed. path={}, requestId={}, reason={}",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getHeaders().getFirst("X-Request-ID"),
                ex.getMessage(),
                ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Unauthorized request");
        pd.setType(URI.create("https://spassu.com/problems/unauthorized"));
        pd.setProperty("code", BffErrorCode.UNAUTHORIZED.getCode());
        pd.setProperty("detail", ex.getMessage());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleForbidden(AccessDeniedException ex, ServerWebExchange exchange) {
        log.error("Access denied. path={}, requestId={}, reason={}",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getHeaders().getFirst("X-Request-ID"),
                ex.getMessage(),
                ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Forbidden access");
        pd.setType(URI.create("https://spassu.com/problems/forbidden"));
        pd.setProperty("code", BffErrorCode.FORBIDDEN.getCode());
        pd.setProperty("detail", ex.getMessage());
        return pd;
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ProblemDetail handleUpstreamError(WebClientResponseException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.BAD_GATEWAY;

        log.error("Upstream orchestration error. path={}, requestId={}, status={}, body={}",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getHeaders().getFirst("X-Request-ID"),
                ex.getStatusCode().value(),
                ex.getResponseBodyAsString(),
                ex);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, "Orchestration failure");
        pd.setType(URI.create("https://spassu.com/problems/bff-upstream-error"));
        pd.setProperty("source", "orchestration");
        pd.setProperty("code", mapUpstreamStatusToBffCode(status));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled BFF error. path={}, requestId={}",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getHeaders().getFirst("X-Request-ID"),
                ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        pd.setType(URI.create("https://spassu.com/problems/internal-error"));
        pd.setProperty("code", BffErrorCode.INTERNAL_ERROR.getCode());
        pd.setProperty("detail", ex.getMessage());
        return pd;
    }

    private String mapUpstreamStatusToBffCode(HttpStatus status) {
        if (status.is4xxClientError()) {
            if (status == HttpStatus.NOT_FOUND) {
                return BffErrorCode.ORCHESTRATION_NOT_FOUND.getCode();
            }
            return BffErrorCode.ORCHESTRATION_BAD_REQUEST.getCode();
        }
        if (status.is5xxServerError()) {
            if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
                return BffErrorCode.ORCHESTRATION_UNAVAILABLE.getCode();
            }
            return BffErrorCode.ORCHESTRATION_INTERNAL.getCode();
        }
        return BffErrorCode.INTERNAL_ERROR.getCode();
    }
}
