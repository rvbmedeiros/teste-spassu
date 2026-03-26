package com.spassu.livros.microservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "livro_autor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LivroAutorEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private LivroAutorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroCodL")
    @JoinColumn(name = "livro_cod_l")
    private LivroEntity livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("autorCodAu")
    @JoinColumn(name = "autor_cod_au")
    private AutorEntity autor;
}
