package com.spassu.livros.microservice.domain.model;

import java.util.HashSet;
import java.util.Set;

import lombok.*;

/**
 * Domain entity — pure Java, no JPA.
 * Persistence handled by AssuntoEntity in the infrastructure layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Assunto {

    @EqualsAndHashCode.Include
    private Integer codAs;

    @Builder.Default
    private Set<Livro> livros = new HashSet<>();

    private String descricao;

    public boolean isNovo() {
        return this.codAs == null;
    }

}
