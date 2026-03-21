package com.spassu.livros.microservice.domain.model;

import java.util.HashSet;
import java.util.Set;

import lombok.*;

/**
 * Domain entity — pure Java, no JPA.
 * Persistence handled by AutorEntity in the infrastructure layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Autor {

    @EqualsAndHashCode.Include
    private Integer codAu;

    @Builder.Default
    private Set<Livro> livros = new HashSet<>();

    private String nome;

    public boolean isNovo() {
        return this.codAu == null;
    }

}
