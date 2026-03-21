package com.spassu.livros.bff.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteLogsClientTest {

    @Test
    @DisplayName("fetchOrchestrationLogs deve montar chamada HTTP e desserializar resposta")
    void fetchOrchestrationLogs_deveMontarChamadaHttpEDesserializarResposta() {
        AtomicReference<String> requestedUrl = new AtomicReference<>();
        WebClient orchestrationClient = WebClient.builder()
                .baseUrl("http://orchestration")
                .exchangeFunction(request -> {
                    requestedUrl.set(request.url().toString());
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .body("[{\"sequence\":1,\"source\":\"orchestration\",\"timestamp\":\"2026-03-21T13:00:00Z\",\"level\":\"INFO\",\"logger\":\"test.Logger\",\"message\":\"ok\",\"thread\":\"main\",\"requestId\":null,\"traceId\":null}]")
                            .build());
                })
                .build();
        WebClient microserviceClient = WebClient.builder().baseUrl("http://microservice").exchangeFunction(request -> Mono.empty()).build();
        RemoteLogsClient client = new RemoteLogsClient(orchestrationClient, microserviceClient);

        var result = client.fetchOrchestrationLogs(25, "WARN", "abc").block();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().source()).isEqualTo("orchestration");
        assertThat(requestedUrl.get()).contains("/internal/logs");
        assertThat(requestedUrl.get()).contains("limit=25");
        assertThat(requestedUrl.get()).contains("level=WARN");
        assertThat(requestedUrl.get()).contains("search=abc");
    }

    @Test
    @DisplayName("fetchMicroserviceLogs deve retornar entrada de erro quando chamada falha")
    void fetchMicroserviceLogs_deveRetornarEntradaDeErroQuandoChamadaFalha() {
        WebClient orchestrationClient = WebClient.builder().baseUrl("http://orchestration").exchangeFunction(request -> Mono.empty()).build();
        WebClient microserviceClient = WebClient.builder()
                .baseUrl("http://microservice")
                .exchangeFunction(request -> Mono.error(new RuntimeException("timeout")))
                .build();
        RemoteLogsClient client = new RemoteLogsClient(orchestrationClient, microserviceClient);

        var result = client.fetchMicroserviceLogs(10, "INFO", "").block();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().source()).isEqualTo("microservice");
        assertThat(result.getFirst().level()).isEqualTo("ERROR");
        assertThat(result.getFirst().message()).contains("timeout");
    }

    @Test
    @DisplayName("mergeAndLimit deve ordenar por timestamp decrescente e limitar")
    void mergeAndLimit_deveOrdenarPorTimestampDecrescenteELimitar() {
        RemoteLogsClient client = new RemoteLogsClient(WebClient.create(), WebClient.create());
        var older = new LogEntryView(1, "bff", Instant.parse("2026-03-21T10:00:00Z"), "INFO", "A", "old", "main", null, null);
        var newest = new LogEntryView(2, "orchestration", Instant.parse("2026-03-21T12:00:00Z"), "INFO", "B", "new", "main", null, null);
        var middle = new LogEntryView(3, "microservice", Instant.parse("2026-03-21T11:00:00Z"), "INFO", "C", "mid", "main", null, null);

        var result = client.mergeAndLimit(List.of(older), List.of(newest), List.of(middle), 2);

        assertThat(result).extracting(LogEntryView::message).containsExactly("new", "mid");
    }
}