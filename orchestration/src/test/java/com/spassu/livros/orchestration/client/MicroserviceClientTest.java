package com.spassu.livros.orchestration.client;

import com.spassu.livros.orchestration.dto.*;
import com.spassu.livros.orchestration.dto.rpc.AtualizarAssuntoMessage;
import com.spassu.livros.orchestration.dto.rpc.AtualizarAutorMessage;
import com.spassu.livros.orchestration.dto.rpc.AtualizarLivroMessage;
import com.spassu.livros.orchestration.dto.rpc.EmptyRequest;
import com.spassu.livros.orchestration.dto.rpc.IdRequest;
import com.spassu.livros.orchestration.dto.rpc.PageRequest;
import com.spassu.livros.orchestration.dto.rpc.RpcResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MicroserviceClientTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("deve executar operacoes RPC de livro com sucesso")
    void deveExecutarOperacoesRpcDeLivroComSucesso() {
        var client = new MicroserviceClient(rabbitTemplate);
        var request = livroRequest();
        var response = livroResponse();

        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.criar"), eq(request), any()))
                .willReturn(new RpcResponse<>(true, response, null, null, 201));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.listar"), any(PageRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, List.of(response), null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.buscar"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, response, null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.atualizar"), any(AtualizarLivroMessage.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, response, null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.excluir"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, null, null, null, 204));

        StepVerifier.create(client.criarLivro(request))
                .expectNext(response)
                .verifyComplete();
        StepVerifier.create(client.listarLivros(0, 20))
                .expectNext(List.of(response))
                .verifyComplete();
        StepVerifier.create(client.buscarLivroPorId(1))
                .expectNext(response)
                .verifyComplete();
        StepVerifier.create(client.atualizarLivro(1, request))
                .expectNext(response)
                .verifyComplete();
        StepVerifier.create(client.excluirLivro(1))
                .verifyComplete();

        then(rabbitTemplate).should().convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.criar"), eq(request), any());
        then(rabbitTemplate).should().convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.listar"), any(PageRequest.class), anyTypeRef());
        then(rabbitTemplate).should().convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.buscar"), any(IdRequest.class), anyTypeRef());
        then(rabbitTemplate).should().convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.atualizar"), any(AtualizarLivroMessage.class), anyTypeRef());
        then(rabbitTemplate).should().convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.excluir"), any(IdRequest.class), anyTypeRef());
    }

    @Test
    @DisplayName("deve executar operacoes RPC de autor com sucesso")
    void deveExecutarOperacoesRpcDeAutorComSucesso() {
        var client = new MicroserviceClient(rabbitTemplate);
        var request = new AutorRequest("Martin Fowler");
        var response = new AutorResponse(1, "Martin Fowler");

        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.listar"), any(PageRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, List.of(response), null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.buscar"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, response, null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.criar"), eq(request), any()))
                .willReturn(new RpcResponse<>(true, response, null, null, 201));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.atualizar"), any(AtualizarAutorMessage.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, response, null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.excluir"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, null, null, null, 204));

        StepVerifier.create(client.listarAutores()).expectNext(List.of(response)).verifyComplete();
        StepVerifier.create(client.buscarAutorPorId(1)).expectNext(response).verifyComplete();
        StepVerifier.create(client.criarAutor(request)).expectNext(response).verifyComplete();
        StepVerifier.create(client.atualizarAutor(1, request)).expectNext(response).verifyComplete();
        StepVerifier.create(client.excluirAutor(1)).verifyComplete();
    }

    @Test
    @DisplayName("deve executar operacoes RPC de assunto com sucesso")
    void deveExecutarOperacoesRpcDeAssuntoComSucesso() {
        var client = new MicroserviceClient(rabbitTemplate);
        var request = new AssuntoRequest("Arquitetura");
        var response = new AssuntoResponse(1, "Arquitetura");

        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.assuntos.listar"), any(PageRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, List.of(response), null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.assuntos.buscar"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, response, null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.assuntos.criar"), eq(request), any()))
                .willReturn(new RpcResponse<>(true, response, null, null, 201));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.assuntos.atualizar"), any(AtualizarAssuntoMessage.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, response, null, null, 200));
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.assuntos.excluir"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, null, null, null, 204));

        StepVerifier.create(client.listarAssuntos()).expectNext(List.of(response)).verifyComplete();
        StepVerifier.create(client.buscarAssuntoPorId(1)).expectNext(response).verifyComplete();
        StepVerifier.create(client.criarAssunto(request)).expectNext(response).verifyComplete();
        StepVerifier.create(client.atualizarAssunto(1, request)).expectNext(response).verifyComplete();
        StepVerifier.create(client.excluirAssunto(1)).verifyComplete();
    }

    @Test
    @DisplayName("gerarRelatorio deve retornar bytes quando RPC tem payload")
    void gerarRelatorio_deveRetornarBytesQuandoRpcTemPayload() {
        var client = new MicroserviceClient(rabbitTemplate);
        byte[] payload = "pdf".getBytes();
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.relatorio.gerar"), any(EmptyRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, payload, null, null, 200));

        StepVerifier.create(client.gerarRelatorio())
                .expectNext(payload)
                .verifyComplete();
    }

    @Test
    @DisplayName("deve retornar erro quando resposta RPC sinaliza falha")
    void deveRetornarErroQuandoRespostaRpcSinalizaFalha() {
        var client = new MicroserviceClient(rabbitTemplate);
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.buscar"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(false, null, "NOT_FOUND", "Autor nao encontrado", 404));

        StepVerifier.create(client.buscarAutorPorId(9))
                .expectErrorSatisfies(error -> {
                    var exception = (MicroserviceRpcException) error;
                    org.assertj.core.api.Assertions.assertThat(exception.getErrorCode()).isEqualTo("NOT_FOUND");
                    org.assertj.core.api.Assertions.assertThat(exception.getHttpStatus()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    @DisplayName("deve retornar erro quando resposta RPC e nula")
    void deveRetornarErroQuandoRespostaRpcENula() {
        var client = new MicroserviceClient(rabbitTemplate);
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.autores.listar"), any(PageRequest.class), anyTypeRef()))
                .willReturn(null);

        StepVerifier.create(client.listarAutores())
                .expectErrorMessage("RPC response is null")
                .verify();
    }

    @Test
    @DisplayName("deve retornar erro quando payload obrigatorio vem vazio")
    void deveRetornarErroQuandoPayloadObrigatorioVemVazio() {
        var client = new MicroserviceClient(rabbitTemplate);
        given(rabbitTemplate.convertSendAndReceiveAsType(eq("livros.rpc"), eq("rpc.livros.buscar"), any(IdRequest.class), anyTypeRef()))
                .willReturn(new RpcResponse<>(true, null, null, null, 200));

        StepVerifier.create(client.buscarLivroPorId(1))
                .expectErrorMessage("RPC response payload is empty")
                .verify();
    }

    private LivroRequest livroRequest() {
        return new LivroRequest(
                "DDD",
                "Addison Wesley",
                1,
                "2003",
                new BigDecimal("99.90"),
                Set.of(1),
                Set.of(1)
        );
    }

    private LivroResponse livroResponse() {
        return new LivroResponse(
                1,
                "DDD",
                "Addison Wesley",
                1,
                "2003",
                new BigDecimal("99.90"),
                List.of(new AutorResponse(1, "Evans")),
                List.of(new AssuntoResponse(1, "Arquitetura"))
        );
    }

        @SuppressWarnings("unchecked")
        private <T> ParameterizedTypeReference<T> anyTypeRef() {
                return any(ParameterizedTypeReference.class);
        }
}