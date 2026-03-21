package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "get-relatorio",
        name = "Gerar Relatório PDF",
        description = "Delega geração de PDF JasperReports ao microservice e retorna os bytes"
)
public class GetRelatorioFlow {

    private final MicroserviceClient client;

    public GetRelatorioFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Solicitar PDF ao microservice", description = "GET /api/relatorio/pdf")
    public Mono<byte[]> solicitarRelatorio() {
        return client.gerarRelatorio();
    }

    public Mono<byte[]> execute() {
        return solicitarRelatorio();
    }
}
