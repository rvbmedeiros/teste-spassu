package com.spassu.livros.orchestration.dto;

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
        LivroRequest request = new LivroRequest(
                "Domain-Driven Design",
                "",
                1,
                "2024",
                new BigDecimal("99.90"),
                Set.of(1),
                Set.of(1)
        );

        var violations = validator.validate(request);

        assertThat(violations)
            .anyMatch(v -> "editora".equals(v.getPropertyPath().toString()));
    }
}
