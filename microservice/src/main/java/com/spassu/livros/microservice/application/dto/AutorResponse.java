package com.spassu.livros.microservice.application.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AutorResponse {
    private Integer codAu;
    private String  nome;
}
