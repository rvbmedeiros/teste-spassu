package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.infrastructure.logging.InMemoryLogStore;
import com.spassu.livros.microservice.infrastructure.logging.LogFileReader;
import com.spassu.livros.microservice.infrastructure.logging.LogEntryView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/logs")
public class LogsController {

    private static final String LOG_FILE_NAME = "microservice.log";

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
