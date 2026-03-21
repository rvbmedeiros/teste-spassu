package com.spassu.livros.bff.web;

import com.spassu.livros.bff.observability.InMemoryLogStore;
import com.spassu.livros.bff.observability.LogFileReader;
import com.spassu.livros.bff.observability.LogEntryView;
import com.spassu.livros.bff.observability.RemoteLogsClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
@Tag(name = "Observability", description = "Consulta autenticada de logs agregados da plataforma")
public class LogsController {

    private static final String LOG_FILE_NAME = "bff.log";

    private final RemoteLogsClient remoteLogsClient;

    @Operation(summary = "Lista logs agregados do BFF, orchestration e microservice")
    @GetMapping
    public Mono<List<LogEntryView>> listLogs(
            @RequestParam(defaultValue = "all") String source,
            @RequestParam(defaultValue = "INFO") String level,
            @RequestParam(defaultValue = "150") int limit,
            @RequestParam(defaultValue = "") String search) {
        String normalizedSource = source.toLowerCase(Locale.ROOT);
        log.debug("Listing aggregated logs. source={}, level={}, limit={}, search={}", normalizedSource, level, limit, search);

        if ("bff".equals(normalizedSource)) {
            return Mono.just(localLogs("bff", level, limit, search));
        }
        if ("orchestration".equals(normalizedSource)) {
            return remoteLogsClient.fetchOrchestrationLogs(limit, level, search);
        }
        if ("microservice".equals(normalizedSource)) {
            return remoteLogsClient.fetchMicroserviceLogs(limit, level, search);
        }

        Mono<List<LogEntryView>> bffLogs = Mono.just(localLogs("bff", level, limit, search));
        Mono<List<LogEntryView>> orchestrationLogs = remoteLogsClient.fetchOrchestrationLogs(limit, level, search);
        Mono<List<LogEntryView>> microserviceLogs = remoteLogsClient.fetchMicroserviceLogs(limit, level, search);

        return Mono.zip(bffLogs, orchestrationLogs, microserviceLogs)
                .map(tuple -> remoteLogsClient.mergeAndLimit(tuple.getT1(), tuple.getT2(), tuple.getT3(), limit));
    }

    private List<LogEntryView> localLogs(String source, String level, int limit, String search) {
        List<LogEntryView> inMemory = InMemoryLogStore.getInstance().recent(source, level, limit, search);
        if (!inMemory.isEmpty()) {
            return inMemory;
        }
        return LogFileReader.recent(source, level, limit, search, LOG_FILE_NAME);
    }
}
