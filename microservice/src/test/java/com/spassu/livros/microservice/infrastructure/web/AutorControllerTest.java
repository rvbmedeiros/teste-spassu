package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.usecase.AutorUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AutorControllerTest {

    @Mock
    private AutorUseCase useCase;

    @InjectMocks
    private AutorController controller;

    @Test
    @DisplayName("listar deve delegar ao usecase")
    void listar_deveDelegarAoUseCase() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(autorResponse("Martin Fowler")), pageable, 1);
        given(useCase.listar(any())).willReturn(page);

        var result = controller.listar(pageable);

        assertThat(result).isEqualTo(page);
        then(useCase).should().listar(pageable);
    }

    @Test
    @DisplayName("buscar deve delegar ao usecase")
    void buscar_deveDelegarAoUseCase() {
        var response = autorResponse("Martin Fowler");
        given(useCase.buscarPorId(1)).willReturn(response);

        var result = controller.buscar(1);

        assertThat(result).isEqualTo(response);
        then(useCase).should().buscarPorId(1);
    }

    @Test
    @DisplayName("criar deve delegar ao usecase")
    void criar_deveDelegarAoUseCase() {
        var request = autorRequest("Kent Beck");
        var response = autorResponse("Kent Beck");
        given(useCase.criar(request)).willReturn(response);

        var result = controller.criar(request);

        assertThat(result).isEqualTo(response);
        then(useCase).should().criar(request);
    }

    @Test
    @DisplayName("atualizar deve delegar ao usecase")
    void atualizar_deveDelegarAoUseCase() {
        var request = autorRequest("Kent Beck");
        var response = autorResponse("Kent Beck");
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

    private AutorRequest autorRequest(String nome) {
        return AutorRequest.builder().nome(nome).build();
    }

    private AutorResponse autorResponse(String nome) {
        return AutorResponse.builder().codAu(1).nome(nome).build();
    }
}