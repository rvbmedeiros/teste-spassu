package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.repository.AssuntoRepository;
import com.spassu.livros.microservice.infrastructure.persistence.mapper.AssuntoEntityMapper;
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
public class AssuntoRepositoryImpl implements AssuntoRepository {

    private final AssuntoJpaRepository jpa;
    private final AssuntoEntityMapper  mapper;

    @Override
    @Transactional
    public Assunto save(Assunto assunto) {
        if (assunto.isNovo()) {
            return mapper.toDomain(jpa.save(mapper.toEntity(assunto)));
        }
        var entity = jpa.findById(assunto.getCodAs())
                .orElseThrow(() -> new EntityNotFoundException("Assunto", assunto.getCodAs()));
        mapper.updateEntity(entity, assunto);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Assunto> findById(Integer id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Assunto> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!jpa.existsById(id)) {
            throw new EntityNotFoundException("Assunto", id);
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
    public Set<Assunto> findAllByIds(Set<Integer> ids) {
        return jpa.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }
}
