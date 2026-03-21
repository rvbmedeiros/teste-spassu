package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.LivroResponse;
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
        id = "delete-livro",
        name = "Excluir Livro",
    description = "Remove Livro e suas associações com Autores e Assuntos",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "livros",
    businessGoal = "Excluir livro com segurança"
)
@FlowStartEvent(nextStep = "verificar-existencia")
@FlowGateway(
    nodeId = "gw-existe",
    name = "Livro existe?",
    description = "Decide continuidade da exclusão",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "deletar"),
        @FlowBranch(label = "Não", nextStep = "fim-nao-encontrado", edgeIntent = "not-found")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-nao-encontrado", name = "Fim - Não encontrado")
public class DeleteLivroFlow {

    private final MicroserviceClient client;

    public DeleteLivroFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "verificar-existencia",
            name = "Verificar existência",
            description = "GET /api/livros/{id}",
            nextSteps = {"gw-existe"},
            purpose = "Impedir exclusão de recurso inexistente",
            inputHint = "codL",
            outputHint = "Livro encontrado",
            failureHint = "Livro não encontrado"
    )
    public Mono<LivroResponse> verificarExistencia(Integer id) {
        return client.buscarLivroPorId(id);
    }

    @FlowStep(
            order = 2,
            nodeId = "deletar",
            name = "Remover no microservice",
            description = "DELETE /api/livros/{id}",
            nextSteps = {"fim-sucesso"},
            purpose = "Executar remoção do livro",
            inputHint = "codL",
            outputHint = "Operação concluída",
            failureHint = "Erro de integração RPC"
    )
    public Mono<Void> deletar(Integer id) {
        return client.excluirLivro(id);
    }

    public Mono<Void> execute(Integer id) {
        return verificarExistencia(id)
                .then(deletar(id));
    }
}
