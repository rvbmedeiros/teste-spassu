package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoRequest;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
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
class AssuntoFlowsTest {

    @Mock
    private MicroserviceClient client;

    @Mock
    private GatewayExecutionCoordinator gatewayExecutionCoordinator;

    @org.mockito.InjectMocks
    private UpdateAssuntoFlow updateFlow;

    @org.mockito.InjectMocks
    private DeleteAssuntoFlow deleteFlow;

    @Test
    @DisplayName("create flow deve persistir assunto")
    void createFlow_devePersistirAssunto() {
        CreateAssuntoFlow createFlow = new CreateAssuntoFlow(client, gatewayExecutionCoordinator);
        var request = new AssuntoRequest("Arquitetura");
        var response = new AssuntoResponse(1, "Arquitetura");
        given(gatewayExecutionCoordinator.routeExclusive(eq("create-assunto"), eq("gw-validacao"), eq("validation-pass"), anyMap()))
                .willAnswer(invocation -> {
                    Map<String, Supplier<Mono<AssuntoResponse>>> routes = invocation.getArgument(3);
                    return routes.get("validation-pass").get();
                });
        given(client.criarAssunto(request)).willReturn(Mono.just(response));

        var result = createFlow.execute(request).block();

        assertThat(result).isEqualTo(response);
        then(client).should().criarAssunto(request);
    }

    @Test
    @DisplayName("update flow deve verificar existencia antes de atualizar")
    void updateFlow_deveVerificarExistenciaAntesDeAtualizar() {
        var request = new AssuntoRequest("DDD");
        var existing = new AssuntoResponse(1, "Arquitetura");
        var updated = new AssuntoResponse(1, "DDD");
        given(client.buscarAssuntoPorId(1)).willReturn(Mono.just(existing));
        given(client.atualizarAssunto(1, request)).willReturn(Mono.just(updated));

        var result = updateFlow.execute(1, request).block();

        assertThat(result).isEqualTo(updated);
        then(client).should().buscarAssuntoPorId(1);
        then(client).should().atualizarAssunto(1, request);
    }

    @Test
    @DisplayName("delete flow deve verificar existencia antes de excluir")
    void deleteFlow_deveVerificarExistenciaAntesDeExcluir() {
        var existing = new AssuntoResponse(1, "Arquitetura");
        given(client.buscarAssuntoPorId(1)).willReturn(Mono.just(existing));
        given(client.excluirAssunto(1)).willReturn(Mono.empty());

        deleteFlow.execute(1).block();

        then(client).should().buscarAssuntoPorId(1);
        then(client).should().excluirAssunto(1);
    }
}