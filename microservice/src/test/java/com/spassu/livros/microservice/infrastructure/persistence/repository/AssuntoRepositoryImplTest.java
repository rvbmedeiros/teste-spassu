package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AssuntoEntity;
import com.spassu.livros.microservice.infrastructure.persistence.mapper.AssuntoEntityMapper;
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
class AssuntoRepositoryImplTest {

    @Mock
    private AssuntoJpaRepository jpa;

    @Mock
    private AssuntoEntityMapper mapper;

    @InjectMocks
    private AssuntoRepositoryImpl repository;

    @Test
    @DisplayName("save quando novo deve persistir entidade mapeada")
    void save_quandoNovo_devePersistirEntidadeMapeada() {
        Assunto assunto = Assunto.builder().descricao("Arquitetura").build();
        AssuntoEntity entity = AssuntoEntity.builder().descricao("Arquitetura").build();
        AssuntoEntity saved = AssuntoEntity.builder().codAs(1).descricao("Arquitetura").build();
        Assunto domain = Assunto.builder().codAs(1).descricao("Arquitetura").build();

        given(mapper.toEntity(assunto)).willReturn(entity);
        given(jpa.save(entity)).willReturn(saved);
        given(mapper.toDomain(saved)).willReturn(domain);

        Assunto result = repository.save(assunto);

        assertThat(result).isEqualTo(domain);
    }

    @Test
    @DisplayName("save quando existente deve atualizar entidade gerenciada")
    void save_quandoExistente_deveAtualizarEntidadeGerenciada() {
        Assunto assunto = Assunto.builder().codAs(7).descricao("DDD").build();
        AssuntoEntity entity = AssuntoEntity.builder().codAs(7).descricao("Legado").build();

        given(jpa.findById(7)).willReturn(Optional.of(entity));
        given(jpa.save(entity)).willReturn(entity);
        given(mapper.toDomain(entity)).willReturn(assunto);

        Assunto result = repository.save(assunto);

        assertThat(result).isEqualTo(assunto);
        then(mapper).should().updateEntity(entity, assunto);
    }

    @Test
    @DisplayName("save quando assunto nao existe deve lancar exception")
    void save_quandoAssuntoNaoExiste_deveLancarException() {
        Assunto assunto = Assunto.builder().codAs(9).descricao("Ausente").build();
        given(jpa.findById(9)).willReturn(Optional.empty());

        assertThatThrownBy(() -> repository.save(assunto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Assunto");
    }

    @Test
    @DisplayName("findById deve mapear entidade encontrada")
    void findById_deveMapearEntidadeEncontrada() {
        AssuntoEntity entity = AssuntoEntity.builder().codAs(3).descricao("Testes").build();
        Assunto assunto = Assunto.builder().codAs(3).descricao("Testes").build();
        given(jpa.findById(3)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(assunto);

        Optional<Assunto> result = repository.findById(3);

        assertThat(result).contains(assunto);
    }

    @Test
    @DisplayName("findAll deve mapear pagina de entidades")
    void findAll_deveMapearPaginaDeEntidades() {
        AssuntoEntity entity = AssuntoEntity.builder().codAs(5).descricao("Refino").build();
        Assunto assunto = Assunto.builder().codAs(5).descricao("Refino").build();
        given(jpa.findAll(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(entity)));
        given(mapper.toDomain(entity)).willReturn(assunto);

        var result = repository.findAll(PageRequest.of(0, 20));

        assertThat(result.getContent()).containsExactly(assunto);
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
                .hasMessageContaining("Assunto");
    }

    @Test
    @DisplayName("existsById deve delegar ao repositório JPA")
    void existsById_deveDelegarAoRepositorioJpa() {
        given(jpa.existsById(11)).willReturn(true);

        assertThat(repository.existsById(11)).isTrue();
    }

    @Test
    @DisplayName("findAllByIds deve mapear conjunto de assuntos")
    void findAllByIds_deveMapearConjuntoDeAssuntos() {
        AssuntoEntity first = AssuntoEntity.builder().codAs(1).descricao("A").build();
        AssuntoEntity second = AssuntoEntity.builder().codAs(2).descricao("B").build();
        Assunto assuntoA = Assunto.builder().codAs(1).descricao("A").build();
        Assunto assuntoB = Assunto.builder().codAs(2).descricao("B").build();
        given(jpa.findAllById(Set.of(1, 2))).willReturn(List.of(first, second));
        given(mapper.toDomain(first)).willReturn(assuntoA);
        given(mapper.toDomain(second)).willReturn(assuntoB);

        Set<Assunto> result = repository.findAllByIds(Set.of(1, 2));

        assertThat(result).containsExactlyInAnyOrder(assuntoA, assuntoB);
    }
}