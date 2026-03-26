package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
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
        id = "delete-autor",
        name = "Excluir Autor",
    description = "Remove Autor existente via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "autores",
    businessGoal = "Excluir autores com segurança"
)
@FlowStartEvent(nextStep = "verificar-existencia")
@FlowGateway(
    nodeId = "gw-existe",
    name = "Autor existe?",
    description = "Decide continuidade da exclusão",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "deletar", edgeIntent = "found"),
        @FlowBranch(label = "Não", nextStep = "fim-nao-encontrado", edgeIntent = "not-found")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-nao-encontrado", name = "Fim - Não encontrado")
public class DeleteAutorFlow {

    private final MicroserviceClient client;

    public DeleteAutorFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "verificar-existencia",
            name = "Verificar existência",
            description = "GET /api/autores/{id}",
            nextSteps = {"gw-existe"},
            purpose = "Garantir existência antes da exclusão",
            inputHint = "codAu",
            outputHint = "Autor encontrado",
            failureHint = "Autor não encontrado"
    )
    public Mono<AutorResponse> verificarExistencia(Integer id) {
        return client.buscarAutorPorId(id);
    }

    @FlowStep(
            order = 2,
            nodeId = "deletar",
            name = "Remover no microservice",
            description = "DELETE /api/autores/{id}",
            nextSteps = {"fim-sucesso"},
            purpose = "Executar remoção do autor",
            inputHint = "codAu",
            outputHint = "Operação concluída",
            failureHint = "Erro de integração RPC"
    )
    public Mono<Void> deletar(Integer id) {
        return client.excluirAutor(id);
    }

    public Mono<Void> execute(Integer id) {
        return verificarExistencia(id)
                .then(deletar(id));
    }
}