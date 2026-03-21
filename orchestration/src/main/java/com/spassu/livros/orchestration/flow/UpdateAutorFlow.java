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
        id = "update-autor",
        name = "Atualizar Autor",
        description = "Atualiza Autor existente via microservice"
)
public class UpdateAutorFlow {

    private final MicroserviceClient client;

    public UpdateAutorFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Verificar existência", description = "GET /api/autores/{id}")
    public Mono<AutorResponse> verificarExistencia(Integer id) {
        return client.buscarAutorPorId(id);
    }

    @FlowStep(order = 2, name = "Atualizar no microservice", description = "PUT /api/autores/{id}")
    public Mono<AutorResponse> atualizar(Integer id, AutorRequest request) {
        return client.atualizarAutor(id, request);
    }

    public Mono<AutorResponse> execute(Integer id, AutorRequest request) {
        return verificarExistencia(id)
                .then(atualizar(id, request));
    }
}