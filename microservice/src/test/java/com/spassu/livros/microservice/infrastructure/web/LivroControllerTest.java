package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.dto.AssuntoResponse;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.application.usecase.LivroUseCase;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LivroControllerTest {

    @Mock
    private LivroUseCase useCase;

    @InjectMocks
    private LivroController controller;

    @Test
    @DisplayName("listar deve delegar ao usecase")
    void listar_deveDelegarAoUseCase() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(novoLivroResponse("Clean Code")), pageable, 1);
        given(useCase.listar(any())).willReturn(page);

        var result = controller.listar(pageable);

        assertThat(result).isEqualTo(page);
        then(useCase).should().listar(pageable);
    }

    @Test
    @DisplayName("buscar deve delegar ao usecase")
    void buscar_deveDelegarAoUseCase() {
        var response = novoLivroResponse("Clean Code");
        given(useCase.buscarPorId(1)).willReturn(response);

        var result = controller.buscar(1);

        assertThat(result).isEqualTo(response);
        then(useCase).should().buscarPorId(1);
    }

    @Test
    @DisplayName("criar deve delegar ao usecase")
    void criar_deveDelegarAoUseCase() {
        var request = novoLivroRequest("Clean Code");
        var response = novoLivroResponse("Clean Code");
        given(useCase.criar(request)).willReturn(response);

        var result = controller.criar(request);

        assertThat(result).isEqualTo(response);
        then(useCase).should().criar(request);
    }

    @Test
    @DisplayName("atualizar deve delegar ao usecase")
    void atualizar_deveDelegarAoUseCase() {
        var request = novoLivroRequest("Clean Architecture");
        var response = novoLivroResponse("Clean Architecture");
        given(useCase.atualizar(1, request)).willReturn(response);

        var result = controller.atualizar(1, request);

        assertThat(result).isEqualTo(response);
        then(useCase).should().atualizar(1, request);
    }

    @Test
    @DisplayName("excluir deve delegar ao usecase")
    void excluir_deveDelegarAoUseCase() {
        controller.excluir(1);

        then(useCase).should().excluir(1);
    }

    private LivroRequest novoLivroRequest(String titulo) {
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

    private LivroResponse novoLivroResponse(String titulo) {
        return LivroResponse.builder()
                .codL(1)
                .titulo(titulo)
                .editora("Prentice Hall")
                .edicao(1)
                .anoPublicacao("2008")
                .valor(new BigDecimal("99.90"))
                .autores(Set.of(AutorResponse.builder().codAu(1).nome("Robert C. Martin").build()))
                .assuntos(Set.of(AssuntoResponse.builder().codAs(1).descricao("Arquitetura").build()))
                .build();
    }
}
