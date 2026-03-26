package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.domain.repository.LivroRepository;
import com.spassu.livros.microservice.infrastructure.persistence.entity.*;
import com.spassu.livros.microservice.infrastructure.persistence.mapper.LivroEntityMapper;
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
public class LivroRepositoryImpl implements LivroRepository {

    private final LivroJpaRepository    livroJpa;
    private final AutorJpaRepository    autorJpa;
    private final AssuntoJpaRepository  assuntoJpa;
    private final LivroEntityMapper     mapper;

    @Override
    @Transactional
    public Livro save(Livro livro) {
        LivroEntity entity;

        if (livro.isNovo()) {
            entity = mapper.toEntity(livro);
            entity = livroJpa.saveAndFlush(entity); // generate PK before syncing join tables
        } else {
            entity = livroJpa.findWithAssociationsByCodL(livro.getCodL())
                    .orElseThrow(() -> new EntityNotFoundException("Livro", livro.getCodL()));
            mapper.updateScalars(entity, livro);
        }

        syncAutores(entity, livro.getAutores().stream()
                .map(a -> a.getCodAu()).collect(Collectors.toSet()));
        syncAssuntos(entity, livro.getAssuntos().stream()
                .map(a -> a.getCodAs()).collect(Collectors.toSet()));

        return mapper.toDomain(livroJpa.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Livro> findById(Integer id) {
        return livroJpa.findWithAssociationsByCodL(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Livro> findAll(Pageable pageable) {
        // Paginated list: load scalars only, then batch-fetch associations
        return livroJpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!livroJpa.existsById(id)) {
            throw new EntityNotFoundException("Livro", id);
        }
        livroJpa.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return livroJpa.existsById(id);
    }

    // ─── Private helpers for join-table synchronisation ──────────────────────

    private void syncAutores(LivroEntity entity, Set<Integer> novoIds) {
        // Remove entries not in the new set
        entity.getAutores().removeIf(la -> !novoIds.contains(la.getId().getAutorCodAu()));

        // Add new entries
        Set<Integer> existingIds = entity.getAutores().stream()
                .map(la -> la.getId().getAutorCodAu())
                .collect(Collectors.toSet());

        novoIds.stream()
                .filter(id -> !existingIds.contains(id))
                .forEach(id -> {
                    AutorEntity autorRef = autorJpa.getReferenceById(id);
                    entity.getAutores().add(new LivroAutorEntity(
                            new LivroAutorId(entity.getCodL(), id), entity, autorRef));
                });
    }

    private void syncAssuntos(LivroEntity entity, Set<Integer> novoIds) {
        entity.getAssuntos().removeIf(la -> !novoIds.contains(la.getId().getAssuntoCodAs()));

        Set<Integer> existingIds = entity.getAssuntos().stream()
                .map(la -> la.getId().getAssuntoCodAs())
                .collect(Collectors.toSet());

        novoIds.stream()
                .filter(id -> !existingIds.contains(id))
                .forEach(id -> {
                    AssuntoEntity assuntoRef = assuntoJpa.getReferenceById(id);
                    entity.getAssuntos().add(new LivroAssuntoEntity(
                            new LivroAssuntoId(entity.getCodL(), id), entity, assuntoRef));
                });
    }
}
