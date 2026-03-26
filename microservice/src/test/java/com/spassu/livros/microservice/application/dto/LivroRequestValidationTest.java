package com.spassu.livros.microservice.application.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LivroRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("validar_quandoEditoraVazia_deveSerInvalido")
    void validar_quandoEditoraVazia_deveSerInvalido() {
        LivroRequest request = LivroRequest.builder()
                .titulo("Domain-Driven Design")
                .editora("")
                .edicao(1)
                .anoPublicacao("2024")
                .valor(new BigDecimal("99.90"))
                .autoresCodAu(Set.of(1))
                .assuntosCodAs(Set.of(1))
                .build();

        var violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> "editora".equals(v.getPropertyPath().toString()));
    }
}
