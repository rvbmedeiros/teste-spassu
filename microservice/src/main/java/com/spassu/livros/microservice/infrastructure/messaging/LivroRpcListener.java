package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.application.dto.rpc.AtualizarLivroMessage;
import com.spassu.livros.microservice.application.dto.rpc.IdRequest;
import com.spassu.livros.microservice.application.dto.rpc.PageRequest;
import com.spassu.livros.microservice.application.dto.rpc.RpcResponse;
import com.spassu.livros.microservice.application.usecase.LivroUseCase;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LivroRpcListener {

    private final LivroUseCase useCase;

    @RabbitListener(queues = RabbitMqConfig.RPC_LIVROS_CRIAR)
    public RpcResponse<LivroResponse> criar(LivroRequest request) {
        try {
            var response = useCase.criar(request);
            log.info("RPC livro criado. id={}", response.getCodL());
            return RpcResponse.ok(response, 201);
        } catch (DataIntegrityViolationException ex) {
            return conflict(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_LIVROS_LISTAR)
    public RpcResponse<List<LivroResponse>> listar(PageRequest request) {
        try {
            var pageable = org.springframework.data.domain.PageRequest.of(request.page(), request.size());
            var response = useCase.listar(pageable).getContent();
            log.debug("RPC lista de livros retornada. size={}", response.size());
            return RpcResponse.ok(response, 200);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_LIVROS_BUSCAR)
    public RpcResponse<LivroResponse> buscar(IdRequest request) {
        try {
            var response = useCase.buscarPorId(request.id());
            log.debug("RPC livro encontrado. id={}", request.id());
            return RpcResponse.ok(response, 200);
        } catch (EntityNotFoundException ex) {
            return notFound(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_LIVROS_ATUALIZAR)
    public RpcResponse<LivroResponse> atualizar(AtualizarLivroMessage message) {
        try {
            var response = useCase.atualizar(message.id(), message.request());
            log.info("RPC livro atualizado. id={}", response.getCodL());
            return RpcResponse.ok(response, 200);
        } catch (EntityNotFoundException ex) {
            return notFound(ex);
        } catch (DataIntegrityViolationException ex) {
            return conflict(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_LIVROS_EXCLUIR)
    public RpcResponse<Void> excluir(IdRequest request) {
        try {
            useCase.excluir(request.id());
            log.info("RPC livro excluido. id={}", request.id());
            return RpcResponse.ok(null, 204);
        } catch (EntityNotFoundException ex) {
            return notFound(ex);
        } catch (DataIntegrityViolationException ex) {
            return conflict(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    private <T> RpcResponse<T> notFound(EntityNotFoundException ex) {
        log.error("RPC livro not found. reason={}", ex.getMessage(), ex);
        return RpcResponse.error("NOT_FOUND", ex.getMessage(), 404);
    }

    private <T> RpcResponse<T> conflict(DataIntegrityViolationException ex) {
        log.error("RPC livro conflict. reason={}", ex.getMessage(), ex);
        return RpcResponse.error("CONFLICT", ex.getMessage(), 409);
    }

    private <T> RpcResponse<T> internalError(Exception ex) {
        log.error("RPC livro internal error", ex);
        return RpcResponse.error("INTERNAL_ERROR", ex.getMessage(), 500);
    }
}
