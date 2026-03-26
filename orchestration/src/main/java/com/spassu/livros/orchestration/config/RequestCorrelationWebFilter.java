package com.spassu.livros.orchestration.config;

import org.slf4j.MDC;
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
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        String finalRequestId = requestId;
        return chain.filter(exchange)
                .contextWrite(context -> context.put(MDC_KEY, finalRequestId))
                .doFirst(() -> MDC.put(MDC_KEY, finalRequestId))
                .doFinally(signalType -> MDC.remove(MDC_KEY));
    }
}