package com.spassu.livros.microservice.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssuntoRequest {
    @NotBlank @Size(max = 20)
    private String descricao;
}
