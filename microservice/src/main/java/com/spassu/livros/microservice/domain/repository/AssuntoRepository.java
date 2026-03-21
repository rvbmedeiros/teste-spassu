package com.spassu.livros.microservice.domain.repository;

import com.spassu.livros.microservice.domain.model.Assunto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface AssuntoRepository {
    Assunto save(Assunto assunto);
    Optional<Assunto> findById(Integer id);
    Page<Assunto> findAll(Pageable pageable);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    Set<Assunto> findAllByIds(Set<Integer> ids);
}
