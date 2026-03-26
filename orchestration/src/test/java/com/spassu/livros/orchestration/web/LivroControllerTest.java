package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.dto.AutorResponse;
import com.spassu.livros.orchestration.dto.LivroRequest;
import com.spassu.livros.orchestration.dto.LivroResponse;
import com.spassu.livros.orchestration.flow.CreateLivroFlow;
import com.spassu.livros.orchestration.flow.DeleteLivroFlow;
import com.spassu.livros.orchestration.flow.UpdateLivroFlow;
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
class LivroControllerTest {

    @Mock
    private CreateLivroFlow createFlow;

    @Mock
    private UpdateLivroFlow updateFlow;

    @Mock
    private DeleteLivroFlow deleteFlow;

    @Mock
    private MicroserviceClient client;

    @InjectMocks
    private LivroController controller;

    @Test
    @DisplayName("buscarPorId deve delegar ao client")
    void buscarPorId_deveDelegarAoClient() {
        var response = novoLivroResponse("Clean Code");
        given(client.buscarLivroPorId(1)).willReturn(Mono.just(response));

        var result = controller.buscarPorId(1).block();

        assertThat(result).isEqualTo(response);
        then(client).should().buscarLivroPorId(1);
    }

    @Test
    @DisplayName("listar deve delegar ao client com pagina")
    void listar_deveDelegarAoClientComPagina() {
        var livros = List.of(novoLivroResponse("Clean Code"));
        given(client.listarLivros(0, 20)).willReturn(Mono.just(livros));

        var result = controller.listar(0, 20).block();

        assertThat(result).isEqualTo(livros);
        then(client).should().listarLivros(0, 20);
    }

    @Test
    @DisplayName("criar deve executar flow")
    void criar_deveExecutarFlow() {
        var request = novoLivroRequest("Clean Code");
        var response = novoLivroResponse("Clean Code");
        given(createFlow.execute(request)).willReturn(Mono.just(response));

        var result = controller.criar(request).block();

        assertThat(result).isEqualTo(response);
        then(createFlow).should().execute(request);
    }

    @Test
    @DisplayName("atualizar deve executar flow")
    void atualizar_deveExecutarFlow() {
        var request = novoLivroRequest("Clean Architecture");
        var response = novoLivroResponse("Clean Architecture");
        given(updateFlow.execute(1, request)).willReturn(Mono.just(response));

        var result = controller.atualizar(1, request).block();

        assertThat(result).isEqualTo(response);
        then(updateFlow).should().execute(1, request);
    }

    @Test
    @DisplayName("excluir deve executar flow")
    void excluir_deveExecutarFlow() {
        given(deleteFlow.execute(1)).willReturn(Mono.empty());

        controller.excluir(1).block();

        then(deleteFlow).should().execute(1);
    }

    private LivroRequest novoLivroRequest(String titulo) {
        return new LivroRequest(
                titulo,
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
