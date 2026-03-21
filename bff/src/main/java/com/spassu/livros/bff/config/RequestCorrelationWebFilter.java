package com.spassu.livros.bff.config;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestCorrelationWebFilter implements WebFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_KEY = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestIdHeader = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        String requestId = (requestIdHeader == null || requestIdHeader.isBlank())
                ? UUID.randomUUID().toString()
                : requestIdHeader;

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(REQUEST_ID_HEADER, requestId)))
                .build();

        mutatedExchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .contextWrite(ctx -> ctx.put(MDC_KEY, finalRequestId))
                .doFirst(() -> MDC.put(MDC_KEY, finalRequestId))
                .doFinally(signalType -> MDC.remove(MDC_KEY));
    }

    public static void propagateRequestId(HttpHeaders downstreamHeaders, HttpHeaders requestHeaders) {
        String requestId = requestHeaders.getFirst(REQUEST_ID_HEADER);
        if (requestId != null && !requestId.isBlank()) {
            downstreamHeaders.set(REQUEST_ID_HEADER, requestId);
        }
    }
}