package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AutorEntity;
import com.spassu.livros.microservice.infrastructure.persistence.mapper.AutorEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AutorRepositoryImplTest {

    @Mock
    private AutorJpaRepository jpa;

    @Mock
    private AutorEntityMapper mapper;

    @InjectMocks
    private AutorRepositoryImpl repository;

    @Test
    @DisplayName("save quando novo deve persistir entidade mapeada")
    void save_quandoNovo_devePersistirEntidadeMapeada() {
        Autor autor = Autor.builder().nome("Martin Fowler").build();
        AutorEntity entity = AutorEntity.builder().nome("Martin Fowler").build();
        AutorEntity saved = AutorEntity.builder().codAu(1).nome("Martin Fowler").build();
        Autor domain = Autor.builder().codAu(1).nome("Martin Fowler").build();

        given(mapper.toEntity(autor)).willReturn(entity);
        given(jpa.save(entity)).willReturn(saved);
        given(mapper.toDomain(saved)).willReturn(domain);

        Autor result = repository.save(autor);

        assertThat(result).isEqualTo(domain);
    }

    @Test
    @DisplayName("save quando existente deve atualizar entidade gerenciada")
    void save_quandoExistente_deveAtualizarEntidadeGerenciada() {
        Autor autor = Autor.builder().codAu(7).nome("Refactoring Guru").build();
        AutorEntity entity = AutorEntity.builder().codAu(7).nome("Antigo").build();

        given(jpa.findById(7)).willReturn(Optional.of(entity));
        given(jpa.save(entity)).willReturn(entity);
        given(mapper.toDomain(entity)).willReturn(autor);

        Autor result = repository.save(autor);

        assertThat(result).isEqualTo(autor);
        then(mapper).should().updateEntity(entity, autor);
    }

    @Test
    @DisplayName("save quando autor nao existe deve lancar exception")
    void save_quandoAutorNaoExiste_deveLancarException() {
        Autor autor = Autor.builder().codAu(9).nome("Ausente").build();
        given(jpa.findById(9)).willReturn(Optional.empty());

        assertThatThrownBy(() -> repository.save(autor))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Autor");
    }

    @Test
    @DisplayName("findById deve mapear entidade encontrada")
    void findById_deveMapearEntidadeEncontrada() {
        AutorEntity entity = AutorEntity.builder().codAu(3).nome("Kent Beck").build();
        Autor autor = Autor.builder().codAu(3).nome("Kent Beck").build();
        given(jpa.findById(3)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(autor);

        Optional<Autor> result = repository.findById(3);

        assertThat(result).contains(autor);
    }

    @Test
    @DisplayName("findAll deve mapear pagina de entidades")
    void findAll_deveMapearPaginaDeEntidades() {
        AutorEntity entity = AutorEntity.builder().codAu(5).nome("Eric Evans").build();
        Autor autor = Autor.builder().codAu(5).nome("Eric Evans").build();
        given(jpa.findAll(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(entity)));
        given(mapper.toDomain(entity)).willReturn(autor);

        var result = repository.findAll(PageRequest.of(0, 20));

        assertThat(result.getContent()).containsExactly(autor);
    }

    @Test
    @DisplayName("deleteById quando id existe deve remover")
    void deleteById_quandoIdExiste_deveRemover() {
        given(jpa.existsById(10)).willReturn(true);

        repository.deleteById(10);

        then(jpa).should().deleteById(10);
    }

    @Test
    @DisplayName("deleteById quando id nao existe deve lancar exception")
    void deleteById_quandoIdNaoExiste_deveLancarException() {
        given(jpa.existsById(10)).willReturn(false);

        assertThatThrownBy(() -> repository.deleteById(10))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Autor");
    }

    @Test
    @DisplayName("existsById deve delegar ao repositório JPA")
    void existsById_deveDelegarAoRepositorioJpa() {
        given(jpa.existsById(11)).willReturn(true);

        assertThat(repository.existsById(11)).isTrue();
    }

    @Test
    @DisplayName("findAllByIds deve mapear conjunto de autores")
    void findAllByIds_deveMapearConjuntoDeAutores() {
        AutorEntity first = AutorEntity.builder().codAu(1).nome("A").build();
        AutorEntity second = AutorEntity.builder().codAu(2).nome("B").build();
        Autor autorA = Autor.builder().codAu(1).nome("A").build();
        Autor autorB = Autor.builder().codAu(2).nome("B").build();
        given(jpa.findAllById(Set.of(1, 2))).willReturn(List.of(first, second));
        given(mapper.toDomain(first)).willReturn(autorA);
        given(mapper.toDomain(second)).willReturn(autorB);

        Set<Autor> result = repository.findAllByIds(Set.of(1, 2));

        assertThat(result).containsExactlyInAnyOrder(autorA, autorB);
    }
}