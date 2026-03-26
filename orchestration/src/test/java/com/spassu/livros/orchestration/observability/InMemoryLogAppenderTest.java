package com.spassu.livros.orchestration.observability;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLogAppenderTest {

    @Test
    @DisplayName("append deve enviar evento para o store")
    void append_deveEnviarEventoParaOStore() {
        InMemoryLogAppender appender = new InMemoryLogAppender();
        appender.start();

        LoggingEvent event = new LoggingEvent();
        event.setTimeStamp(System.currentTimeMillis());
        event.setLevel(Level.INFO);
        event.setLoggerName("logger.test");
        event.setMessage("mensagem appender");
        event.setThreadName("main");
        event.setMDCPropertyMap(Map.of("requestId", "req-app", "traceId", "trace-app"));

        appender.doAppend(event);

        assertThat(InMemoryLogStore.getInstance().recent("orchestration", "INFO", 10, "appender"))
                .isNotEmpty();
    }
}