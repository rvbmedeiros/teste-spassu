package com.spassu.livros.microservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "livro_assunto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LivroAssuntoEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private LivroAssuntoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroCodL")
    @JoinColumn(name = "livro_cod_l")
    private LivroEntity livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("assuntoCodAs")
    @JoinColumn(name = "assunto_cod_as")
    private AssuntoEntity assunto;
}
