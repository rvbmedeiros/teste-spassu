package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.application.mapper.LivroDtoMapper;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.domain.repository.AutorRepository;
import com.spassu.livros.microservice.domain.repository.AssuntoRepository;
import com.spassu.livros.microservice.domain.repository.LivroRepository;
import com.spassu.livros.microservice.infrastructure.messaging.LivroEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LivroUseCaseTest {

    @Mock LivroRepository       repository;
    @Mock AutorRepository       autorRepository;
    @Mock AssuntoRepository     assuntoRepository;
    @Mock LivroDtoMapper        mapper;
    @Mock LivroEventPublisher   eventPublisher;
    @InjectMocks LivroUseCase   useCase;

    @Test
    @DisplayName("criar — deve resolver autores, assuntos e publicar evento")
    void criar_deveResolverAssociacoesEPublicarEvento() {
        var request = LivroRequest.builder()
                .titulo("Clean Code").valor(BigDecimal.valueOf(199.90))
                .autoresCodAu(Set.of(1)).assuntosCodAs(Set.of(2)).build();

        var domain  = Livro.builder().titulo("Clean Code").valor(BigDecimal.valueOf(199.90)).build();
        var autor   = Autor.builder().codAu(1).nome("Robert C. Martin").build();
        var assunto = Assunto.builder().codAs(2).descricao("Clean Code").build();
        var saved   = Livro.builder().codL(1).titulo("Clean Code").build();
        var response = LivroResponse.builder().codL(1).titulo("Clean Code").build();

        given(mapper.toDomain(request)).willReturn(domain);
        given(autorRepository.findAllByIds(Set.of(1))).willReturn(Set.of(autor));
        given(assuntoRepository.findAllByIds(Set.of(2))).willReturn(Set.of(assunto));
        given(repository.save(domain)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(response);

        LivroResponse result = useCase.criar(request);

        assertThat(result.getCodL()).isEqualTo(1);
        then(eventPublisher).should().publishCreated(saved);
    }

    @Test
    @DisplayName("buscarPorId — deve lançar EntityNotFoundException quando não encontrado")
    void buscarPorId_quandoNaoEncontrado_deveLancarException() {
        given(repository.findById(99)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.buscarPorId(99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("atualizar — deve resolver associações e publicar evento")
    void atualizar_deveResolverAssociacoesEPublicarEvento() {
        var request = LivroRequest.builder()
                .titulo("Refactoring")
                .editora("Addison-Wesley")
                .edicao(2)
                .anoPublicacao("2018")
                .valor(BigDecimal.valueOf(210.00))
                .autoresCodAu(Set.of(1))
                .assuntosCodAs(Set.of(2))
                .build();

        var existing = Livro.builder()
                .codL(1)
                .titulo("Clean Code")
            .autores(new HashSet<>())
            .assuntos(new HashSet<>())
                .build();
        var autor = Autor.builder().codAu(1).nome("Martin Fowler").build();
        var assunto = Assunto.builder().codAs(2).descricao("Refactoring").build();
        var saved = Livro.builder().codL(1).titulo("Refactoring").build();
        var response = LivroResponse.builder().codL(1).titulo("Refactoring").build();

        given(repository.findById(1)).willReturn(Optional.of(existing));
        given(autorRepository.findAllByIds(Set.of(1))).willReturn(Set.of(autor));
        given(assuntoRepository.findAllByIds(Set.of(2))).willReturn(Set.of(assunto));
        given(repository.save(existing)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(response);

        LivroResponse result = useCase.atualizar(1, request);

        assertThat(result.getTitulo()).isEqualTo("Refactoring");
        then(eventPublisher).should().publishUpdated(saved);
    }

    @Test
    @DisplayName("listar — deve mapear página de livros")
    void listar_deveMapearPaginaDeLivros() {
        var pageable = PageRequest.of(0, 20);
        var domain = Livro.builder().codL(1).titulo("Clean Code").build();
        var response = LivroResponse.builder().codL(1).titulo("Clean Code").build();

        given(repository.findAll(pageable)).willReturn(new PageImpl<>(List.of(domain), pageable, 1));
        given(mapper.toResponse(domain)).willReturn(response);

        var result = useCase.listar(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    @DisplayName("excluir — deve publicar evento de deleção")
    void excluir_devePublicarEventoDelecao() {
        willDoNothing().given(repository).deleteById(1);

        useCase.excluir(1);

        then(repository).should().deleteById(1);
        then(eventPublisher).should().publishDeleted(1);
    }
}
