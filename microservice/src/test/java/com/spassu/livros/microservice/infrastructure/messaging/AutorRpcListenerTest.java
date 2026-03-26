package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.dto.rpc.AtualizarAutorMessage;
import com.spassu.livros.microservice.application.dto.rpc.IdRequest;
import com.spassu.livros.microservice.application.dto.rpc.PageRequest;
import com.spassu.livros.microservice.application.usecase.AutorUseCase;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AutorRpcListenerTest {

    @Mock
    private AutorUseCase useCase;

    @InjectMocks
    private AutorRpcListener listener;

    @Test
    @DisplayName("criar deve retornar response ok")
    void criar_deveRetornarResponseOk() {
        var response = AutorResponse.builder().codAu(1).nome("Martin Fowler").build();
        given(useCase.criar(any())).willReturn(response);

        var result = listener.criar(AutorRequest.builder().nome("Martin Fowler").build());

        assertThat(result.success()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(201);
        assertThat(result.payload()).isEqualTo(response);
    }

    @Test
    @DisplayName("listar deve retornar conteudo paginado")
    void listar_deveRetornarConteudoPaginado() {
        var page = new PageImpl<>(List.of(AutorResponse.builder().codAu(1).nome("Kent Beck").build()));
        given(useCase.listar(any())).willReturn(page);

        var result = listener.listar(new PageRequest(0, 20));

        assertThat(result.success()).isTrue();
        assertThat(result.payload()).hasSize(1);
    }

    @Test
    @DisplayName("buscar deve retornar not found quando entidade nao existe")
    void buscar_deveRetornarNotFoundQuandoEntidadeNaoExiste() {
        given(useCase.buscarPorId(7)).willThrow(new EntityNotFoundException("Autor", 7));

        var result = listener.buscar(new IdRequest(7));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.httpStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("atualizar deve retornar conflict quando ocorre violacao")
    void atualizar_deveRetornarConflictQuandoOcorreViolacao() {
        var request = AutorRequest.builder().nome("Duplicado").build();
        given(useCase.atualizar(1, request)).willThrow(new DataIntegrityViolationException("duplicado"));

        var result = listener.atualizar(new AtualizarAutorMessage(1, request));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("CONFLICT");
        assertThat(result.httpStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("excluir deve retornar no content")
    void excluir_deveRetornarNoContent() {
        var result = listener.excluir(new IdRequest(5));

        assertThat(result.success()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(204);
        assertThat(result.payload()).isNull();
    }
}