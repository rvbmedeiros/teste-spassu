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
        id = "create-autor",
        name = "Criar Autor",
    description = "Persiste um novo Autor via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "autores",
    businessGoal = "Cadastrar autor de forma consistente"
)
@FlowStartEvent(nextStep = "validar-payload")
@FlowGateway(
    nodeId = "gw-validacao",
    name = "Payload válido?",
    description = "Decide continuidade da criação",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "persistir"),
        @FlowBranch(label = "Não", nextStep = "fim-invalido", edgeIntent = "validation-fail")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-invalido", name = "Fim - Payload inválido")
public class CreateAutorFlow {

    private final MicroserviceClient client;

    public CreateAutorFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "validar-payload",
            name = "Validar payload",
            description = "Bean Validation já executado pelo controller",
            nextSteps = {"gw-validacao"},
            purpose = "Evitar persistência com dados inválidos",
            inputHint = "AutorRequest",
            outputHint = "Payload válido",
            failureHint = "Requisição rejeitada"
    )
    public Mono<Void> validarPayload(AutorRequest request) {
        return Mono.empty();
    }

    @FlowStep(
            order = 2,
            nodeId = "persistir",
            name = "Enviar para microservice",
            description = "POST /api/autores",
            nextSteps = {"fim-sucesso"},
            purpose = "Persistir novo autor",
            inputHint = "AutorRequest validado",
            outputHint = "AutorResponse criado",
            failureHint = "Erro de integração RPC"
    )
    public Mono<AutorResponse> persistir(AutorRequest request) {
        return client.criarAutor(request);
    }

    public Mono<AutorResponse> execute(AutorRequest request) {
        return validarPayload(request)
                .then(persistir(request));
    }
}