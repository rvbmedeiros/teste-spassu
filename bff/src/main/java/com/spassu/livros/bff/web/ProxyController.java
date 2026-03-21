package com.spassu.livros.bff.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spassu.livros.bff.config.RequestCorrelationWebFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Transparent proxy: forwards every request under /api/** and /flows/**
 * to the orchestration layer, injecting the validated JWT as Authorization header.
 *
 * <p>Because the BFF already validated the token via Spring Security's resource server,
 * the orchestration service can trust the forwarded Bearer token without re-validating
 * the signature (it still validates, but we avoid double round-trip to Keycloak).
 */
@Slf4j
@RestController
public class ProxyController {

    private static final Set<String> EXCLUDED_REQUEST_HEADERS = Set.of(
            HttpHeaders.HOST.toLowerCase(),
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            HttpHeaders.CONNECTION.toLowerCase(),
            HttpHeaders.ORIGIN.toLowerCase(),
            HttpHeaders.REFERER.toLowerCase(),
            "access-control-request-method",
            "access-control-request-headers",
            "sec-fetch-site",
            "sec-fetch-mode",
            "sec-fetch-dest",
            "sec-ch-ua",
            "sec-ch-ua-mobile",
            "sec-ch-ua-platform"
    );

    private final WebClient orchestrationClient;
    private final ObjectMapper objectMapper;

    public ProxyController(@Qualifier("orchestrationClient") WebClient orchestrationClient, ObjectMapper objectMapper) {
        this.orchestrationClient = orchestrationClient;
        this.objectMapper = objectMapper;
    }

    @RequestMapping({"/api/**", "/flows/**"})
    public Mono<Void> proxy(ServerWebExchange exchange, Authentication auth) {
        ServerHttpRequest req = exchange.getRequest();
        ServerHttpResponse res = exchange.getResponse();
        String pathWithQuery = req.getURI().getRawPath() +
            (req.getURI().getRawQuery() != null ? "?" + req.getURI().getRawQuery() : "");
        String requestId = resolveRequestId(exchange);

        String bearerToken = extractToken(auth);

        log.info("Proxying request to orchestration. method={}, path={}, requestId={}",
            req.getMethod(), pathWithQuery, requestId);
        log.debug("Proxy request headers received. requestId={}, headerCount={}", requestId, req.getHeaders().size());

        return orchestrationClient
                .method(req.getMethod())
            .uri(pathWithQuery)
                .headers(h -> h.addAll(buildForwardHeaders(req.getHeaders(), bearerToken, requestId)))
                .body(BodyInserters.fromDataBuffers(req.getBody()))
                .exchangeToMono(upstream -> {
                    if (upstream.statusCode().is2xxSuccessful()) {
                log.info("Proxy response success. method={}, path={}, status={}, requestId={}",
                    req.getMethod(), pathWithQuery, upstream.statusCode().value(), requestId);
                        res.setStatusCode(upstream.statusCode());
                        res.getHeaders().addAll(upstream.headers().asHttpHeaders());
                        res.getHeaders().set(RequestCorrelationWebFilter.REQUEST_ID_HEADER, requestId);
                        return res.writeWith(upstream.body(BodyExtractors.toDataBuffers()));
                    }

                    // Normalize orchestration error to BFF error code table
                    return upstream.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                    log.error("Proxy response error. method={}, path={}, status={}, requestId={}, body={}",
                        req.getMethod(), pathWithQuery, upstream.statusCode().value(), requestId, body);
                                Map<String, Object> payload = new HashMap<>();
                                payload.put("timestamp", Instant.now().toString());
                                payload.put("status", upstream.statusCode().value());
                                payload.put("error", "Orchestration error");
                                payload.put("detail", "A backend orchestration error occurred. See code for mapping.");
                                payload.put("type", "https://spassu.com/problems/bff-upstream-error");
                                payload.put("code", mapUpstreamStatusToBffCode(upstream.statusCode().value()));
                                payload.put("instance", exchange.getRequest().getPath().value());

                                res.setStatusCode(upstream.statusCode());
                                res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                res.getHeaders().set(RequestCorrelationWebFilter.REQUEST_ID_HEADER, requestId);
                                byte[] bytes;
                                try {
                                    bytes = objectMapper.writeValueAsBytes(payload);
                                } catch (Exception e) {
                                    log.error("Could not serialize normalized orchestration error payload. requestId={}", requestId, e);
                                    bytes = ("{\"error\":\"Serialization failure\"}").getBytes();
                                }
                                return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
                            });
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Proxy call failed with WebClientResponseException. method={}, path={}, status={}, requestId={}",
                            req.getMethod(), pathWithQuery, ex.getStatusCode().value(), requestId, ex);
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("timestamp", Instant.now().toString());
                    payload.put("status", ex.getStatusCode().value());
                    payload.put("error", "Orchestration http exception");
                    payload.put("detail", "Cannot complete call to orchestration service.");
                    payload.put("type", "https://spassu.com/problems/bff-upstream-error");
                    payload.put("code", mapUpstreamStatusToBffCode(ex.getStatusCode().value()));
                    payload.put("instance", exchange.getRequest().getPath().value());

                    res.setStatusCode(ex.getStatusCode());
                    res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    res.getHeaders().set(RequestCorrelationWebFilter.REQUEST_ID_HEADER, requestId);
                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(payload);
                    } catch (Exception e) {
                        log.error("Could not serialize WebClientResponseException payload. requestId={}", requestId, e);
                        bytes = ("{\"error\":\"Serialization failure\"}").getBytes();
                    }
                    return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected proxy error. method={}, path={}, requestId={}",
                            req.getMethod(), pathWithQuery, requestId, ex);
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("timestamp", Instant.now().toString());
                    payload.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    payload.put("error", "BFF proxy error");
                    payload.put("detail", "Internal BFF failure while proxying request.");
                    payload.put("type", "https://spassu.com/problems/internal-error");
                    payload.put("code", BffErrorCode.INTERNAL_ERROR.getCode());
                    payload.put("instance", exchange.getRequest().getPath().value());

                    res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    res.getHeaders().set(RequestCorrelationWebFilter.REQUEST_ID_HEADER, requestId);
                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(payload);
                    } catch (Exception e) {
                        log.error("Could not serialize internal proxy error payload. requestId={}", requestId, e);
                        bytes = ("{\"error\":\"Serialization failure\"}").getBytes();
                    }
                    return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
                });
    }

    private String extractToken(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }

    static HttpHeaders buildForwardHeaders(HttpHeaders incomingHeaders, String bearerToken, String requestId) {
        HttpHeaders forwardedHeaders = new HttpHeaders();

        incomingHeaders.forEach((name, values) -> {
            if (!EXCLUDED_REQUEST_HEADERS.contains(name.toLowerCase())) {
                forwardedHeaders.put(name, values);
            }
        });

        if (bearerToken != null && !bearerToken.isBlank()) {
            forwardedHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        }

        forwardedHeaders.set(RequestCorrelationWebFilter.REQUEST_ID_HEADER, requestId);
        return forwardedHeaders;
    }

    private String resolveRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestCorrelationWebFilter.REQUEST_ID_HEADER);
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }

        String responseRequestId = exchange.getResponse().getHeaders().getFirst(RequestCorrelationWebFilter.REQUEST_ID_HEADER);
        if (responseRequestId != null && !responseRequestId.isBlank()) {
            return responseRequestId;
        }

        return UUID.randomUUID().toString();
    }

    private String mapUpstreamStatusToBffCode(int status) {
        if (status == 400) {
            return BffErrorCode.ORCHESTRATION_BAD_REQUEST.getCode();
        }
        if (status == 404) {
            return BffErrorCode.ORCHESTRATION_NOT_FOUND.getCode();
        }
        if (status == 503 || status == 504 || status == 502) {
            return BffErrorCode.ORCHESTRATION_UNAVAILABLE.getCode();
        }
        if (status >= 500 && status < 600) {
            return BffErrorCode.ORCHESTRATION_INTERNAL.getCode();
        }
        return BffErrorCode.INTERNAL_ERROR.getCode();
    }
}
