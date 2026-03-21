package com.spassu.livros.orchestration.client;

import com.spassu.livros.orchestration.dto.*;
import com.spassu.livros.orchestration.dto.rpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Typed RabbitTemplate wrapper for all request-reply calls to the microservice.
 */
@Slf4j
@Component
public class MicroserviceClient {

    private static final String RPC_EXCHANGE = "livros.rpc";
    private static final String RPC_LIVROS_CRIAR = "rpc.livros.criar";
    private static final String RPC_LIVROS_LISTAR = "rpc.livros.listar";
    private static final String RPC_LIVROS_BUSCAR = "rpc.livros.buscar";
    private static final String RPC_LIVROS_ATUALIZAR = "rpc.livros.atualizar";
    private static final String RPC_LIVROS_EXCLUIR = "rpc.livros.excluir";
    private static final String RPC_AUTORES_CRIAR = "rpc.autores.criar";
    private static final String RPC_AUTORES_LISTAR = "rpc.autores.listar";
    private static final String RPC_AUTORES_BUSCAR = "rpc.autores.buscar";
    private static final String RPC_AUTORES_ATUALIZAR = "rpc.autores.atualizar";
    private static final String RPC_AUTORES_EXCLUIR = "rpc.autores.excluir";
    private static final String RPC_ASSUNTOS_CRIAR = "rpc.assuntos.criar";
    private static final String RPC_ASSUNTOS_LISTAR = "rpc.assuntos.listar";
    private static final String RPC_ASSUNTOS_BUSCAR = "rpc.assuntos.buscar";
    private static final String RPC_ASSUNTOS_ATUALIZAR = "rpc.assuntos.atualizar";
    private static final String RPC_ASSUNTOS_EXCLUIR = "rpc.assuntos.excluir";
    private static final String RPC_RELATORIO_GERAR = "rpc.relatorio.gerar";

    private final RabbitTemplate rabbitTemplate;

    public MicroserviceClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─── Livro ────────────────────────────────────────────────────────────────

    public Mono<LivroResponse> criarLivro(LivroRequest request) {
        log.info("Calling microservice RPC to create livro. titulo={}", request.titulo());
        return rpc(RPC_LIVROS_CRIAR, request, new ParameterizedTypeReference<RpcResponse<LivroResponse>>() {})
                .doOnSuccess(resp -> log.info("Livro created via RPC. codL={}", resp.codL()))
                .doOnError(ex -> log.error("Error creating livro via RPC. titulo={}", request.titulo(), ex));
    }

    public Mono<List<LivroResponse>> listarLivros(int page, int size) {
        log.debug("Calling microservice RPC to list livros. page={}, size={}", page, size);
        return rpc(RPC_LIVROS_LISTAR, new PageRequest(page, size),
                new ParameterizedTypeReference<RpcResponse<List<LivroResponse>>>() {})
                .doOnSuccess(resp -> log.debug("Livros listing via RPC completed. size={}", resp.size()))
                .doOnError(ex -> log.error("Error listing livros via RPC. page={}, size={}", page, size, ex));
    }

    public Mono<LivroResponse> buscarLivroPorId(Integer id) {
        log.debug("Calling microservice RPC to fetch livro by id. id={}", id);
        return rpc(RPC_LIVROS_BUSCAR, new IdRequest(id), new ParameterizedTypeReference<RpcResponse<LivroResponse>>() {})
                .doOnSuccess(resp -> log.debug("Livro fetched via RPC. id={}", resp.codL()))
                .doOnError(ex -> log.error("Error fetching livro via RPC. id={}", id, ex));
    }

    public Mono<LivroResponse> atualizarLivro(Integer id, LivroRequest request) {
        log.info("Calling microservice RPC to update livro. id={}", id);
        return rpc(RPC_LIVROS_ATUALIZAR, new AtualizarLivroMessage(id, request),
                new ParameterizedTypeReference<RpcResponse<LivroResponse>>() {})
                .doOnSuccess(resp -> log.info("Livro updated via RPC. id={}", resp.codL()))
                .doOnError(ex -> log.error("Error updating livro via RPC. id={}", id, ex));
    }

    public Mono<Void> excluirLivro(Integer id) {
        log.info("Calling microservice RPC to delete livro. id={}", id);
        return rpcNoPayload(RPC_LIVROS_EXCLUIR, new IdRequest(id), new ParameterizedTypeReference<RpcResponse<Void>>() {})
                .doOnSuccess(resp -> log.info("Livro deleted via RPC. id={}", id))
                .doOnError(ex -> log.error("Error deleting livro via RPC. id={}", id, ex));
    }

    // ─── Autor ────────────────────────────────────────────────────────────────

    public Mono<List<AutorResponse>> listarAutores() {
        log.debug("Calling microservice RPC to list autores");
        return rpc(RPC_AUTORES_LISTAR, new PageRequest(0, 20),
                new ParameterizedTypeReference<RpcResponse<List<AutorResponse>>>() {})
                .doOnSuccess(resp -> log.debug("Autores listing via RPC completed. size={}", resp.size()))
                .doOnError(ex -> log.error("Error listing autores via RPC", ex));
    }

    public Mono<AutorResponse> buscarAutorPorId(Integer id) {
        log.debug("Calling microservice RPC to fetch autor by id. id={}", id);
        return rpc(RPC_AUTORES_BUSCAR, new IdRequest(id), new ParameterizedTypeReference<RpcResponse<AutorResponse>>() {})
                .doOnSuccess(resp -> log.debug("Autor fetched via RPC. id={}", resp.codAu()))
                .doOnError(ex -> log.error("Error fetching autor via RPC. id={}", id, ex));
    }

    public Mono<AutorResponse> criarAutor(AutorRequest request) {
        log.info("Calling microservice RPC to create autor. nome={}", request.nome());
        return rpc(RPC_AUTORES_CRIAR, request, new ParameterizedTypeReference<RpcResponse<AutorResponse>>() {})
                .doOnSuccess(resp -> log.info("Autor created via RPC. id={}", resp.codAu()))
                .doOnError(ex -> log.error("Error creating autor via RPC. nome={}", request.nome(), ex));
    }

    public Mono<AutorResponse> atualizarAutor(Integer id, AutorRequest request) {
        log.info("Calling microservice RPC to update autor. id={}", id);
        return rpc(RPC_AUTORES_ATUALIZAR, new AtualizarAutorMessage(id, request),
                new ParameterizedTypeReference<RpcResponse<AutorResponse>>() {})
                .doOnSuccess(resp -> log.info("Autor updated via RPC. id={}", resp.codAu()))
                .doOnError(ex -> log.error("Error updating autor via RPC. id={}", id, ex));
    }

    public Mono<Void> excluirAutor(Integer id) {
        log.info("Calling microservice RPC to delete autor. id={}", id);
        return rpcNoPayload(RPC_AUTORES_EXCLUIR, new IdRequest(id), new ParameterizedTypeReference<RpcResponse<Void>>() {})
                .doOnSuccess(resp -> log.info("Autor deleted via RPC. id={}", id))
                .doOnError(ex -> log.error("Error deleting autor via RPC. id={}", id, ex));
    }

    // ─── Assunto ──────────────────────────────────────────────────────────────

    public Mono<List<AssuntoResponse>> listarAssuntos() {
        log.debug("Calling microservice RPC to list assuntos");
        return rpc(RPC_ASSUNTOS_LISTAR, new PageRequest(0, 20),
                new ParameterizedTypeReference<RpcResponse<List<AssuntoResponse>>>() {})
                .doOnSuccess(resp -> log.debug("Assuntos listing via RPC completed. size={}", resp.size()))
                .doOnError(ex -> log.error("Error listing assuntos via RPC", ex));
    }

    public Mono<AssuntoResponse> buscarAssuntoPorId(Integer id) {
        log.debug("Calling microservice RPC to fetch assunto by id. id={}", id);
        return rpc(RPC_ASSUNTOS_BUSCAR, new IdRequest(id), new ParameterizedTypeReference<RpcResponse<AssuntoResponse>>() {})
                .doOnSuccess(resp -> log.debug("Assunto fetched via RPC. id={}", resp.codAs()))
                .doOnError(ex -> log.error("Error fetching assunto via RPC. id={}", id, ex));
    }

    public Mono<AssuntoResponse> criarAssunto(AssuntoRequest request) {
        log.info("Calling microservice RPC to create assunto. descricao={}", request.descricao());
        return rpc(RPC_ASSUNTOS_CRIAR, request, new ParameterizedTypeReference<RpcResponse<AssuntoResponse>>() {})
                .doOnSuccess(resp -> log.info("Assunto created via RPC. id={}", resp.codAs()))
                .doOnError(ex -> log.error("Error creating assunto via RPC. descricao={}", request.descricao(), ex));
    }

    public Mono<AssuntoResponse> atualizarAssunto(Integer id, AssuntoRequest request) {
        log.info("Calling microservice RPC to update assunto. id={}", id);
        return rpc(RPC_ASSUNTOS_ATUALIZAR, new AtualizarAssuntoMessage(id, request),
                new ParameterizedTypeReference<RpcResponse<AssuntoResponse>>() {})
                .doOnSuccess(resp -> log.info("Assunto updated via RPC. id={}", resp.codAs()))
                .doOnError(ex -> log.error("Error updating assunto via RPC. id={}", id, ex));
    }

    public Mono<Void> excluirAssunto(Integer id) {
        log.info("Calling microservice RPC to delete assunto. id={}", id);
        return rpcNoPayload(RPC_ASSUNTOS_EXCLUIR, new IdRequest(id), new ParameterizedTypeReference<RpcResponse<Void>>() {})
                .doOnSuccess(resp -> log.info("Assunto deleted via RPC. id={}", id))
                .doOnError(ex -> log.error("Error deleting assunto via RPC. id={}", id, ex));
    }

    // ─── Relatório ────────────────────────────────────────────────────────────

    public Mono<byte[]> gerarRelatorio() {
        log.info("Calling microservice RPC to generate relatorio PDF");
        return rpc(RPC_RELATORIO_GERAR, new EmptyRequest(), new ParameterizedTypeReference<RpcResponse<byte[]>>() {})
                .doOnSuccess(resp -> log.info("Relatorio PDF generated via RPC. bytes={}", resp == null ? 0 : resp.length))
                .doOnError(ex -> log.error("Error generating relatorio PDF via RPC", ex));
    }

    private <T> Mono<T> rpc(String routingKey, Object request, ParameterizedTypeReference<RpcResponse<T>> type) {
        return rpcRaw(routingKey, request, type)
                .flatMap(response -> {
                    if (!response.success()) {
                        return Mono.error(new MicroserviceRpcException(
                                response.errorCode(),
                                response.message(),
                                response.httpStatus()));
                    }
                    return Mono.justOrEmpty(response.payload())
                            .switchIfEmpty(Mono.error(new IllegalStateException("RPC response payload is empty")));
                });
    }

    private Mono<Void> rpcNoPayload(String routingKey, Object request, ParameterizedTypeReference<RpcResponse<Void>> type) {
        return rpcRaw(routingKey, request, type)
                .flatMap(response -> {
                    if (!response.success()) {
                        return Mono.error(new MicroserviceRpcException(
                                response.errorCode(),
                                response.message(),
                                response.httpStatus()));
                    }
                    return Mono.empty();
                });
    }

    private <T> Mono<RpcResponse<T>> rpcRaw(String routingKey, Object request, ParameterizedTypeReference<RpcResponse<T>> type) {
        return Mono.fromCallable(() -> rabbitTemplate.convertSendAndReceiveAsType(RPC_EXCHANGE, routingKey, request, type))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> response == null
                        ? Mono.error(new IllegalStateException("RPC response is null"))
                        : Mono.just(response));
    }
}
