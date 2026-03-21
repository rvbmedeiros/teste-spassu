package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoRequest;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.flow.CreateAssuntoFlow;
import com.spassu.livros.orchestration.flow.DeleteAssuntoFlow;
import com.spassu.livros.orchestration.flow.UpdateAssuntoFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AssuntoControllerTest {

    @Mock
    private CreateAssuntoFlow createFlow;

    @Mock
    private UpdateAssuntoFlow updateFlow;

    @Mock
    private DeleteAssuntoFlow deleteFlow;

    @Mock
    private MicroserviceClient client;

    @InjectMocks
    private AssuntoController controller;

    @Test
    @DisplayName("buscarPorId deve delegar ao client")
    void buscarPorId_deveDelegarAoClient() {
        var response = new AssuntoResponse(1, "Arquitetura");
        given(client.buscarAssuntoPorId(1)).willReturn(Mono.just(response));

        var result = controller.buscarPorId(1).block();

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("listar deve delegar ao client")
    void listar_deveDelegarAoClient() {
        var assuntos = List.of(new AssuntoResponse(1, "Arquitetura"));
        given(client.listarAssuntos()).willReturn(Mono.just(assuntos));

        var result = controller.listar().block();

        assertThat(result).isEqualTo(assuntos);
    }

    @Test
    @DisplayName("criar deve executar flow")
    void criar_deveExecutarFlow() {
        var request = new AssuntoRequest("Arquitetura");
        var response = new AssuntoResponse(1, "Arquitetura");
        given(createFlow.execute(request)).willReturn(Mono.just(response));

        var result = controller.criar(request).block();

        assertThat(result).isEqualTo(response);
        then(createFlow).should().execute(request);
    }

    @Test
    @DisplayName("atualizar deve executar flow")
    void atualizar_deveExecutarFlow() {
        var request = new AssuntoRequest("DDD");
        var response = new AssuntoResponse(1, "DDD");
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
}