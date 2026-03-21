package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.application.dto.rpc.AtualizarLivroMessage;
import com.spassu.livros.microservice.application.dto.rpc.IdRequest;
import com.spassu.livros.microservice.application.dto.rpc.PageRequest;
import com.spassu.livros.microservice.application.usecase.LivroUseCase;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LivroRpcListenerTest {

    @Mock
    private LivroUseCase useCase;

    @InjectMocks
    private LivroRpcListener listener;

    @Test
    @DisplayName("criar deve retornar response ok")
    void criar_deveRetornarResponseOk() {
        var response = livroResponse("DDD");
        given(useCase.criar(any())).willReturn(response);

        var result = listener.criar(livroRequest("DDD"));

        assertThat(result.success()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(201);
        assertThat(result.payload()).isEqualTo(response);
    }

    @Test
    @DisplayName("listar deve retornar conteudo paginado")
    void listar_deveRetornarConteudoPaginado() {
        var page = new PageImpl<>(List.of(livroResponse("Clean Code")));
        given(useCase.listar(any())).willReturn(page);

        var result = listener.listar(new PageRequest(0, 20));

        assertThat(result.success()).isTrue();
        assertThat(result.payload()).hasSize(1);
    }

    @Test
    @DisplayName("buscar deve retornar not found quando entidade nao existe")
    void buscar_deveRetornarNotFoundQuandoEntidadeNaoExiste() {
        given(useCase.buscarPorId(7)).willThrow(new EntityNotFoundException("Livro", 7));

        var result = listener.buscar(new IdRequest(7));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.httpStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("atualizar deve retornar conflict quando ocorre violacao")
    void atualizar_deveRetornarConflictQuandoOcorreViolacao() {
        var request = livroRequest("Duplicado");
        given(useCase.atualizar(1, request)).willThrow(new DataIntegrityViolationException("duplicado"));

        var result = listener.atualizar(new AtualizarLivroMessage(1, request));

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

    private LivroRequest livroRequest(String titulo) {
        return LivroRequest.builder()
                .titulo(titulo)
                .editora("Prentice Hall")
                .edicao(1)
                .anoPublicacao("2008")
                .valor(new BigDecimal("99.90"))
                .autoresCodAu(Set.of(1))
                .assuntosCodAs(Set.of(1))
                .build();
    }

    private LivroResponse livroResponse(String titulo) {
        return LivroResponse.builder()
                .codL(1)
                .titulo(titulo)
                .editora("Prentice Hall")
                .edicao(1)
                .anoPublicacao("2008")
                .valor(new BigDecimal("99.90"))
                .autores(Set.of())
                .assuntos(Set.of())
                .build();
    }
}