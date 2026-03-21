package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.LivroRequest;
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
        id = "update-livro",
        name = "Atualizar Livro",
    description = "Atualiza Livro existente, mantendo integridade das associações",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "livros",
    businessGoal = "Permitir atualização segura de livros"
)
@FlowStartEvent(nextStep = "verificar-existencia")
@FlowGateway(
    nodeId = "gw-existe",
    name = "Livro existe?",
    description = "Decide continuidade da atualização",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "atualizar"),
        @FlowBranch(label = "Não", nextStep = "fim-nao-encontrado", edgeIntent = "not-found")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-nao-encontrado", name = "Fim - Não encontrado")
public class UpdateLivroFlow {

    private final MicroserviceClient client;

    public UpdateLivroFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "verificar-existencia",
            name = "Verificar existência",
            description = "GET /api/livros/{id}",
            nextSteps = {"gw-existe"},
            purpose = "Evitar atualização de recurso inexistente",
            inputHint = "codL",
            outputHint = "Livro encontrado",
            failureHint = "Livro não encontrado"
    )
    public Mono<LivroResponse> verificarExistencia(Integer id) {
        return client.buscarLivroPorId(id);
    }

    @FlowStep(
            order = 2,
            nodeId = "atualizar",
            name = "Atualizar no microservice",
            description = "PUT /api/livros/{id}",
            nextSteps = {"fim-sucesso"},
            purpose = "Persistir mudanças do livro",
            inputHint = "LivroRequest",
            outputHint = "Livro atualizado",
            failureHint = "Erro de integração RPC"
    )
    public Mono<LivroResponse> atualizar(Integer id, LivroRequest request) {
        return client.atualizarLivro(id, request);
    }

    public Mono<LivroResponse> execute(Integer id, LivroRequest request) {
        return verificarExistencia(id)
                .then(atualizar(id, request));
    }
}
