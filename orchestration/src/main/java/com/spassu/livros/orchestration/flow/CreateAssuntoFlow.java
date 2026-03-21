package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoRequest;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "create-assunto",
        name = "Criar Assunto",
        description = "Persiste um novo Assunto via microservice"
)
public class CreateAssuntoFlow {

    private final MicroserviceClient client;

    public CreateAssuntoFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Validar payload", description = "Bean Validation já executado pelo controller")
    public Mono<Void> validarPayload(AssuntoRequest request) {
        return Mono.empty();
    }

    @FlowStep(order = 2, name = "Enviar para microservice", description = "POST /api/assuntos")
    public Mono<AssuntoResponse> persistir(AssuntoRequest request) {
        return client.criarAssunto(request);
    }

    public Mono<AssuntoResponse> execute(AssuntoRequest request) {
        return validarPayload(request)
                .then(persistir(request));
    }
}