package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.AssuntoRequest;
import com.spassu.livros.microservice.application.dto.AssuntoResponse;
import com.spassu.livros.microservice.application.mapper.AssuntoDtoMapper;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.repository.AssuntoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class AssuntoUseCaseTest {

    @Mock
    private AssuntoRepository repository;

    @Mock
    private AssuntoDtoMapper mapper;

    @InjectMocks
    private AssuntoUseCase useCase;

    @Test
    @DisplayName("criar — deve salvar e retornar response")
    void criar_deveSalvarERetornarResponse() {
        var request = AssuntoRequest.builder().descricao("Arquitetura").build();
        var domain = Assunto.builder().descricao("Arquitetura").build();
        var saved = Assunto.builder().codAs(1).descricao("Arquitetura").build();
        var response = AssuntoResponse.builder().codAs(1).descricao("Arquitetura").build();

        given(mapper.toDomain(request)).willReturn(domain);
        given(repository.save(domain)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(response);

        AssuntoResponse result = useCase.criar(request);

        assertThat(result.getCodAs()).isEqualTo(1);
        then(repository).should().save(domain);
    }

    @Test
    @DisplayName("atualizar — deve aplicar mudanças e salvar assunto existente")
    void atualizar_deveAplicarMudancasESalvarAssuntoExistente() {
        var request = AssuntoRequest.builder().descricao("DDD").build();
        var domain = Assunto.builder().codAs(1).descricao("Arquitetura").build();
        var saved = Assunto.builder().codAs(1).descricao("DDD").build();
        var response = AssuntoResponse.builder().codAs(1).descricao("DDD").build();

        given(repository.findById(1)).willReturn(Optional.of(domain));
        given(repository.save(domain)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(response);

        AssuntoResponse result = useCase.atualizar(1, request);

        assertThat(result.getDescricao()).isEqualTo("DDD");
        then(mapper).should().updateDomain(domain, request);
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
    @DisplayName("listar — deve mapear página de assuntos")
    void listar_deveMapearPaginaDeAssuntos() {
        var pageable = PageRequest.of(0, 20);
        var domain = Assunto.builder().codAs(1).descricao("Arquitetura").build();
        var response = AssuntoResponse.builder().codAs(1).descricao("Arquitetura").build();

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