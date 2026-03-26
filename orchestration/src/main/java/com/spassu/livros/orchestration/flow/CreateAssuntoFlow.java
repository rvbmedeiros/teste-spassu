package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoRequest;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowBranch;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowEndEvent;
import com.spassu.livros.orchestration.flowcockpit.FlowGateway;
import com.spassu.livros.orchestration.flowcockpit.GatewayExecutionCoordinator;
import com.spassu.livros.orchestration.flowcockpit.FlowStartEvent;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import com.spassu.livros.orchestration.flowcockpit.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@FlowDefinition(
        id = "create-assunto",
        name = "Criar Assunto",
    description = "Persiste um novo Assunto via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "assuntos",
    businessGoal = "Cadastrar assuntos de forma consistente"
)
@FlowStartEvent(nextStep = "validar-payload")
@FlowGateway(
    nodeId = "gw-validacao",
    name = "Payload válido?",
    description = "Decide continuidade da criação",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "persistir", edgeIntent = "validation-pass"),
        @FlowBranch(label = "Não", nextStep = "fim-invalido", edgeIntent = "validation-fail")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-invalido", name = "Fim - Payload inválido")
public class CreateAssuntoFlow {

    private final MicroserviceClient client;
    private final GatewayExecutionCoordinator gatewayExecutionCoordinator;

    public CreateAssuntoFlow(MicroserviceClient client, GatewayExecutionCoordinator gatewayExecutionCoordinator) {
        this.client = client;
        this.gatewayExecutionCoordinator = gatewayExecutionCoordinator;
    }

    @FlowStep(
            order = 1,
            nodeId = "validar-payload",
            name = "Validar payload",
            description = "Bean Validation já executado pelo controller",
            nextSteps = {"gw-validacao"},
            purpose = "Evitar persistência com dados inválidos",
            inputHint = "AssuntoRequest",
            outputHint = "Payload válido",
            failureHint = "Requisição rejeitada"
    )
    public Mono<Void> validarPayload(AssuntoRequest request) {
        return Mono.empty();
    }

    @FlowStep(
            order = 2,
            nodeId = "persistir",
            name = "Enviar para microservice",
            description = "POST /api/assuntos",
            nextSteps = {"fim-sucesso"},
            purpose = "Persistir novo assunto",
            inputHint = "AssuntoRequest validado",
            outputHint = "AssuntoResponse criado",
            failureHint = "Erro de integração RPC"
    )
    public Mono<AssuntoResponse> persistir(AssuntoRequest request) {
        return client.criarAssunto(request);
    }

    public Mono<AssuntoResponse> execute(AssuntoRequest request) {
        String validationIntent = (request != null && request.descricao() != null && !request.descricao().isBlank())
            ? "validation-pass"
            : "validation-fail";

        return gatewayExecutionCoordinator.routeExclusive(
            "create-assunto",
            "gw-validacao",
            validationIntent,
            Map.of(
                "validation-pass", () -> validarPayload(request).then(persistir(request)),
                "validation-fail", () -> Mono.error(new IllegalArgumentException("Payload invalido para criação de assunto"))
            )
        );
    }
}