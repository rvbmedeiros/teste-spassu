package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.usecase.RelatorioGenerationException;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised error handling using RFC 9457 ProblemDetail (Spring 6+).
 * No generic catch-all — each exception type has a specific handler.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found. entity={}, id={}, message={}", ex.getEntityName(), ex.getEntityId(), ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setType(URI.create("/errors/not-found"));
        detail.setTitle("Recurso não encontrado");
        detail.setProperty("entityName", ex.getEntityName());
        detail.setProperty("entityId",   ex.getEntityId());
        detail.setProperty("timestamp",  Instant.now());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Inválido",
                        (a, b) -> a));
        log.warn("Validation error on request payload. fields={}", errors.keySet());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_CONTENT, "Falha de validação dos campos");
        detail.setType(URI.create("/errors/validation"));
        detail.setTitle("Erro de validação");
        detail.setProperty("errors",    errors);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation while processing request", ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Violação de integridade dos dados");
        detail.setType(URI.create("/errors/conflict"));
        detail.setTitle("Conflito de dados");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    @ExceptionHandler(RelatorioGenerationException.class)
    public ProblemDetail handleRelatorioGeneration(RelatorioGenerationException ex) {
        log.error("Error generating report", ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        detail.setType(URI.create("/errors/report-generation"));
        detail.setTitle("Erro ao gerar relatório");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }
}
