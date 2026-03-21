package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AssuntoEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AutorEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAssuntoEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAssuntoId;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAutorEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAutorId;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroEntity;
import com.spassu.livros.microservice.infrastructure.persistence.mapper.LivroEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LivroRepositoryImplTest {

    @Mock
    private LivroJpaRepository livroJpa;

    @Mock
    private AutorJpaRepository autorJpa;

    @Mock
    private AssuntoJpaRepository assuntoJpa;

    @Mock
    private LivroEntityMapper mapper;

    @InjectMocks
    private LivroRepositoryImpl repository;

    @Test
    @DisplayName("save quando novo deve sincronizar tabelas de associacao")
    void save_quandoNovo_deveSincronizarTabelasDeAssociacao() {
        Livro livro = livro(null, Set.of(1, 2), Set.of(11, 12));
        LivroEntity transientEntity = LivroEntity.builder()
                .titulo("DDD")
                .autores(new java.util.HashSet<>())
                .assuntos(new java.util.HashSet<>())
                .build();
        LivroEntity persisted = LivroEntity.builder()
                .codL(100)
                .titulo("DDD")
                .autores(new java.util.HashSet<>())
                .assuntos(new java.util.HashSet<>())
                .build();
        Livro domain = livro(100, Set.of(1, 2), Set.of(11, 12));

        given(mapper.toEntity(livro)).willReturn(transientEntity);
        given(livroJpa.saveAndFlush(transientEntity)).willReturn(persisted);
        given(autorJpa.getReferenceById(1)).willReturn(AutorEntity.builder().codAu(1).build());
        given(autorJpa.getReferenceById(2)).willReturn(AutorEntity.builder().codAu(2).build());
        given(assuntoJpa.getReferenceById(11)).willReturn(AssuntoEntity.builder().codAs(11).build());
        given(assuntoJpa.getReferenceById(12)).willReturn(AssuntoEntity.builder().codAs(12).build());
        given(livroJpa.save(persisted)).willReturn(persisted);
        given(mapper.toDomain(persisted)).willReturn(domain);

        Livro result = repository.save(livro);

        assertThat(result).isEqualTo(domain);
        assertThat(persisted.getAutores())
                .extracting(link -> link.getId().getAutorCodAu())
                .containsExactlyInAnyOrder(1, 2);
        assertThat(persisted.getAssuntos())
                .extracting(link -> link.getId().getAssuntoCodAs())
                .containsExactlyInAnyOrder(11, 12);
    }

    @Test
    @DisplayName("save quando existente deve atualizar escalares e reconciliar associacoes")
    void save_quandoExistente_deveAtualizarEscalaresEReconciliarAssociacoes() {
        Livro livro = livro(50, Set.of(2, 3), Set.of(20, 30));
        LivroEntity entity = LivroEntity.builder()
                .codL(50)
                .titulo("Legado")
                .autores(new java.util.HashSet<>())
                .assuntos(new java.util.HashSet<>())
                .build();
        entity.getAutores().add(new LivroAutorEntity(new LivroAutorId(50, 1), entity, AutorEntity.builder().codAu(1).build()));
        entity.getAutores().add(new LivroAutorEntity(new LivroAutorId(50, 2), entity, AutorEntity.builder().codAu(2).build()));
        entity.getAssuntos().add(new LivroAssuntoEntity(new LivroAssuntoId(50, 10), entity, AssuntoEntity.builder().codAs(10).build()));
        entity.getAssuntos().add(new LivroAssuntoEntity(new LivroAssuntoId(50, 20), entity, AssuntoEntity.builder().codAs(20).build()));

        given(livroJpa.findWithAssociationsByCodL(50)).willReturn(Optional.of(entity));
        given(autorJpa.getReferenceById(3)).willReturn(AutorEntity.builder().codAu(3).build());
        given(assuntoJpa.getReferenceById(30)).willReturn(AssuntoEntity.builder().codAs(30).build());
        given(livroJpa.save(entity)).willReturn(entity);
        given(mapper.toDomain(entity)).willReturn(livro);

        Livro result = repository.save(livro);

        assertThat(result).isEqualTo(livro);
        then(mapper).should().updateScalars(entity, livro);
        assertThat(entity.getAutores())
                .extracting(link -> link.getId().getAutorCodAu())
                .containsExactlyInAnyOrder(2, 3);
        assertThat(entity.getAssuntos())
                .extracting(link -> link.getId().getAssuntoCodAs())
                .containsExactlyInAnyOrder(20, 30);
    }

    @Test
    @DisplayName("save quando livro existente nao encontrado deve lancar exception")
    void save_quandoLivroExistenteNaoEncontrado_deveLancarException() {
        Livro livro = livro(99, Set.of(), Set.of());
        given(livroJpa.findWithAssociationsByCodL(99)).willReturn(Optional.empty());

        assertThatThrownBy(() -> repository.save(livro))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Livro");
    }

    @Test
    @DisplayName("findById deve mapear entidade encontrada")
    void findById_deveMapearEntidadeEncontrada() {
        LivroEntity entity = LivroEntity.builder().codL(1).titulo("DDD").build();
        Livro livro = livro(1, Set.of(), Set.of());
        given(livroJpa.findWithAssociationsByCodL(1)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(livro);

        assertThat(repository.findById(1)).contains(livro);
    }

    @Test
    @DisplayName("findAll deve mapear pagina de livros")
    void findAll_deveMapearPaginaDeLivros() {
        LivroEntity entity = LivroEntity.builder().codL(8).titulo("TDD").build();
        Livro livro = livro(8, Set.of(), Set.of());
        given(livroJpa.findAll(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(entity)));
        given(mapper.toDomain(entity)).willReturn(livro);

        var result = repository.findAll(PageRequest.of(0, 20));

        assertThat(result.getContent()).containsExactly(livro);
    }

    @Test
    @DisplayName("deleteById quando livro existe deve remover")
    void deleteById_quandoLivroExiste_deveRemover() {
        given(livroJpa.existsById(12)).willReturn(true);

        repository.deleteById(12);

        then(livroJpa).should().deleteById(12);
    }

    @Test
    @DisplayName("deleteById quando livro nao existe deve lancar exception")
    void deleteById_quandoLivroNaoExiste_deveLancarException() {
        given(livroJpa.existsById(12)).willReturn(false);

        assertThatThrownBy(() -> repository.deleteById(12))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Livro");
    }

    @Test
    @DisplayName("existsById deve delegar ao repositório JPA")
    void existsById_deveDelegarAoRepositorioJpa() {
        given(livroJpa.existsById(4)).willReturn(true);

        assertThat(repository.existsById(4)).isTrue();
    }

    private Livro livro(Integer codL, Set<Integer> autoresIds, Set<Integer> assuntosIds) {
        return Livro.builder()
                .codL(codL)
                .titulo("DDD")
                .editora("Addison-Wesley")
                .edicao(1)
                .anoPublicacao("2003")
                .valor(new BigDecimal("99.90"))
                .autores(autoresIds.stream().map(id -> Autor.builder().codAu(id).build()).collect(java.util.stream.Collectors.toSet()))
                .assuntos(assuntosIds.stream().map(id -> Assunto.builder().codAs(id).build()).collect(java.util.stream.Collectors.toSet()))
                .build();
    }
}