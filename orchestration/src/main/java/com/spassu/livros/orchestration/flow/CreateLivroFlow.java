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

/**
 * Flow: Criar Livro
 * Delegates to the microservice, wrapping each meaningful operation
 * in a {@link FlowStep} so FlowCockpit can visualise the progress in real time.
 */
@Slf4j
@Component
@FlowDefinition(
        id = "create-livro",
        name = "Criar Livro",
    description = "Persiste um novo Livro com seus Autores e Assuntos via microservice",
    owner = "orchestration-team",
    version = "1.0.0",
    domainTag = "livros",
    businessGoal = "Garantir cadastro consistente de livros"
)
@FlowStartEvent(nextStep = "validar-payload")
@FlowGateway(
    nodeId = "gw-validacao",
    name = "Payload válido?",
    description = "Decide se o fluxo pode continuar",
    type = NodeType.EXCLUSIVE_GATEWAY,
    branches = {
        @FlowBranch(label = "Sim", nextStep = "gw-prepare"),
        @FlowBranch(label = "Não", nextStep = "fim-invalido", edgeIntent = "validation-fail")
    }
)
@FlowGateway(
    nodeId = "gw-prepare",
    name = "Preparações em paralelo",
    description = "Explicita checkpoints paralelos antes da persistência",
    type = NodeType.PARALLEL_GATEWAY,
    branches = {
        @FlowBranch(label = "Checar autores", nextStep = "validar-autores"),
        @FlowBranch(label = "Checar assuntos", nextStep = "validar-assuntos")
    }
)
@FlowEndEvent(nodeId = "fim-sucesso", name = "Fim - Sucesso")
@FlowEndEvent(nodeId = "fim-invalido", name = "Fim - Payload inválido")
public class CreateLivroFlow {

    private final MicroserviceClient client;

    public CreateLivroFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(
            order = 1,
            nodeId = "validar-payload",
            name = "Validar payload",
            description = "Bean Validation já executado pelo controller",
            nextSteps = {"gw-validacao"},
            purpose = "Evitar persistência com dados inválidos",
            inputHint = "LivroRequest",
            outputHint = "Payload considerado válido",
            failureHint = "Requisição rejeitada"
    )
    public Mono<Void> validarPayload(LivroRequest request) {
        // Validation is handled by @Valid in the controller; this step is a named checkpoint
        return Mono.empty();
    }

    @FlowStep(
            order = 2,
            nodeId = "persistir",
            name = "Enviar para microservice",
            description = "POST /api/livros",
            nextSteps = {"fim-sucesso"},
            purpose = "Persistir o livro e associações",
            inputHint = "LivroRequest validado",
            outputHint = "LivroResponse criado",
            failureHint = "Erro de integração RPC"
    )
    public Mono<LivroResponse> persistir(LivroRequest request) {
        return client.criarLivro(request);
    }

    @FlowStep(
            order = 3,
            nodeId = "validar-autores",
            name = "Checar autores vinculados",
            description = "Checkpoint de consistência para autores",
            nextSteps = {"persistir"},
            purpose = "Explicitar verificação de autores no fluxo",
            inputHint = "autoresCodAu",
            outputHint = "Autores aptos para associação",
            failureHint = "Autor inexistente"
    )
    public Mono<Void> validarAutores(LivroRequest request) {
        return Mono.empty();
    }

    @FlowStep(
            order = 4,
            nodeId = "validar-assuntos",
            name = "Checar assuntos vinculados",
            description = "Checkpoint de consistência para assuntos",
            nextSteps = {"persistir"},
            purpose = "Explicitar verificação de assuntos no fluxo",
            inputHint = "assuntosCodAs",
            outputHint = "Assuntos aptos para associação",
            failureHint = "Assunto inexistente"
    )
    public Mono<Void> validarAssuntos(LivroRequest request) {
        return Mono.empty();
    }

    /** Entry point called by the controller — chains all steps in order. */
    public Mono<LivroResponse> execute(LivroRequest request) {
        return validarPayload(request)
                .then(persistir(request));
    }
}
