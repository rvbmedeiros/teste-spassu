package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AutorRequest;
import com.spassu.livros.orchestration.dto.AutorResponse;
import com.spassu.livros.orchestration.flowcockpit.GatewayExecutionCoordinator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AutorFlowsTest {

    @Mock
    private MicroserviceClient client;

    @Mock
    private GatewayExecutionCoordinator gatewayExecutionCoordinator;

    @org.mockito.InjectMocks
    private UpdateAutorFlow updateFlow;

    @org.mockito.InjectMocks
    private DeleteAutorFlow deleteFlow;

    @Test
    @DisplayName("create flow deve persistir autor")
    void createFlow_devePersistirAutor() {
        CreateAutorFlow createFlow = new CreateAutorFlow(client, gatewayExecutionCoordinator);
        var request = new AutorRequest("Martin Fowler");
        var response = new AutorResponse(1, "Martin Fowler");
        given(gatewayExecutionCoordinator.routeExclusive(eq("create-autor"), eq("gw-validacao"), eq("validation-pass"), anyMap()))
                .willAnswer(invocation -> {
                    Map<String, Supplier<Mono<AutorResponse>>> routes = invocation.getArgument(3);
                    return routes.get("validation-pass").get();
                });
        given(client.criarAutor(request)).willReturn(Mono.just(response));

        var result = createFlow.execute(request).block();

        assertThat(result).isEqualTo(response);
        then(client).should().criarAutor(request);
    }

    @Test
    @DisplayName("update flow deve verificar existencia antes de atualizar")
    void updateFlow_deveVerificarExistenciaAntesDeAtualizar() {
        var request = new AutorRequest("Kent Beck");
        var existing = new AutorResponse(1, "Martin Fowler");
        var updated = new AutorResponse(1, "Kent Beck");
        given(client.buscarAutorPorId(1)).willReturn(Mono.just(existing));
        given(client.atualizarAutor(1, request)).willReturn(Mono.just(updated));

        var result = updateFlow.execute(1, request).block();

        assertThat(result).isEqualTo(updated);
        then(client).should().buscarAutorPorId(1);
        then(client).should().atualizarAutor(1, request);
    }

    @Test
    @DisplayName("delete flow deve verificar existencia antes de excluir")
    void deleteFlow_deveVerificarExistenciaAntesDeExcluir() {
        var existing = new AutorResponse(1, "Martin Fowler");
        given(client.buscarAutorPorId(1)).willReturn(Mono.just(existing));
        given(client.excluirAutor(1)).willReturn(Mono.empty());

        deleteFlow.execute(1).block();

        then(client).should().buscarAutorPorId(1);
        then(client).should().excluirAutor(1);
    }
}