package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.observability.InMemoryLogStore;
import com.spassu.livros.orchestration.observability.LogFileReader;
import com.spassu.livros.orchestration.observability.LogEntryView;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Hidden
@RestController
@RequestMapping("/internal/logs")
@Tag(name = "Observability", description = "Consulta interna de logs da orchestration")
public class LogsController {

    private static final String LOG_FILE_NAME = "orchestration.log";

    @Operation(summary = "Lista logs recentes da orchestration")
    @GetMapping
    public Mono<List<LogEntryView>> listLogs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "INFO") String level,
            @RequestParam(defaultValue = "") String search) {
        log.debug("Listing internal orchestration logs. limit={}, level={}, search={}", limit, level, search);

        List<LogEntryView> inMemory = InMemoryLogStore.getInstance().recent("orchestration", level, limit, search);
        if (!inMemory.isEmpty()) {
            return Mono.just(inMemory);
        }

        return Mono.just(LogFileReader.recent("orchestration", level, limit, search, LOG_FILE_NAME));
    }
}
