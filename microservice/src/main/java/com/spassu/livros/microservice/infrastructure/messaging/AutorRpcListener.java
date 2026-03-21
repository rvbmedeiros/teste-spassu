package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.dto.rpc.AtualizarAutorMessage;
import com.spassu.livros.microservice.application.dto.rpc.IdRequest;
import com.spassu.livros.microservice.application.dto.rpc.PageRequest;
import com.spassu.livros.microservice.application.dto.rpc.RpcResponse;
import com.spassu.livros.microservice.application.usecase.AutorUseCase;
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
public class AutorRpcListener {

    private final AutorUseCase useCase;

    @RabbitListener(queues = RabbitMqConfig.RPC_AUTORES_CRIAR)
    public RpcResponse<AutorResponse> criar(AutorRequest request) {
        try {
            var response = useCase.criar(request);
            log.info("RPC autor criado. id={}", response.getCodAu());
            return RpcResponse.ok(response, 201);
        } catch (DataIntegrityViolationException ex) {
            return conflict(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_AUTORES_LISTAR)
    public RpcResponse<List<AutorResponse>> listar(PageRequest request) {
        try {
            var pageable = org.springframework.data.domain.PageRequest.of(request.page(), request.size());
            var response = useCase.listar(pageable).getContent();
            log.debug("RPC lista de autores retornada. size={}", response.size());
            return RpcResponse.ok(response, 200);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_AUTORES_BUSCAR)
    public RpcResponse<AutorResponse> buscar(IdRequest request) {
        try {
            var response = useCase.buscarPorId(request.id());
            log.debug("RPC autor encontrado. id={}", request.id());
            return RpcResponse.ok(response, 200);
        } catch (EntityNotFoundException ex) {
            return notFound(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_AUTORES_ATUALIZAR)
    public RpcResponse<AutorResponse> atualizar(AtualizarAutorMessage message) {
        try {
            var response = useCase.atualizar(message.id(), message.request());
            log.info("RPC autor atualizado. id={}", response.getCodAu());
            return RpcResponse.ok(response, 200);
        } catch (EntityNotFoundException ex) {
            return notFound(ex);
        } catch (DataIntegrityViolationException ex) {
            return conflict(ex);
        } catch (Exception ex) {
            return internalError(ex);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.RPC_AUTORES_EXCLUIR)
    public RpcResponse<Void> excluir(IdRequest request) {
        try {
            useCase.excluir(request.id());
            log.info("RPC autor excluido. id={}", request.id());
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
        log.error("RPC autor not found. reason={}", ex.getMessage(), ex);
        return RpcResponse.error("NOT_FOUND", ex.getMessage(), 404);
    }

    private <T> RpcResponse<T> conflict(DataIntegrityViolationException ex) {
        log.error("RPC autor conflict. reason={}", ex.getMessage(), ex);
        return RpcResponse.error("CONFLICT", ex.getMessage(), 409);
    }

    private <T> RpcResponse<T> internalError(Exception ex) {
        log.error("RPC autor internal error", ex);
        return RpcResponse.error("INTERNAL_ERROR", ex.getMessage(), 500);
    }
}
