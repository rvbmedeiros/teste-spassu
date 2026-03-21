package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AutorResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "delete-autor",
        name = "Excluir Autor",
        description = "Remove Autor existente via microservice"
)
public class DeleteAutorFlow {

    private final MicroserviceClient client;

    public DeleteAutorFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Verificar existência", description = "GET /api/autores/{id}")
    public Mono<AutorResponse> verificarExistencia(Integer id) {
        return client.buscarAutorPorId(id);
    }

    @FlowStep(order = 2, name = "Remover no microservice", description = "DELETE /api/autores/{id}")
    public Mono<Void> deletar(Integer id) {
        return client.excluirAutor(id);
    }

    public Mono<Void> execute(Integer id) {
        return verificarExistencia(id)
                .then(deletar(id));
    }
}