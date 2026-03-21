package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.infrastructure.logging.InMemoryLogStore;
import com.spassu.livros.microservice.infrastructure.logging.LogFileReader;
import com.spassu.livros.microservice.infrastructure.logging.LogEntryView;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Hidden
@RestController
@RequestMapping("/internal/logs")
@Tag(name = "Observability", description = "Consulta interna de logs do microservice")
public class LogsController {

    private static final String LOG_FILE_NAME = "microservice.log";

    @Operation(summary = "Lista logs recentes do microservice")
    @GetMapping
    public List<LogEntryView> listLogs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "INFO") String level,
            @RequestParam(defaultValue = "") String search) {
        log.debug("Listing internal microservice logs. limit={}, level={}, search={}", limit, level, search);

        List<LogEntryView> inMemory = InMemoryLogStore.getInstance().recent("microservice", level, limit, search);
        if (!inMemory.isEmpty()) {
            return inMemory;
        }

        return LogFileReader.recent("microservice", level, limit, search, LOG_FILE_NAME);
    }
}
