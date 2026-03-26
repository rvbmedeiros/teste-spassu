package com.spassu.livros.microservice.infrastructure.web;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogsControllerTest {

    private final LogsController controller = new LogsController();

    @Test
    @DisplayName("listLogs deve retornar entrada do store em memoria")
    void listLogs_deveRetornarEntradaDoStoreEmMemoria() {
        String message = "micro-log-" + UUID.randomUUID();
        appendLog(message);

        var result = controller.listLogs(10, "INFO", message);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().source()).isEqualTo("microservice");
        assertThat(result.getFirst().message()).contains(message);
    }

    private void appendLog(String message) {
        LoggingEvent event = new LoggingEvent();
        event.setTimeStamp(System.currentTimeMillis());
        event.setLevel(Level.INFO);
        event.setLoggerName("test.logger");
        event.setMessage(message);
        event.setThreadName("main");
        event.setMDCPropertyMap(Map.of("requestId", "req-1", "traceId", "trace-1"));
        com.spassu.livros.microservice.infrastructure.logging.InMemoryLogStore.getInstance().append(event);
    }
}