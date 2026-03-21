package com.spassu.livros.bff.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RemoteLogsClient {

    private static final ParameterizedTypeReference<List<LogEntryView>> LOG_LIST_TYPE =
            new ParameterizedTypeReference<>() { };

    private final WebClient orchestrationInternalClient;
    private final WebClient microserviceInternalClient;

    public RemoteLogsClient(
            @Qualifier("orchestrationInternalClient") WebClient orchestrationInternalClient,
            @Qualifier("microserviceInternalClient") WebClient microserviceInternalClient) {
        this.orchestrationInternalClient = orchestrationInternalClient;
        this.microserviceInternalClient = microserviceInternalClient;
    }

    public Mono<List<LogEntryView>> fetchOrchestrationLogs(int limit, String level, String search) {
        return fetch(orchestrationInternalClient, "orchestration", limit, level, search);
    }

    public Mono<List<LogEntryView>> fetchMicroserviceLogs(int limit, String level, String search) {
        return fetch(microserviceInternalClient, "microservice", limit, level, search);
    }

    private Mono<List<LogEntryView>> fetch(WebClient client, String source, int limit, String level, String search) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/logs")
                        .queryParam("limit", limit)
                        .queryParam("level", level)
                        .queryParamIfPresent("search", search == null || search.isBlank()
                                ? Optional.empty()
                                : Optional.of(search))
                        .build())
                .retrieve()
                .bodyToMono(LOG_LIST_TYPE)
                .doOnSuccess(entries -> log.debug("Fetched {} logs from {}", entries.size(), source))
                .onErrorResume(ex -> {
                    log.error("Could not fetch logs from {}", source, ex);
                    return Mono.just(List.of(new LogEntryView(
                            -1,
                            source,
                            Instant.now(),
                            "ERROR",
                            RemoteLogsClient.class.getName(),
                            "Falha ao consultar logs do serviço " + source + ": " + ex.getMessage(),
                            Thread.currentThread().getName(),
                            null,
                            null)));
                });
    }

    public List<LogEntryView> mergeAndLimit(List<LogEntryView> first, List<LogEntryView> second, List<LogEntryView> third, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return java.util.stream.Stream.of(first, second, third)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(LogEntryView::timestamp).reversed())
                .limit(safeLimit)
                .toList();
    }
}