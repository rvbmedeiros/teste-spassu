package com.spassu.livros.orchestration.observability;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLogStoreTest {

    private final InMemoryLogStore store = InMemoryLogStore.getInstance();

    @BeforeEach
    void resetStore() throws Exception {
        Field entriesField = InMemoryLogStore.class.getDeclaredField("entries");
        entriesField.setAccessible(true);
        entriesField.set(store, new ArrayDeque<>());

        Field sequenceField = InMemoryLogStore.class.getDeclaredField("sequence");
        sequenceField.setAccessible(true);
        sequenceField.set(store, new AtomicLong());
    }

    @Test
    @DisplayName("append e recent devem filtrar por nivel e busca")
    void appendERecent_devemFiltrarPorNivelEBusca() {
        store.append(event(Level.INFO, "Mensagem comum", "req-1", "trace-1"));
        store.append(event(Level.ERROR, "Falha critica", "req-2", "trace-2"));

        var result = store.recent("orchestration", "WARN", 10, "falha");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().message()).isEqualTo("Falha critica");
    }

    @Test
    @DisplayName("recent deve respeitar limite seguro")
    void recent_deveRespeitarLimiteSeguro() {
        for (int index = 0; index < 4; index++) {
            store.append(event(Level.INFO, "Mensagem " + index, "req-" + index, "trace-" + index));
        }

        var result = store.recent("orchestration", "INFO", 2, null);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().message()).isEqualTo("Mensagem 3");
        assertThat(result.get(1).message()).isEqualTo("Mensagem 2");
    }

    private LoggingEvent event(Level level, String message, String requestId, String traceId) {
        LoggingEvent event = new LoggingEvent();
        event.setTimeStamp(System.currentTimeMillis());
        event.setLevel(level);
        event.setLoggerName("test.logger");
        event.setMessage(message);
        event.setThreadName("main");
        event.setMDCPropertyMap(Map.of("requestId", requestId, "traceId", traceId));
        return event;
    }
}