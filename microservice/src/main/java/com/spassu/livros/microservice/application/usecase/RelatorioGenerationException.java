package com.spassu.livros.microservice.application.usecase;

public class RelatorioGenerationException extends RuntimeException {
    public RelatorioGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
