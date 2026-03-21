package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.dto.AssuntoRequest;
import com.spassu.livros.microservice.application.dto.AssuntoResponse;
import com.spassu.livros.microservice.application.usecase.AssuntoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/assuntos")
@RequiredArgsConstructor
@Tag(name = "Assuntos", description = "CRUD de Assuntos")
public class AssuntoController {

    private final AssuntoUseCase useCase;

    @GetMapping
    @Operation(summary = "Listar assuntos paginados")
    public Page<AssuntoResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return useCase.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar assunto por ID")
    public AssuntoResponse buscar(@PathVariable Integer id) {
        return useCase.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar assunto")
    public AssuntoResponse criar(@Valid @RequestBody AssuntoRequest request) {
        return useCase.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar assunto")
    public AssuntoResponse atualizar(@PathVariable Integer id,
                                     @Valid @RequestBody AssuntoRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir assunto")
    public void excluir(@PathVariable Integer id) {
        useCase.excluir(id);
    }
}
