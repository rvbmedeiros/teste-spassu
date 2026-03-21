package com.spassu.livros.microservice.application.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssuntoResponse {
    private Integer codAs;
    private String  descricao;
}
