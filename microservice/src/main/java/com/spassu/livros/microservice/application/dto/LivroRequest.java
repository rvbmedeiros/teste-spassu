package com.spassu.livros.microservice.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivroRequest {

    @NotBlank
    @Size(max = 40)
    private String titulo;

    @NotBlank
    @Size(max = 40)
    private String editora;

    @Positive
    private Integer edicao;

    @Pattern(regexp = "\\d{4}", message = "Ano de publicação deve conter 4 dígitos")
    private String anoPublicacao;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal valor;

    @NotEmpty
    private Set<Integer> autoresCodAu;

    @NotEmpty
    private Set<Integer> assuntosCodAs;
}
