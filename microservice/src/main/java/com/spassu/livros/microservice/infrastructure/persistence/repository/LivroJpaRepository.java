package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface LivroJpaRepository extends JpaRepository<LivroEntity, Integer> {

    /**
     * Eagerly fetch join tables when loading a single livro
     * (avoids N+1 for the sync logic in the repository impl).
     */
    @EntityGraph(attributePaths = {"autores.autor", "assuntos.assunto"})
    Optional<LivroEntity> findWithAssociationsByCodL(Integer codL);
}
