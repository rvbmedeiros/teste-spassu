package com.spassu.livros.orchestration.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogFileReaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("recent deve retornar vazio quando arquivo nao existe")
    void recent_deveRetornarVazioQuandoArquivoNaoExiste() {
        List<LogEntryView> result = LogFileReader.recent("orchestration", "INFO", 10, null, tempDir.resolve("missing.log").toString());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("recent deve filtrar por nivel, busca e normalizar ids vazios")
    void recent_deveFiltrarPorNivelBuscaENormalizarIdsVazios() throws IOException {
        Path file = tempDir.resolve("app.log");
        Files.write(file, List.of(
                "2026-03-21 10:00:00.000 INFO  [main] [orch] traceId=trace-1 requestId=req-1 com.spassu.Log - primeira mensagem",
                "linha invalida",
                "2026-03-21 10:00:01.000 ERROR [worker] [orch] traceId=- requestId= com.spassu.Other - falha critica"
        ));

        List<LogEntryView> result = LogFileReader.recent("orchestration", "WARN", 10, "falha", file.toString());

        assertThat(result).hasSize(1);
        LogEntryView entry = result.getFirst();
        assertThat(entry.level()).isEqualTo("ERROR");
        assertThat(entry.message()).contains("falha critica");
        assertThat(entry.requestId()).isNull();
        assertThat(entry.traceId()).isNull();
    }

    @Test
    @DisplayName("recent deve respeitar limite maximo e ordem reversa")
    void recent_deveRespeitarLimiteMaximoEOrdemReversa() throws IOException {
        Path file = tempDir.resolve("ordered.log");
        Files.write(file, List.of(
                "2026-03-21 10:00:00.000 INFO  [main] [orch] traceId=t1 requestId=r1 logger.One - msg 1",
                "2026-03-21 10:00:01.000 INFO  [main] [orch] traceId=t2 requestId=r2 logger.Two - msg 2",
                "2026-03-21 10:00:02.000 INFO  [main] [orch] traceId=t3 requestId=r3 logger.Three - msg 3"
        ));

        List<LogEntryView> result = LogFileReader.recent("orchestration", "INFO", 2, null, file.toString());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).message()).isEqualTo("msg 3");
        assertThat(result.get(1).message()).isEqualTo("msg 2");
    }
}