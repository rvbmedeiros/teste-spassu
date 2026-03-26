package com.spassu.livros.bff.web;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.spassu.livros.bff.observability.LogEntryView;
import com.spassu.livros.bff.observability.RemoteLogsClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LogsControllerTest {

    @Mock
    private RemoteLogsClient remoteLogsClient;

    @InjectMocks
    private LogsController controller;

    @Test
    @DisplayName("listLogs com source bff deve retornar logs locais")
    void listLogs_comSourceBff_deveRetornarLogsLocais() {
        String message = "bff-log-" + UUID.randomUUID();
        appendLog(message);

        var result = controller.listLogs("bff", "INFO", 10, message).block();

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().source()).isEqualTo("bff");
        assertThat(result.getFirst().message()).contains(message);
    }

    @Test
    @DisplayName("listLogs com source orchestration deve delegar ao client remoto")
    void listLogs_comSourceOrchestration_deveDelegarAoClientRemoto() {
        var remote = List.of(new LogEntryView(1, "orchestration", Instant.now(), "INFO", "logger", "msg", "main", null, null));
        given(remoteLogsClient.fetchOrchestrationLogs(20, "WARN", "abc")).willReturn(Mono.just(remote));

        var result = controller.listLogs("orchestration", "WARN", 20, "abc").block();

        assertThat(result).isEqualTo(remote);
        then(remoteLogsClient).should().fetchOrchestrationLogs(20, "WARN", "abc");
    }

    @Test
    @DisplayName("listLogs com source all deve mesclar entradas")
    void listLogs_comSourceAll_deveMesclarEntradas() {
        var bffLogs = List.of(new LogEntryView(1, "bff", Instant.now(), "INFO", "logger", "bff", "main", null, null));
        var orchestrationLogs = List.of(new LogEntryView(2, "orchestration", Instant.now(), "INFO", "logger", "orch", "main", null, null));
        var microserviceLogs = List.of(new LogEntryView(3, "microservice", Instant.now(), "INFO", "logger", "micro", "main", null, null));
        given(remoteLogsClient.fetchOrchestrationLogs(15, "INFO", "")).willReturn(Mono.just(orchestrationLogs));
        given(remoteLogsClient.fetchMicroserviceLogs(15, "INFO", "")).willReturn(Mono.just(microserviceLogs));
        given(remoteLogsClient.mergeAndLimit(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.eq(15)))
                .willReturn(List.of(bffLogs.getFirst(), orchestrationLogs.getFirst(), microserviceLogs.getFirst()));
        appendLog("all-log-" + UUID.randomUUID());

        var result = controller.listLogs("all", "INFO", 15, "").block();

        assertThat(result).hasSize(3);
        then(remoteLogsClient).should().mergeAndLimit(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.eq(15));
    }

    private void appendLog(String message) {
        LoggingEvent event = new LoggingEvent();
        event.setTimeStamp(System.currentTimeMillis());
        event.setLevel(Level.INFO);
        event.setLoggerName("test.logger");
        event.setMessage(message);
        event.setThreadName("main");
        event.setMDCPropertyMap(Map.of("requestId", "req-1", "traceId", "trace-1"));
        com.spassu.livros.bff.observability.InMemoryLogStore.getInstance().append(event);
    }
}