package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.flow.GetRelatorioFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RelatorioControllerTest {

    @Mock
    private GetRelatorioFlow flow;

    @InjectMocks
    private RelatorioController controller;

    @Test
    @DisplayName("gerarPdf deve retornar resposta com content type e content disposition")
    void gerarPdf_deveRetornarRespostaComContentTypeEContentDisposition() {
        var bytes = new byte[] {1, 2, 3};
        given(flow.execute()).willReturn(Mono.just(bytes));

        var response = controller.gerarPdf().block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"relatorio-livros.pdf\"");
        assertThat(response.getBody()).isEqualTo(bytes);
        then(flow).should().execute();
    }
}
