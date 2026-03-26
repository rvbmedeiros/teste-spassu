package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.application.dto.rpc.EmptyRequest;
import com.spassu.livros.microservice.application.usecase.RelatorioUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RelatorioRpcListenerTest {

    @Mock
    private RelatorioUseCase useCase;

    @InjectMocks
    private RelatorioRpcListener listener;

    @Test
    @DisplayName("gerarPdf deve retornar bytes do relatorio")
    void gerarPdf_deveRetornarBytesDoRelatorio() {
        byte[] payload = "pdf".getBytes();
        given(useCase.gerarPdf()).willReturn(payload);

        var result = listener.gerarPdf(new EmptyRequest());

        assertThat(result.success()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(200);
        assertThat(result.payload()).isEqualTo(payload);
    }

    @Test
    @DisplayName("gerarPdf deve retornar internal error em falha")
    void gerarPdf_deveRetornarInternalErrorEmFalha() {
        given(useCase.gerarPdf()).willThrow(new RuntimeException("jasper down"));

        var result = listener.gerarPdf(new EmptyRequest());

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(result.httpStatus()).isEqualTo(500);
    }
}