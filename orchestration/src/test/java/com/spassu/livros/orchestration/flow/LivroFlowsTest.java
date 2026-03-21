package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.dto.AutorResponse;
import com.spassu.livros.orchestration.dto.LivroRequest;
import com.spassu.livros.orchestration.dto.LivroResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LivroFlowsTest {

    @Mock
    private MicroserviceClient client;

    @InjectMocks
    private CreateLivroFlow createFlow;

    @InjectMocks
    private UpdateLivroFlow updateFlow;

    @InjectMocks
    private DeleteLivroFlow deleteFlow;

    @Test
    @DisplayName("create flow deve persistir livro")
    void createFlow_devePersistirLivro() {
        var request = novoLivroRequest();
        var response = novoLivroResponse("Clean Code");
        given(client.criarLivro(request)).willReturn(Mono.just(response));

        var result = createFlow.execute(request).block();

        assertThat(result).isEqualTo(response);
        then(client).should().criarLivro(request);
    }

    @Test
    @DisplayName("update flow deve verificar existencia antes de atualizar")
    void updateFlow_deveVerificarExistenciaAntesDeAtualizar() {
        var request = novoLivroRequest();
        var existing = novoLivroResponse("Clean Code");
        var updated = novoLivroResponse("Clean Architecture");
        given(client.buscarLivroPorId(1)).willReturn(Mono.just(existing));
        given(client.atualizarLivro(1, request)).willReturn(Mono.just(updated));

        var result = updateFlow.execute(1, request).block();

        assertThat(result).isEqualTo(updated);
        then(client).should().buscarLivroPorId(1);
        then(client).should().atualizarLivro(1, request);
    }

    @Test
    @DisplayName("delete flow deve verificar existencia antes de excluir")
    void deleteFlow_deveVerificarExistenciaAntesDeExcluir() {
        var existing = novoLivroResponse("Clean Code");
        given(client.buscarLivroPorId(1)).willReturn(Mono.just(existing));
        given(client.excluirLivro(1)).willReturn(Mono.empty());

        deleteFlow.execute(1).block();

        then(client).should().buscarLivroPorId(1);
        then(client).should().excluirLivro(1);
    }

    private LivroRequest novoLivroRequest() {
        return new LivroRequest(
                "Clean Code",
                "Prentice Hall",
                1,
                "2008",
                new BigDecimal("99.90"),
                Set.of(1),
                Set.of(1)
        );
    }

    private LivroResponse novoLivroResponse(String titulo) {
        return new LivroResponse(
                1,
                titulo,
                "Prentice Hall",
                1,
                "2008",
                new BigDecimal("99.90"),
                List.of(new AutorResponse(1, "Robert C. Martin")),
                List.of(new AssuntoResponse(1, "Arquitetura"))
        );
    }
}
