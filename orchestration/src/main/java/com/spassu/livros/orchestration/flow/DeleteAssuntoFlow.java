package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "delete-assunto",
        name = "Excluir Assunto",
        description = "Remove Assunto existente via microservice"
)
public class DeleteAssuntoFlow {

    private final MicroserviceClient client;

    public DeleteAssuntoFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Verificar existência", description = "GET /api/assuntos/{id}")
    public Mono<AssuntoResponse> verificarExistencia(Integer id) {
        return client.buscarAssuntoPorId(id);
    }

    @FlowStep(order = 2, name = "Remover no microservice", description = "DELETE /api/assuntos/{id}")
    public Mono<Void> deletar(Integer id) {
        return client.excluirAssunto(id);
    }

    public Mono<Void> execute(Integer id) {
        return verificarExistencia(id)
                .then(deletar(id));
    }
}