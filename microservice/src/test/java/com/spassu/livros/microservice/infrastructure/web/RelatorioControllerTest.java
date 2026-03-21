package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.usecase.RelatorioUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RelatorioControllerTest {

    @Mock
    private RelatorioUseCase useCase;

    @InjectMocks
    private RelatorioController controller;

    @Test
    @DisplayName("gerarPdf deve retornar payload PDF com headers corretos")
    void gerarPdf_deveRetornarPayloadPdfComHeadersCorretos() {
        var bytes = new byte[] {1, 2, 3, 4};
        given(useCase.gerarPdf()).willReturn(bytes);

        var response = controller.gerarPdf();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"relatorio-livros.pdf\"");
        assertThat(response.getBody()).isEqualTo(bytes);
        then(useCase).should().gerarPdf();
    }
}
