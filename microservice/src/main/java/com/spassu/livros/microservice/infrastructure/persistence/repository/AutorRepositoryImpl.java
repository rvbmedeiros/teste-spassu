package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.repository.AutorRepository;
import com.spassu.livros.microservice.infrastructure.persistence.mapper.AutorEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AutorRepositoryImpl implements AutorRepository {

    private final AutorJpaRepository jpa;
    private final AutorEntityMapper  mapper;

    @Override
    @Transactional
    public Autor save(Autor autor) {
        if (autor.isNovo()) {
            return mapper.toDomain(jpa.save(mapper.toEntity(autor)));
        }
        var entity = jpa.findById(autor.getCodAu())
                .orElseThrow(() -> new EntityNotFoundException("Autor", autor.getCodAu()));
        mapper.updateEntity(entity, autor);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Autor> findById(Integer id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Autor> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!jpa.existsById(id)) {
            throw new EntityNotFoundException("Autor", id);
        }
        jpa.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return jpa.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Autor> findAllByIds(Set<Integer> ids) {
        return jpa.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }
}
