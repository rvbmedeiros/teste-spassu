package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.application.dto.rpc.EmptyRequest;
import com.spassu.livros.microservice.application.dto.rpc.RpcResponse;
import com.spassu.livros.microservice.application.usecase.RelatorioUseCase;
import com.spassu.livros.microservice.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelatorioRpcListener {

    private final RelatorioUseCase useCase;

    @RabbitListener(queues = RabbitMqConfig.RPC_RELATORIO_GERAR)
    public RpcResponse<byte[]> gerarPdf(EmptyRequest ignored) {
        try {
            var bytes = useCase.gerarPdf();
            log.info("RPC relatorio gerado. bytes={}", bytes == null ? 0 : bytes.length);
            return RpcResponse.ok(bytes, 200);
        } catch (Exception ex) {
            log.error("RPC relatorio internal error", ex);
            return RpcResponse.error("INTERNAL_ERROR", ex.getMessage(), 500);
        }
    }
}
