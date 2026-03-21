package com.spassu.livros.microservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LivroAutorId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "livro_cod_l")
    private Integer livroCodL;

    @Column(name = "autor_cod_au")
    private Integer autorCodAu;
}
