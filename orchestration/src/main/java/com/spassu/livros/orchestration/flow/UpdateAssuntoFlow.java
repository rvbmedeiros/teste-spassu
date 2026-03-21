package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoRequest;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowBranch;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowEndEvent;
import com.spassu.livros.orchestration.flowcockpit.FlowGateway;
import com.spassu.livros.orchestration.flowcockpit.FlowStartEvent;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import com.spassu.livros.orchestration.flowcockpit.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "update-assunto",
        name = "Atualizar Assunto",
    description = "Atualiza Assunto existente via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "assuntos",
    businessGoal = "Atualizar assuntos com consistência"
)
@FlowStartEvent(nextStep = "verificar-existencia")
@FlowGateway(
    nodeId = "gw-existe",
    name = "Assunto existe?",
    description = "Decide continuidade da atualização",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "atualizar"),
        @FlowBranch(label = "Não", nextStep = "fim-nao-encontrado", edgeIntent = "not-found")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-nao-encontrado", name = "Fim - Não encontrado")
public class UpdateAssuntoFlow {

    private final MicroserviceClient client;

    public UpdateAssuntoFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "verificar-existencia",
            name = "Verificar existência",
            description = "GET /api/assuntos/{id}",
            nextSteps = {"gw-existe"},
            purpose = "Garantir existência antes da atualização",
            inputHint = "codAs",
            outputHint = "Assunto encontrado",
            failureHint = "Assunto não encontrado"
    )
    public Mono<AssuntoResponse> verificarExistencia(Integer id) {
        return client.buscarAssuntoPorId(id);
    }

    @FlowStep(
            order = 2,
            nodeId = "atualizar",
            name = "Atualizar no microservice",
            description = "PUT /api/assuntos/{id}",
            nextSteps = {"fim-sucesso"},
            purpose = "Persistir alterações do assunto",
            inputHint = "AssuntoRequest",
            outputHint = "Assunto atualizado",
            failureHint = "Erro de integração RPC"
    )
    public Mono<AssuntoResponse> atualizar(Integer id, AssuntoRequest request) {
        return client.atualizarAssunto(id, request);
    }

    public Mono<AssuntoResponse> execute(Integer id, AssuntoRequest request) {
        return verificarExistencia(id)
                .then(atualizar(id, request));
    }
}