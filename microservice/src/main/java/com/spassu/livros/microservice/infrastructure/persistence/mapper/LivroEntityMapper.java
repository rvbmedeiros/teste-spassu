package com.spassu.livros.microservice.infrastructure.persistence.mapper;

import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class (not interface) because the join-table extraction
 * requires custom logic: LivroEntity.autores is Set<LivroAutorEntity>
 * while Livro.autores is Set<Autor> — not a direct field-to-field mapping.
 */
@Mapper(componentModel = "spring")
public abstract class LivroEntityMapper {

    @Autowired
    protected AutorEntityMapper autorMapper;

    @Autowired
    protected AssuntoEntityMapper assuntoMapper;

    // ─── Entity → Domain ────────────────────────────────────────────────────

    public Livro toDomain(LivroEntity entity) {
        if (entity == null) return null;

        Set<Autor> autores = entity.getAutores().stream()
                .map(la -> autorMapper.toDomain(la.getAutor()))
                .collect(Collectors.toSet());

        Set<Assunto> assuntos = entity.getAssuntos().stream()
                .map(la -> assuntoMapper.toDomain(la.getAssunto()))
                .collect(Collectors.toSet());

        return Livro.builder()
                .codL(entity.getCodL())
                .titulo(entity.getTitulo())
                .editora(entity.getEditora())
                .edicao(entity.getEdicao())
                .anoPublicacao(entity.getAnoPublicacao())
                .valor(entity.getValor())
                .autores(autores)
                .assuntos(assuntos)
                .build();
    }

    // ─── Domain → Entity (scalar fields only; join tables synced in repo) ──

    @Mapping(target = "autores",  ignore = true)
    @Mapping(target = "assuntos", ignore = true)
    public abstract LivroEntity toEntity(Livro domain);

    /**
     * Updates scalar fields on an existing managed entity.
     * Join tables are handled separately in the repository.
     */
    @Mapping(target = "codL",     ignore = true)
    @Mapping(target = "autores",  ignore = true)
    @Mapping(target = "assuntos", ignore = true)
    public abstract void updateScalars(@MappingTarget LivroEntity entity, Livro domain);
}
