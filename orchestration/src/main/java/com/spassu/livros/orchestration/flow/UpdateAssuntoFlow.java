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
        id = "update-assunto",
        name = "Atualizar Assunto",
        description = "Atualiza Assunto existente via microservice"
)
public class UpdateAssuntoFlow {

    private final MicroserviceClient client;

    public UpdateAssuntoFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Verificar existência", description = "GET /api/assuntos/{id}")
    public Mono<AssuntoResponse> verificarExistencia(Integer id) {
        return client.buscarAssuntoPorId(id);
    }

    @FlowStep(order = 2, name = "Atualizar no microservice", description = "PUT /api/assuntos/{id}")
    public Mono<AssuntoResponse> atualizar(Integer id, AssuntoRequest request) {
        return client.atualizarAssunto(id, request);
    }

    public Mono<AssuntoResponse> execute(Integer id, AssuntoRequest request) {
        return verificarExistencia(id)
                .then(atualizar(id, request));
    }
}