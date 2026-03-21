package com.spassu.livros.microservice.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AutorRequest {
    @NotBlank @Size(max = 40)
    private String nome;
}
