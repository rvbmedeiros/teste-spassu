package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.LivroRequest;
import com.spassu.livros.orchestration.dto.LivroResponse;
import com.spassu.livros.orchestration.flowcockpit.FlowDefinition;
import com.spassu.livros.orchestration.flowcockpit.FlowStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@FlowDefinition(
        id = "update-livro",
        name = "Atualizar Livro",
        description = "Atualiza Livro existente, mantendo integridade das associações"
)
public class UpdateLivroFlow {

    private final MicroserviceClient client;

    public UpdateLivroFlow(MicroserviceClient client) {
        this.client = client;
    }

    @FlowStep(order = 1, name = "Verificar existência", description = "GET /api/livros/{id}")
    public Mono<LivroResponse> verificarExistencia(Integer id) {
        return client.buscarLivroPorId(id);
    }

    @FlowStep(order = 2, name = "Atualizar no microservice", description = "PUT /api/livros/{id}")
    public Mono<LivroResponse> atualizar(Integer id, LivroRequest request) {
        return client.atualizarLivro(id, request);
    }

    public Mono<LivroResponse> execute(Integer id, LivroRequest request) {
        return verificarExistencia(id)
                .then(atualizar(id, request));
    }
}
