package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.usecase.RelatorioGenerationException;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleEntityNotFound deve retornar problem detail com status 404")
    void handleEntityNotFound_deveRetornarProblemDetailComStatus404() {
        var ex = new EntityNotFoundException("Livro", 99);

        var detail = handler.handleEntityNotFound(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getType().toString()).isEqualTo("/errors/not-found");
        assertThat(detail.getTitle()).isEqualTo("Recurso não encontrado");
        assertThat(detail.getProperties()).containsEntry("entityName", "Livro");
        assertThat(detail.getProperties()).containsEntry("entityId", 99);
        assertThat(detail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("handleDataIntegrity deve retornar problem detail com status 409")
    void handleDataIntegrity_deveRetornarProblemDetailComStatus409() {
        var ex = new DataIntegrityViolationException("FK violation");

        var detail = handler.handleDataIntegrity(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(detail.getType().toString()).isEqualTo("/errors/conflict");
        assertThat(detail.getTitle()).isEqualTo("Conflito de dados");
        assertThat(detail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("handleRelatorioGeneration deve retornar problem detail com status 500")
    void handleRelatorioGeneration_deveRetornarProblemDetailComStatus500() {
        var ex = new RelatorioGenerationException("Falha ao gerar relatório PDF", new RuntimeException("boom"));

        var detail = handler.handleRelatorioGeneration(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getType().toString()).isEqualTo("/errors/report-generation");
        assertThat(detail.getTitle()).isEqualTo("Erro ao gerar relatório");
        assertThat(detail.getDetail()).isEqualTo("Falha ao gerar relatório PDF");
        assertThat(detail.getProperties()).containsKey("timestamp");
    }
}
