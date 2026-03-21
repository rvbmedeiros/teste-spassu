package com.spassu.livros.microservice.application.dto;

import lombok.*;

import java.math.BigDecimal;

/** Flattened row from vw_relatorio_livros, used by JasperReports. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RelatorioLivroRow {
    private String     autorNome;
    private String     livroTitulo;
    private String     livroEditora;
    private Integer    livroEdicao;
    private String     livroAnoPublicacao;
    private BigDecimal livroValor;
    private String     assuntos;
}
