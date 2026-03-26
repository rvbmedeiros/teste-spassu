package com.spassu.livros.microservice.application.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivroResponse {
    private Integer      codL;
    private String       titulo;
    private String       editora;
    private Integer      edicao;
    private String       anoPublicacao;
    private BigDecimal   valor;
    private Set<AutorResponse>   autores;
    private Set<AssuntoResponse> assuntos;
}
