package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
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
        id = "delete-assunto",
        name = "Excluir Assunto",
    description = "Remove Assunto existente via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "assuntos",
    businessGoal = "Excluir assuntos com segurança"
)
@FlowStartEvent(nextStep = "verificar-existencia")
@FlowGateway(
    nodeId = "gw-existe",
    name = "Assunto existe?",
    description = "Decide continuidade da exclusão",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "deletar", edgeIntent = "found"),
        @FlowBranch(label = "Não", nextStep = "fim-nao-encontrado", edgeIntent = "not-found")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-nao-encontrado", name = "Fim - Não encontrado")
public class DeleteAssuntoFlow {

    private final MicroserviceClient client;

    public DeleteAssuntoFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "verificar-existencia",
            name = "Verificar existência",
            description = "GET /api/assuntos/{id}",
            nextSteps = {"gw-existe"},
            purpose = "Garantir existência antes da exclusão",
            inputHint = "codAs",
            outputHint = "Assunto encontrado",
            failureHint = "Assunto não encontrado"
    )
    public Mono<AssuntoResponse> verificarExistencia(Integer id) {
        return client.buscarAssuntoPorId(id);
    }

    @FlowStep(
            order = 2,
            nodeId = "deletar",
            name = "Remover no microservice",
            description = "DELETE /api/assuntos/{id}",
            nextSteps = {"fim-sucesso"},
            purpose = "Executar remoção do assunto",
            inputHint = "codAs",
            outputHint = "Operação concluída",
            failureHint = "Erro de integração RPC"
    )
    public Mono<Void> deletar(Integer id) {
        return client.excluirAssunto(id);
    }

    public Mono<Void> execute(Integer id) {
        return verificarExistencia(id)
                .then(deletar(id));
    }
}