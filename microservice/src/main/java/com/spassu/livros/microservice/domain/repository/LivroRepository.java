package com.spassu.livros.microservice.domain.repository;

import com.spassu.livros.microservice.domain.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Domain repository contract — defined in the domain layer.
 * Implementation lives in infrastructure/persistence.
 */
public interface LivroRepository {
    Livro save(Livro livro);
    Optional<Livro> findById(Integer id);
    Page<Livro> findAll(Pageable pageable);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}
