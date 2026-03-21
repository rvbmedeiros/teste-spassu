package com.spassu.livros.microservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "autor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AutorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_au")
    @EqualsAndHashCode.Include
    private Integer codAu;

    @Column(name = "nome", length = 40, nullable = false)
    private String nome;
}
