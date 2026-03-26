package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AutorRequest;
import com.spassu.livros.orchestration.dto.AutorResponse;
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
        id = "update-autor",
        name = "Atualizar Autor",
    description = "Atualiza Autor existente via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "autores",
    businessGoal = "Atualizar autores com consistência"
)
@FlowStartEvent(nextStep = "verificar-existencia")
@FlowGateway(
    nodeId = "gw-existe",
    name = "Autor existe?",
    description = "Decide continuidade da atualização",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "atualizar", edgeIntent = "found"),
        @FlowBranch(label = "Não", nextStep = "fim-nao-encontrado", edgeIntent = "not-found")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-nao-encontrado", name = "Fim - Não encontrado")
public class UpdateAutorFlow {

    private final MicroserviceClient client;

    public UpdateAutorFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "verificar-existencia",
            name = "Verificar existência",
            description = "GET /api/autores/{id}",
            nextSteps = {"gw-existe"},
            purpose = "Garantir existência antes da atualização",
            inputHint = "codAu",
            outputHint = "Autor encontrado",
            failureHint = "Autor não encontrado"
    )
    public Mono<AutorResponse> verificarExistencia(Integer id) {
        return client.buscarAutorPorId(id);
    }

    @FlowStep(
            order = 2,
            nodeId = "atualizar",
            name = "Atualizar no microservice",
            description = "PUT /api/autores/{id}",
            nextSteps = {"fim-sucesso"},
            purpose = "Persistir alterações do autor",
            inputHint = "AutorRequest",
            outputHint = "Autor atualizado",
            failureHint = "Erro de integração RPC"
    )
    public Mono<AutorResponse> atualizar(Integer id, AutorRequest request) {
        return client.atualizarAutor(id, request);
    }

    public Mono<AutorResponse> execute(Integer id, AutorRequest request) {
        return verificarExistencia(id)
                .then(atualizar(id, request));
    }
}