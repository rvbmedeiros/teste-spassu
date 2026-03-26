package com.spassu.livros.microservice.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Aggregate root — pure domain object, no framework annotations.
 * JPA persistence is handled by LivroEntity in the infrastructure layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Livro {

    @EqualsAndHashCode.Include
    private Integer codL;

    private String titulo;
    private String editora;
    private Integer edicao;
    private String anoPublicacao;
    private BigDecimal valor;

    @Builder.Default
    private Set<Autor> autores = new HashSet<>();

    @Builder.Default
    private Set<Assunto> assuntos = new HashSet<>();

    // ─── Domain behaviours ────────────────────────────────────────────────────

    public void adicionarAutor(Autor autor) {
        this.autores.add(autor);
    }

    public void removerAutor(Autor autor) {
        this.autores.remove(autor);
    }

    public void adicionarAssunto(Assunto assunto) {
        this.assuntos.add(assunto);
    }

    public void removerAssunto(Assunto assunto) {
        this.assuntos.remove(assunto);
    }

    public boolean isNovo() {
        return this.codL == null;
    }
}
