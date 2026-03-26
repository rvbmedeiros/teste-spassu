package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowEndEvent;
import com.spassu.livros.orchestration.flowcockpit.FlowStartEvent;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "get-relatorio",
        name = "Gerar Relatório PDF",
    description = "Delega geração de PDF JasperReports ao microservice e retorna os bytes",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "relatorio",
    businessGoal = "Gerar relatório consolidado em PDF"
)
@FlowStartEvent(nextStep = "solicitar-relatorio")
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
public class GetRelatorioFlow {

    private final MicroserviceClient client;

    public GetRelatorioFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "solicitar-relatorio",
            name = "Solicitar PDF ao microservice",
            description = "GET /api/relatorio/pdf",
            nextSteps = {"fim-sucesso"},
            purpose = "Delegar geração de relatório ao microservice",
            inputHint = "Parâmetros padrão do relatório",
            outputHint = "Bytes do PDF",
            failureHint = "Erro de integração RPC"
    )
    public Mono<byte[]> solicitarRelatorio() {
        return client.gerarRelatorio();
    }

    public Mono<byte[]> execute() {
        return solicitarRelatorio();
    }
}
