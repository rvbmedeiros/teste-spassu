package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.LivroRequest;
import com.spassu.livros.orchestration.dto.LivroResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
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
        description = "Persiste um novo Livro com seus Autores e Assuntos via microservice"
)
public class CreateLivroFlow {

    private final MicroserviceClient client;

    public CreateLivroFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Validar payload", description = "Bean Validation já executado pelo controller")
    public Mono<Void> validarPayload(LivroRequest request) {
        // Validation is handled by @Valid in the controller; this step is a named checkpoint
        return Mono.empty();
    }

    @FlowStep(order = 2, name = "Enviar para microservice", description = "POST /api/livros")
    public Mono<LivroResponse> persistir(LivroRequest request) {
        return client.criarLivro(request);
    }

    /** Entry point called by the controller — chains all steps in order. */
    public Mono<LivroResponse> execute(LivroRequest request) {
        return validarPayload(request)
                .then(persistir(request));
    }
}
