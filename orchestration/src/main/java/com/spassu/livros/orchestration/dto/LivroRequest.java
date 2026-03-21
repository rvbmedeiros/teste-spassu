package com.spassu.livros.orchestration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record LivroRequest(
        @NotBlank @Size(max = 40) String titulo,
        @NotBlank @Size(max = 40) String editora,
        @NotNull @Min(1) Integer edicao,
        @NotBlank @Pattern(regexp = "\\d{4}") String anoPublicacao,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        @NotEmpty Set<Integer> autoresCodAu,
        @NotEmpty Set<Integer> assuntosCodAs
) {}
