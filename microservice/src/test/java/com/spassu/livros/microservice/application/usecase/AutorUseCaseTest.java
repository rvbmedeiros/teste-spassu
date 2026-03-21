package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.mapper.AutorDtoMapper;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.repository.AutorRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AutorUseCaseTest {

    @Mock AutorRepository  repository;
    @Mock AutorDtoMapper   mapper;
    @InjectMocks AutorUseCase useCase;

    @Test
    @DisplayName("criar — deve salvar e retornar response")
    void criar_deveRetornarResponse() {
        var request  = new AutorRequest("Martin Fowler");
        var domain   = Autor.builder().nome("Martin Fowler").build();
        var saved    = Autor.builder().codAu(1).nome("Martin Fowler").build();
        var response = new AutorResponse(1, "Martin Fowler");

        given(mapper.toDomain(request)).willReturn(domain);
        given(repository.save(domain)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(response);

        AutorResponse result = useCase.criar(request);

        assertThat(result.getCodAu()).isEqualTo(1);
        assertThat(result.getNome()).isEqualTo("Martin Fowler");
        then(repository).should().save(domain);
    }

    @Test
    @DisplayName("buscarPorId — deve lançar EntityNotFoundException quando não encontrado")
    void buscarPorId_quandoNaoEncontrado_deveLancarException() {
        given(repository.findById(99)).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.buscarPorId(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("atualizar — deve aplicar mudanças e salvar autor existente")
    void atualizar_deveAplicarMudancasESalvarAutorExistente() {
        var request = new AutorRequest("Kent Beck");
        var domain = Autor.builder().codAu(1).nome("Martin Fowler").build();
        var saved = Autor.builder().codAu(1).nome("Kent Beck").build();
        var response = new AutorResponse(1, "Kent Beck");

        given(repository.findById(1)).willReturn(Optional.of(domain));
        given(repository.save(domain)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(response);

        AutorResponse result = useCase.atualizar(1, request);

        assertThat(result.getNome()).isEqualTo("Kent Beck");
        then(mapper).should().updateDomain(domain, request);
        then(repository).should().save(domain);
    }

    @Test
    @DisplayName("listar — deve mapear página de autores")
    void listar_deveMapearPaginaDeAutores() {
        var pageable = PageRequest.of(0, 20);
        var domain = Autor.builder().codAu(1).nome("Martin Fowler").build();
        var response = new AutorResponse(1, "Martin Fowler");

        given(repository.findAll(pageable)).willReturn(new PageImpl<>(List.of(domain), pageable, 1));
        given(mapper.toResponse(domain)).willReturn(response);

        var result = useCase.listar(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    @DisplayName("excluir — deve delegar ao repository")
    void excluir_deveDelegarAoRepository() {
        willDoNothing().given(repository).deleteById(1);

        useCase.excluir(1);

        then(repository).should().deleteById(1);
    }
}
