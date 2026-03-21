package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.LivroResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "delete-livro",
        name = "Excluir Livro",
        description = "Remove Livro e suas associações com Autores e Assuntos"
)
public class DeleteLivroFlow {

    private final MicroserviceClient client;

    public DeleteLivroFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Verificar existência", description = "GET /api/livros/{id}")
    public Mono<LivroResponse> verificarExistencia(Integer id) {
        return client.buscarLivroPorId(id);
    }

    @FlowStep(order = 2, name = "Remover no microservice", description = "DELETE /api/livros/{id}")
    public Mono<Void> deletar(Integer id) {
        return client.excluirLivro(id);
    }

    public Mono<Void> execute(Integer id) {
        return verificarExistencia(id)
                .then(deletar(id));
    }
}
