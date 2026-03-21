package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AutorRequest;
import com.spassu.livros.orchestration.dto.AutorResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "create-autor",
        name = "Criar Autor",
        description = "Persiste um novo Autor via microservice"
)
public class CreateAutorFlow {

    private final MicroserviceClient client;

    public CreateAutorFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Validar payload", description = "Bean Validation já executado pelo controller")
    public Mono<Void> validarPayload(AutorRequest request) {
        return Mono.empty();
    }

    @FlowStep(order = 2, name = "Enviar para microservice", description = "POST /api/autores")
    public Mono<AutorResponse> persistir(AutorRequest request) {
        return client.criarAutor(request);
    }

    public Mono<AutorResponse> execute(AutorRequest request) {
        return validarPayload(request)
                .then(persistir(request));
    }
}