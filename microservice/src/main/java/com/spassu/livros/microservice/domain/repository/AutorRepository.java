package com.spassu.livros.microservice.domain.repository;

import com.spassu.livros.microservice.domain.model.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface AutorRepository {
    Autor save(Autor autor);
    Optional<Autor> findById(Integer id);
    Page<Autor> findAll(Pageable pageable);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    Set<Autor> findAllByIds(Set<Integer> ids);
}
